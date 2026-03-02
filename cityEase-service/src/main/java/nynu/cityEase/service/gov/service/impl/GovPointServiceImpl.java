package nynu.cityEase.service.gov.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.service.gov.repository.entity.GovPointLogDO;
import nynu.cityEase.service.gov.repository.mapper.GovPointLogMapper;
import nynu.cityEase.service.gov.service.IGovPointService;
import nynu.cityEase.service.pms.repository.entity.RoomDO;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GovPointServiceImpl extends ServiceImpl<GovPointLogMapper, GovPointLogDO> implements IGovPointService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private GovPointLogMapper pointLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePoints(Long roomId, Long userId, Integer amount, boolean isAdd, String reason) {
        if (amount <= 0) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "积分变动数值必须大于0");
        }

        RoomDO room = roomMapper.selectById(roomId);
        if (room == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "操作的房屋不存在");
        }

        // 1. 尝试原子更新余额
        LambdaUpdateWrapper<RoomDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RoomDO::getId, roomId);

        if (isAdd) {
            // 增加积分: UPDATE pms_room SET points_balance = points_balance + ? WHERE id = ?
            updateWrapper.setSql("points_balance = points_balance + " + amount);
        } else {
            // 扣减积分: UPDATE pms_room SET points_balance = points_balance - ? WHERE id = ? AND points_balance >= ?
            // ★ 核心防线：必须保证扣减前余额充足，防止数据库出现负数超卖！
            updateWrapper.ge(RoomDO::getPointsBalance, amount) // >= amount
                         .setSql("points_balance = points_balance - " + amount);
        }

        // 执行更新
        int updateRows = roomMapper.update(null, updateWrapper);

        // 如果更新行数为0，说明扣减失败（余额不足）或者房屋突然被物理删除了
        if (updateRows == 0) {
            if (!isAdd) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "房屋积分余额不足，扣减失败");
            } else {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "积分更新失败，请重试");
            }
        }

        // 2. 重新查询最新的余额，用于记录流水对账
        // 因为刚刚是在数据库层面计算的，Java里的 room 对象还是旧的，必须重查一次
        RoomDO newRoom = roomMapper.selectById(roomId);

        // 3. 记录积分流水
        GovPointLogDO logDO = new GovPointLogDO();
        logDO.setRoomId(roomId);
        logDO.setUserId(userId);
        logDO.setChangeType(isAdd ? 1 : 2);
        logDO.setChangeAmount(amount);
        logDO.setAfterBalance(newRoom.getPointsBalance()); // 记录算完之后的最新余额
        logDO.setReason(reason);

        pointLogMapper.insert(logDO);
    }
}