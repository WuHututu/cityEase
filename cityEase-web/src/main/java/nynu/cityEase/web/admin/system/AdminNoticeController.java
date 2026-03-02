package nynu.cityEase.web.admin.system;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.system.NoticeSaveReq;
import nynu.cityEase.service.system.notice.service.ISysNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/system/notice")
@Api(tags = "【后台】系统公告管理")
public class AdminNoticeController {

    @Autowired
    private ISysNoticeService noticeService;

    @PostMapping("/save")
    @ApiOperation("新增或修改公告(可存草稿或直接发布)")
    public ResVo<String> saveNotice(@RequestBody NoticeSaveReq req) {
        noticeService.saveOrUpdateNotice(req);
        
        String actionMsg = req.getIsPublish() ? "发布成功" : "草稿保存成功";
        return ResVo.ok(actionMsg);
    }
}