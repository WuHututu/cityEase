package nynu.cityEase.web.global;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import nynu.cityEase.api.context.ReqInfoContext;
import nynu.cityEase.api.vo.user.dto.BaseUserInfoDTO;
import nynu.cityEase.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author YiHui
 * @date 2022/9/3
 */
@Slf4j
@Service
public class GlobalInitService {

    @Autowired
    private IUserService userService;

//    @Resource
//    private NotifyService notifyService;

    /**
     * 初始化用户信息
     *
     * @param reqInfo
     */
    public void initLoginUser(ReqInfoContext.ReqInfo reqInfo) {
        if (!StpUtil.isLogin()) {
            return;
        }
        try {
            // 2. 从 Token 中获取 UserId
            Long userId = StpUtil.getLoginIdAsLong();

            // 3. 查询用户信息 (直接用 ID 查，不再需要查 Session 表)
            // 这里建议你用 queryBasicUserInfo 或者 querySimpleUserInfo，看你需要多详细的信息
            BaseUserInfoDTO user = userService.queryBasicUserInfo(userId, reqInfo.getClientIp());

            if (user != null) {
                // 4. 填充上下文
                // session 字段现在存放 Token 值，方便日志追踪
                reqInfo.setToken(StpUtil.getTokenValue());
                reqInfo.setUserId(user.getUserId());
                reqInfo.setUser(user);
//                reqInfo.setMsgNum(notifyService.queryUserNotifyMsgCount(user.getUserId()));
            }
        } catch (Exception e) {
            log.error("初始化用户信息失败", e);
        }

    }
}
