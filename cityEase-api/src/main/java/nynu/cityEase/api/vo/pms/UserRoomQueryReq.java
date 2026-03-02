package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserRoomQueryReq {
    @ApiModelProperty("页码，默认1")
    private Integer pageNo = 1;

    @ApiModelProperty("每页条数，默认10")
    private Integer pageSize = 10;

    @ApiModelProperty("审核状态: 0-待审核, 1-通过, 2-驳回 (不传则查全部)")
    private Integer status;

    @ApiModelProperty("身份类型: 1-业主, 2-家属, 3-租客")
    private Integer relationType;
}