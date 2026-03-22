package nynu.cityEase.web.admin.pms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.service.gov.repository.entity.GovPointLogDO;
import nynu.cityEase.service.gov.repository.mapper.GovPointLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/gov/point/log")
@Api(tags = "【后台】积分流水日志")
public class AdminGovPointLogController {

    @Autowired
    private GovPointLogMapper govPointLogMapper;

    @GetMapping("/page")
    @ApiOperation("分页查询积分流水")
    public ResVo<Page<GovPointLogDO>> page(
            @ApiParam(value = "当前页", example = "1")
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @ApiParam(value = "每页条数", example = "20")
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @ApiParam(value = "房屋ID")
            @RequestParam(value = "roomId", required = false) Long roomId,
            @ApiParam(value = "用户ID")
            @RequestParam(value = "userId", required = false) Long userId,
            @ApiParam(value = "变动类型：1-增加，2-扣减")
            @RequestParam(value = "changeType", required = false) Integer changeType) {

        Page<GovPointLogDO> page = new Page<>(current, size);
        LambdaQueryWrapper<GovPointLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, GovPointLogDO::getRoomId, roomId)
                .eq(userId != null, GovPointLogDO::getUserId, userId)
                .eq(changeType != null, GovPointLogDO::getChangeType, changeType)
                .orderByDesc(GovPointLogDO::getCreateTime);

        return ResVo.ok(govPointLogMapper.selectPage(page, wrapper));
    }

    @GetMapping("/latest")
    @ApiOperation("查询最新积分流水")
    public ResVo<List<GovPointLogDO>> latest(
            @ApiParam(value = "返回条数", example = "20")
            @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        LambdaQueryWrapper<GovPointLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(GovPointLogDO::getCreateTime)
                .last("LIMIT " + Math.max(1, limit));
        return ResVo.ok(govPointLogMapper.selectList(wrapper));
    }
}
