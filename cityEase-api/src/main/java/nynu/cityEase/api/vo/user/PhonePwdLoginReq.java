package nynu.cityEase.api.vo.user;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/7
 * Time: 18:10
 * Description: TODO
 * @author 90924
 */
@Data
@Accessors(chain = true)
public class PhonePwdLoginReq implements Serializable {
    private static final long serialVersionUID = -5941617870303218990L;

    private Long userId;
    /**
     * 登录手机号
     */
    private String phone;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 显示名称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;
}
