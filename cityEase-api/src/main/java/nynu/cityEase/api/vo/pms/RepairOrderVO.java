package nynu.cityEase.api.vo.pms;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RepairOrderVO {
    
    // 使用 String 类型防止前端大数字精度丢失
    @ApiModelProperty("工单ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("报修人姓名/昵称")
    private String submitterName;

    @ApiModelProperty("完整的报修地址 (如: 江南星城-1号楼-1单元-101，公共区域可能为空)")
    private String fullAddress;

    @ApiModelProperty("报修类型")
    private String repairType;

    @ApiModelProperty("报修描述")
    private String description;

    @ApiModelProperty("报修现场图片列表")
    private List<String> imagesList;

    @ApiModelProperty("工单状态: 0-待派单, 1-处理中, 2-已处理, 3-已结单, 4-已取消")
    private Integer status;

    @ApiModelProperty("维修师傅姓名")
    private String handlerName;

    @ApiModelProperty("维修处理结果")
    private String handleResult;

    @ApiModelProperty("维修后结果图片列表")
    private List<String> handleImagesList;

    @ApiModelProperty("报修时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("维修完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;

    @ApiModelProperty("评价分数 (1-5 分)")
    private Integer rating;

    @ApiModelProperty("评价内容")
    private String evaluateContent;
}