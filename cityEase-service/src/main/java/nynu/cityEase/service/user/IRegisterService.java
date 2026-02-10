package nynu.cityEase.service.user;

import nynu.cityEase.api.vo.user.PhonePwdLoginReq;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/7
 * Time: 15:48
 * Description: TODO
 */

public interface IRegisterService{

    /**
     * 通过手机号/密码进行注册
     */
    Long registerByPhoneAndPassword(PhonePwdLoginReq loginReq);

}