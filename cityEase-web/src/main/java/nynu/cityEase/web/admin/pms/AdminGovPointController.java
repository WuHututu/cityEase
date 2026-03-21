package nynu.cityEase.web.admin.pms;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.gov.GovPointChangeReq;
import nynu.cityEase.api.vo.gov.GovPointOverviewVO;
import nynu.cityEase.api.vo.gov.GovPointTrendVO;
import nynu.cityEase.service.gov.service.IGovPointOverviewService;
import nynu.cityEase.service.gov.service.IGovPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/gov/point")
@Api(tags = "【后台】社区治理积分管理")
public class AdminGovPointController {

    @Autowired
    private IGovPointService govPointService;

    @Autowired
    private IGovPointOverviewService overviewService;

    @PostMapping("/change")
    @ApiOperation("手动增减房屋积分")
    public ResVo<String> changePoint(@RequestBody GovPointChangeReq req) {
        long adminUserId = StpUtil.getLoginIdAsLong();
        govPointService.changePoints(req.getRoomId(), adminUserId, req.getAmount(), req.getIsAdd(), req.getReason());
        String action = req.getIsAdd() ? "发放" : "扣减";
        return ResVo.ok("积分" + action + "成功");
    }

    @GetMapping("/overview")
    @ApiOperation("积分总览指标")
    public ResVo<GovPointOverviewVO> overview() {
        return ResVo.ok(overviewService.getOverview());
    }

    @GetMapping("/trend")
    @ApiOperation("积分趋势图数据")
    public ResVo<GovPointTrendVO> trend(
            @ApiParam(value = "查询天数，默认7", example = "7")
            @RequestParam(value = "days", required = false) Integer days) {
        return ResVo.ok(overviewService.getRecentTrend(days));
    }
}
