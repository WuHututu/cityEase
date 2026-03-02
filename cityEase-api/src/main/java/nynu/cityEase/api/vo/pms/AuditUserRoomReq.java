package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AuditUserRoomReq {
    
    @ApiModelProperty(value = "申请记录的ID (pms_user_room_rel主键)", required = true)
    private Long relId;

    @ApiModelProperty(value = "审核结果: 1-通过, 2-驳回", required = true)
    private Integer status;

    @ApiModelProperty(value = "审核备注/驳回原因 (驳回时必填)")
    private String remark;
}