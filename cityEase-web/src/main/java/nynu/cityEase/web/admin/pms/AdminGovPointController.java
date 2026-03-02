package nynu.cityEase.web.admin.pms;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.gov.GovPointChangeReq;
import nynu.cityEase.service.gov.service.IGovPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/gov/point")
@Api(tags = "【后台】社区治理积分管理")
public class AdminGovPointController {

    @Autowired
    private IGovPointService govPointService;

    @PostMapping("/change")
    @ApiOperation("手动增减房屋积分")
    public ResVo<String> changePoint(@RequestBody GovPointChangeReq req) {
        // 1. 获取当前操作的管理员ID (记录是谁发放/扣减的积分)
        long adminUserId = StpUtil.getLoginIdAsLong();

        // 2. 调用我们刚才写的安全积分操作核心方法
        govPointService.changePoints(
                req.getRoomId(), 
                adminUserId, 
                req.getAmount(), 
                req.getIsAdd(), 
                req.getReason()
        );

        String action = req.getIsAdd() ? "发放" : "扣减";
        return ResVo.ok("积分" + action + "成功");
    }
}