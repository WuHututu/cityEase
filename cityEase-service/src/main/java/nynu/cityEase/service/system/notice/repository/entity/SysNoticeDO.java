package nynu.cityEase.service.system.notice.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
    
    /** 是否置顶：0-否，1-是 */
    private Integer isTop;
    
    private Long creatorId;
    
    /** 封面图 URL */
    private String coverImage;
}