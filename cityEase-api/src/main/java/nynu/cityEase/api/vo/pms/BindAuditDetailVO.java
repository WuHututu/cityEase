package nynu.cityEase.api.vo.pms;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BindAuditDetailVO {
    private Long id;
    private String ownerName;
    private String phone;
    private String roomInfo;
    private Integer status;
    private LocalDateTime applyTime;
    private String auditorName;
    private LocalDateTime auditTime;
    private String remark;
}
