package nynu.cityEase.api.vo.user.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import nynu.cityEase.api.entity.BaseDTO;

import java.util.Date;

/**
 * @author YiHui
 * @date 2022/8/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("用户基础实体对象")
@Accessors(chain = true)
public class BaseUserInfoDTO extends BaseDTO {
    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id", required = true)
    private Long userId;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名", required = true)
    private String username;

    /**
     * 性别: 0-保密, 1-男, 2-女
     */
    @ApiModelProperty(value = "gender")
    private String gender;

    /**
     * 是否删除
     */
    @ApiModelProperty(hidden = true, value = "用户是否被删除")
    private Integer isDeleted;

    /**
     * 用户角色 1 admin, 0 normal
     */
    @ApiModelProperty(value = "角色", example = "1 admin, 0 normal")
    private String userRole;

    /**
     * 用户图像
     */
    @ApiModelProperty(value = "用户头像")
    private String avatar;

    /**
     * 用户最后登录区域
     */
    @ApiModelProperty(value = "用户最后登录的地理位置", example = "湖北·武汉")
    private String region;
}


















