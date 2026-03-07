package nynu.cityEase.service.pms.fee.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pms_fee_bill")
public class PmsFeeBillDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long roomId;

    private String feeMonth;

    private BigDecimal amount;

    /** 0-待缴费，1-已缴费 */
    private Integer status;

    private Long payerId;

    private LocalDateTime payTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;
}
