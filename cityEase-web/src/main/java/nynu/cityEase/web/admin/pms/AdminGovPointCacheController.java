package nynu.cityEase.web.admin.pms;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.service.gov.service.IGovPointRankingCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/gov/point/cache")
@Api(tags = "【后台】积分缓存管理")
public class AdminGovPointCacheController {

    @Autowired
    private IGovPointRankingCacheService cacheService;

    @PostMapping("/refresh")
    @ApiOperation("刷新积分排行榜缓存")
    public ResVo<String> refresh() {
        cacheService.refreshAllRankings();
        return ResVo.ok("缓存刷新成功");
    }

    @PostMapping("/clear")
    @ApiOperation("清空积分排行榜缓存")
    public ResVo<String> clear() {
        cacheService.clearAllRankings();
        return ResVo.ok("缓存清除成功");
    }

    @PostMapping("/status")
    @ApiOperation("查询积分缓存状态")
    public ResVo<String> status() {
        return ResVo.ok(cacheService.getCacheStatus());
    }
}
