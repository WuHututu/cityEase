package nynu.cityEase.service.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import nynu.cityEase.api.vo.pms.HouseQueryReq;
import nynu.cityEase.api.vo.pms.HouseVO;
import nynu.cityEase.service.pms.repository.entity.RoomDO;

public interface IPmsHouseService {

    /**
     * 分页查询房屋和公共区域
     */
    Page<HouseVO> getHousePage(HouseQueryReq req);
}