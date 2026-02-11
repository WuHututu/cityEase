package nynu.cityEase.web.admin.pms;

import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.pms.PublicAreaReq;
import nynu.cityEase.service.pms.service.IPmsPublicAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/11
 * Time: 16:45
 * Description: TODO
 */
@RestController
@RequestMapping
public class PmsRestController{
    @Autowired
    IPmsPublicAreaService publicAreaService;

    @GetMapping("admin/public")
    public ResVo<String> getPublicAreaTree() {
        return ResVo.ok(publicAreaService.getAreaTree().toString());
    }

    @PostMapping("admin/public/add")
    public ResVo<String> addPublicArea(PublicAreaReq req) {
        publicAreaService.addArea(req);
        return ResVo.ok("添加成功");
    }

    @DeleteMapping("admin/public/{id}")
    public ResVo<String> removePublicArea(@PathVariable("id") Long id) {
        publicAreaService.removeArea(id);
        return ResVo.ok("删除成功");
    }
}
