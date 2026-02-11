package nynu.cityEase.service.user.service.user;

import cn.dev33.satoken.stp.StpUtil;
import nynu.cityEase.api.vo.user.dto.BaseUserInfoDTO;
import nynu.cityEase.core.util.IpUtil;
import nynu.cityEase.service.user.IUserService;
import nynu.cityEase.service.user.converter.UserConverter;
import nynu.cityEase.service.user.repository.dao.UserDao;
import nynu.cityEase.service.user.repository.entity.IpInfo;
import nynu.cityEase.service.user.repository.entity.UserInfoDO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/10
 * Time: 16:19
 * Description: TODO
 */
@Service("UserServiceImpl")
public class UserServiceImpl implements IUserService {
    @Resource
    UserDao userDao;

    @Override
    public BaseUserInfoDTO queryBasicUserInfo(Long userId, String clientIp) {
        if (userId == null) {
            return null;
        }

        if (!StpUtil.isLogin()) {
            return null;
        }

        // 查询用户信息，并更新最后一次使用的ip
        UserInfoDO user = userDao.getByUserId(userId);
        if (user == null) {
            StpUtil.logout(userId);
            return null;
        }

        IpInfo ip = user.getIp();
        if (clientIp != null && !Objects.equals(ip.getLatestIp(), clientIp)) {
            // ip不同，需要更新
            ip.setLatestIp(clientIp);
            ip.setLatestRegion(IpUtil.getLocationByIp(clientIp).toRegionStr());

            if (ip.getFirstIp() == null) {
                ip.setFirstIp(clientIp);
                ip.setFirstRegion(ip.getLatestRegion());
            }
            userDao.updateById(user);
        }

        return UserConverter.toDTO(user);
    }
}
