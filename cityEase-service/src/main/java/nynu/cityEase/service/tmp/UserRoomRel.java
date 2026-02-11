package nynu.cityEase.service.tmp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 人房关联表
 * </p>
 *
 * @author WuHututu
 * @since 2026年02月11日
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("pms_user_room_rel")
public class UserRoomRel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 房产ID
     */
    @TableField("room_id")
    private Long roomId;

    /**
     * 身份: 1-业主, 2-家属/租客
     */
    @TableField("role_type")
    private Integer roleType;

    /**
     * 审核: 0-待审, 1-通过
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableField("is_deleted")
    private Integer isDeleted;


}
