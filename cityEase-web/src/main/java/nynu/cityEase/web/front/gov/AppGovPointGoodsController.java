package nynu.cityEase.web.front.gov;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.gov.PointRedeemReq;
import nynu.cityEase.service.gov.service.IGovPointGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/gov/goods")
@Api(tags = "【App端】社区积分商城")
public class AppGovPointGoodsController {

    @Autowired
    private IGovPointGoodsService govPointGoodsService;

    @PostMapping("/redeem")
    @ApiOperation("业主发起积分商品兑换")
    public ResVo<String> redeemGoods(@RequestBody PointRedeemReq req) {
        // 调用 Service 层的核心兑换逻辑 (扣积分 + 扣库存 + 生成订单)
        govPointGoodsService.redeemGoods(req);
        
        // 如果上面没抛异常，说明兑换成功
        return ResVo.ok("兑换成功，请凭记录前往物业中心核销领取！");
    }
}