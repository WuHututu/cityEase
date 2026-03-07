package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("后台物业费账单分页查询请求")
public class FeeBillPageReq {

    @ApiModelProperty("页码")
    private Integer pageNo = 1;

    @ApiModelProperty("每页条数")
    private Integer pageSize = 10;

    @ApiModelProperty("计费月份，格式：yyyy-MM（如：2026-03）")
    private String feeMonth;

    @ApiModelProperty("缴费状态：0-待缴费，1-已缴费")
    private Integer status;

    @ApiModelProperty("房屋关键字：房号模糊匹配")
    private String roomKeyword;
}
