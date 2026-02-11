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
 * 报修工单表
 * </p>
 *
 * @author WuHututu
 * @since 2026年02月11日
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("pms_repair_order")
public class RepairOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 报修人ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 故障区域ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 设施类型
     */
    @TableField("facility_type_id")
    private Long facilityTypeId;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 图片
     */
    @TableField("images")
    private String images;

    /**
     * 状态: 0-待派, 1-处理...
     */
    @TableField("status")
    private Integer status;

    /**
     * 评分
     */
    @TableField("rating")
    private Integer rating;

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
