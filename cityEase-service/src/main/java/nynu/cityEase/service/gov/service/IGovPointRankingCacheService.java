package nynu.cityEase.service.gov.service;

import nynu.cityEase.api.vo.gov.BuildingPointStatsVO;
import nynu.cityEase.api.vo.gov.PointRankingVO;

import java.util.List;

/**
 * 积分排行榜缓存服务接口
 * Created: 2026/3/18
 */
public interface IGovPointRankingCacheService {

    /**
     * 刷新所有排行榜缓存
     */
    void refreshAllRankings();

    /**
     * 刷新房屋积分排行榜缓存
     */
    void refreshRoomRanking();

    /**
     * 刷新楼栋积分统计缓存
     */
    void refreshBuildingStats();

    /**
     * 获取房屋积分排行榜（从缓存）
     * @param limit 返回条数限制
     * @return 排行榜列表
     */
    List<PointRankingVO> getRoomRankingFromCache(Integer limit);

    /**
     * 获取指定房屋排名（从缓存）
     * @param roomId 房屋ID
     * @return 排名信息
     */
    PointRankingVO getRoomRankingFromCache(Long roomId);

    /**
     * 获取楼栋积分统计（从缓存）
     * @return 楼栋统计列表
     */
    List<BuildingPointStatsVO> getBuildingStatsFromCache();

    /**
     * 获取楼栋内房屋排行（从缓存）
     * @param areaId 区域ID
     * @param limit 返回条数限制
     * @return 楼栋内排行
     */
    List<PointRankingVO> getBuildingRoomRankingFromCache(Long areaId, Integer limit);

    /**
     * 更新单个房屋积分缓存
     * @param roomId 房屋ID
     * @param pointsBalance 新的积分余额
     */
    void updateRoomPoints(Long roomId, Integer pointsBalance);

    /**
     * 删除房屋排名缓存
     * @param roomId 房屋ID
     */
    void removeRoomRanking(Long roomId);

    /**
     * 获取缓存状态
     * @return 缓存状态信息
     */
    String getCacheStatus();
}
