package nynu.cityEase.service.user;

import nynu.cityEase.api.vo.user.dto.BaseUserInfoDTO;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/10
 * Time: 16:19
 * Description: TODO
 */

public interface IUserService {

    BaseUserInfoDTO queryBasicUserInfo(Long userId, String clientIp);
}