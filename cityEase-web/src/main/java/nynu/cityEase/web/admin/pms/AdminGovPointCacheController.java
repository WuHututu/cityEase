package nynu.cityEase.web.admin.pms;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.service.gov.service.IGovPointRankingCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 积分缓存管理Controller
 * Created: 2026/3/18
 */
@RestController
@RequestMapping("/admin/gov/cache")
@Api(tags = "【后台】积分缓存管理")
public class AdminGovPointCacheController {

    @Autowired
    private IGovPointRankingCacheService cacheService;

    @PostMapping("/refresh/all")
    @ApiOperation("刷新所有积分缓存")
    public ResVo<String> refreshAllRankings() {
        try {
            cacheService.refreshAllRankings();
            return ResVo.ok("缓存刷新成功");
        } catch (Exception e) {
            return ResVo.fail(StatusEnum.valueOf("缓存刷新失败: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh/room")
    @ApiOperation("刷新房屋排行榜缓存")
    public ResVo<String> refreshRoomRanking() {
        try {
            cacheService.refreshRoomRanking();
            return ResVo.ok("房屋排行榜缓存刷新成功");
        } catch (Exception e) {
            return ResVo.fail(StatusEnum.valueOf("房屋排行榜缓存刷新失败: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh/building")
    @ApiOperation("刷新楼栋统计缓存")
    public ResVo<String> refreshBuildingStats() {
        try {
            cacheService.refreshBuildingStats();
            return ResVo.ok("楼栋统计缓存刷新成功");
        } catch (Exception e) {
            return ResVo.fail(StatusEnum.valueOf("楼栋统计缓存刷新失败: " + e.getMessage()));
        }
    }

    @PostMapping("/remove/room")
    @ApiOperation("清除指定房屋的排名缓存")
    public ResVo<String> removeRoomRanking(
            @RequestParam("roomId") Long roomId) {
        try {
            cacheService.removeRoomRanking(roomId);
            return ResVo.ok("房屋排名缓存清除成功");
        } catch (Exception e) {
            return ResVo.fail(StatusEnum.valueOf("房屋排名缓存清除失败: " + e.getMessage()));
        }
    }

    @GetMapping("/status")
    @ApiOperation("获取缓存状态")
    public ResVo<Map<String, Object>> getCacheStatus() {
        try {
            String status = cacheService.getCacheStatus();
            Map<String, Object> result = new HashMap<>();
            result.put("status", status);
            result.put("timestamp", System.currentTimeMillis());
            return ResVo.ok(result);
        } catch (Exception e) {
            return ResVo.fail(StatusEnum.valueOf("获取缓存状态失败: " + e.getMessage()));
        }
    }
}
