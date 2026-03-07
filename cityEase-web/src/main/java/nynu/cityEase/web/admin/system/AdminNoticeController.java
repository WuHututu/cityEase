package nynu.cityEase.web.admin.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.system.AdminNoticeDetailVO;
import nynu.cityEase.api.vo.system.AdminNoticeListVO;
import nynu.cityEase.api.vo.system.AdminNoticeQueryReq;
import nynu.cityEase.service.system.notice.repository.entity.SysNoticeDO;
import nynu.cityEase.service.system.notice.service.ISysNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/system/notice")
@Api(tags = "后台公告管理")
public class AdminNoticeController {

    @Autowired
    private ISysNoticeService noticeService;

    @PostMapping("/save")
    @ApiOperation("新增/修改公告")
    public ResVo<String> saveNotice(@RequestBody SysNoticeDO notice){
        noticeService.saveOrUpdateNotice(notice);
        return ResVo.ok("保存成功");
    }

    @PostMapping("/page")
    @ApiOperation("分页列表")
    public ResVo<Page<AdminNoticeListVO>> page(@RequestBody AdminNoticeQueryReq req){
        return ResVo.ok(noticeService.getNoticePage(req));
    }

    @GetMapping("/detail")
    @ApiOperation("详情")
    public ResVo<AdminNoticeDetailVO> detail(@RequestParam("id") Long id){
        return ResVo.ok(noticeService.getNoticeDetailForAdmin(id));
    }

    @PostMapping("/delete")
    @ApiOperation("删除公告")
    public ResVo<String> delete(@RequestBody java.util.Map<String,Object> body){
        Object idObj = body.get("id");
        if(idObj==null) return ResVo.fail(StatusEnum.valueOf("缺少公告ID"));
        noticeService.deleteNotice(Long.valueOf(String.valueOf(idObj)));
        return ResVo.ok("删除成功");
    }

    @PostMapping("/publish")
    @ApiOperation("发布公告")
    public ResVo<String> publish(@RequestBody java.util.Map<String,Object> body){
        Object idObj = body.get("id");
        if(idObj==null) return ResVo.fail(StatusEnum.valueOf("缺少公告ID"));
        noticeService.publishNotice(Long.valueOf(String.valueOf(idObj)));
        return ResVo.ok("发布成功");
    }

    @PostMapping("/withdraw")
    @ApiOperation("撤回公告")
    public ResVo<String> withdraw(@RequestBody java.util.Map<String,Object> body){
        Object idObj = body.get("id");
        if(idObj==null) return ResVo.fail(StatusEnum.valueOf("缺少公告ID"));
        noticeService.withdrawNotice(Long.valueOf(String.valueOf(idObj)));
        return ResVo.ok("已撤回");
    }
}
