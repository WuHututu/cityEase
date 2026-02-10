package nynu.cityEase.web.front.login.pwd;

import io.swagger.annotations.ApiParam;
import nynu.cityEase.api.exception.CommunityException;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.user.PhonePwdLoginReq;
import nynu.cityEase.core.mdc.MdcDot;
import nynu.cityEase.service.user.ILoginService;
import nynu.cityEase.service.user.IRegisterService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResVo<String> login(
            @ApiParam("手机号") @RequestParam(name = "phone") String phone,
            @ApiParam("密码") @RequestParam(name = "password") String password
    ) {
        try{
            return ResVo.ok(loginService.loginByPhonePwd(phone,password));
        }catch (CommunityException e){
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

}
