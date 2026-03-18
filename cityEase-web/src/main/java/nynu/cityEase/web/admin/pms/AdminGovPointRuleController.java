package nynu.cityEase.web.admin.pms;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.gov.PointRuleLogVO;
import nynu.cityEase.api.vo.gov.PointRuleSaveReq;
import nynu.cityEase.api.vo.gov.PointRuleVO;
import nynu.cityEase.service.gov.service.IGovPointRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 后台积分规则管理Controller
 * Created: 2026/3/18
 */
@RestController
@RequestMapping("/admin/gov/rule")
@Api(tags = "【后台】积分规则管理")
public class AdminGovPointRuleController {

    @Autowired
    private IGovPointRuleService ruleService;

    @GetMapping("/page")
    @ApiOperation("分页查询积分规则")
    public ResVo<IPage<PointRuleVO>> pageRules(
            @ApiParam(value = "当前页码", example = "1")
            @RequestParam(value = "current", required = false, defaultValue = "1") Integer current,
            @ApiParam(value = "每页大小", example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @ApiParam(value = "规则类型", example = "1")
            @RequestParam(value = "ruleType", required = false) Integer ruleType,
            @ApiParam(value = "状态", example = "1")
            @RequestParam(value = "status", required = false) Integer status) {
        
        IPage<PointRuleVO> page = ruleService.pageRules(current, size, ruleType, status);
        return ResVo.ok(page);
    }

    @GetMapping("/list")
    @ApiOperation("获取所有启用的规则")
    public ResVo<List<PointRuleVO>> getEnabledRules() {
        List<PointRuleVO> rules = ruleService.getEnabledRules();
        return ResVo.ok(rules);
    }

    @GetMapping("/detail")
    @ApiOperation("获取规则详情")
    public ResVo<PointRuleVO> getRuleDetail(
            @ApiParam(value = "规则ID", required = true)
            @RequestParam("id") Long id) {
        PointRuleVO rule = ruleService.getRuleDetail(id);
        if (rule == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "规则不存在");
        }
        return ResVo.ok(rule);
    }

    @PostMapping("/save")
    @ApiOperation("新增或修改规则")
    public ResVo<String> saveOrUpdateRule(@Valid @RequestBody PointRuleSaveReq req) {
        ruleService.saveOrUpdateRule(req);
        return ResVo.ok(req.getId() == null ? "新增成功" : "修改成功");
    }

    @PostMapping("/delete")
    @ApiOperation("删除规则")
    public ResVo<String> deleteRule(
            @ApiParam(value = "规则ID", required = true)
            @RequestParam("id") Long id) {
        ruleService.deleteRule(id);
        return ResVo.ok("删除成功");
    }

    @PostMapping("/status")
    @ApiOperation("启用/禁用规则")
    public ResVo<String> changeRuleStatus(
            @ApiParam(value = "规则ID", required = true)
            @RequestParam("id") Long id,
            @ApiParam(value = "状态：1-启用，0-禁用", required = true)
            @RequestParam("status") Integer status) {
        ruleService.changeRuleStatus(id, status);
        return ResVo.ok(status == 1 ? "已启用" : "已禁用");
    }

    @GetMapping("/log/page")
    @ApiOperation("分页查询规则执行记录")
    public ResVo<IPage<PointRuleLogVO>> pageRuleLogs(
            @ApiParam(value = "当前页码", example = "1")
            @RequestParam(value = "current", required = false, defaultValue = "1") Integer current,
            @ApiParam(value = "每页大小", example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @ApiParam(value = "规则ID", example = "1")
            @RequestParam(value = "ruleId", required = false) Long ruleId,
            @ApiParam(value = "房屋ID", example = "1")
            @RequestParam(value = "roomId", required = false) Long roomId,
            @ApiParam(value = "执行结果", example = "1")
            @RequestParam(value = "executionResult", required = false) Integer executionResult) {
        
        IPage<PointRuleLogVO> page = ruleService.pageRuleLogs(current, size, ruleId, roomId, executionResult);
        return ResVo.ok(page);
    }

    @PostMapping("/execute")
    @ApiOperation("手动触发规则执行")
    public ResVo<String> executeRule(
            @ApiParam(value = "规则ID", required = true)
            @RequestParam("ruleId") Long ruleId,
            @ApiParam(value = "房屋ID", required = true)
            @RequestParam("roomId") Long roomId,
            @ApiParam(value = "用户ID", required = true)
            @RequestParam("userId") Long userId,
            @ApiParam(value = "触发事件", required = true)
            @RequestParam("triggerEvent") String triggerEvent,
            @ApiParam(value = "触发数据JSON", required = false)
            @RequestParam(value = "triggerData", required = false) String triggerData) {
        
        boolean result = ruleService.executeRule(ruleId, roomId, userId, triggerEvent, triggerData);
        return ResVo.ok(result ? "规则执行成功" : "规则执行失败");
    }
}
