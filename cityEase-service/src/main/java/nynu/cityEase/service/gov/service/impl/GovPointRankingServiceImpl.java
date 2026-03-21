package nynu.cityEase.service.gov.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import nynu.cityEase.api.vo.gov.BuildingPointStatsVO;
import nynu.cityEase.api.vo.gov.PointRankingVO;
import nynu.cityEase.service.gov.service.IGovPointRankingCacheService;
import nynu.cityEase.service.gov.service.IGovPointRankingService;
import nynu.cityEase.service.pms.repository.entity.PublicAreaDO;
import nynu.cityEase.service.pms.repository.entity.RoomDO;
import nynu.cityEase.service.pms.repository.mapper.PublicAreaMapper;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 积分排行榜服务实现类
 * Created: 2026/3/18
 */
@Service
public class GovPointRankingServiceImpl implements IGovPointRankingService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private PublicAreaMapper publicAreaMapper;

    @Autowired
    @Lazy
    private IGovPointRankingCacheService cacheService;

    @Override
    public List<PointRankingVO> getRoomPointRanking(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 50;
        }

        // 优先从缓存获取
        try {
            return cacheService.getRoomRankingFromCache(limit);
        } catch (Exception e) {
            // 缓存异常时降级到数据库查询
            System.err.println("从缓存获取排行榜失败，降级到数据库查询: " + e.getMessage());
        }

        // 数据库查询逻辑
        LambdaQueryWrapper<RoomDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.gt(RoomDO::getPointsBalance, 0)
                   .orderByDesc(RoomDO::getPointsBalance)
                   .last("LIMIT " + limit);

        List<RoomDO> rooms = roomMapper.selectList(queryWrapper);
        
        // 获取区域信息缓存
        Map<Long, String> areaNameMap = getAreaNameMap(rooms.stream()
                .map(RoomDO::getAreaId)
                .collect(Collectors.toSet()));

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

    @Override
    public PointRankingVO getRoomRanking(Long roomId) {
        // 优先从缓存获取
        try {
            PointRankingVO cached = cacheService.getRoomRankingFromCache(roomId);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            System.err.println("从缓存获取房屋排名失败，降级到数据库查询: " + e.getMessage());
        }

        // 数据库查询逻辑
        RoomDO room = roomMapper.selectById(roomId);
        if (room == null) {
            return null;
        }

        // 计算该房屋的排名
        LambdaQueryWrapper<RoomDO> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.gt(RoomDO::getPointsBalance, room.getPointsBalance());
        Long higherCount = roomMapper.selectCount(countWrapper);
        int ranking = higherCount.intValue() + 1;

        // 获取区域名称
        String areaName = getAreaName(room.getAreaId());

        PointRankingVO vo = new PointRankingVO();
        vo.setRanking(ranking);
        vo.setRoomId(room.getId());
        vo.setRoomNum(room.getRoomNum());
        vo.setAreaId(room.getAreaId());
        vo.setAreaName(areaName);
        vo.setPointsBalance(room.getPointsBalance());
        vo.setRankingChange(0);
        vo.setUpdateTime(room.getUpdateTime());

        return vo;
    }

    @Override
    public List<BuildingPointStatsVO> getBuildingPointStats() {
        // 优先从缓存获取
        try {
            List<BuildingPointStatsVO> cached = cacheService.getBuildingStatsFromCache();
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }
        } catch (Exception e) {
            System.err.println("从缓存获取楼栋统计失败，降级到数据库查询: " + e.getMessage());
        }

        // 数据库查询逻辑
        // 查询所有楼栋级别的区域
        LambdaQueryWrapper<PublicAreaDO> areaWrapper = new LambdaQueryWrapper<>();
        // 楼栋级别
        areaWrapper.eq(PublicAreaDO::getLevel, 2);
        List<PublicAreaDO> buildings = publicAreaMapper.selectList(areaWrapper);

        List<BuildingPointStatsVO> result = new ArrayList<>();
        
        // 统计每个楼栋的积分情况
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
            
            int totalPoints = rooms.stream()
                    .mapToInt(room -> room.getPointsBalance() != null ? room.getPointsBalance() : 0)
                    .sum();
            stats.setTotalPoints(totalPoints);
            stats.setRoomCount(rooms.size());
            stats.setAvgPoints(rooms.size() > 0 ? (double) totalPoints / rooms.size() : 0.0);
            
            int maxPoints = rooms.stream()
                    .mapToInt(room -> room.getPointsBalance() != null ? room.getPointsBalance() : 0)
                    .max()
                    .orElse(0);
            stats.setMaxPoints(maxPoints);
            
            int minPoints = rooms.stream()
                    .mapToInt(room -> room.getPointsBalance() != null ? room.getPointsBalance() : 0)
                    .min()
                    .orElse(0);
            stats.setMinPoints(minPoints);
            
            stats.setUpdateTime(building.getUpdateTime());
            
            result.add(stats);
        }

        // 按总积分降序排序并设置排名
        result.sort((a, b) -> b.getTotalPoints().compareTo(a.getTotalPoints()));
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setBuildingRanking(i + 1);
        }

        return result;
    }

    @Override
    public List<PointRankingVO> getBuildingRoomRanking(Long areaId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 50;
        }

        // 优先从缓存获取
        try {
            return cacheService.getBuildingRoomRankingFromCache(areaId, limit);
        } catch (Exception e) {
            System.err.println("从缓存获取楼栋排行失败，降级到数据库查询: " + e.getMessage());
        }

        // 数据库查询逻辑
        LambdaQueryWrapper<RoomDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomDO::getAreaId, areaId)
                   .gt(RoomDO::getPointsBalance, 0)
                   .orderByDesc(RoomDO::getPointsBalance)
                   .last("LIMIT " + limit);

        List<RoomDO> rooms = roomMapper.selectList(queryWrapper);
        
        // 获取区域名称
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

    /**
     * 获取区域名称映射
     */
    private Map<Long, String> getAreaNameMap(Set<Long> areaIds) {
        if (areaIds.isEmpty()) {
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<PublicAreaDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PublicAreaDO::getId, areaIds);
        List<PublicAreaDO> areas = publicAreaMapper.selectList(wrapper);

        return areas.stream()
                .collect(Collectors.toMap(PublicAreaDO::getId, PublicAreaDO::getName));
    }

    /**
     * 获取单个区域名称
     */
    private String getAreaName(Long areaId) {
        if (areaId == null) {
            return "";
        }
        
        PublicAreaDO area = publicAreaMapper.selectById(areaId);
        return area != null ? area.getName() : "";
    }

    @Override
    public Long getRoomPointBalance(Long roomId) {
        RoomDO room = roomMapper.selectById(roomId);
        return room != null ? room.getPointsBalance() : 0L;
    }
}
