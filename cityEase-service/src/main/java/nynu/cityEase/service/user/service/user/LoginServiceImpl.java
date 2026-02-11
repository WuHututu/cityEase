package nynu.cityEase.service.user.service.user;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.service.user.ILoginService;
import nynu.cityEase.service.user.repository.dao.UserDao;
import nynu.cityEase.service.user.repository.entity.UserDO;
import nynu.cityEase.service.user.service.user.help.UserInfoEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/7
 * Time: 13:28
 * Description: TODO
 *
 * @author 90924
 */
@Service("LoginServiceImpl")
public class LoginServiceImpl implements ILoginService {
    @Resource
    private UserDao userDao;
    @Autowired
    private UserInfoEncoder userInfoEncoder;

    @Override
    public String loginByPhonePwd(String phone, String password) {
        if (StrUtil.isBlank(phone)) {
            throw ExceptionUtil.of(StatusEnum.LOGIN_PHONE_BLANK);
        }
        if (StrUtil.isBlank(password)) {
            throw ExceptionUtil.of(StatusEnum.LOGIN_PWD_BLANK);
        }
        UserDO user = userDao.getUserByPhone(phone);
        if (user == null) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, phone);
        }
        if (!userInfoEncoder.match(password, user.getPassword())){
            throw ExceptionUtil.of(StatusEnum.PHONE_PWD_ERROR);
        }
        StpUtil.login(user.getId());
        return StpUtil.getTokenValue();
    }

    @Override
    public void logout(String session) {

    }
}
