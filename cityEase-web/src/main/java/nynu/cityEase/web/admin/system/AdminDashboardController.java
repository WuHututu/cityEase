package nynu.cityEase.web.admin.system;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.system.DashboardMetricsVO;
import nynu.cityEase.service.system.dict.service.ISysDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/system/dashboard")
@Api(tags = "【后台】数据大屏与统计")
public class AdminDashboardController {

    @Autowired
    private ISysDashboardService dashboardService;

    @GetMapping("/metrics")
    @ApiOperation("获取后台首页核心指标数据")
    public ResVo<DashboardMetricsVO> getCoreMetrics() {
        return ResVo.ok(dashboardService.getCoreMetrics());
    }
}