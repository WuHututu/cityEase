package nynu.cityEase.service.system.dict.service;

import nynu.cityEase.api.vo.system.ChartDataVO;
import nynu.cityEase.api.vo.system.DashboardMetricsVO;

public interface ISysDashboardService {

    /**
     * 获取后台首页核心统计数据
     */
    DashboardMetricsVO getCoreMetrics();

    /**
     * 近七日工单趋势
     */
    ChartDataVO getRepairTrendChart();
}