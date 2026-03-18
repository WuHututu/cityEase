package nynu.cityEase.api.vo.gov;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 积分规则保存请求
 * Created: 2026/3/18
 */
@Data
public class PointRuleSaveReq {

    @ApiModelProperty(value = "规则ID，新增时为空", required = false)
    private Long id;

    @ApiModelProperty(value = "规则名称", required = true)
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @ApiModelProperty(value = "规则类型：1-报修完成评价，2-连续签到，3-社区活动参与，4-违规行为扣减，5-自定义事件", required = true)
    @NotNull(message = "规则类型不能为空")
    private Integer ruleType;

    @ApiModelProperty(value = "触发条件JSON配置", required = false)
    private String triggerCondition;

    @ApiModelProperty(value = "积分数量（正数为奖励，负数为扣减）", required = true)
    @NotNull(message = "积分数量不能为空")
    private Integer pointsAmount;

    @ApiModelProperty(value = "每日最大触发次数，NULL表示无限制", required = false)
    private Integer maxDailyTimes;

    @ApiModelProperty(value = "每月最大触发次数，NULL表示无限制", required = false)
    private Integer maxMonthlyTimes;

    @ApiModelProperty(value = "状态：1-启用，0-禁用", required = true)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "规则描述", required = false)
    private String description;

    @ApiModelProperty(value = "排序顺序", required = false)
    private Integer sortOrder;
}
