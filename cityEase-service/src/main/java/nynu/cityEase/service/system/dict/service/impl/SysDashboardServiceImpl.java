package nynu.cityEase.service.system.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import nynu.cityEase.api.vo.system.ChartDataVO;
import nynu.cityEase.api.vo.system.DashboardMetricsVO;
import nynu.cityEase.service.pms.repository.entity.RepairOrderDO;
import nynu.cityEase.service.pms.repository.entity.UserRoomRelDO;
import nynu.cityEase.service.pms.repository.mapper.RepairOrderMapper;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import nynu.cityEase.service.pms.repository.mapper.UserRoomRelMapper;
import nynu.cityEase.service.system.dict.service.ISysDashboardService;
import nynu.cityEase.service.system.notice.repository.entity.SysNoticeDO;
import nynu.cityEase.service.system.notice.repository.mapper.SysNoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SysDashboardServiceImpl implements ISysDashboardService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private UserRoomRelMapper userRoomRelMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Autowired
    private SysNoticeMapper sysNoticeMapper;

    @Override
    public DashboardMetricsVO getCoreMetrics() {
        DashboardMetricsVO vo = new DashboardMetricsVO();

        // 1. 统计房屋总数
        Long totalRooms = roomMapper.selectCount(null);
        vo.setTotalRooms(totalRooms);

        // 2. 统计已认证通过的用户总数 (status = 1)
        LambdaQueryWrapper<UserRoomRelDO> authWrapper = new LambdaQueryWrapper<>();
        authWrapper.eq(UserRoomRelDO::getStatus, 1);
        Long authUsers = userRoomRelMapper.selectCount(authWrapper);
        vo.setAuthenticatedUsers(authUsers);

        // 3. 统计待派发的报修工单 (status = 0)
        LambdaQueryWrapper<RepairOrderDO> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(RepairOrderDO::getStatus, 0);
        Long pendingRepairs = repairOrderMapper.selectCount(pendingWrapper);
        vo.setPendingRepairs(pendingRepairs);

        // 4. 统计处理中的报修工单 (status = 1)
        LambdaQueryWrapper<RepairOrderDO> processWrapper = new LambdaQueryWrapper<>();
        processWrapper.eq(RepairOrderDO::getStatus, 1);
        Long processingRepairs = repairOrderMapper.selectCount(processWrapper);
        vo.setProcessingRepairs(processingRepairs);

        // 5. 统计已发布的公告 (status = 1)
        LambdaQueryWrapper<SysNoticeDO> noticeWrapper = new LambdaQueryWrapper<>();
        noticeWrapper.eq(SysNoticeDO::getStatus, 1);
        Long notices = sysNoticeMapper.selectCount(noticeWrapper);
        vo.setPublishedNotices(notices);

        // 6. 统计今日新增公告
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<SysNoticeDO> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(SysNoticeDO::getStatus, 1)
                   .ge(SysNoticeDO::getCreateTime, todayStart);
        Long todayNotices = sysNoticeMapper.selectCount(todayWrapper);
        vo.setTodayNotices(todayNotices);

        // 7. 统计本周新增公告
        LocalDateTime weekStart = LocalDate.now().minusDays(6).atStartOfDay();
        LambdaQueryWrapper<SysNoticeDO> weekWrapper = new LambdaQueryWrapper<>();
        weekWrapper.eq(SysNoticeDO::getStatus, 1)
                   .ge(SysNoticeDO::getCreateTime, weekStart);
        Long weekNotices = sysNoticeMapper.selectCount(weekWrapper);
        vo.setWeekNotices(weekNotices);

        // 8. 统计置顶公告
        LambdaQueryWrapper<SysNoticeDO> topWrapper = new LambdaQueryWrapper<>();
        topWrapper.eq(SysNoticeDO::getStatus, 1)
                  .eq(SysNoticeDO::getIsTop, 1);
        Long topNotices = sysNoticeMapper.selectCount(topWrapper);
        vo.setTopNotices(topNotices);

        return vo;
    }

    @Override
    public ChartDataVO getRepairTrendChart() {
        // 1. 准备近 7 天的日期作为 X 轴
        List<String> xAxis = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            xAxis.add(today.minusDays(i).format(formatter));
        }

        // 2. 计算 7 天前的起始时间
        LocalDateTime startTime = today.minusDays(6).atStartOfDay();

        // 3. 查询近 7 天所有的工单 - 修复查询条件，使用LambdaQueryWrapper
        LambdaQueryWrapper<RepairOrderDO> query = new LambdaQueryWrapper<>();
        query.ge(RepairOrderDO::getCreateTime, startTime);
        List<RepairOrderDO> recentOrders = repairOrderMapper.selectList(query);

        // 4. 在内存中按日期进行统计分类
        List<Integer> newOrderData = new ArrayList<>(7);
        List<Integer> completedData = new ArrayList<>(7);

        // 初始化数组为 0
        for (int i = 0; i < 7; i++) {
            newOrderData.add(0);
            completedData.add(0);
        }

        for (RepairOrderDO order : recentOrders) {
            if (order.getCreateTime() == null) continue;

            // 计算当前记录是近 7 天中的哪一天（索引 0-6）
            LocalDate orderDate = order.getCreateTime().toLocalDate();
            // 如果工单日期早于起始日期或晚于今天，则跳过(理论上 query wrapper 已经过滤，这是双保险)
            if(orderDate.isBefore(today.minusDays(6)) || orderDate.isAfter(today)) continue;

            // 计算出它在 xAxis 列表中的索引
            // 今天是第N天，6天前是第(N-6)天，所以今天在xAxis中的索引是6，昨天是5，...，6天前是0
            // 日期差值 = orderDate距离epoch的天数 - (today距离epoch的天数 - 6)
            // 所以索引 = 6 - (today.toEpochDay() - orderDate.toEpochDay())
            // 即：索引 = 6 - today.toEpochDay() + orderDate.toEpochDay()
            int dayIndex = 6 - (int)(today.toEpochDay() - orderDate.toEpochDay());

            if(dayIndex >= 0 && dayIndex < 7) {
                // 只要创建了，就是"新增报修"
                newOrderData.set(dayIndex, newOrderData.get(dayIndex) + 1);

                // 如果状态是 2 (已完成)，则算作"完成维修"
                if (order.getStatus() != null && order.getStatus() == 2) {
                    completedData.set(dayIndex, completedData.get(dayIndex) + 1);
                }
            } else {
                // 添加调试日志，看是否有索引越界问题
                System.out.println("Invalid dayIndex: " + dayIndex + ", orderDate: " + orderDate + ", today: " + today);
            }
        }

        // 5. 组装返回对象
        ChartDataVO chartVO = new ChartDataVO();
        chartVO.setXAxisData(xAxis);

        List<ChartDataVO.SeriesData> series = new ArrayList<>();
        chartVO.setSeries(series);

        series.add(new ChartDataVO.SeriesData().setName("新增报修").setData(newOrderData));
        series.add(new ChartDataVO.SeriesData().setName("完成维修").setData(completedData));

        return chartVO;
    }

}