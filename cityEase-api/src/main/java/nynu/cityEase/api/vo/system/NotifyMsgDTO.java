package nynu.cityEase.api.vo.system;

import lombok.Data;

import java.io.Serializable;

@Data
public class NotifyMsgDTO implements Serializable {
    
    private Long receiveUserId;
    
    private String title;
    
    private String content;
    
    private String relatedBizId;
    
    private String notifyType;
}