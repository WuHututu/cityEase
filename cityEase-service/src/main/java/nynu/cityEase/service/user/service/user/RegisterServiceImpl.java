package nynu.cityEase.service.user.service.user;

import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.user.PhonePwdLoginReq;
import nynu.cityEase.service.user.IRegisterService;
import nynu.cityEase.service.user.repository.dao.UserDao;
import nynu.cityEase.service.user.repository.entity.UserDO;
import nynu.cityEase.service.user.repository.entity.UserInfoDO;
import nynu.cityEase.service.user.service.user.help.UserInfoEncoder;
import nynu.cityEase.service.user.service.user.help.UserRandomGenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/7
 * Time: 15:48
 * Description: TODO
 */
@Service("RegisterServiceImpl")
public class RegisterServiceImpl implements IRegisterService {
    @Autowired
    UserDao userDao;
    @Autowired
    UserInfoEncoder encoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerByPhoneAndPassword(PhonePwdLoginReq loginReq) {
        // 验证请求对象不为null
        if (loginReq == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS, "登录请求对象不能为null");
        }
        
        String phone = loginReq.getPhone();
        String password = loginReq.getPassword();
        
        // 验证手机号和密码不为null
        if (phone == null || phone.trim().isEmpty()) {
            throw ExceptionUtil.of(StatusEnum.LOGIN_PHONE_BLANK);
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw ExceptionUtil.of(StatusEnum.LOGIN_PWD_BLANK);
        }

        UserDO user = userDao.getUserByPhone(phone);
        if (user != null) {
            throw ExceptionUtil.of(StatusEnum.USER_EXISTS, phone);
        }

        user = new UserDO();
        user.setPhone(phone).setPassword(encoder.encInfo(password));
        userDao.saveUser(user);

        UserInfoDO userInfoDO = new UserInfoDO();
        // todo 更多用户信息
        userInfoDO.setUserId(user.getId()).setUsername(UserRandomGenHelper.genUsername()).setAvatar(UserRandomGenHelper.genAvatar());
        userDao.saveUserInfo(userInfoDO);
        // todo 注册后的消息通知
        return user.getId();
    }
}
