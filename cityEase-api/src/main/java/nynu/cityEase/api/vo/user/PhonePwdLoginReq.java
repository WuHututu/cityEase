package nynu.cityEase.api.vo.user;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class PhonePwdLoginReq implements Serializable {
    private static final long serialVersionUID = -5941617870303218990L;

    private Long userId;

    /** 登录手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    /** 登录密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需在6到64位之间")
    private String password;

    /** 显示名称 */
    private String nickname;

    /** 用户头像 */
    private String avatar;
}
