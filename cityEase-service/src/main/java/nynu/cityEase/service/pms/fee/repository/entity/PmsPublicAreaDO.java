package nynu.cityEase.service.pms.fee.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_public_area")
public class PmsPublicAreaDO {
    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
