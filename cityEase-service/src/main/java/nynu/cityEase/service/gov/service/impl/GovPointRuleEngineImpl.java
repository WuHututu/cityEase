package nynu.cityEase.service.gov.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nynu.cityEase.service.gov.service.IGovPointRuleEngine;
import nynu.cityEase.service.gov.service.IGovPointRuleService;
import nynu.cityEase.api.vo.gov.PointRuleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分规则执行引擎实现类
 * Created: 2026/3/18
 */
@Service
public class GovPointRuleEngineImpl implements IGovPointRuleEngine {

    @Autowired
    private IGovPointRuleService ruleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean triggerRules(String eventType, Map<String, Object> eventData) {
        try {
            // 获取所有启用的规则
            List<PointRuleVO> enabledRules = ruleService.getEnabledRules();
            
            boolean anySuccess = false;
            
            for (PointRuleVO rule : enabledRules) {
                // 根据事件类型匹配规则
                if (isRuleMatched(rule, eventType, eventData)) {
                    Long roomId = getRoomIdFromEventData(eventData);
                    Long userId = getUserIdFromEventData(eventData);
                    
                    if (roomId != null && userId != null) {
                        String triggerDataJson = objectMapper.writeValueAsString(eventData);
                        boolean result = ruleService.executeRule(
                            rule.getId(), 
                            roomId, 
                            userId, 
                            eventType, 
                            triggerDataJson
                        );
                        
                        if (result) {
                            anySuccess = true;
                        }
                    }
                }
            }
            
            return anySuccess;
            
        } catch (Exception e) {
            // 记录错误日志
            System.err.println("规则执行引擎异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void handleRepairRatingEvent(Long repairOrderId, Integer rating, Long roomId, Long userId) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("repairOrderId", repairOrderId);
        eventData.put("rating", rating);
        eventData.put("roomId", roomId);
        eventData.put("userId", userId);
        
        triggerRules("REPAIR_RATING", eventData);
    }

    @Override
    public void handleCheckInEvent(Long roomId, Long userId, Integer continuousDays) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("roomId", roomId);
        eventData.put("userId", userId);
        eventData.put("continuousDays", continuousDays);
        
        triggerRules("CHECK_IN", eventData);
    }

    @Override
    public void handleActivityEvent(Long roomId, Long userId, String activityType, Long activityId) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("roomId", roomId);
        eventData.put("userId", userId);
        eventData.put("activityType", activityType);
        eventData.put("activityId", activityId);
        
        triggerRules("ACTIVITY_PARTICIPATION", eventData);
    }

    @Override
    public void handleViolationEvent(Long roomId, Long userId, String violationType, String description) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("roomId", roomId);
        eventData.put("userId", userId);
        eventData.put("violationType", violationType);
        eventData.put("description", description);
        
        triggerRules("VIOLATION", eventData);
    }

    /**
     * 判断规则是否匹配事件
     */
    private boolean isRuleMatched(PointRuleVO rule, String eventType, Map<String, Object> eventData) {
        try {
            // 根据规则类型判断事件类型匹配
            switch (rule.getRuleType()) {
                case 1: // 报修完成评价
                    return "REPAIR_RATING".equals(eventType) && checkRepairRatingCondition(rule, eventData);
                case 2: // 连续签到
                    return "CHECK_IN".equals(eventType) && checkCheckInCondition(rule, eventData);
                case 3: // 社区活动参与
                    return "ACTIVITY_PARTICIPATION".equals(eventType) && checkActivityCondition(rule, eventData);
                case 4: // 违规行为扣减
                    return "VIOLATION".equals(eventType) && checkViolationCondition(rule, eventData);
                case 5: // 自定义事件
                    return checkCustomEventCondition(rule, eventType, eventData);
                default:
                    return false;
            }
        } catch (Exception e) {
            System.err.println("规则匹配检查异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查报修评价条件
     */
    private boolean checkRepairRatingCondition(PointRuleVO rule, Map<String, Object> eventData) {
        try {
            if (rule.getTriggerCondition() == null || rule.getTriggerCondition().isEmpty()) {
                return true;
            }
            
            // 解析触发条件JSON
            Map<String, Object> condition = objectMapper.readValue(rule.getTriggerCondition(), Map.class);
            
            Integer ratingMin = (Integer) condition.get("rating_min");
            Integer rating = (Integer) eventData.get("rating");
            
            return ratingMin == null || rating >= ratingMin;
            
        } catch (JsonProcessingException e) {
            System.err.println("报修评价条件解析异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查签到条件
     */
    private boolean checkCheckInCondition(PointRuleVO rule, Map<String, Object> eventData) {
        try {
            if (rule.getTriggerCondition() == null || rule.getTriggerCondition().isEmpty()) {
                return true;
            }
            
            Map<String, Object> condition = objectMapper.readValue(rule.getTriggerCondition(), Map.class);
            
            Integer continuousDaysRequired = (Integer) condition.get("continuous_days");
            Integer continuousDays = (Integer) eventData.get("continuousDays");
            
            return continuousDaysRequired == null || continuousDays >= continuousDaysRequired;
            
        } catch (JsonProcessingException e) {
            System.err.println("签到条件解析异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查活动参与条件
     */
    private boolean checkActivityCondition(PointRuleVO rule, Map<String, Object> eventData) {
        try {
            if (rule.getTriggerCondition() == null || rule.getTriggerCondition().isEmpty()) {
                return true;
            }
            
            Map<String, Object> condition = objectMapper.readValue(rule.getTriggerCondition(), Map.class);
            
            String activityTypeRequired = (String) condition.get("activity_type");
            String activityType = (String) eventData.get("activityType");
            
            return activityTypeRequired == null || activityTypeRequired.equals(activityType);
            
        } catch (JsonProcessingException e) {
            System.err.println("活动条件解析异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查违规条件
     */
    private boolean checkViolationCondition(PointRuleVO rule, Map<String, Object> eventData) {
        try {
            if (rule.getTriggerCondition() == null || rule.getTriggerCondition().isEmpty()) {
                return true;
            }
            
            Map<String, Object> condition = objectMapper.readValue(rule.getTriggerCondition(), Map.class);
            
            String violationTypeRequired = (String) condition.get("violation_type");
            String violationType = (String) eventData.get("violationType");
            
            return violationTypeRequired == null || violationTypeRequired.equals(violationType);
            
        } catch (JsonProcessingException e) {
            System.err.println("违规条件解析异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查自定义事件条件
     */
    private boolean checkCustomEventCondition(PointRuleVO rule, String eventType, Map<String, Object> eventData) {
        try {
            if (rule.getTriggerCondition() == null || rule.getTriggerCondition().isEmpty()) {
                return true;
            }
            
            Map<String, Object> condition = objectMapper.readValue(rule.getTriggerCondition(), Map.class);
            
            String eventTypeRequired = (String) condition.get("event_type");
            return eventTypeRequired == null || eventTypeRequired.equals(eventType);
            
        } catch (JsonProcessingException e) {
            System.err.println("自定义事件条件解析异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从事件数据中获取房屋ID
     */
    private Long getRoomIdFromEventData(Map<String, Object> eventData) {
        Object roomId = eventData.get("roomId");
        return roomId != null ? Long.valueOf(roomId.toString()) : null;
    }

    /**
     * 从事件数据中获取用户ID
     */
    private Long getUserIdFromEventData(Map<String, Object> eventData) {
        Object userId = eventData.get("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }
}
