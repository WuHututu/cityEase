package nynu.cityEase.api.vo.user;

import lombok.Data;

import java.util.Map;

/**
 * 用户信息入参
 *
 * @author louzai
 * @date 2022-07-24
 */
@Data
public class UserInfoSaveReq {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户图像
     */
    private String avatar;
}
