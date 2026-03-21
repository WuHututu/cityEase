package nynu.cityEase.web.admin.pms;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.service.gov.service.IGovPointRankingCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/admin/gov/cache", "/admin/gov/point/cache"})
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

    @GetMapping("/status")
    @ApiOperation("查询积分缓存状态")
    public ResVo<Map<String, Object>> status() {
        String status = cacheService.getCacheStatus();
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("healthy", status.contains("roomRanking=true") && status.contains("buildingStats=true"));
        return ResVo.ok(result);
    }
}
