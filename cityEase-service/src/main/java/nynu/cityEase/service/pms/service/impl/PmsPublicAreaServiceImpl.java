package nynu.cityEase.service.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.RedisKeyConstants;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.pms.AdminAreaTreeVO;
import nynu.cityEase.api.vo.pms.AdminAreaUpsertReq;
import nynu.cityEase.api.vo.pms.PublicAreaReq;
import nynu.cityEase.api.vo.pms.PublicAreaTreeVO;
import nynu.cityEase.service.pms.repository.dao.PmsDao;
import nynu.cityEase.service.pms.repository.entity.PublicAreaDO;
import nynu.cityEase.service.pms.repository.entity.RoomDO;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import nynu.cityEase.service.pms.service.IPmsPublicAreaService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service("PmsPublicAreaServiceImpl")
public class PmsPublicAreaServiceImpl implements IPmsPublicAreaService {
    @Autowired
    private PmsDao pmsDao;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RoomMapper roomMapper;

    @Override
    public List<PublicAreaTreeVO> getAreaTree() {
        String jsonStr = redisTemplate.opsForValue().get(RedisKeyConstants.PUBLIC_AREA_TREE_KEY);
        if (StrUtil.isNotBlank(jsonStr)) {
            return JSONUtil.toList(jsonStr, PublicAreaTreeVO.class);
        }

        List<PublicAreaDO> allList = pmsDao.list(new LambdaQueryWrapper<PublicAreaDO>().orderByAsc(PublicAreaDO::getSort));
        List<PublicAreaTreeVO> tree = buildTree(allList);
        if (CollUtil.isNotEmpty(tree)) {
            redisTemplate.opsForValue().set(RedisKeyConstants.PUBLIC_AREA_TREE_KEY, JSONUtil.toJsonStr(tree), 24, TimeUnit.HOURS);
        }
        return tree;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addArea(PublicAreaReq req) {
        if (req.getParentId() != null && req.getParentId() != 0L) {
            PublicAreaDO parent = pmsDao.getById(req.getParentId());
            if (parent == null) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "父级区域不存在");
            }
            if (parent.getLevel() >= req.getLevel()) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "架构层级错误：父节点层级必须高于当前节点");
            }
        } else if (req.getLevel() != 1) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "顶级区域的层级必须是【小区】");
        }

        PublicAreaDO pdo = new PublicAreaDO();
        BeanUtils.copyProperties(req, pdo);
        pmsDao.saveOrUpdate(pdo);
        redisTemplate.delete(RedisKeyConstants.PUBLIC_AREA_TREE_KEY);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeArea(Long id) {
        if (pmsDao.hasChildren(id)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "该区域下存在子区域，请先删除子区域");
        }

        boolean result = pmsDao.removeById(id);
        if (!result) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "区域不存在或已删除");
        }

        redisTemplate.delete(RedisKeyConstants.PUBLIC_AREA_TREE_KEY);
    }

    @Override
    public List<AdminAreaTreeVO> getAdminAreaTree() {
        List<PublicAreaDO> list = pmsDao.list(new LambdaQueryWrapper<PublicAreaDO>().orderByAsc(PublicAreaDO::getSort));
        Map<Long, PublicAreaDO> areaMap = list.stream().collect(Collectors.toMap(PublicAreaDO::getId, area -> area, (a, b) -> a));

        Map<Long, AdminAreaTreeVO> idMap = list.stream().map(area -> {
            AdminAreaTreeVO vo = new AdminAreaTreeVO();
            vo.setId(area.getId());
            vo.setParentId(area.getParentId());
            vo.setAreaName(area.getName());
            vo.setLevel(area.getLevel());
            vo.setSort(area.getSort());
            vo.setChildren(new ArrayList<>());
            vo.setAreaType(null);
            vo.setFullAddress(buildFullAddress(area.getId(), areaMap));
            return vo;
        }).collect(Collectors.toMap(AdminAreaTreeVO::getId, vo -> vo, (a, b) -> a));

        List<AdminAreaTreeVO> roots = new ArrayList<>();
        for (AdminAreaTreeVO node : idMap.values()) {
            Long parentId = node.getParentId();
            if (parentId != null && parentId != 0L && idMap.containsKey(parentId)) {
                idMap.get(parentId).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }

        sortAdminTree(roots);
        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAdminArea(AdminAreaUpsertReq req) {
        req.setId(null);
        upsertAdminArea(req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdminArea(AdminAreaUpsertReq req) {
        if (req.getId() == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "缺少id");
        }
        upsertAdminArea(req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAdminArea(Long id) {
        if (id == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "缺少 id 参数");
        }
        if (pmsDao.hasChildren(id)) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "该区域下存在子区域，请先删除子区域");
        }

        boolean removed = pmsDao.removeById(id);
        if (!removed) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "区域不存在或已删除");
        }

        redisTemplate.delete(RedisKeyConstants.PUBLIC_AREA_TREE_KEY);
    }

    private void upsertAdminArea(AdminAreaUpsertReq req) {
        Long parentId = req.getParentId() == null ? 0L : req.getParentId();
        int level;
        if (parentId == 0L) {
            level = 1;
        } else {
            PublicAreaDO parent = pmsDao.getById(parentId);
            if (parent == null) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "父级区域不存在");
            }
            level = (parent.getLevel() == null ? 1 : parent.getLevel()) + 1;
        }

        PublicAreaDO area = new PublicAreaDO();
        area.setId(req.getId());
        area.setParentId(parentId);
        area.setName(req.getAreaName().trim());
        area.setLevel(level);
        area.setSort(req.getSort() == null ? 0 : req.getSort());

        boolean result = pmsDao.saveOrUpdate(area);
        if (!result) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "区域保存失败");
        }

        redisTemplate.delete(RedisKeyConstants.PUBLIC_AREA_TREE_KEY);
    }

    private List<PublicAreaTreeVO> buildTree(List<PublicAreaDO> allList) {
        if (CollUtil.isEmpty(allList)) {
            return new ArrayList<>();
        }

        Map<Long, PublicAreaTreeVO> idMap = allList.stream()
                .map(doEntity -> {
                    PublicAreaTreeVO vo = new PublicAreaTreeVO();
                    BeanUtils.copyProperties(doEntity, vo);
                    vo.setChildren(new ArrayList<>());
                    return vo;
                })
                .collect(Collectors.toMap(PublicAreaTreeVO::getId, vo -> vo));

        List<PublicAreaTreeVO> rootList = new ArrayList<>();
        for (PublicAreaTreeVO node : idMap.values()) {
            Long parentId = node.getParentId();
            if (parentId != null && idMap.containsKey(parentId)) {
                idMap.get(parentId).getChildren().add(node);
            } else {
                rootList.add(node);
            }
        }
        return rootList;
    }

    @Override
    public String getFullAreaName(Long areaId) {
        if (areaId == null) {
            return "";
        }

        StringBuilder fullName = new StringBuilder();
        Long currentId = areaId;
        int maxDepth = 10;
        while (currentId != null && currentId != 0L && maxDepth-- > 0) {
            PublicAreaDO area = pmsDao.getById(currentId);
            if (area == null) {
                break;
            }

            if (!fullName.isEmpty()) {
                fullName.insert(0, "-");
            }
            fullName.insert(0, area.getName());
            currentId = area.getParentId();
        }
        return fullName.toString();
    }

    private void sortAdminTree(List<AdminAreaTreeVO> nodes) {
        if (CollUtil.isEmpty(nodes)) {
            return;
        }
        nodes.sort(Comparator.comparingInt(node -> node.getSort() == null ? 0 : node.getSort()));
        for (AdminAreaTreeVO node : nodes) {
            sortAdminTree(node.getChildren());
        }
    }

    private String buildFullAddress(Long areaId, Map<Long, PublicAreaDO> areaMap) {
        if (areaId == null) {
            return "";
        }

        StringBuilder fullName = new StringBuilder();
        Long currentId = areaId;
        int maxDepth = 10;
        while (currentId != null && currentId != 0L && maxDepth-- > 0) {
            PublicAreaDO area = areaMap.get(currentId);
            if (area == null) {
                break;
            }
            if (!fullName.isEmpty()) {
                fullName.insert(0, "-");
            }
            fullName.insert(0, area.getName());
            currentId = area.getParentId();
        }
        return fullName.toString();
    }

    @Override
    public List<RoomDO> getRoomsByArea(Long areaId) {
        if (areaId == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<RoomDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomDO::getAreaId, areaId).orderByAsc(RoomDO::getRoomNum);
        return roomMapper.selectList(wrapper);
    }

    @Override
    public List<RoomDO> searchRooms(String keyword) {
        LambdaQueryWrapper<RoomDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(keyword), RoomDO::getRoomNum, keyword).orderByAsc(RoomDO::getRoomNum);
        return roomMapper.selectList(wrapper);
    }
}
