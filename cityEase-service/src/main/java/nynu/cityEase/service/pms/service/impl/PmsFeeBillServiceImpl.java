package nynu.cityEase.service.pms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.pms.FeeBillVO;
import nynu.cityEase.service.pms.repository.entity.FeeBillDO;
import nynu.cityEase.service.pms.repository.entity.RoomDO;
import nynu.cityEase.service.pms.repository.entity.UserRoomRelDO;
import nynu.cityEase.service.pms.repository.mapper.FeeBillMapper;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import nynu.cityEase.service.pms.service.IPmsFeeBillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PmsFeeBillServiceImpl extends ServiceImpl<FeeBillMapper, FeeBillDO> implements IPmsFeeBillService {

    private static final Logger log = LoggerFactory.getLogger(PmsFeeBillServiceImpl.class);

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private nynu.cityEase.service.pms.repository.mapper.UserRoomRelMapper userRoomRelMapper;

    @Autowired
    private nynu.cityEase.service.pms.service.IPmsPublicAreaService publicAreaService;

    @Override
    public List<FeeBillVO> getMyBills() {

        long userId = StpUtil.getLoginIdAsLong();

        // 1. 查询该用户当前绑定的所有且已审核通过的房屋
        LambdaQueryWrapper<UserRoomRelDO> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(UserRoomRelDO::getUserId, userId)
                .eq(UserRoomRelDO::getStatus, 1);
        List<UserRoomRelDO> relList = userRoomRelMapper.selectList(relWrapper);

        if (cn.hutool.core.collection.CollUtil.isEmpty(relList)) {
            return java.util.Collections.emptyList();
        }

        // 2. 提取出所有的 roomId
        List<Long> roomIds = relList.stream()
                .map(UserRoomRelDO::getRoomId)
                .collect(java.util.stream.Collectors.toList());

        // 3. 根据 roomIds 查询账单，未缴费的排在前面，月份最新的排在前面
        LambdaQueryWrapper<FeeBillDO> billWrapper = new LambdaQueryWrapper<>();
        billWrapper.in(FeeBillDO::getRoomId, roomIds)
                .orderByAsc(FeeBillDO::getStatus)
                .orderByDesc(FeeBillDO::getFeeMonth);
        List<FeeBillDO> billList = this.list(billWrapper);

        // 4. 组装 VO 并拼接房屋详细地址
        List<FeeBillVO> voList = new java.util.ArrayList<>();
        for (FeeBillDO billDO : billList) {
            FeeBillVO vo = new FeeBillVO();
            org.springframework.beans.BeanUtils.copyProperties(billDO, vo);

            RoomDO roomDO = roomMapper.selectById(billDO.getRoomId());
            if (roomDO != null) {
                String areaFullName = publicAreaService.getFullAreaName(roomDO.getAreaId());
                vo.setRoomAddress(areaFullName + "-" + roomDO.getRoomNum());
            }

            voList.add(vo);
        }

        return voList;
    }

    @Override
    public void generateMonthlyBills(String targetMonth) {
        
        List<RoomDO> allRooms = roomMapper.selectList(null);
        int successCount = 0;
        int skipCount = 0;

        for (RoomDO room : allRooms) {
            
            LambdaQueryWrapper<FeeBillDO> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(FeeBillDO::getRoomId, room.getId())
                        .eq(FeeBillDO::getFeeMonth, targetMonth);
                        
            if (this.count(existWrapper) > 0) {
                skipCount++;
                continue;
            }

            FeeBillDO newBill = new FeeBillDO();
            newBill.setRoomId(room.getId());
            newBill.setFeeMonth(targetMonth);
            newBill.setAmount(new BigDecimal("150.00"));
            newBill.setStatus(0);
            
            this.save(newBill);
            successCount++;
        }

        log.info("生成 {} 月份账单完毕。成功生成: {} 笔，跳过已存在: {} 笔", targetMonth, successCount, skipCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payBill(Long billId) {
        
        long userId = StpUtil.getLoginIdAsLong();

        FeeBillDO bill = this.getById(billId);
        if (bill == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "账单不存在");
        }

        if (bill.getStatus() == 1) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "该账单已缴费，请勿重复支付");
        }

        LambdaUpdateWrapper<FeeBillDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FeeBillDO::getId, billId)
                     .eq(FeeBillDO::getStatus, 0)
                     .set(FeeBillDO::getStatus, 1)
                     .set(FeeBillDO::getPayerId, userId)
                     .set(FeeBillDO::getPayTime, LocalDateTime.now());

        int updateRows = this.baseMapper.update(null, updateWrapper);

        if (updateRows == 0) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "支付失败，账单状态已发生变化");
        }
        
        log.info("用户 {} 成功支付了账单 {}", userId, billId);
    }
}