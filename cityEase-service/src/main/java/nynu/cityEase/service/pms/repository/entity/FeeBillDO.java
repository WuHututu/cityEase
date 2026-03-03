package nynu.cityEase.service.pms.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import nynu.cityEase.api.entity.BaseDO;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pms_fee_bill")
public class FeeBillDO extends BaseDO {
    
    private Long roomId;

    /**
     * 计费月份 (格式: 2026-05)
     */
    private String feeMonth;
    
    private BigDecimal amount;
    
    private Integer status;
    
    private Long payerId;
    
    private LocalDateTime payTime;
}