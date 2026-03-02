package nynu.cityEase.service.system.notice.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import nynu.cityEase.api.entity.BaseDO;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_notice")
public class SysNoticeDO extends BaseDO {
    
    private String noticeTitle;
    
    private Integer noticeType;
    
    private String noticeContent;
    
    private Integer status;
    
    private Long creatorId;
}