package nynu.cityEase.api.vo.system;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("图表通用数据模型")
public class ChartDataVO {

    @ApiModelProperty("X轴数据列表 (如：日期、类别名)")
    private List<String> xAxisData;

    @ApiModelProperty("Y轴系列数据")
    private List<SeriesData> series;

    @Data
    @Accessors(chain = true)
    public static class SeriesData {
        @ApiModelProperty("数据系列名称 (如：新增报修)")
        private String name;
        
        @ApiModelProperty("该系列对应的数值列表，与X轴对应")
        private List<Integer> data;
    }
}