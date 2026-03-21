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
 * 积分规则执行记录实体类
 * Created: 2026/3/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("gov_point_rule_log")
public class GovPointRuleLogDO extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("规则ID")
    private Long ruleId;

    @ApiModelProperty("房屋ID")
    private Long roomId;

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

    @ApiModelProperty("失败原因")
    private String failureReason;
}
