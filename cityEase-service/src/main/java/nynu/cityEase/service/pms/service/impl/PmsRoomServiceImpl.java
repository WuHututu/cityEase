package nynu.cityEase.service.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.pms.BatchCreateRoomReq;
import nynu.cityEase.service.pms.repository.entity.PublicAreaDO;
import nynu.cityEase.service.pms.repository.entity.RoomDO;
import nynu.cityEase.service.pms.repository.mapper.PublicAreaMapper;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import nynu.cityEase.service.pms.service.IPmsRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PmsRoomServiceImpl extends ServiceImpl<RoomMapper, RoomDO> implements IPmsRoomService {

    @Autowired
    private PublicAreaMapper publicAreaMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateRooms(BatchCreateRoomReq req) {
        // 1. 基础校验：参数不能乱传
        if (req.getTotalFloors() <= 0 || req.getRoomsPerFloor() <= 0) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "楼层数和每层户数必须大于0");
        }

        // 2. 校验单元是否存在
        PublicAreaDO area = publicAreaMapper.selectById(req.getAreaId());
        if (area == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "指定的单元不存在");
        }

        // 3. 幂等性校验 (防呆设计)：如果这个单元下已经有房间了，禁止再次批量生成，防止数据灾难
        long existCount = this.count(new LambdaQueryWrapper<RoomDO>()
                .eq(RoomDO::getAreaId, req.getAreaId()));
        if (existCount > 0) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "该单元下已存在房间，无法使用批量生成");
        }

        // 4. 核心逻辑：双层循环生成房间实体
        List<RoomDO> roomList = new ArrayList<>();

        for (int floor = 1; floor <= req.getTotalFloors(); floor++) {
            for (int index = 1; index <= req.getRoomsPerFloor(); index++) {

                // 生成门牌号字符串，例如: 1层1户 -> 101, 12层4户 -> 1204
                // %d 代表楼层，%02d 代表户号(不足2位前面补0)
                String roomNum = String.format("%d%02d", floor, index);

                RoomDO room = new RoomDO();
                room.setAreaId(req.getAreaId());
                room.setRoomNum(roomNum);
                // 初始积分 0
                room.setPointsBalance(0);

                roomList.add(room);
            }
        }

        // 5. 批量插入数据库 (MyBatis-Plus 提供的超级方法，底层自动拼装批量 Insert SQL)
        this.saveBatch(roomList);
    }
}