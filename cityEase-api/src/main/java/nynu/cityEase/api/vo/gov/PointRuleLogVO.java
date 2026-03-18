package nynu.cityEase.api.vo.gov;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分规则执行记录VO
 * Created: 2026/3/18
 */
@Data
public class PointRuleLogVO {

    @ApiModelProperty("记录ID")
    private Long id;

    @ApiModelProperty("规则ID")
    private Long ruleId;

    @ApiModelProperty("规则名称")
    private String ruleName;

    @ApiModelProperty("房屋ID")
    private Long roomId;

    @ApiModelProperty("房屋编号")
    private String roomNum;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("触发事件")
    private String triggerEvent;

    @ApiModelProperty("触发事件数据JSON")
    private String triggerData;

    @ApiModelProperty("实际发放积分数量")
    private Integer pointsAwarded;

    @ApiModelProperty("执行结果：1-成功，0-失败")
    private Integer executionResult;

    @ApiModelProperty("执行结果名称")
    private String executionResultName;

    @ApiModelProperty("失败原因")
    private String failureReason;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
