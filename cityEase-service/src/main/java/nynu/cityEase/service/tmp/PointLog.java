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
 * 积分流水表
 * </p>
 *
 * @author WuHututu
 * @since 2026年02月11日
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("gov_point_log")
public class PointLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 房产ID
     */
    @TableField("room_id")
    private Long roomId;

    /**
     * 操作人ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 类型: 1-加, 2-减
     */
    @TableField("change_type")
    private Integer changeType;

    /**
     * 分值
     */
    @TableField("point_value")
    private Integer pointValue;

    /**
     * 摘要
     */
    @TableField("description")
    private String description;

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
