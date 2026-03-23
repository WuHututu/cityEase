package nynu.cityEase.web.admin.pms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.pms.AuditUserRoomReq;
import nynu.cityEase.api.vo.pms.UpdateUserRoomAttachmentsReq;
import nynu.cityEase.api.vo.pms.UserRoomAuditDetailVO;
import nynu.cityEase.api.vo.pms.UserRoomQueryReq;
import nynu.cityEase.api.vo.pms.UserRoomVO;
import nynu.cityEase.service.pms.service.IPmsUserRoomRelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/pms/userRoom")
@Api(tags = "【后台】人房认证管理")
public class AdminUserRoomRelController {

    @Autowired
    private IPmsUserRoomRelService userRoomRelService;

    @PostMapping("/audit")
    @ApiOperation("审核用户的绑定申请")
    public ResVo<String> audit(@RequestBody AuditUserRoomReq req) {
        userRoomRelService.auditBindRequest(req);
        return ResVo.ok("审核操作成功");
    }

    @PostMapping("/page")
    @ApiOperation("分页获取人房认证申请列表")
    public ResVo<Page<UserRoomVO>> getAuditPage(@RequestBody UserRoomQueryReq req) {
        return ResVo.ok(userRoomRelService.getAuditPage(req));
    }

    @GetMapping("/detail")
    @ApiOperation("获取绑定审核详情")
    public ResVo<UserRoomAuditDetailVO> getAuditDetail(@RequestParam("relId") Long relId) {
        return ResVo.ok(userRoomRelService.getAuditDetail(relId));
    }

    @PostMapping("/attachments")
    @ApiOperation("更新绑定审核证明材料")
    public ResVo<String> updateAuditAttachments(@RequestBody UpdateUserRoomAttachmentsReq req) {
        userRoomRelService.updateAuditAttachments(req);
        return ResVo.ok("证明材料已保存");
    }
}
