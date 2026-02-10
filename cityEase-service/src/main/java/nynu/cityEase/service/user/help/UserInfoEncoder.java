package nynu.cityEase.service.user.help;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 密码加密器，后续接入SpringSecurity之后，可以使用 PasswordEncoder 进行替换
 *
 * @author YiHui
 * @date 2022/12/5
 */
@Component
public class UserInfoEncoder {
    /**
     * 密码加盐，更推荐的做法是每个用户都使用独立的盐，提高安全性
     */
    @Value("${security.salt:city_ease}")
    private String salt;

    @Value("${security.salt-index:3}")
    private Integer saltIndex;

    public boolean match(String plainInfo, String encInfo) {
        return Objects.equals(encInfo(plainInfo), encInfo);
    }

    /**
     * 明文密码处理
     *
     * @param plainInfo
     * @return
     */
    public String encInfo(String plainInfo) {
        if (plainInfo.length() > saltIndex) {
            plainInfo = plainInfo.substring(0, saltIndex) + salt + plainInfo.substring(saltIndex);
        } else {
            plainInfo = plainInfo + salt;
        }
        return DigestUtils.md5DigestAsHex(plainInfo.getBytes(StandardCharsets.UTF_8));
    }

}
