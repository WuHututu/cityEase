package nynu.cityEase.service.system.dict.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import nynu.cityEase.api.entity.BaseDO;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_user_message")
public class SysUserMessageDO extends BaseDO {

    private Long receiveUserId;
    private String title;
    private String content;
    private String relatedBizId;
    private String notifyType;
    private Integer readStatus;
}