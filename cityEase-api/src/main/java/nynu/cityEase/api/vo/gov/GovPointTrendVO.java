package nynu.cityEase.api.vo.gov;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("积分趋势图数据")
@Data
public class GovPointTrendVO {

    @ApiModelProperty("X轴日期")
    private List<String> dates;

    @ApiModelProperty("每日积分发放")
    private List<Integer> grantSeries;

    @ApiModelProperty("每日积分消耗")
    private List<Integer> consumeSeries;

    @ApiModelProperty("每日净变动（发放-消耗）")
    private List<Integer> netSeries;
}
