package nynu.cityEase.service.gov.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import nynu.cityEase.api.entity.BaseDO;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("gov_point_log")
public class GovPointLogDO extends BaseDO {
    private Long roomId;
    private Long userId;
    // 1-增加, 2-扣减
    private Integer changeType;
    // 变动额度
    private Integer changeAmount;
    // 变动后余额
    private Integer afterBalance;
    // 变动原因
    private String reason;
}