package nynu.cityEase.api.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("用户管理 - 用户分页 VO")
public class UserPageVO {

    @ApiModelProperty("用户 ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("真实姓名")
    private String realName;

    @ApiModelProperty("头像 URL")
    private String avatar;

    @ApiModelProperty("性别：0-保密，1-男，2-女")
    private Integer gender;

    @ApiModelProperty("用户角色：0-普通用户，1-超管")
    private Integer userRole;

    @ApiModelProperty("注册时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("是否禁用：0-否，1-是")
    private Integer isDisabled;

    @ApiModelProperty("手机号")
    private String phone;
}
