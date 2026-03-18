package nynu.cityEase.api.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户管理 - 分页请求")
public class UserPageReq {

    @ApiModelProperty("当前页")
    private Integer current = 1;

    @ApiModelProperty("每页大小")
    private Integer size = 10;

    @ApiModelProperty("用户名/手机号搜索")
    private String keyword;

    @ApiModelProperty("用户角色：0-普通用户，1-超管")
    private Integer userRole;

    @ApiModelProperty("是否禁用：0-否，1-是")
    private Integer isDisabled;
}
