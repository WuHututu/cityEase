package nynu.cityEase.web.admin.pms;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.pms.HouseQueryReq;
import nynu.cityEase.api.vo.pms.HouseVO;
import nynu.cityEase.service.pms.service.IPmsHouseService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/pms/house")
@Api(tags = "【后台】房屋与公共区域管理")
public class AdminHouseController {

    @Autowired
    private IPmsHouseService houseService;

    @PostMapping("/page")
    @ApiOperation("分页获取房屋与公共区域列表")
    public ResVo<Page<HouseVO>> getHousePage(@RequestBody HouseQueryReq req) {
        return ResVo.ok(houseService.getHousePage(req));
    }
}