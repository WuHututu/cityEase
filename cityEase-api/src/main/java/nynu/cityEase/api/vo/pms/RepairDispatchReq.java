package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("物业后台派发工单请求")
public class RepairDispatchReq {

    @ApiModelProperty(value = "报修工单的主键ID", required = true)
    private Long orderId;

    @ApiModelProperty(value = "指派的维修员工ID (sys_user表的ID)", required = true)
    private Long handlerId;
}