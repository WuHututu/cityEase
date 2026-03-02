package nynu.cityEase.service.system.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

        return vo;
    }
}