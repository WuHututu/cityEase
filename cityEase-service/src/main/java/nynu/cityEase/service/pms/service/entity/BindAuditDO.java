package nynu.cityEase.service.pms.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import nynu.cityEase.api.entity.BaseDO;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("pms_bind_audit")
public class BindAuditDO extends BaseDO {

    @TableField("rel_id")
    private Long relId;

    @TableField("owner_id")
    private Long ownerId;

    @TableField("room_id")
    private Long roomId;

    @TableField("owner_name")
    private String ownerName;

    @TableField("phone")
    private String phone;

    @TableField("room_info")
    private String roomInfo;

    @TableField("attachments")
    private String attachments;

    @TableField("status")
    private Integer status;

    @TableField("apply_time")
    private LocalDateTime applyTime;

    @TableField("auditor_id")
    private Long auditorId;

    @TableField("auditor_name")
    private String auditorName;

    @TableField("audit_time")
    private LocalDateTime auditTime;

    @TableField("remark")
    private String remark;
}
