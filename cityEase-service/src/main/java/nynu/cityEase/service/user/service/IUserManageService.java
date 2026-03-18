package nynu.cityEase.service.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import nynu.cityEase.api.vo.user.UserPageReq;
import nynu.cityEase.api.vo.user.UserPageVO;
import nynu.cityEase.api.vo.user.UserSaveReq;

public interface IUserManageService {

    /**
     * 分页查询用户列表
     */
    IPage<UserPageVO> page(UserPageReq req);

    /**
     * 新增或更新用户
     */
    void saveOrUpdate(UserSaveReq req);

    /**
     * 禁用/启用用户
     */
    void disableUser(Long userId, Integer disable);

    /**
     * 重置用户密码
     */
    void resetPassword(Long userId);
}
