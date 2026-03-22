package nynu.cityEase.web.front.login.pwd;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.user.PhonePwdLoginReq;
import nynu.cityEase.core.mdc.MdcDot;
import nynu.cityEase.service.user.ILoginService;
import nynu.cityEase.service.user.IRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping
@Validated
public class LoginRestController {
    @Resource
    @Qualifier("LoginServiceImpl")
    ILoginService loginService;

    @Resource
    @Qualifier("RegisterServiceImpl")
    IRegisterService registerService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping(path = "/login")
    @MdcDot
    public ResVo<String> login(@Valid @RequestBody PhonePwdLoginReq req) {
        return ResVo.ok(loginService.loginByPhonePwd(req.getPhone(), req.getPassword()));
    }

    @PostMapping(path = "/register")
    public ResVo<Long> register(@Valid @RequestBody PhonePwdLoginReq req) {
        return ResVo.ok(registerService.registerByPhoneAndPassword(req));
    }

    @PostMapping(path = "/logout")
    @ApiOperation("退出登录")
    public ResVo<String> logout() {
        long userId = StpUtil.getLoginIdAsLong();
        String userKey = "user:info:" + userId;
        stringRedisTemplate.delete(userKey);
        StpUtil.logout();
        return ResVo.ok("退出成功");
    }
}
