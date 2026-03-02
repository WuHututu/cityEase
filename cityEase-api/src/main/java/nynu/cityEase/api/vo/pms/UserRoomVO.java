package nynu.cityEase.api.vo.pms;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserRoomVO {
    @ApiModelProperty("申请记录ID")
    private Long relId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("申请人姓名/昵称")
    private String userName;

    @ApiModelProperty("房屋ID")
    private Long roomId;

    @ApiModelProperty("完整的房屋地址 (如: 江南星城-1号楼-1单元-101)")
    private String fullRoomName;

    @ApiModelProperty("身份: 1-业主, 2-家属, 3-租客")
    private Integer relationType;

    @ApiModelProperty("审核状态: 0-待审核, 1-通过, 2-驳回")
    private Integer status;

    @ApiModelProperty("证明材料URL列表")
    private List<String> attachmentsList;

    @ApiModelProperty("申请时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}