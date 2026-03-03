package nynu.cityEase.api.vo.pms;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FeeBillVO {

    @ApiModelProperty("账单ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("房屋ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long roomId;

    @ApiModelProperty("完整的房屋地址 (例如：1号楼-1单元-101)")
    private String roomAddress;

    @ApiModelProperty("计费月份")
    private String feeMonth;

    @ApiModelProperty("应收金额")
    private BigDecimal amount;

    @ApiModelProperty("状态: 0-待缴费, 1-已缴费")
    private Integer status;

    @ApiModelProperty("实际支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
}