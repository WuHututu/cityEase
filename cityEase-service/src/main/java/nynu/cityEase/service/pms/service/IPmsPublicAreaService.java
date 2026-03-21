package nynu.cityEase.service.pms.service;

import nynu.cityEase.api.vo.pms.PublicAreaReq;
import nynu.cityEase.api.vo.pms.PublicAreaTreeVO;
import nynu.cityEase.service.pms.repository.entity.RoomDO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/11
 * Time: 14:46
 * Description: TODO
 */

public interface IPmsPublicAreaService {
    /**
     * 获取公共区域树状结构
     * @return 树状列表
     */
    List<PublicAreaTreeVO> getAreaTree();

    void addArea(PublicAreaReq req);

    void removeArea(Long areaId);

    /**
     * 根据底层节点ID，向上溯源获取完整的区域名称
     * @param areaId 底层区域ID (如单元ID)
     * @return 完整名称 (如: 江南星城-1号楼-1单元)
     */
    String getFullAreaName(Long areaId);
    
    /**
     * 根据区域ID获取房屋列表
     * @param areaId 区域ID
     * @return 房屋列表
     */
    List<RoomDO> getRoomsByArea(Long areaId);
    
    /**
     * 搜索房屋
     * @param keyword 搜索关键词
     * @return 房屋列表
     */
    List<RoomDO> searchRooms(String keyword);
}