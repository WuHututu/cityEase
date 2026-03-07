package nynu.cityEase.web.admin.pms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.pms.AdminRoomPageReq;
import nynu.cityEase.api.vo.pms.AdminRoomUpsertReq;
import nynu.cityEase.api.vo.pms.AdminRoomVO;
import nynu.cityEase.service.pms.repository.entity.PublicAreaDO;
import nynu.cityEase.service.pms.repository.entity.RoomDO;
import nynu.cityEase.service.pms.repository.mapper.PublicAreaMapper;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import nynu.cityEase.service.pms.service.IPmsPublicAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/pms/room")
@Api(tags = "〖后台〗房屋管理")
public class AdminRoomController {

  @Autowired
  private RoomMapper roomMapper;

  @Autowired
  private PublicAreaMapper publicAreaMapper;

  @Autowired
  private IPmsPublicAreaService publicAreaService;

  @PostMapping("/page")
  @ApiOperation("分页查询房屋")
  public ResVo<Page<AdminRoomVO>> page(@RequestBody AdminRoomPageReq req) {
    LambdaQueryWrapper<RoomDO> qw = new LambdaQueryWrapper<>();
    if (req.getAreaId() != null) {
      qw.eq(RoomDO::getAreaId, req.getAreaId());
    }
    if (req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
      qw.like(RoomDO::getRoomNum, req.getKeyword().trim());
    }
    qw.orderByDesc(RoomDO::getCreateTime);

    Page<RoomDO> page = roomMapper.selectPage(req.toPage(), qw);

    List<AdminRoomVO> records = page.getRecords().stream().map(r -> {
      AdminRoomVO vo = new AdminRoomVO();
      vo.setId(r.getId());
      vo.setAreaId(r.getAreaId());
      vo.setRoomNum(r.getRoomNum());
      vo.setCreateTime(r.getCreateTime());

      PublicAreaDO area = publicAreaMapper.selectById(r.getAreaId());
      vo.setAreaName(area == null ? null : area.getName());

      String prefix = publicAreaService.getFullAreaName(r.getAreaId());
      vo.setFullAddress(prefix == null || prefix.isEmpty() ? r.getRoomNum() : prefix + "-" + r.getRoomNum());
      return vo;
    }).collect(Collectors.toList());

    Page<AdminRoomVO> out = new Page<>();
    out.setCurrent(page.getCurrent());
    out.setSize(page.getSize());
    out.setTotal(page.getTotal());
    out.setRecords(records);

    return ResVo.ok(out);
  }

  @GetMapping("/detail")
  @ApiOperation("房屋详情")
  public ResVo<AdminRoomVO> detail(@RequestParam("id") Long id) {
    RoomDO r = roomMapper.selectById(id);
    if (r == null) {
      throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "房屋不存在或已删除");
    }

    AdminRoomVO vo = new AdminRoomVO();
    vo.setId(r.getId());
    vo.setAreaId(r.getAreaId());
    vo.setRoomNum(r.getRoomNum());
    vo.setCreateTime(r.getCreateTime());

    PublicAreaDO area = publicAreaMapper.selectById(r.getAreaId());
    vo.setAreaName(area == null ? null : area.getName());

    String prefix = publicAreaService.getFullAreaName(r.getAreaId());
    vo.setFullAddress(prefix == null || prefix.isEmpty() ? r.getRoomNum() : prefix + "-" + r.getRoomNum());

    return ResVo.ok(vo);
  }

  @PostMapping("/save")
  @ApiOperation("新增房屋")
  @Transactional(rollbackFor = Exception.class)
  public ResVo<String> save(@RequestBody AdminRoomUpsertReq req) {
    req.setId(null);
    upsert(req);
    return ResVo.ok();
  }

  @PostMapping("/update")
  @ApiOperation("编辑房屋")
  @Transactional(rollbackFor = Exception.class)
  public ResVo<String> update(@RequestBody AdminRoomUpsertReq req) {
    if (req.getId() == null) {
      throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "缺少id");
    }
    upsert(req);
    return ResVo.ok();
  }

  @DeleteMapping("/delete")
  @ApiOperation("删除房屋")
  @Transactional(rollbackFor = Exception.class)
  public ResVo<String> delete(@RequestParam("id") Long id) {
    int rows = roomMapper.deleteById(id);
    if (rows <= 0) {
      throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "房屋不存在或已删除");
    }
    return ResVo.ok();
  }

  private void upsert(AdminRoomUpsertReq req) {
    if (req.getAreaId() == null) {
      throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "所属区域不能为空");
    }
    if (req.getRoomNum() == null || req.getRoomNum().trim().isEmpty()) {
      throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "门牌号不能为空");
    }

    // 校验 area 是否存在
    PublicAreaDO area = publicAreaMapper.selectById(req.getAreaId());
    if (area == null) {
      throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "所属区域不存在");
    }

    // 防重：同一 areaId 下 roomNum 不允许重复
    LambdaQueryWrapper<RoomDO> dupQw = new LambdaQueryWrapper<RoomDO>()
        .eq(RoomDO::getAreaId, req.getAreaId())
        .eq(RoomDO::getRoomNum, req.getRoomNum().trim());
    if (req.getId() != null) {
      dupQw.ne(RoomDO::getId, req.getId());
    }
    Long cnt = roomMapper.selectCount(dupQw);
    if (cnt != null && cnt > 0) {
      throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "该区域下门牌号已存在");
    }

    RoomDO room = new RoomDO();
    if (req.getId() != null) {
      room.setId(req.getId());
    }
    room.setAreaId(req.getAreaId());
    room.setRoomNum(req.getRoomNum().trim());

    if (req.getId() == null) {
      roomMapper.insert(room);
    } else {
      int rows = roomMapper.updateById(room);
      if (rows <= 0) {
        throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "房屋不存在或已删除");
      }
    }
  }
}
