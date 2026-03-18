package nynu.cityEase.web.front.gov;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.gov.BuildingPointStatsVO;
import nynu.cityEase.api.vo.gov.PointRankingVO;
import nynu.cityEase.service.gov.service.IGovPointRankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * App端积分排行榜Controller
 * Created: 2026/3/18
 */
@RestController
@RequestMapping("/app/gov/ranking")
@Api(tags = "【App端】积分排行榜")
public class AppGovPointRankingController {

    @Autowired
    private IGovPointRankingService rankingService;

    @GetMapping("/room")
    @ApiOperation("获取房屋积分排行榜")
    public ResVo<List<PointRankingVO>> getRoomRanking(
            @ApiParam(value = "返回条数限制，默认50", example = "20")
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit) {
        // App端默认返回前20名
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        // 限制App端最多查看50名
        limit = Math.min(limit, 50);
        
        List<PointRankingVO> ranking = rankingService.getRoomPointRanking(limit);
        return ResVo.ok(ranking);
    }

    @GetMapping("/room/{roomId}")
    @ApiOperation("获取指定房屋的排名信息")
    public ResVo<PointRankingVO> getRoomRankingDetail(
            @ApiParam(value = "房屋ID", required = true)
            @PathVariable("roomId") Long roomId) {
        PointRankingVO ranking = rankingService.getRoomRanking(roomId);
        if (ranking == null) {
            return ResVo.fail(StatusEnum.valueOf("房屋不存在"));
        }
        return ResVo.ok(ranking);
    }

    @GetMapping("/building")
    @ApiOperation("获取楼栋积分对比统计")
    public ResVo<List<BuildingPointStatsVO>> getBuildingStats() {
        List<BuildingPointStatsVO> stats = rankingService.getBuildingPointStats();
        return ResVo.ok(stats);
    }

    @GetMapping("/building/{areaId}/rooms")
    @ApiOperation("获取指定楼栋的房屋积分排行")
    public ResVo<List<PointRankingVO>> getBuildingRoomRanking(
            @ApiParam(value = "楼栋ID", required = true)
            @PathVariable("areaId") Long areaId,
            @ApiParam(value = "返回条数限制，默认20", example = "20")
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit) {
        // App端默认返回前20名
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        // 限制App端最多查看50名
        limit = Math.min(limit, 50);
        
        List<PointRankingVO> ranking = rankingService.getBuildingRoomRanking(areaId, limit);
        return ResVo.ok(ranking);
    }
}
