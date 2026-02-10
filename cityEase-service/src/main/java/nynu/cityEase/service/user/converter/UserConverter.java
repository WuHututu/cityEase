package nynu.cityEase.service.user.converter;

import nynu.cityEase.api.enums.GenderEnum;
import nynu.cityEase.api.enums.RoleEnum;
import nynu.cityEase.api.vo.user.UserInfoSaveReq;
import nynu.cityEase.api.vo.user.UserSaveReq;
import nynu.cityEase.api.vo.user.dto.BaseUserInfoDTO;
import nynu.cityEase.service.user.repository.entity.UserDO;
import nynu.cityEase.service.user.repository.entity.UserInfoDO;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

/**
 * 用户转换
 *
 * @author louzai
 * @date 2022-07-20
 */
public class UserConverter {

    public static UserDO toDO(UserSaveReq req) {
        if (req == null) {
            return null;
        }
        UserDO userDO = new UserDO();
        userDO.setId(req.getUserId());
        userDO.setThirdAccountId(req.getThirdAccountId());
        userDO.setLoginType(req.getLoginType());
        return userDO;
    }

    public static UserInfoDO toDO(UserInfoSaveReq req) {
        if (req == null) {
            return null;
        }
        UserInfoDO userInfoDO = new UserInfoDO();
        userInfoDO.setUserId(req.getUserId());
        userInfoDO.setUsername(req.getUserName());
        return userInfoDO;
    }

    public static BaseUserInfoDTO toDTO(UserInfoDO info) {
        if (info == null) {
            return null;
        }
        BaseUserInfoDTO user = new BaseUserInfoDTO();
        BeanUtils.copyProperties(info, user);
        // 设置用户最新登录地理位置
        user.setRegion(info.getIp().getLatestRegion());
        // 设置用户角色
        user.setUserRole(RoleEnum.role(info.getUserRole()));
        user.setGender(GenderEnum.sex(info.getGender()));
        return user;
    }
}
