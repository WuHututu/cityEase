package nynu.cityEase.api.vo.pms;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserRoomAuditDetailVO {

    @ApiModelProperty("申请记录ID")
    private Long relId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("申请人")
    private String userName;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("房屋ID")
    private Long roomId;

    @ApiModelProperty("完整房屋地址")
    private String fullRoomName;

    @ApiModelProperty("绑定身份")
    private Integer relationType;

    @ApiModelProperty("审核状态")
    private Integer status;

    @ApiModelProperty("证明材料 URL 列表")
    private List<String> attachmentsList;

    @ApiModelProperty("审核快照ID")
    private Long auditId;

    @ApiModelProperty("审核人")
    private String auditorName;

    @ApiModelProperty("审核备注")
    private String remark;

    @ApiModelProperty("申请时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
}
