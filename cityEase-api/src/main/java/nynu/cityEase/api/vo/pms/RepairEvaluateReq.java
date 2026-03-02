package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("业主评价并结单请求")
public class RepairEvaluateReq {

    @ApiModelProperty(value = "报修工单的主键ID", required = true)
    private Long orderId;

    @ApiModelProperty(value = "评价星级(1-5)", required = true, example = "5")
    private Integer rating;

    @ApiModelProperty(value = "评价内容详细描述")
    private String evaluateContent;
}