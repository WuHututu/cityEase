package nynu.cityEase.api.vo.pms;

import lombok.Data;

@Data
public class RepairDetailReq {
    /** 兼容前端不同字段 */
    private Long orderId;
    private Long id;
}
