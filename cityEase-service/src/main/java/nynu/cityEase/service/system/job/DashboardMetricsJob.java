package nynu.cityEase.service.system.job;

import cn.hutool.json.JSONUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import nynu.cityEase.api.vo.constants.RedisKeyConstants;
import nynu.cityEase.api.vo.system.DashboardMetricsVO;
import nynu.cityEase.service.system.dict.service.ISysDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DashboardMetricsJob {

    @Autowired
    private ISysDashboardService dashboardService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @XxlJob("refreshDashboardMetricsJob")
    public void refreshDashboardMetrics() {
        
        // 1. 调用 Service 去数据库执行耗时的聚合统计
        DashboardMetricsVO metrics = dashboardService.getCoreMetrics();

        // 2. 将统计结果写入 Redis，永不过期，等待下次定时任务覆盖
        stringRedisTemplate.opsForValue().set(
                RedisKeyConstants.DASHBOARD_METRICS_KEY,
                JSONUtil.toJsonStr(metrics)
        );

        // XXL-JOB 要求的标准日志输出
        XxlJobHelper.log("仪表盘数据刷新完成，当前总房屋数: " + metrics.getTotalRooms());
    }
}