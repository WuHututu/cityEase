package nynu.cityEase.service.gov.service.impl;

import nynu.cityEase.api.vo.gov.BuildingPointStatsVO;
import nynu.cityEase.api.vo.gov.PointRankingVO;
import nynu.cityEase.service.gov.service.IGovPointRankingCacheService;
import nynu.cityEase.service.gov.service.IGovPointRankingDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 积分排行榜缓存服务实现类
 * Created: 2026/3/18
 */
@Service
public class GovPointRankingCacheServiceImpl implements IGovPointRankingCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private IGovPointRankingDataService dataService;

    // Redis Key 常量
    private static final String ROOM_RANKING_KEY = "point:ranking:room";
    private static final String BUILDING_STATS_KEY = "point:stats:building";
    private static final String ROOM_RANKING_DETAIL_KEY = "point:ranking:room:detail:";
    private static final String BUILDING_ROOM_RANKING_KEY = "point:ranking:building:room:";
    private static final String CACHE_STATUS_KEY = "point:cache:status";

    // 缓存过期时间（分钟）
    private static final long CACHE_EXPIRE_MINUTES = 30;

    @Override
    @Scheduled(cron = "0 */10 * * * ?") // 每10分钟执行一次
    public void refreshAllRankings() {
        try {
            refreshRoomRanking();
            refreshBuildingStats();
            updateCacheStatus("success", "定时刷新完成");
        } catch (Exception e) {
            updateCacheStatus("error", "定时刷新失败: " + e.getMessage());
        }
    }

    @Override
    public void refreshRoomRanking() {
        try {
            // 清除旧缓存
            redisTemplate.delete(ROOM_RANKING_KEY);
            
            // 获取前100名房屋积分数据
            List<PointRankingVO> rankings = dataService.getRoomPointRankingFromDB(100);
            
            // 使用ZSet存储排行榜数据
            for (PointRankingVO ranking : rankings) {
                // 按积分倒序存储（Redis ZSet默认是升序，所以用负数）
                redisTemplate.opsForZSet().add(ROOM_RANKING_KEY, ranking.getRoomId(), -ranking.getPointsBalance());
                
                // 存储详细信息
                String detailKey = ROOM_RANKING_DETAIL_KEY + ranking.getRoomId();
                redisTemplate.opsForValue().set(detailKey, ranking, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            }
            
            // 设置排行榜过期时间
            redisTemplate.expire(ROOM_RANKING_KEY, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
        } catch (Exception e) {
            throw new RuntimeException("刷新房屋排行榜缓存失败", e);
        }
    }

    @Override
    public void refreshBuildingStats() {
        try {
            // 清除旧缓存
            redisTemplate.delete(BUILDING_STATS_KEY);
            
            // 获取楼栋统计数据
            List<BuildingPointStatsVO> stats = dataService.getBuildingPointStatsFromDB();
            
            // 存储楼栋统计数据
            redisTemplate.opsForValue().set(BUILDING_STATS_KEY, stats, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
        } catch (Exception e) {
            throw new RuntimeException("刷新楼栋统计缓存失败", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PointRankingVO> getRoomRankingFromCache(Integer limit) {
        try {
            // 获取排名数据（按积分倒序）
            Set<Object> roomIds = redisTemplate.opsForZSet().reverseRange(ROOM_RANKING_KEY, 0, limit - 1);
            
            if (roomIds == null || roomIds.isEmpty()) {
                // 缓存为空，尝试刷新
                refreshRoomRanking();
                roomIds = redisTemplate.opsForZSet().reverseRange(ROOM_RANKING_KEY, 0, limit - 1);
            }
            
            if (roomIds == null || roomIds.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 获取详细信息
            List<PointRankingVO> result = new ArrayList<>();
            int ranking = 1;
            
            for (Object roomIdObj : roomIds) {
                Long roomId = Long.valueOf(roomIdObj.toString());
                String detailKey = ROOM_RANKING_DETAIL_KEY + roomId;
                PointRankingVO detail = (PointRankingVO) redisTemplate.opsForValue().get(detailKey);
                
                if (detail != null) {
                    detail.setRanking(ranking++);
                    result.add(detail);
                }
            }
            
            return result;
            
        } catch (Exception e) {
            // 缓存异常时降级到数据库查询
            System.err.println("从缓存获取排行榜失败，降级到数据库查询: " + e.getMessage());
            return dataService.getRoomPointRankingFromDB(limit);
        }
    }

    @Override
    public PointRankingVO getRoomRankingFromCache(Long roomId) {
        try {
            // 先尝试从缓存获取详细信息
            String detailKey = ROOM_RANKING_DETAIL_KEY + roomId;
            PointRankingVO detail = (PointRankingVO) redisTemplate.opsForValue().get(detailKey);
            
            if (detail != null) {
                // 计算排名
                Long rank = redisTemplate.opsForZSet().reverseRank(ROOM_RANKING_KEY, roomId);
                if (rank != null) {
                    detail.setRanking(rank.intValue() + 1);
                }
                return detail;
            }
            
            // 缓存中没有，从数据库查询并缓存
            detail = dataService.getRoomRankingFromDB(roomId);
            if (detail != null) {
                redisTemplate.opsForValue().set(detailKey, detail, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            }
            
            return detail;
            
        } catch (Exception e) {
            // 缓存异常时降级到数据库查询
            System.err.println("从缓存获取房屋排名失败，降级到数据库查询: " + e.getMessage());
            return dataService.getRoomRankingFromDB(roomId);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BuildingPointStatsVO> getBuildingStatsFromCache() {
        try {
            Object cached = redisTemplate.opsForValue().get(BUILDING_STATS_KEY);
            if (cached != null) {
                return (List<BuildingPointStatsVO>) cached;
            }
            
            // 缓存为空，尝试刷新
            refreshBuildingStats();
            cached = redisTemplate.opsForValue().get(BUILDING_STATS_KEY);
            
            return cached != null ? (List<BuildingPointStatsVO>) cached : Collections.emptyList();
            
        } catch (Exception e) {
            // 缓存异常时降级到数据库查询
            System.err.println("从缓存获取楼栋统计失败，降级到数据库查询: " + e.getMessage());
            return dataService.getBuildingPointStatsFromDB();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PointRankingVO> getBuildingRoomRankingFromCache(Long areaId, Integer limit) {
        try {
            String buildingKey = BUILDING_ROOM_RANKING_KEY + areaId;
            Object cached = redisTemplate.opsForValue().get(buildingKey);
            
            if (cached != null) {
                return (List<PointRankingVO>) cached;
            }
            
            // 缓存为空，从数据库查询并缓存
            List<PointRankingVO> result = dataService.getBuildingRoomRankingFromDB(areaId, limit);
            redisTemplate.opsForValue().set(buildingKey, result, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
            return result;
            
        } catch (Exception e) {
            // 缓存异常时降级到数据库查询
            System.err.println("从缓存获取楼栋排行失败，降级到数据库查询: " + e.getMessage());
            return dataService.getBuildingRoomRankingFromDB(areaId, limit);
        }
    }

    @Override
    public void updateRoomPoints(Long roomId, Integer pointsBalance) {
        try {
            // 更新排行榜中的积分
            redisTemplate.opsForZSet().add(ROOM_RANKING_KEY, roomId, -pointsBalance);
            
            // 更新详细信息
            PointRankingVO vo = new PointRankingVO();
            vo.setRoomId(roomId);
            vo.setPointsBalance(pointsBalance);
            vo.setRankingChange(0);
            
            String detailKey = ROOM_RANKING_DETAIL_KEY + roomId;
            redisTemplate.opsForValue().set(detailKey, vo, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
            // 清除相关楼栋缓存
            String buildingKey = BUILDING_ROOM_RANKING_KEY + "unknown"; // 暂时清除所有楼栋缓存
            redisTemplate.delete(buildingKey);
            
        } catch (Exception e) {
            // 更新失败不影响主业务，记录日志即可
            System.err.println("更新积分缓存失败: " + e.getMessage());
        }
    }

    @Override
    public void removeRoomRanking(Long roomId) {
        try {
            // 从排行榜中移除
            redisTemplate.opsForZSet().remove(ROOM_RANKING_KEY, roomId);
            
            // 删除详细信息
            String detailKey = ROOM_RANKING_DETAIL_KEY + roomId;
            redisTemplate.delete(detailKey);
            
        } catch (Exception e) {
            System.err.println("删除积分缓存失败: " + e.getMessage());
        }
    }

    @Override
    public String getCacheStatus() {
        try {
            Object status = redisTemplate.opsForValue().get(CACHE_STATUS_KEY);
            return status != null ? status.toString() : "未知";
        } catch (Exception e) {
            return "获取缓存状态失败: " + e.getMessage();
        }
    }

    /**
     * 更新缓存状态
     */
    private void updateCacheStatus(String status, String message) {
        try {
            Map<String, Object> statusInfo = new HashMap<>();
            statusInfo.put("status", status);
            statusInfo.put("message", message);
            statusInfo.put("timestamp", new Date());
            
            String statusJson = statusInfo.toString();
            redisTemplate.opsForValue().set(CACHE_STATUS_KEY, statusJson, 24, TimeUnit.HOURS);
            
        } catch (Exception e) {
            System.err.println("更新缓存状态失败: " + e.getMessage());
        }
    }
}
