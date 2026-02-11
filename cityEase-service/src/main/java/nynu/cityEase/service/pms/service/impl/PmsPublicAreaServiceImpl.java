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
        if (req.getParentId() != null && req.getParentId() != 0L) {
            PublicAreaDO parent = pmsDao.getById(req.getParentId());
            if (parent == null) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "父级区域不存在");
            }
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
}
