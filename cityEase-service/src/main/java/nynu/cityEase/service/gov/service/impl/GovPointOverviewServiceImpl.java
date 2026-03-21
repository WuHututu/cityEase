package nynu.cityEase.service.gov.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import nynu.cityEase.api.vo.gov.GovPointOverviewVO;
import nynu.cityEase.api.vo.gov.GovPointTrendVO;
import nynu.cityEase.service.gov.repository.entity.GovPointLogDO;
import nynu.cityEase.service.gov.repository.mapper.GovPointLogMapper;
import nynu.cityEase.service.gov.service.IGovPointOverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GovPointOverviewServiceImpl implements IGovPointOverviewService {

    private static final int CHANGE_TYPE_ADD = 1;
    private static final int CHANGE_TYPE_SUB = 2;

    @Autowired
    private GovPointLogMapper pointLogMapper;

    @Override
    public GovPointOverviewVO getOverview() {
        GovPointOverviewVO vo = new GovPointOverviewVO();
        vo.setTotalGranted(sumByType(CHANGE_TYPE_ADD));
        vo.setTotalConsumed(sumByType(CHANGE_TYPE_SUB));
        vo.setParticipantUsers(countParticipants());

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        int todayGranted = sumByTypeAndTimeRange(CHANGE_TYPE_ADD, todayStart, tomorrowStart);
        int todayConsumed = sumByTypeAndTimeRange(CHANGE_TYPE_SUB, todayStart, tomorrowStart);
        vo.setTodayNetChange(todayGranted - todayConsumed);
        return vo;
    }

    @Override
    public GovPointTrendVO getRecentTrend(Integer days) {
        int safeDays = (days == null || days <= 0 || days > 30) ? 7 : days;
        LocalDate startDate = LocalDate.now().minusDays(safeDays - 1L);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = LocalDate.now().plusDays(1).atStartOfDay();

        QueryWrapper<GovPointLogDO> wrapper = new QueryWrapper<>();
        wrapper.select("DATE(create_time) AS day", "change_type", "COALESCE(SUM(change_amount), 0) AS total")
                .ge("create_time", startTime)
                .lt("create_time", endTime)
                .groupBy("DATE(create_time)", "change_type")
                .orderByAsc("day");

        List<Map<String, Object>> rows = pointLogMapper.selectMaps(wrapper);
        Map<String, Integer> grantMap = new HashMap<>();
        Map<String, Integer> consumeMap = new HashMap<>();

        for (Map<String, Object> row : rows) {
            String day = String.valueOf(row.get("day"));
            int changeType = toInt(row.get("change_type"));
            int total = toInt(row.get("total"));
            if (changeType == CHANGE_TYPE_ADD) {
                grantMap.put(day, total);
            } else if (changeType == CHANGE_TYPE_SUB) {
                consumeMap.put(day, total);
            }
        }

        List<String> dates = new ArrayList<>();
        List<Integer> grants = new ArrayList<>();
        List<Integer> consumes = new ArrayList<>();
        List<Integer> nets = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 0; i < safeDays; i++) {
            LocalDate d = startDate.plusDays(i);
            String dateKey = d.toString();
            int dayGrant = grantMap.getOrDefault(dateKey, 0);
            int dayConsume = consumeMap.getOrDefault(dateKey, 0);

            dates.add(d.format(formatter));
            grants.add(dayGrant);
            consumes.add(dayConsume);
            nets.add(dayGrant - dayConsume);
        }

        GovPointTrendVO vo = new GovPointTrendVO();
        vo.setDates(dates);
        vo.setGrantSeries(grants);
        vo.setConsumeSeries(consumes);
        vo.setNetSeries(nets);
        return vo;
    }

    private Integer sumByType(Integer changeType) {
        QueryWrapper<GovPointLogDO> wrapper = new QueryWrapper<>();
        wrapper.select("COALESCE(SUM(change_amount), 0) AS total")
                .eq("change_type", changeType);

        Map<String, Object> map = pointLogMapper.selectMaps(wrapper).stream().findFirst().orElse(Collections.emptyMap());
        return toInt(map.get("total"));
    }

    private Integer sumByTypeAndTimeRange(Integer changeType, LocalDateTime start, LocalDateTime end) {
        QueryWrapper<GovPointLogDO> wrapper = new QueryWrapper<>();
        wrapper.select("COALESCE(SUM(change_amount), 0) AS total")
                .eq("change_type", changeType)
                .ge("create_time", start)
                .lt("create_time", end);

        Map<String, Object> map = pointLogMapper.selectMaps(wrapper).stream().findFirst().orElse(Collections.emptyMap());
        return toInt(map.get("total"));
    }

    private Long countParticipants() {
        QueryWrapper<GovPointLogDO> wrapper = new QueryWrapper<>();
        wrapper.select("COUNT(DISTINCT user_id) AS total");
        Map<String, Object> map = pointLogMapper.selectMaps(wrapper).stream().findFirst().orElse(Collections.emptyMap());
        Object value = map.get("total");
        return value == null ? 0L : Long.parseLong(String.valueOf(value));
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
