package nynu.cityEase.web.admin.user;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.user.CurrentUserInfoVO;
import nynu.cityEase.service.user.repository.dao.UserDao;
import nynu.cityEase.service.user.repository.entity.UserInfoDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin/user")
@Api(tags = "【后台】管理员个人信息")
public class AdminUserController {

    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    // 注入你原本就写好的 UserDao
    @Autowired
    private UserDao userDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/info")
    @ApiOperation("获取当前登录管理员的信息")
    public ResVo<CurrentUserInfoVO> getCurrentUserInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        String userKey = "user:info:" + userId;

        // 1. 优先从 Redis 缓存中读取
        String cachedInfo = stringRedisTemplate.opsForValue().get(userKey);
        if (StrUtil.isNotBlank(cachedInfo)) {
            // 如果缓存存在，直接反序列化返回
            CurrentUserInfoVO vo = JSONUtil.toBean(cachedInfo, CurrentUserInfoVO.class);
            return ResVo.ok(vo);
        }

        // 2. 缓存中没有，查询数据库
        UserInfoDO userInfo = userDao.getByUserId(userId);

        CurrentUserInfoVO vo = new CurrentUserInfoVO();
        vo.setUserId(userId);

        // 防空指针兜底处理（注册 但还没生成详情表数据）
        if (userInfo != null) {
            vo.setUserName(userInfo.getUsername());

            // 头像真实读取逻辑
            if (userInfo.getAvatar() != null && !userInfo.getAvatar().trim().isEmpty()) {
                vo.setAvatar(userInfo.getAvatar());
            } else {
                vo.setAvatar("https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png");
            }
        } else {
            // 如果查不到资料，给个默认值
            vo.setUserName("系统管理员_" + userId);
            vo.setAvatar("https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png");
        }

        // 3. 将组装好的最新数据存入 Redis，并设置一个过期时间（比如 7 天），防止死缓存占用太多内存
        stringRedisTemplate.opsForValue().set(userKey, JSONUtil.toJsonStr(vo), 30, TimeUnit.DAYS);

        return ResVo.ok(vo);
    }

    @PostMapping("/updateAvatar")
    @ApiOperation("更新当前用户的头像")
    public ResVo<String> updateAvatar(
            @ApiParam("OSS返回的头像绝对路径URL") @RequestParam("avatarUrl") String avatarUrl) {

        long userId = StpUtil.getLoginIdAsLong();

        // 先查出原本的资料
        UserInfoDO userInfo = userDao.getByUserId(userId);
        if (userInfo == null) {
            // 如果连资料都没有，就新建一个关联
            userInfo = new UserInfoDO();
            userInfo.setUserId(userId);
            userInfo.setUsername("用户_" + userId);
        }

        // 设置最新的头像
        userInfo.setAvatar(avatarUrl);

        // 调用你写好的 saveUserInfo 进行插入或更新
        userDao.saveUserInfo(userInfo);
        log.info("用户 [{}] 成功更新了头像: {}", userId, avatarUrl);

        // ================== 核心新增 ==================
        // 数据更新后，必须删除 Redis 中的缓存，这样下次调用 /info 接口时才会重新查库并存入最新的头像
        String userKey = "user:info:" + userId;
        stringRedisTemplate.delete(userKey);
        // ==============================================

        return ResVo.ok("头像更新成功");
    }
}