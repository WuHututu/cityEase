package nynu.cityEase.api.vo.pms;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FeeBillPageVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long roomId;

    private String feeMonth;

    private BigDecimal amount;

    /** 0-待缴费，1-已缴费 */
    private Integer status;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long payerId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 额外展示字段 */
    private String roomNum;
    private String fullAddress;
}
