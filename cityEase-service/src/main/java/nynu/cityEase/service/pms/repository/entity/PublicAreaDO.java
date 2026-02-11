package nynu.cityEase.service.pms.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import nynu.cityEase.api.entity.BaseDO;

/**
 * <p>
 * 公共区域表
 * </p>
 *
 * @author WuHututu
 * @since 2026年02月11日
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("pms_public_area")
public class PublicAreaDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 父级ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 区域名称
     */
    @TableField("name")
    private String name;

    /**
     * 层级: 1-小区, 2-楼栋...
     */
    @TableField("level")
    private Integer level;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

}
