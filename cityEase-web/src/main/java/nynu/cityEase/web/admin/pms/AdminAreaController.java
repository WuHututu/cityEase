package nynu.cityEase.web.admin.pms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.constants.RedisKeyConstants;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.pms.AdminAreaTreeVO;
import nynu.cityEase.api.vo.pms.AdminAreaUpsertReq;
import nynu.cityEase.service.pms.repository.entity.PublicAreaDO;
import nynu.cityEase.service.pms.repository.mapper.PublicAreaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/pms/area")
@Api(tags = "〖后台〗公共区域管理")
public class AdminAreaController {

  @Autowired
  private PublicAreaMapper publicAreaMapper;

  @Autowired
  private StringRedisTemplate redisTemplate;

  @GetMapping("/tree")
  @ApiOperation("获取公共区域树")
  public ResVo<List<AdminAreaTreeVO>> tree() {
    // 直接查库（树缓存逻辑已经在 service 里做过；这里为了字段名适配前端，走 controller 转换）
    List<PublicAreaDO> list = publicAreaMapper.selectList(
        new LambdaQueryWrapper<PublicAreaDO>().orderByAsc(PublicAreaDO::getSort)
    );

    Map<Long, AdminAreaTreeVO> idMap = list.stream().map(a -> {
      AdminAreaTreeVO vo = new AdminAreaTreeVO();
      vo.setId(a.getId());
      vo.setParentId(a.getParentId());
      vo.setAreaName(a.getName());
      vo.setLevel(a.getLevel());
      vo.setSort(a.getSort());
      vo.setChildren(new ArrayList<>());
      return vo;
    }).collect(Collectors.toMap(AdminAreaTreeVO::getId, x -> x, (a, b) -> a));

    List<AdminAreaTreeVO> roots = new ArrayList<>();
    for (AdminAreaTreeVO node : idMap.values()) {
      Long pid = node.getParentId();
      if (pid != null && pid != 0L && idMap.containsKey(pid)) {
        idMap.get(pid).getChildren().add(node);
      } else {
        roots.add(node);
      }
    }

    // 保证 children 也按 sort 排序（可选，但体验更好）
    sortTreeBySort(roots);

    return ResVo.ok(roots);
  }

  @PostMapping("/save")
  @ApiOperation("新增公共区域")
  @Transactional(rollbackFor = Exception.class)
  public ResVo<String> save(@RequestBody AdminAreaUpsertReq req) {
    req.setId(null);
    upsert(req);
    return ResVo.ok();
  }

  @PostMapping("/update")
  @ApiOperation("编辑公共区域")
  @Transactional(rollbackFor = Exception.class)
  public ResVo<String> update(@RequestBody AdminAreaUpsertReq req) {
    if (req.getId() == null) {
      throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "缺少id");
    }
    upsert(req);
    return ResVo.ok();
  }

  @DeleteMapping("/delete")
  @ApiOperation("删除公共区域")
  @Transactional(rollbackFor = Exception.class)
  public ResVo<String> delete(@RequestParam("id") Long id) {
    // 1) 存在子节点不允许删
    Long children = publicAreaMapper.selectCount(new LambdaQueryWrapper<PublicAreaDO>()
        .eq(PublicAreaDO::getParentId, id));
    if (children != null && children > 0) {
      throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "该区域下存在子区域，请先删除子区域");
    }

    int rows = publicAreaMapper.deleteById(id);
    if (rows <= 0) {
      throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "区域不存在或已删除");
    }

    // 清理树缓存
    redisTemplate.delete(RedisKeyConstants.PUBLIC_AREA_TREE_KEY);

    return ResVo.ok();
  }

  private void upsert(AdminAreaUpsertReq req) {
    if (req.getAreaName() == null || req.getAreaName().trim().isEmpty()) {
      throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "区域名称不能为空");
    }

    Long parentId = (req.getParentId() == null) ? 0L : req.getParentId();

    int level;
    if (parentId == 0L) {
      level = 1;
    } else {
      PublicAreaDO parent = publicAreaMapper.selectById(parentId);
      if (parent == null) {
        throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "父级区域不存在");
      }
      level = (parent.getLevel() == null ? 1 : parent.getLevel()) + 1;
    }

    PublicAreaDO po = new PublicAreaDO();
    if (req.getId() != null) {
      po.setId(req.getId());
    }
    po.setParentId(parentId);
    po.setName(req.getAreaName().trim());
    po.setLevel(level);
    po.setSort(req.getSort() == null ? 0 : req.getSort());

    // insert / update
    if (req.getId() == null) {
      publicAreaMapper.insert(po);
    } else {
      int rows = publicAreaMapper.updateById(po);
      if (rows <= 0) {
        throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "区域不存在或已删除");
      }
    }

    // 清理树缓存
    redisTemplate.delete(RedisKeyConstants.PUBLIC_AREA_TREE_KEY);
  }

  private void sortTreeBySort(List<AdminAreaTreeVO> nodes) {
    if (nodes == null || nodes.isEmpty()) {
      return;
    }
    nodes.sort(Comparator.comparingInt(n -> n.getSort() == null ? 0 : n.getSort()));
    for (AdminAreaTreeVO n : nodes) {
      sortTreeBySort(n.getChildren());
    }
  }
}
