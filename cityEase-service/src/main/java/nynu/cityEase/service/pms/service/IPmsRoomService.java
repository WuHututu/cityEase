package nynu.cityEase.service.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import nynu.cityEase.api.vo.pms.BatchCreateRoomReq;
import nynu.cityEase.service.pms.repository.entity.RoomDO;

public interface IPmsRoomService extends IService<RoomDO> {
    
    /**
     * 一键批量生成房间
     * @param req 生成参数 (单元ID, 楼层数, 每层户数)
     */
    void batchCreateRooms(BatchCreateRoomReq req);
}