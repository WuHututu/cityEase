package nynu.cityEase.web.front.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.system.AppNoticeQueryReq;
import nynu.cityEase.api.vo.system.NoticeDetailVO;
import nynu.cityEase.api.vo.system.NoticeListVO;
import nynu.cityEase.service.system.notice.service.ISysNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/system/notice")
@Api(tags = "【App端】社区新闻公告浏览")
public class AppNoticeController {

    @Autowired
    private ISysNoticeService noticeService;

    @PostMapping("/page")
    @ApiOperation("分页获取已发布的公告列表")
    public ResVo<Page<NoticeListVO>> getPublishedNotices(@RequestBody AppNoticeQueryReq req) {
        return ResVo.ok(noticeService.getPublishedNotices(req));
    }

    @GetMapping("/detail")
    @ApiOperation("获取单个公告富文本详情")
    public ResVo<NoticeDetailVO> getNoticeDetail(@RequestParam("id") Long id) {
        return ResVo.ok(noticeService.getNoticeDetail(id));
    }
}