package nynu.cityEase.service.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.RedisKeyConstants;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.pms.PublicAreaReq;
import nynu.cityEase.api.vo.pms.PublicAreaTreeVO;
import nynu.cityEase.service.pms.repository.dao.PmsDao;
import nynu.cityEase.service.pms.repository.entity.PublicAreaDO;
import nynu.cityEase.service.pms.service.IPmsPublicAreaService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/11
 * Time: 14:48
 * Description: TODO
 */
@Service("PmsPublicAreaServiceImpl")
public class PmsPublicAreaServiceImpl implements IPmsPublicAreaService {
    @Autowired
    PmsDao pmsDao;
    @Autowired
    StringRedisTemplate redisTemplate;

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
        // 1. 父级关系与层级架构校验 (核心优化点)
        if (req.getParentId() != null && req.getParentId() != 0L) {
            // 有父节点的情况
            PublicAreaDO parent = pmsDao.getById(req.getParentId());
            if (parent == null) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "父级区域不存在");
            }

            // ★ 灵活且严格的层级规则：父节点的级别(数字)必须小于当前节点的级别
            // 例如：当前是楼栋(3)，父亲可以是分期(2)或者小区(1)，但不能是单元(4)或楼栋(3)
            if (parent.getLevel() >= req.getLevel()) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "架构层级错误：父节点层级必须高于当前节点");
            }
        } else {
            // ★ 无父节点(顶级节点)的情况：强制必须是“小区”级别 (假设字典里小区的值是1)
            if (req.getLevel() != 1) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "顶级区域的层级必须是【小区】");
            }
        }

        // 2. 保存数据
        PublicAreaDO pdo = new PublicAreaDO();
        BeanUtils.copyProperties(req, pdo);
        pmsDao.saveOrUpdate(pdo);

        // 3. 清理缓存，保证前端获取最新树结构
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

    /**
     * 1. 把所有节点放进 Map<ID, Node>，方便快速查找。
     * 2. 遍历列表，尝试去找每个节点的“父亲”。
     * 3. 找到了父亲 ->把自己加到父亲的 children 里。
     * 4. 没找到父亲 -> 说明自己就是顶级节点 (Root)，加到结果集。
     */
    private List<PublicAreaTreeVO> buildTree(List<PublicAreaDO> allList) {
        if (CollUtil.isEmpty(allList)) {
            return new ArrayList<>();
        }

        Map<Long, PublicAreaTreeVO> idMap = allList.stream()
                .map(doEntity -> {
                    PublicAreaTreeVO vo = new PublicAreaTreeVO();
                    BeanUtils.copyProperties(doEntity, vo);
                    vo.setChildren(new ArrayList<>()); // 预先初始化空列表
                    return vo;
                })
                .collect(Collectors.toMap(PublicAreaTreeVO::getId, vo -> vo));

        List<PublicAreaTreeVO> rootList = new ArrayList<>();

        for (PublicAreaTreeVO node : idMap.values()) {
            Long parentId = node.getParentId();

            if (parentId != null && idMap.containsKey(parentId)) {
                PublicAreaTreeVO parent = idMap.get(parentId);
                parent.getChildren().add(node);
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

        // 向上溯源，直到 parentId = 0 或者找不到为止 (加个计数器防止死循环)
        int maxDepth = 10;
        while (currentId != null && currentId != 0L && maxDepth-- > 0) {
            PublicAreaDO area = pmsDao.getById(currentId);
            if (area == null) {
                break;
            }

            // 往头部插入，因为是从底层往上查的
            if (!fullName.isEmpty()) {
                fullName.insert(0, "-");
            }
            fullName.insert(0, area.getName());

            currentId = area.getParentId();
        }
        return fullName.toString();
    }


}
