package nynu.cityEase.web.admin.pms;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.pms.AdminAreaTreeVO;
import nynu.cityEase.api.vo.pms.AdminAreaUpsertReq;
import nynu.cityEase.service.pms.service.IPmsPublicAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/pms/area")
@Api(tags = "〖后台〗公共区域管理")
@Validated
public class AdminAreaController {

  @Autowired
  private IPmsPublicAreaService publicAreaService;

  @GetMapping("/tree")
  @ApiOperation("获取公共区域树")
  public ResVo<List<AdminAreaTreeVO>> tree() {
    return ResVo.ok(publicAreaService.getAdminAreaTree());
  }

  @PostMapping("/save")
  @ApiOperation("新增公共区域")
  public ResVo<String> save(@Valid @RequestBody AdminAreaUpsertReq req) {
    publicAreaService.saveAdminArea(req);
    return ResVo.ok();
  }

  @PostMapping("/update")
  @ApiOperation("编辑公共区域")
  public ResVo<String> update(@Valid @RequestBody AdminAreaUpsertReq req) {
    publicAreaService.updateAdminArea(req);
    return ResVo.ok();
  }

  @PostMapping("/delete")
  @ApiOperation("删除公共区域")
  public ResVo<String> delete(@RequestBody Map<String, Long> params) {
    publicAreaService.deleteAdminArea(params.get("id"));
    return ResVo.ok();
  }
}
