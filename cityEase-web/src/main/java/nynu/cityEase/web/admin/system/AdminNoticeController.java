package nynu.cityEase.web.admin.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.system.AdminNoticeQueryReq;
import nynu.cityEase.api.vo.system.NoticeDetailVO;
import nynu.cityEase.api.vo.system.NoticeListVO;
import nynu.cityEase.api.vo.system.NoticeSaveReq;
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
    public ResVo<Boolean> saveNotice(@RequestBody NoticeSaveReq req){
        noticeService.saveOrUpdateNotice(req);
        return ResVo.ok(true);
    }
    
    @PostMapping("/page")
    @ApiOperation("分页列表（支持类型、状态、标题筛选）")
    public ResVo<Page<NoticeListVO>> page(@RequestBody AdminNoticeQueryReq req){
        return ResVo.ok(noticeService.page(req));
    }
    
    @GetMapping("/detail/{id}")
    @ApiOperation("详情")
    public ResVo<NoticeDetailVO> detail(@PathVariable Long id){
        return ResVo.ok(noticeService.getNoticeDetail(id));
    }
    
    @PostMapping("/delete/{id}")
    @ApiOperation("删除公告")
    public ResVo<Boolean> delete(@PathVariable Long id){
        System.out.println("12323213123");
        noticeService.deleteNotice(id);
        return ResVo.ok(true);
    }
    
    @PostMapping("/toggleTop/{id}")
    @ApiOperation("切换置顶状态")
    public ResVo<Boolean> toggleTop(@PathVariable Long id){
        noticeService.toggleTop(id);
        return ResVo.ok(true);
    }
}
