package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RepairOrderQueryReq {
    
    @ApiModelProperty("页码，默认1")
    private Integer pageNo = 1;

    @ApiModelProperty("每页条数，默认10")
    private Integer pageSize = 10;

    @ApiModelProperty("工单状态: 0-待派单, 1-处理中, 2-已处理, 3-已结单, 4-已取消")
    private Integer status;

    @ApiModelProperty("报修类型 (关联字典表)")
    private String repairType;
}