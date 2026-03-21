package nynu.cityEase.service.gov.service;

import nynu.cityEase.api.vo.gov.GovPointOverviewVO;
import nynu.cityEase.api.vo.gov.GovPointTrendVO;

public interface IGovPointOverviewService {

    GovPointOverviewVO getOverview();

    GovPointTrendVO getRecentTrend(Integer days);
}
