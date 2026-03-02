package nynu.cityEase.service.pms.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import nynu.cityEase.api.entity.BaseDO;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pms_repair_order")
public class RepairOrderDO extends BaseDO {
    private Long userId;
    private Long roomId;
    private String repairType;
    private String description;
    // 报修图片 JSON
    private String images;
    // 0-待派单, 1-处理中, 2-已处理, 3-已结单, 4-已取消
    private Integer status;
    
    private Long handlerId;
    private LocalDateTime handleTime;
    private String handleResult;
    // 维修结果图片 JSON
    private String handleImages;

    //评价星级
    private Integer rating;
    //评价内容
    private String evaluateContent;
}