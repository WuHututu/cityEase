package nynu.cityEase.service.gov.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import nynu.cityEase.api.exception.ExceptionUtil;
import nynu.cityEase.api.vo.constants.StatusEnum;
import nynu.cityEase.api.vo.gov.PointRuleLogVO;
import nynu.cityEase.api.vo.gov.PointRuleSaveReq;
import nynu.cityEase.api.vo.gov.PointRuleVO;
import nynu.cityEase.service.gov.repository.entity.GovPointRuleDO;
import nynu.cityEase.service.gov.repository.entity.GovPointRuleLogDO;
import nynu.cityEase.service.gov.repository.mapper.GovPointRuleLogMapper;
import nynu.cityEase.service.gov.repository.mapper.GovPointRuleMapper;
import nynu.cityEase.service.gov.service.IGovPointRuleService;
import nynu.cityEase.service.gov.service.IGovPointService;
import nynu.cityEase.service.pms.repository.entity.RoomDO;
import nynu.cityEase.service.pms.repository.mapper.RoomMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 积分规则管理服务实现类
 * Created: 2026/3/18
 */
@Service
public class GovPointRuleServiceImpl extends ServiceImpl<GovPointRuleMapper, GovPointRuleDO> implements IGovPointRuleService {

    @Autowired
    private GovPointRuleLogMapper ruleLogMapper;

    @Autowired
    private GovPointRuleMapper ruleMapper;

    @Autowired
    private IGovPointService pointService;

    @Autowired
    private RoomMapper roomMapper;

    private static final Map<Integer, String> RULE_TYPE_MAP = Map.of(
            1, "报修完成评价",
            2, "连续签到",
            3, "社区活动参与",
            4, "违规行为扣减",
            5, "自定义事件"
    );

    private static final Map<Integer, String> STATUS_MAP = Map.of(
            0, "禁用",
            1, "启用"
    );

    private static final Map<Integer, String> EXECUTION_RESULT_MAP = Map.of(
            0, "失败",
            1, "成功"
    );

    @Override
    public IPage<PointRuleVO> pageRules(Integer current, Integer size, Integer ruleType, Integer status) {
        Page<GovPointRuleDO> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        
        LambdaQueryWrapper<GovPointRuleDO> wrapper = new LambdaQueryWrapper<>();
        if (ruleType != null) {
            wrapper.eq(GovPointRuleDO::getRuleType, ruleType);
        }
        if (status != null) {
            wrapper.eq(GovPointRuleDO::getStatus, status);
        }
        wrapper.orderByDesc(GovPointRuleDO::getSortOrder, GovPointRuleDO::getCreateTime);

        IPage<GovPointRuleDO> rulePage = this.page(page, wrapper);
        
        return rulePage.convert(this::convertToVO);
    }

    @Override
    public PointRuleVO getRuleDetail(Long id) {
        GovPointRuleDO rule = this.getById(id);
        if (rule == null) {
            return null;
        }
        return convertToVO(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateRule(PointRuleSaveReq req) {
        GovPointRuleDO rule;
        if (req.getId() != null) {
            rule = this.getById(req.getId());
            if (rule == null) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "规则不存在");
            }
        } else {
            rule = new GovPointRuleDO();
        }

        BeanUtils.copyProperties(req, rule);
        
        if (req.getSortOrder() == null) {
            rule.setSortOrder(0);
        }

        if (req.getId() != null) {
            this.updateById(rule);
        } else {
            this.save(rule);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long id) {
        GovPointRuleDO rule = this.getById(id);
        if (rule == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "规则不存在");
        }
        
        // 检查是否有执行记录
        LambdaQueryWrapper<GovPointRuleLogDO> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(GovPointRuleLogDO::getRuleId, id);
        Long logCount = ruleLogMapper.selectCount(logWrapper);
        if (logCount > 0) {
            // 有执行记录，只做逻辑删除
            rule.setIsDeleted(1);
            this.updateById(rule);
        } else {
            // 没有执行记录，直接删除
            this.removeById(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeRuleStatus(Long id, Integer status) {
        GovPointRuleDO rule = this.getById(id);
        if (rule == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "规则不存在");
        }
        
        rule.setStatus(status);
        this.updateById(rule);
    }

    @Override
    public List<PointRuleVO> getEnabledRules() {
        LambdaQueryWrapper<GovPointRuleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GovPointRuleDO::getStatus, 1)
               .eq(GovPointRuleDO::getIsDeleted, 0)
               .orderByAsc(GovPointRuleDO::getSortOrder, GovPointRuleDO::getCreateTime);
        
        List<GovPointRuleDO> rules = this.list(wrapper);
        return rules.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public IPage<PointRuleLogVO> pageRuleLogs(Integer current, Integer size, Long ruleId, Long roomId, Integer executionResult) {
        Page<GovPointRuleLogDO> page = new Page<>(current != null ? current : 1, size != null ? size : 10);
        
        LambdaQueryWrapper<GovPointRuleLogDO> wrapper = new LambdaQueryWrapper<>();
        if (ruleId != null) {
            wrapper.eq(GovPointRuleLogDO::getRuleId, ruleId);
        }
        if (roomId != null) {
            wrapper.eq(GovPointRuleLogDO::getRoomId, roomId);
        }
        if (executionResult != null) {
            wrapper.eq(GovPointRuleLogDO::getExecutionResult, executionResult);
        }
        wrapper.orderByDesc(GovPointRuleLogDO::getCreateTime);

        IPage<GovPointRuleLogDO> logPage = ruleLogMapper.selectPage(page, wrapper);
        
        return logPage.convert(this::convertLogToVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executeRule(Long ruleId, Long roomId, Long userId, String triggerEvent, String triggerData) {
        try {
            // 1. 获取规则
            GovPointRuleDO rule = this.getById(ruleId);
            if (rule == null || rule.getStatus() != 1) {
                recordExecutionLog(ruleId, roomId, userId, triggerEvent, triggerData, 0, "规则不存在或已禁用");
                return false;
            }

            // 2. 检查触发次数限制
            if (!checkTriggerLimits(ruleId, roomId, rule)) {
                recordExecutionLog(ruleId, roomId, userId, triggerEvent, triggerData, 0, "超出触发次数限制");
                return false;
            }

            // 3. 检查触发条件（这里简化处理，实际应该根据triggerCondition进行复杂判断）
            if (!checkTriggerCondition(rule, triggerData)) {
                recordExecutionLog(ruleId, roomId, userId, triggerEvent, triggerData, 0, "不满足触发条件");
                return false;
            }

            // 4. 执行积分变动
            boolean isAdd = rule.getPointsAmount() > 0;
            pointService.changePoints(roomId, userId, Math.abs(rule.getPointsAmount()), isAdd, rule.getRuleName());

            // 5. 记录成功执行
            recordExecutionLog(ruleId, roomId, userId, triggerEvent, triggerData, 1, null);
            return true;

        } catch (Exception e) {
            recordExecutionLog(ruleId, roomId, userId, triggerEvent, triggerData, 0, "执行异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查触发次数限制
     */
    private boolean checkTriggerLimits(Long ruleId, Long roomId, GovPointRuleDO rule) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDate monthStart = today.withDayOfMonth(1);

        // 检查每日限制
        if (rule.getMaxDailyTimes() != null) {
            LambdaQueryWrapper<GovPointRuleLogDO> dailyWrapper = new LambdaQueryWrapper<>();
            dailyWrapper.eq(GovPointRuleLogDO::getRuleId, ruleId)
                        .eq(GovPointRuleLogDO::getRoomId, roomId)
                        .eq(GovPointRuleLogDO::getExecutionResult, 1)
                        .ge(GovPointRuleLogDO::getCreateTime, today.atStartOfDay())
                        .lt(GovPointRuleLogDO::getCreateTime, today.plusDays(1).atStartOfDay());
            Long dailyCount = ruleLogMapper.selectCount(dailyWrapper);
            if (dailyCount >= rule.getMaxDailyTimes()) {
                return false;
            }
        }

        // 检查每月限制
        if (rule.getMaxMonthlyTimes() != null) {
            LambdaQueryWrapper<GovPointRuleLogDO> monthlyWrapper = new LambdaQueryWrapper<>();
            monthlyWrapper.eq(GovPointRuleLogDO::getRuleId, ruleId)
                         .eq(GovPointRuleLogDO::getRoomId, roomId)
                         .eq(GovPointRuleLogDO::getExecutionResult, 1)
                         .ge(GovPointRuleLogDO::getCreateTime, monthStart.atStartOfDay());
            Long monthlyCount = ruleLogMapper.selectCount(monthlyWrapper);
            if (monthlyCount >= rule.getMaxMonthlyTimes()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查触发条件（简化版本）
     */
    private boolean checkTriggerCondition(GovPointRuleDO rule, String triggerData) {
        // 这里应该根据rule.getTriggerCondition()和triggerData进行复杂判断
        // 暂时返回true，表示条件满足
        return true;
    }

    /**
     * 记录执行日志
     */
    private void recordExecutionLog(Long ruleId, Long roomId, Long userId, String triggerEvent, 
                                  String triggerData, Integer result, String failureReason) {
        GovPointRuleLogDO log = new GovPointRuleLogDO();
        log.setRuleId(ruleId);
        log.setRoomId(roomId);
        log.setUserId(userId);
        log.setTriggerEvent(triggerEvent);
        log.setTriggerData(triggerData);
        log.setPointsAwarded(0); // 在成功执行后需要更新
        log.setExecutionResult(result);
        log.setFailureReason(failureReason);
        
        ruleLogMapper.insert(log);
    }

    /**
     * 转换为VO
     */
    private PointRuleVO convertToVO(GovPointRuleDO rule) {
        PointRuleVO vo = new PointRuleVO();
        BeanUtils.copyProperties(rule, vo);
        vo.setRuleTypeName(RULE_TYPE_MAP.get(rule.getRuleType()));
        vo.setStatusName(STATUS_MAP.get(rule.getStatus()));
        return vo;
    }

    /**
     * 转换日志为VO
     */
    private PointRuleLogVO convertLogToVO(GovPointRuleLogDO log) {
        PointRuleLogVO vo = new PointRuleLogVO();
        BeanUtils.copyProperties(log, vo);
        
        // 获取规则名称
        GovPointRuleDO rule = this.getById(log.getRuleId());
        if (rule != null) {
            vo.setRuleName(rule.getRuleName());
        }
        
        // 获取房屋编号
        RoomDO room = roomMapper.selectById(log.getRoomId());
        if (room != null) {
            vo.setRoomNum(room.getRoomNum());
        }
        
        vo.setExecutionResultName(EXECUTION_RESULT_MAP.get(log.getExecutionResult()));
        return vo;
    }
}
