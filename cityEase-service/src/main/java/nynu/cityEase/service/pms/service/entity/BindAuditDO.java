package nynu.cityEase.service.pms.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("pms_bind_audit")
public class BindAuditDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long ownerId;
    private Long roomId;
    private String ownerName;
    private String phone;
    private String roomInfo;
    private Integer status; // 0-待审核,1-已通过,2-已拒绝
    private LocalDateTime applyTime;
    private Long auditorId;
    private String auditorName;
    private LocalDateTime auditTime;
    private String remark;
}
