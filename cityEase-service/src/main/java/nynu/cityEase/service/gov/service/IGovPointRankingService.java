package nynu.cityEase.service.gov.service;

import nynu.cityEase.api.vo.gov.PointRankingVO;
import nynu.cityEase.api.vo.gov.BuildingPointStatsVO;

import java.util.List;

/**
 * 积分排行榜服务接口
 * Created: 2026/3/18
 */
public interface IGovPointRankingService {

    /**
     * 获取房屋积分排行榜
     * @param limit 返回条数限制，默认50
     * @return 房屋积分排行榜列表
     */
    List<PointRankingVO> getRoomPointRanking(Integer limit);

    /**
     * 获取指定房屋的排名信息
     * @param roomId 房屋ID
     * @return 排名信息，包含排名和积分
     */
    PointRankingVO getRoomRanking(Long roomId);

    /**
     * 获取楼栋积分对比统计
     * @return 楼栋积分统计列表
     */
    List<BuildingPointStatsVO> getBuildingPointStats();

    /**
     * 获取指定楼栋的房屋积分排行
     * @param areaId 区域ID（楼栋）
     * @param limit 返回条数限制
     * @return 该楼栋内的房屋积分排行
     */
    List<PointRankingVO> getBuildingRoomRanking(Long areaId, Integer limit);
}
