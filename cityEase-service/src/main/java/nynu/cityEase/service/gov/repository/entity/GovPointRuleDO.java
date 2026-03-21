package nynu.cityEase.service.gov.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nynu.cityEase.api.entity.BaseDO;

import java.time.LocalDateTime;

/**
 * 积分规则实体类
 * Created: 2026/3/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("gov_point_rule")
public class GovPointRuleDO extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("规则名称")
    private String ruleName;

    @ApiModelProperty("规则类型：1-报修完成评价，2-连续签到，3-社区活动参与，4-违规行为扣减，5-自定义事件")
    private Integer ruleType;

    @ApiModelProperty("触发条件JSON配置")
    private String triggerCondition;

    @ApiModelProperty("积分数量（正数为奖励，负数为扣减）")
    private Integer pointsAmount;

    @ApiModelProperty("每日最大触发次数，NULL表示无限制")
    private Integer maxDailyTimes;

    @ApiModelProperty("每月最大触发次数，NULL表示无限制")
    private Integer maxMonthlyTimes;

    @ApiModelProperty("状态：1-启用，0-禁用")
    private Integer status;

    @ApiModelProperty("规则描述")
    private String description;

    @ApiModelProperty("排序顺序")
    private Integer sortOrder;
}
