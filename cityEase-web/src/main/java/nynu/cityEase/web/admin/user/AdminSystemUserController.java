package nynu.cityEase.web.admin.user;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.exception.CommunityException;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.user.UserPageReq;
import nynu.cityEase.api.vo.user.UserSaveReq;
import nynu.cityEase.service.user.service.IUserManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/system/user")
@Api(tags = "【后台】用户管理")
public class AdminSystemUserController {

    @Autowired
    private IUserManageService userManageService;

    @PostMapping("/page")
    @ApiOperation("分页查询用户列表")
    public ResVo<Object> page(@RequestBody UserPageReq req) {
        try {
            com.baomidou.mybatisplus.core.metadata.IPage<nynu.cityEase.api.vo.user.UserPageVO> page = userManageService.page(req);
            return ResVo.ok(page);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommunityException(500, "查询用户列表失败：" + e.getMessage());
        }
    }

    @PostMapping("/save")
    @ApiOperation("新增或更新用户")
    public ResVo<Object> saveOrUpdate(@RequestBody UserSaveReq req) {
        try {
            userManageService.saveOrUpdate(req);
            return ResVo.ok(req.getUserId() != null ? "更新成功" : "新增成功");
        } catch (CommunityException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommunityException(500, "操作失败：" + e.getMessage());
        }
    }

    @PostMapping("/disable")
    @ApiOperation("禁用/启用用户")
    public ResVo<Object> disable(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            // disable 参数可能是 Boolean 类型（true/false）或 Integer 类型（1/0）
            Object disableObj = params.get("disable");
            Integer disable;
            if (disableObj instanceof Boolean) {
                // 前端传的是 true/false，转换为 1/0
                disable = (Boolean) disableObj ? 1 : 0;
            } else if (disableObj instanceof Integer) {
                disable = (Integer) disableObj;
            } else {
                disable = Integer.valueOf(disableObj.toString());
            }
            userManageService.disableUser(userId, disable);
            return ResVo.ok(disable == 1 ? "禁用成功" : "启用成功");
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommunityException(500, "操作失败：" + e.getMessage());
        }
    }

    @PostMapping("/resetPwd")
    @ApiOperation("重置用户密码")
    public ResVo<Object> resetPwd(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            userManageService.resetPassword(userId);
            return ResVo.ok("密码已重置为：123456");
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommunityException(500, "重置密码失败：" + e.getMessage());
        }
    }
}
