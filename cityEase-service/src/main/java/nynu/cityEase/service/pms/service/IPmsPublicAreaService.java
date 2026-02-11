package nynu.cityEase.service.pms.service;

import nynu.cityEase.api.vo.pms.PublicAreaReq;
import nynu.cityEase.api.vo.pms.PublicAreaTreeVO;

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
}