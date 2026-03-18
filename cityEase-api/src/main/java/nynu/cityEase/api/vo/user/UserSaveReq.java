package nynu.cityEase.api.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户管理 - 新增/编辑请求")
public class UserSaveReq {

    @ApiModelProperty("用户 ID：编辑时必传")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("真实姓名")
    private String realName;

    @ApiModelProperty("头像 URL")
    private String avatar;

    @ApiModelProperty("性别：0-保密，1-男，2-女")
    private Integer gender;

    @ApiModelProperty("用户角色：0-普通用户，1-超管，2-物业，3-业主，4-维修工")
    private Integer userRole;

    @ApiModelProperty("密码：新增时必传")
    private String password;
}
