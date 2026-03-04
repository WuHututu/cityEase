package nynu.cityEase.web.admin.user;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.user.CurrentUserInfoVO;
import nynu.cityEase.service.user.IUserService;
import nynu.cityEase.service.user.repository.entity.UserDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user")
@Api(tags = "【后台】管理员个人信息")
public class AdminUserController {

    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    @Autowired
    private IUserService userService;

    @GetMapping("/info")
    @ApiOperation("获取当前登录管理员的信息")
    public ResVo<CurrentUserInfoVO> getCurrentUserInfo() {
        // 1. 从 Sa-Token 线程上下文中安全获取当前登录人的 ID
        long userId = StpUtil.getLoginIdAsLong();
        
        // 2. 真实查库获取用户数据
        UserDO userDO = userService.getById(userId);

        CurrentUserInfoVO vo = new CurrentUserInfoVO();
        vo.setUserId(userId);
        
        // 注意：这里的 getUsername() 请根据你 UserDO 实际生成的 getter 方法名调整（可能是 getUserName()）
        vo.setUserName(userDO.getUsername()); 
        
        // 3. 头像真实读取逻辑
        // 如果数据库里有真实上传的 OSS 头像，直接使用；否则给一个 Element Plus 的默认兜底图
        if (userDO.getAvatar() != null && !userDO.getAvatar().trim().isEmpty()) {
            vo.setAvatar(userDO.getAvatar());
        } else {
            // 默认的静态占位头像
            vo.setAvatar("https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png");
        }

        return ResVo.ok(vo);
    }

    @PostMapping("/updateAvatar")
    @ApiOperation("更新当前用户的头像")
    public ResVo<String> updateAvatar(
            @ApiParam("OSS返回的头像绝对路径URL") @RequestParam("avatarUrl") String avatarUrl) {
            
        long userId = StpUtil.getLoginIdAsLong();
        
        // 使用 MyBatis-Plus 构造实体类进行定向更新（只更新 avatar 字段，不影响其他字段）
        UserDO updateDO = new UserDO();
        updateDO.setId(userId);
        updateDO.setAvatar(avatarUrl);
        
        boolean success = userService.updateById(updateDO);
        
        if (success) {
            log.info("用户 [{}] 成功更新了头像: {}", userId, avatarUrl);
            return ResVo.ok("头像更新成功");
        } else {
            return ResVo.fail(500, "系统异常，头像更新失败");
        }
    }
}