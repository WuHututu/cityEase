package nynu.cityEase.service.user;

//import nynu.cityEase.api.vo.user.UserInfoVO;
import nynu.cityEase.api.vo.user.dto.BaseUserInfoDTO;
import nynu.cityEase.service.user.repository.entity.UserDO;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/10
 * Time: 16:19
 * Description: TODO
 */

public interface IUserService {

    BaseUserInfoDTO queryBasicUserInfo(Long userId, String clientIp);

    UserDO getById(Long userId);

    boolean updateById(UserDO userDO);

    /**
     * 获取用户信息
     */
//    UserInfoVO getUserInfo(Long userId);
}