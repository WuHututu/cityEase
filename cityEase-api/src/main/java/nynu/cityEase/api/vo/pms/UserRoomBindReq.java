package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
public class UserRoomBindReq {
    @ApiModelProperty(value = "房屋ID", required = true)
    private Long roomId;

    @ApiModelProperty(value = "身份: 1-业主, 2-家属, 3-租客", required = true)
    private Integer relationType;

    @ApiModelProperty(value = "证明材料图片URL列表")
    private List<String> attachments;
}