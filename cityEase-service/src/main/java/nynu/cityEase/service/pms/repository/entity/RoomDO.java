package nynu.cityEase.service.pms.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import nynu.cityEase.api.entity.BaseDO;

/**
 * <p>
 * 房产档案表
 * </p>
 *
 * @author WuHututu
 * @since 2026年02月11日
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("pms_room")
public class RoomDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联pms_public_area.id
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 门牌号(如:2001)
     */
    @TableField("room_num")
    private String roomNum;

    /**
     * 治理积分余额
     */
    @TableField("points_balance")
    private Integer pointsBalance;


}
