package nynu.cityEase.api.vo.pms;

import lombok.Data;

@Data
public class RepairCancelReq {
    private Long orderId;
    /** 可选：取消原因 */
    private String reason;
}
