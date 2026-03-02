package nynu.cityEase.api.vo.gov;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PointRedeemReq {
    @ApiModelProperty(value = "要兑换的商品ID", required = true)
    private Long goodsId;

    @ApiModelProperty(value = "使用哪个房屋的积分 (用户必须是该房屋的认证人员)", required = true)
    private Long roomId;
}