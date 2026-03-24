package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRoomAttachmentsReq {

    @ApiModelProperty(value = "绑定申请记录ID", required = true)
    private Long relId;

    @ApiModelProperty("证明材料 URL 列表")
    private List<String> attachments;
}
