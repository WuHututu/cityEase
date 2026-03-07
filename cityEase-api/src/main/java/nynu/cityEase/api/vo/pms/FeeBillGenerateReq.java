package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("后台批量生成物业费账单请求")
public class FeeBillGenerateReq {

    @ApiModelProperty("计费月份，格式：yyyy-MM（如：2026-03）")
    private String feeMonth;

    @ApiModelProperty("每套房的应收金额")
    private BigDecimal amount;
}
