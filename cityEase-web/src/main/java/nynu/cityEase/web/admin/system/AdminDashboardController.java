package nynu.cityEase.web.admin.system;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.constants.RedisKeyConstants;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.system.DashboardMetricsVO;
import nynu.cityEase.service.system.dict.service.ISysDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/system/dashboard")
@Api(tags = "【后台】数据大屏与统计")
public class AdminDashboardController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/metrics")
    @ApiOperation("获取后台首页核心指标数据")
    public ResVo<DashboardMetricsVO> getCoreMetrics() {

        String cachedData = stringRedisTemplate.opsForValue().get(RedisKeyConstants.DASHBOARD_METRICS_KEY);

        if (cn.hutool.core.util.StrUtil.isBlank(cachedData)) {
            return ResVo.fail(StatusEnum.DATA_INITIALIZE_ERROR);
        }

        DashboardMetricsVO vo = cn.hutool.json.JSONUtil.toBean(cachedData, DashboardMetricsVO.class);
        return ResVo.ok(vo);
    }
}