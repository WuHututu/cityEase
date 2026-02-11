package nynu.cityEase.service.user.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import nynu.cityEase.api.entity.BaseDO;

/**
 * 用户鉴权表
 * @TableName sys_user
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="sys_user")
@Data
@Accessors(chain = true)
public class UserDO extends BaseDO implements Serializable {
    /**
     * 手机号(登录账号)
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 密码(加密后)
     */
    @TableField(value = "password")
    private String password;

    /**
     * 状态: 0-正常, 1-冻结
     */
    @TableField(value = "status")
    private Integer status;


    /**
     * 登录方式: 0-微信登录，1-手机号密码登录
     */
    @TableField(value = "login_type")
    private Integer loginType;

    /**
     * 第三方用户ID
     */
    @TableField(value = "third_account_id")
    private String thirdAccountId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}