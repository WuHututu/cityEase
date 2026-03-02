package nynu.cityEase.service.gov.service;

import com.baomidou.mybatisplus.extension.service.IService;
import nynu.cityEase.api.vo.gov.PointRedeemReq;
import nynu.cityEase.service.gov.repository.entity.GovPointGoodsDO;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/3/1
 * Time: 15:01
 * Description: TODO
 */

public interface IGovPointGoodsService extends IService<GovPointGoodsDO> {

    /**
     * 积分商城商品兑换
     *
     * @param req
     */
    void redeemGoods(PointRedeemReq req);
}