package nynu.cityEase.service.pms.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import nynu.cityEase.api.entity.BaseDO;

/**
 * 人房绑定关系表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pms_user_room_rel")
public class UserRoomRelDO extends BaseDO {

    /**
     * 用户ID (关联 sys_user_info.user_id)
     */
    @TableField("user_id")
    private Long userId;
    /**
     * 房屋ID (关联 pms_room.id)
     */
    @TableField("room_id")
    private Long roomId;
    /**
     * 身份: 1-业主, 2-家属, 3-租客
     */
    @TableField("relation_type")
    private Integer relationType;
    /**
     * 认证状态: 0-待审核, 1-审核通过, 2-审核驳回
     */
    @TableField("status")
    private Integer status;
    /**
     * 证明材料 JSON
     */
    @TableField("attachments")
    private String attachments;
    /**
     * 物业审核驳回原因
     */
    @TableField("remark")
    private String remark;
    /**
     * 是否为当前房屋: 0-否, 1-是
     */
    @TableField("is_current")
    private Integer isCurrent;
}