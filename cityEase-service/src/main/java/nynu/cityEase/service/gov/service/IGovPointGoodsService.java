package nynu.cityEase.service.gov.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import nynu.cityEase.api.vo.gov.GoodsQueryReq;
import nynu.cityEase.api.vo.gov.GoodsVO;
import nynu.cityEase.api.vo.gov.PointRedeemRecordVO;
import nynu.cityEase.api.vo.gov.PointRedeemReq;
import nynu.cityEase.api.vo.gov.RedeemRecordQueryReq;
import nynu.cityEase.service.gov.repository.entity.GovPointGoodsDO;

import java.util.List;

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

    /**
     * 获取商品列表
     */
    List<GoodsVO> getGoodsList(GoodsQueryReq req);

    /**
     * 获取商品详情
     */
    GoodsVO getGoodsDetail(Long id);

    /**
     * 分页获取兑换记录
     */
    Page<PointRedeemRecordVO> getRedeemRecordPage(RedeemRecordQueryReq req);

    /**
     * 获取兑换记录详情
     */
    PointRedeemRecordVO getRedeemRecordDetail(Long id);
}