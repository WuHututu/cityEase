package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("业主支付物业费请求")
public class FeePayReq {

    @ApiModelProperty(value = "账单ID", required = true)
    private Long billId;
}