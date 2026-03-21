package nynu.cityEase.web.admin.pms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.service.gov.repository.entity.GovPointLogDO;
import nynu.cityEase.service.gov.repository.mapper.GovPointLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 后台积分流水日志Controller
 * Created: 2026/3/18
 */
@RestController
@RequestMapping("/admin/gov/point/log")
@Api(tags = "【后台】积分流水日志")
public class AdminGovPointLogController {

    @Autowired
    private GovPointLogMapper pointLogMapper;

    @GetMapping("/page")
    @ApiOperation("获取积分流水日志分页列表")
    public ResVo<Page<GovPointLogDO>> getPointLogPage(
            @ApiParam(value = "当前页", example = "1")
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            
            @ApiParam(value = "每页条数", example = "20")
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            
            @ApiParam(value = "房屋ID")
            @RequestParam(value = "roomId", required = false) Long roomId,
            
            @ApiParam(value = "用户ID")
            @RequestParam(value = "userId", required = false) Long userId,
            
            @ApiParam(value = "变动类型：1-增加 2-减少")
            @RequestParam(value = "changeType", required = false) Integer changeType,
            
            @ApiParam(value = "开始日期")
            @RequestParam(value = "startDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            
            @ApiParam(value = "结束日期")
            @RequestParam(value = "endDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        Page<GovPointLogDO> page = new Page<>(current, size);
        
        LambdaQueryWrapper<GovPointLogDO> queryWrapper = Wrappers.lambdaQuery();
        
        // 按条件筛选
        if (roomId != null) {
            queryWrapper.eq(GovPointLogDO::getRoomId, roomId);
        }
        
        if (userId != null) {
            queryWrapper.eq(GovPointLogDO::getUserId, userId);
        }
        
        if (changeType != null) {
            queryWrapper.eq(GovPointLogDO::getChangeType, changeType);
        }
        
        // 日期范围筛选
        if (startDate != null) {
            LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.MIN);
            queryWrapper.ge(GovPointLogDO::getCreateTime, startDateTime);
        }
        
        if (endDate != null) {
            LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.MAX);
            queryWrapper.le(GovPointLogDO::getCreateTime, endDateTime);
        }
        
        // 按时间倒序排列
        queryWrapper.orderByDesc(GovPointLogDO::getCreateTime);
        
        pointLogMapper.selectPage(page, queryWrapper);
        
        return ResVo.ok(page);
    }

    @GetMapping("/latest")
    @ApiOperation("获取最新的积分流水日志")
    public ResVo<List<GovPointLogDO>> getLatestPointLog(
            @ApiParam(value = "返回条数限制", example = "10")
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        
        LambdaQueryWrapper<GovPointLogDO> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.orderByDesc(GovPointLogDO::getCreateTime);
        queryWrapper.last("LIMIT " + limit);
        
        List<GovPointLogDO> list = pointLogMapper.selectList(queryWrapper);
        
        return ResVo.ok(list);
    }
}
