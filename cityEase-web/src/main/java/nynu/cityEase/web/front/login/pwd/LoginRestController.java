package nynu.cityEase.web.front.login.pwd;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nynu.cityEase.api.exception.CommunityException;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.user.PhonePwdLoginReq;
import nynu.cityEase.core.mdc.MdcDot;
import nynu.cityEase.service.user.ILoginService;
import nynu.cityEase.service.user.IRegisterService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/3
 * Time: 17:37
 * Description: TODO
 */
@RestController
@RequestMapping
public class LoginRestController {
    @Resource
    @Qualifier("LoginServiceImpl")
    ILoginService loginService;

    @Resource
    @Qualifier("RegisterServiceImpl")
    IRegisterService registerService;


    /**
     * 手机号-密码登录
     */
    @PostMapping(path = "/login")
    @MdcDot
    public ResVo<String> login(@RequestBody PhonePwdLoginReq req) {
        try {
            // 从实体类中获取 phone 和 password
            return ResVo.ok(loginService.loginByPhonePwd(req.getPhone(), req.getPassword()));
        } catch (CommunityException e) {
            return ResVo.fail(e.getStatus());
        }
    }

    /**
     * 注册
     */
    @PostMapping(path = "/register")
    public ResVo<Long> register(
            @ApiParam() PhonePwdLoginReq req
            ) {
        return ResVo.ok(registerService.registerByPhoneAndPassword(req));
    }

    /**
     * 退出登录 (核心安全修复)
     */
    @PostMapping(path = "/logout")
    @ApiOperation("退出登录")
    public ResVo<String> logout() {
        // StpUtil.logout() 会自动获取当前请求头里的 Token，并在 Redis 里将其销毁
        StpUtil.logout();
        return ResVo.ok("退出成功");
    }
}
