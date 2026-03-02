package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
@ApiModel("业主提交报修请求")
public class RepairSubmitReq {
    
    @ApiModelProperty(value = "关联房屋ID (室内报修传房间ID，公共区域可不传)")
    private Long roomId;

    @ApiModelProperty(value = "报修类型 (对应字典值，如: '水电维修')", required = true)
    private String repairType;

    @ApiModelProperty(value = "报修详细描述", required = true)
    private String description;

    @ApiModelProperty(value = "现场图片URL列表")
    private List<String> images;
}