package nynu.cityEase.service.gov.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import nynu.cityEase.api.vo.gov.BuildingPointStatsVO;
import nynu.cityEase.api.vo.gov.PointRankingVO;
import nynu.cityEase.service.gov.service.IGovPointRankingDataService;
import nynu.cityEase.service.pms.repository.entity.PublicAreaDO;
import nynu.cityEase.service.pms.repository.entity.RoomDO;
import nynu.cityEase.service.pms.repository.mapper.PublicAreaMapper;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 积分排行榜数据查询服务实现类
 * 专门用于数据库查询，避免循环依赖
 * Created: 2026/3/18
 */
@Service
public class GovPointRankingDataServiceImpl implements IGovPointRankingDataService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private PublicAreaMapper publicAreaMapper;

    @Override
    public List<PointRankingVO> getRoomPointRankingFromDB(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 50;
        }

        // 查询所有有积分的房屋，按积分降序排列
        LambdaQueryWrapper<RoomDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.gt(RoomDO::getPointsBalance, 0)
                   .orderByDesc(RoomDO::getPointsBalance)
                   .last("LIMIT " + limit);

        List<RoomDO> rooms = roomMapper.selectList(queryWrapper);
        
        if (rooms.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取所有区域信息
        Set<Long> areaIds = rooms.stream()
                .map(RoomDO::getAreaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<Long, String> areaNameMap = new HashMap<>();
        if (!areaIds.isEmpty()) {
            List<PublicAreaDO> areas = publicAreaMapper.selectBatchIds(areaIds);
            areaNameMap = areas.stream()
                    .collect(Collectors.toMap(PublicAreaDO::getId, PublicAreaDO::getName));
        }

        // 转换为VO并添加排名
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
            vo.setRankingChange(0); // 暂时设为0，可以后续计算排名变化
            vo.setUpdateTime(room.getUpdateTime());
            
            result.add(vo);
        }

        return result;
    }

    @Override
    public PointRankingVO getRoomRankingFromDB(Long roomId) {
        RoomDO room = roomMapper.selectById(roomId);
        if (room == null) {
            return null;
        }

        // 获取区域名称
        String areaName = null;
        if (room.getAreaId() != null) {
            PublicAreaDO area = publicAreaMapper.selectById(room.getAreaId());
            if (area != null) {
                areaName = area.getName();
            }
        }

        // 计算排名
        LambdaQueryWrapper<RoomDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.gt(RoomDO::getPointsBalance, room.getPointsBalance());
        Long higherCount = roomMapper.selectCount(queryWrapper);
        int ranking = higherCount.intValue() + 1;

        PointRankingVO vo = new PointRankingVO();
        vo.setRanking(ranking);
        vo.setRoomId(room.getId());
        vo.setRoomNum(room.getRoomNum());
        vo.setAreaId(room.getAreaId());
        vo.setAreaName(areaName);
        vo.setPointsBalance(room.getPointsBalance());
        vo.setRankingChange(0); // 暂时设为0
        vo.setUpdateTime(room.getUpdateTime());

        return vo;
    }

    @Override
    public List<BuildingPointStatsVO> getBuildingPointStatsFromDB() {
        // 查询所有楼栋（level=2的公共区域）
        LambdaQueryWrapper<PublicAreaDO> areaQuery = new LambdaQueryWrapper<>();
        areaQuery.eq(PublicAreaDO::getLevel, 2)
                 .orderByAsc(PublicAreaDO::getSort);
        
        List<PublicAreaDO> buildings = publicAreaMapper.selectList(areaQuery);
        
        if (buildings.isEmpty()) {
            return Collections.emptyList();
        }

        List<BuildingPointStatsVO> result = new ArrayList<>();
        
        for (int i = 0; i < buildings.size(); i++) {
            PublicAreaDO building = buildings.get(i);
            
            // 查询该楼栋下的所有房屋
            LambdaQueryWrapper<RoomDO> roomQuery = new LambdaQueryWrapper<>();
            roomQuery.eq(RoomDO::getAreaId, building.getId());
            
            List<RoomDO> rooms = roomMapper.selectList(roomQuery);
            
            if (rooms.isEmpty()) {
                continue;
            }
            
            // 计算统计数据
            int totalPoints = rooms.stream()
                    .mapToInt(room -> room.getPointsBalance() != null ? room.getPointsBalance() : 0)
                    .sum();
            
            double avgPoints = rooms.stream()
                    .mapToInt(room -> room.getPointsBalance() != null ? room.getPointsBalance() : 0)
                    .average()
                    .orElse(0.0);
            
            int maxPoints = rooms.stream()
                    .mapToInt(room -> room.getPointsBalance() != null ? room.getPointsBalance() : 0)
                    .max()
                    .orElse(0);
            
            int minPoints = rooms.stream()
                    .mapToInt(room -> room.getPointsBalance() != null ? room.getPointsBalance() : 0)
                    .min()
                    .orElse(0);
            
            BuildingPointStatsVO vo = new BuildingPointStatsVO();
            vo.setAreaId(building.getId());
            vo.setAreaName(building.getName());
            vo.setTotalPoints(totalPoints);
            vo.setRoomCount(rooms.size());
            vo.setAvgPoints(avgPoints);
            vo.setMaxPoints(maxPoints);
            vo.setMinPoints(minPoints);
            vo.setBuildingRanking(i + 1);
            vo.setUpdateTime(LocalDateTime.now());
            
            result.add(vo);
        }

        // 按总积分降序排列
        result.sort((a, b) -> Integer.compare(b.getTotalPoints(), a.getTotalPoints()));
        
        // 重新设置排名
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setBuildingRanking(i + 1);
        }

        return result;
    }

    @Override
    public List<PointRankingVO> getBuildingRoomRankingFromDB(Long areaId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 50;
        }

        // 查询该楼栋下的所有房屋，按积分降序排列
        LambdaQueryWrapper<RoomDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomDO::getAreaId, areaId)
                   .gt(RoomDO::getPointsBalance, 0)
                   .orderByDesc(RoomDO::getPointsBalance)
                   .last("LIMIT " + limit);

        List<RoomDO> rooms = roomMapper.selectList(queryWrapper);
        
        if (rooms.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取楼栋名称
        PublicAreaDO area = publicAreaMapper.selectById(areaId);
        String areaName = area != null ? area.getName() : null;

        // 转换为VO并添加排名
        List<PointRankingVO> result = new ArrayList<>();
        for (int i = 0; i < rooms.size(); i++) {
            RoomDO room = rooms.get(i);
            PointRankingVO vo = new PointRankingVO();
            vo.setRanking(i + 1);
            vo.setRoomId(room.getId());
            vo.setRoomNum(room.getRoomNum());
            vo.setAreaId(areaId);
            vo.setAreaName(areaName);
            vo.setPointsBalance(room.getPointsBalance());
            vo.setRankingChange(0); // 暂时设为0
            vo.setUpdateTime(room.getUpdateTime());
            
            result.add(vo);
        }

        return result;
    }
}
