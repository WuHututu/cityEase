package nynu.cityEase.web.admin.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.system.AdminNoticeQueryReq;
import nynu.cityEase.api.vo.system.NoticeDetailVO;
import nynu.cityEase.api.vo.system.NoticeListVO;
import nynu.cityEase.api.vo.system.NoticeSaveReq;
import nynu.cityEase.service.system.notice.service.ISysNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/system/notice")
@Api(tags = "Admin notice management")
public class AdminNoticeController {

    @Autowired
    private ISysNoticeService noticeService;

    @PostMapping("/save")
    @ApiOperation("Create or update notice")
    public ResVo<Boolean> saveNotice(@RequestBody NoticeSaveReq req) {
        noticeService.saveOrUpdateNotice(req);
        return ResVo.ok(true);
    }

    @PostMapping("/page")
    @ApiOperation("Page notices")
    public ResVo<Page<NoticeListVO>> page(@RequestBody AdminNoticeQueryReq req) {
        return ResVo.ok(noticeService.page(req));
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("Get notice detail")
    public ResVo<NoticeDetailVO> detail(@PathVariable Long id) {
        return ResVo.ok(noticeService.getNoticeDetail(id));
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("Delete notice")
    public ResVo<Boolean> delete(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResVo.ok(true);
    }

    @PostMapping("/toggleTop/{id}")
    @ApiOperation("Toggle top status")
    public ResVo<Boolean> toggleTop(@PathVariable Long id) {
        noticeService.toggleTop(id);
        return ResVo.ok(true);
    }
}
