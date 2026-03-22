package nynu.cityEase.service.gov.service;

import nynu.cityEase.api.vo.gov.BuildingPointStatsVO;
import nynu.cityEase.api.vo.gov.PointRankingVO;

import java.util.List;

/**
 * 积分排行榜数据查询服务接口
 * 专门用于数据库查询，避免循环依赖
 * Created: 2026/3/18
 */
public interface IGovPointRankingDataService {

    /**
     * 获取房屋积分排行榜（从数据库）
     * @param limit 限制数量
     * @return 房屋积分排行榜
     */
    List<PointRankingVO> getRoomPointRankingFromDB(Integer limit);

    /**
     * 获取指定房屋的排名信息（从数据库）
     * @param roomId 房屋ID
     * @return 房屋排名信息
     */
    PointRankingVO getRoomRankingFromDB(Long roomId);

    /**
     * 获取楼栋积分对比统计（从数据库）
     * @return 楼栋积分统计列表
     */
    List<BuildingPointStatsVO> getBuildingPointStatsFromDB();

    /**
     * 获取指定楼栋内的房屋积分排行（从数据库）
     * @param areaId 楼栋ID
     * @param limit 限制数量
     * @return 楼栋内房屋积分排行
     */
    List<PointRankingVO> getBuildingRoomRankingFromDB(Long areaId, Integer limit);
}
