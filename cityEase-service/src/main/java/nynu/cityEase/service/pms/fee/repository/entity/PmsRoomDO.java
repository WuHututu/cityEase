package nynu.cityEase.service.pms.fee.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_room")
public class PmsRoomDO {
    private Long id;
    private Long areaId;
    private String roomNum;
    private Integer pointsBalance;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
