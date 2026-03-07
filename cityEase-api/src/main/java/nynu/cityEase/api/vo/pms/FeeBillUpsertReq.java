package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("后台物业费账单新增/编辑请求")
public class FeeBillUpsertReq {

    @ApiModelProperty("账单ID（新增不传，编辑必传）")
    private Long id;

    @ApiModelProperty("房屋ID")
    private Long roomId;

    @ApiModelProperty("计费月份，格式：yyyy-MM")
    private String feeMonth;

    @ApiModelProperty("应收金额")
    private BigDecimal amount;

    @ApiModelProperty("状态：0-待缴费，1-已缴费（可选）")
    private Integer status;
}
