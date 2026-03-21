package nynu.cityEase.service.gov.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import nynu.cityEase.api.vo.constants.RedisKeyConstants;
import nynu.cityEase.api.vo.gov.BuildingPointStatsVO;
import nynu.cityEase.api.vo.gov.PointRankingVO;
import nynu.cityEase.service.gov.service.IGovPointRankingCacheService;
import nynu.cityEase.service.pms.repository.entity.PublicAreaDO;
import nynu.cityEase.service.pms.repository.entity.RoomDO;
import nynu.cityEase.service.pms.repository.mapper.PublicAreaMapper;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class GovPointRankingCacheServiceImpl implements IGovPointRankingCacheService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private PublicAreaMapper publicAreaMapper;

    @Override
    public void refreshAllRankings() {
        refreshRoomRanking();
        refreshBuildingStats();
    }

    @Override
    public void clearAllRankings() {
        redisTemplate.delete(RedisKeyConstants.GOV_POINT_ROOM_RANKING_KEY);
        redisTemplate.delete(RedisKeyConstants.GOV_POINT_BUILDING_STATS_KEY);

        Set<String> buildingKeys = redisTemplate.keys(RedisKeyConstants.GOV_POINT_BUILDING_ROOM_RANKING_KEY_PREFIX + "*");
        if (buildingKeys != null && !buildingKeys.isEmpty()) {
            redisTemplate.delete(buildingKeys);
        }
    }

    @Override
    public void refreshRoomRanking() {
        List<PointRankingVO> ranking = queryRoomRanking(500);
        redisTemplate.opsForValue().set(
                RedisKeyConstants.GOV_POINT_ROOM_RANKING_KEY,
                JSONUtil.toJsonStr(ranking),
                30,
                TimeUnit.MINUTES
        );
    }

    @Override
    public void refreshBuildingStats() {
        List<BuildingPointStatsVO> stats = queryBuildingStats();
        redisTemplate.opsForValue().set(
                RedisKeyConstants.GOV_POINT_BUILDING_STATS_KEY,
                JSONUtil.toJsonStr(stats),
                30,
                TimeUnit.MINUTES
        );
    }

    @Override
    public List<PointRankingVO> getRoomRankingFromCache(Integer limit) {
        int finalLimit = (limit == null || limit <= 0) ? 50 : limit;
        String json = redisTemplate.opsForValue().get(RedisKeyConstants.GOV_POINT_ROOM_RANKING_KEY);
        List<PointRankingVO> ranking;
        if (json == null || json.isEmpty()) {
            ranking = queryRoomRanking(Math.max(finalLimit, 200));
            redisTemplate.opsForValue().set(RedisKeyConstants.GOV_POINT_ROOM_RANKING_KEY, JSONUtil.toJsonStr(ranking), 30, TimeUnit.MINUTES);
        } else {
            ranking = JSONUtil.toList(json, PointRankingVO.class);
        }

        return ranking.stream().limit(finalLimit).collect(Collectors.toList());
    }

    @Override
    public PointRankingVO getRoomRankingFromCache(Long roomId) {
        if (roomId == null) {
            return null;
        }
        List<PointRankingVO> ranking = getRoomRankingFromCache(500);
        return ranking.stream().filter(item -> roomId.equals(item.getRoomId())).findFirst().orElse(null);
    }

    @Override
    public List<BuildingPointStatsVO> getBuildingStatsFromCache() {
        String json = redisTemplate.opsForValue().get(RedisKeyConstants.GOV_POINT_BUILDING_STATS_KEY);
        if (json == null || json.isEmpty()) {
            List<BuildingPointStatsVO> stats = queryBuildingStats();
            redisTemplate.opsForValue().set(RedisKeyConstants.GOV_POINT_BUILDING_STATS_KEY, JSONUtil.toJsonStr(stats), 30, TimeUnit.MINUTES);
            return stats;
        }
        return JSONUtil.toList(json, BuildingPointStatsVO.class);
    }

    @Override
    public List<PointRankingVO> getBuildingRoomRankingFromCache(Long areaId, Integer limit) {
        if (areaId == null) {
            return Collections.emptyList();
        }

        int finalLimit = (limit == null || limit <= 0) ? 50 : limit;
        String key = RedisKeyConstants.GOV_POINT_BUILDING_ROOM_RANKING_KEY_PREFIX + areaId;
        String json = redisTemplate.opsForValue().get(key);
        List<PointRankingVO> ranking;
        if (json == null || json.isEmpty()) {
            ranking = queryBuildingRoomRanking(areaId, Math.max(finalLimit, 200));
            redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(ranking), 30, TimeUnit.MINUTES);
        } else {
            ranking = JSONUtil.toList(json, PointRankingVO.class);
        }

        return ranking.stream().limit(finalLimit).collect(Collectors.toList());
    }

    @Override
    public void updateRoomPoints(Long roomId, Integer pointsBalance) {
        if (roomId == null) {
            return;
        }
        refreshRoomRanking();
    }

    @Override
    public void removeRoomRanking(Long roomId) {
        if (roomId == null) {
            return;
        }
        refreshRoomRanking();
    }

    @Override
    public String getCacheStatus() {
        Boolean hasRoom = redisTemplate.hasKey(RedisKeyConstants.GOV_POINT_ROOM_RANKING_KEY);
        Boolean hasBuilding = redisTemplate.hasKey(RedisKeyConstants.GOV_POINT_BUILDING_STATS_KEY);
        return "roomRanking=" + Boolean.TRUE.equals(hasRoom) + ", buildingStats=" + Boolean.TRUE.equals(hasBuilding);
    }

    private List<PointRankingVO> queryRoomRanking(int limit) {
        LambdaQueryWrapper<RoomDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.gt(RoomDO::getPointsBalance, 0)
                .orderByDesc(RoomDO::getPointsBalance)
                .last("LIMIT " + limit);

        List<RoomDO> rooms = roomMapper.selectList(queryWrapper);
        Set<Long> areaIds = rooms.stream().map(RoomDO::getAreaId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> areaNameMap = getAreaNameMap(areaIds);

        List<PointRankingVO> result = new ArrayList<>();
        for (int i = 0; i < rooms.size(); i++) {
            RoomDO room = rooms.get(i);
            PointRankingVO vo = new PointRankingVO();
            vo.setRanking(i + 1);
            vo.setRoomId(room.getId());
            vo.setRoomNum(room.getRoomNum());
            vo.setAreaId(room.getAreaId());
            vo.setAreaName(areaNameMap.get(room.getAreaId()));
            vo.setPointsBalance(room.getPointsBalance());
            vo.setRankingChange(0);
            vo.setUpdateTime(room.getUpdateTime());
            result.add(vo);
        }
        return result;
    }

    private List<BuildingPointStatsVO> queryBuildingStats() {
        LambdaQueryWrapper<PublicAreaDO> areaWrapper = new LambdaQueryWrapper<>();
        areaWrapper.eq(PublicAreaDO::getLevel, 2);
        List<PublicAreaDO> buildings = publicAreaMapper.selectList(areaWrapper);

        List<BuildingPointStatsVO> result = new ArrayList<>();
        for (PublicAreaDO building : buildings) {
            LambdaQueryWrapper<RoomDO> roomWrapper = new LambdaQueryWrapper<>();
            roomWrapper.eq(RoomDO::getAreaId, building.getId());
            List<RoomDO> rooms = roomMapper.selectList(roomWrapper);
            if (rooms.isEmpty()) {
                continue;
            }

            BuildingPointStatsVO stats = new BuildingPointStatsVO();
            stats.setAreaId(building.getId());
            stats.setAreaName(building.getName());

            int totalPoints = rooms.stream().mapToInt(room -> room.getPointsBalance() == null ? 0 : room.getPointsBalance()).sum();
            stats.setTotalPoints(totalPoints);
            stats.setRoomCount(rooms.size());
            stats.setAvgPoints((double) totalPoints / rooms.size());
            stats.setMaxPoints(rooms.stream().mapToInt(room -> room.getPointsBalance() == null ? 0 : room.getPointsBalance()).max().orElse(0));
            stats.setMinPoints(rooms.stream().mapToInt(room -> room.getPointsBalance() == null ? 0 : room.getPointsBalance()).min().orElse(0));
            stats.setUpdateTime(building.getUpdateTime());
            result.add(stats);
        }

        result.sort((a, b) -> b.getTotalPoints().compareTo(a.getTotalPoints()));
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setBuildingRanking(i + 1);
        }

        return result;
    }

    private List<PointRankingVO> queryBuildingRoomRanking(Long areaId, int limit) {
        LambdaQueryWrapper<RoomDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomDO::getAreaId, areaId)
                .gt(RoomDO::getPointsBalance, 0)
                .orderByDesc(RoomDO::getPointsBalance)
                .last("LIMIT " + limit);

        List<RoomDO> rooms = roomMapper.selectList(queryWrapper);
        String areaName = getAreaName(areaId);

        List<PointRankingVO> result = new ArrayList<>();
        for (int i = 0; i < rooms.size(); i++) {
            RoomDO room = rooms.get(i);
            PointRankingVO vo = new PointRankingVO();
            vo.setRanking(i + 1);
            vo.setRoomId(room.getId());
            vo.setRoomNum(room.getRoomNum());
            vo.setAreaId(room.getAreaId());
            vo.setAreaName(areaName);
            vo.setPointsBalance(room.getPointsBalance());
            vo.setRankingChange(0);
            vo.setUpdateTime(room.getUpdateTime());
            result.add(vo);
        }
        return result;
    }

    private Map<Long, String> getAreaNameMap(Set<Long> areaIds) {
        if (areaIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<PublicAreaDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PublicAreaDO::getId, areaIds);
        List<PublicAreaDO> areas = publicAreaMapper.selectList(wrapper);
        return areas.stream().collect(Collectors.toMap(PublicAreaDO::getId, PublicAreaDO::getName));
    }

    private String getAreaName(Long areaId) {
        if (areaId == null) {
            return null;
        }
        PublicAreaDO area = publicAreaMapper.selectById(areaId);
        return area == null ? null : area.getName();
    }
}
