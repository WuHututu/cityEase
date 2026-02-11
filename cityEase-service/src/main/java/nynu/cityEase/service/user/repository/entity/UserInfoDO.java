package nynu.cityEase.service.user.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import nynu.cityEase.api.entity.BaseDO;

/**
 * 用户资料表
 *
 * @author 90924
 * @TableName sys_user_info
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_info", autoResultMap = true)
@Data
@Accessors(chain = true)
public class UserInfoDO extends BaseDO implements Serializable {
    /**
     * 关联sys_user.id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 昵称
     */
    @TableField(value = "username")
    private String username;

    /**
     * 头像URL
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 真实姓名
     */
    @TableField(value = "real_name")
    private String realName;

    /**
     * 性别: 0-保密, 1-男, 2-女
     */
    @TableField(value = "gender")
    private Integer gender;

    /**
     * 出生日期
     */
    @TableField(value = "birthday")
    private LocalDateTime birthday;


    /**
     * 0 普通用户 1 超管
     */
    @TableField(value = "user_role")
    private Integer userRole;

    /**
     * 用户的ip信息
     */
    @TableField(value = "ip",typeHandler = JacksonTypeHandler.class)
    private IpInfo ip;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public IpInfo getIp() {
        if (ip == null) {
            ip = new IpInfo();
        }
        return ip;
    }
}