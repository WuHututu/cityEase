package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("批量生成房间请求")
public class BatchCreateRoomReq {

    @ApiModelProperty(value = "所属单元的ID (pms_public_area表的主键)", required = true)
    private Long areaId;

    @ApiModelProperty(value = "总楼层数", required = true, example = "10")
    private Integer totalFloors;

    @ApiModelProperty(value = "每层户数", required = true, example = "4")
    private Integer roomsPerFloor;
}