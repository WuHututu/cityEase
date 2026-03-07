package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("后台标记缴费/取消缴费请求")
public class FeeBillMarkPaidReq {

    @ApiModelProperty("账单ID")
    private Long id;

    @ApiModelProperty("支付人ID（可选，后台手工标记时可不传）")
    private Long payerId;
}
