package nynu.cityEase.service.user;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/7
 * Time: 13:25
 * Description: TODO
 * @author 90924
 */

public interface ILoginService{

    /**
     * 用户名密码方式登录
     *
     * @param username 用户名
     * @param password 密码
     */
    String loginByPhonePwd(String username, String password);

    /**
     * 登出
     *
     * @param session 用户会话
     */
    void logout(String session);
}