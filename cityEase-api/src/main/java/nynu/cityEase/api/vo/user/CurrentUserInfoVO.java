package nynu.cityEase.api.vo.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CurrentUserInfoVO {
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("用户姓名/昵称")
    private String userName;
    
    @ApiModelProperty("用户头像URL")
    private String avatar;
}