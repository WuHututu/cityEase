package nynu.cityEase.api.vo.pms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
@ApiModel("维修师傅处理完毕工单请求")
public class RepairCompleteReq {

    @ApiModelProperty(value = "报修工单的主键ID", required = true)
    private Long orderId;

    @ApiModelProperty(value = "维修结果/处理反馈描述", required = true)
    private String handleResult;

    @ApiModelProperty(value = "维修后结果图片URL列表")
    private List<String> handleImages;
}