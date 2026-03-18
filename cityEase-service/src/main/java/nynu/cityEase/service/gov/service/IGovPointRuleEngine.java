package nynu.cityEase.service.gov.service;

import java.util.Map;

/**
 * 积分规则执行引擎接口
 * Created: 2026/3/18
 */
public interface IGovPointRuleEngine {

    /**
     * 根据事件触发积分规则
     * @param eventType 事件类型
     * @param eventData 事件数据
     * @return 执行结果
     */
    boolean triggerRules(String eventType, Map<String, Object> eventData);

    /**
     * 处理报修完成评价事件
     * @param repairOrderId 报修订单ID
     * @param rating 评分
     * @param roomId 房屋ID
     * @param userId 用户ID
     */
    void handleRepairRatingEvent(Long repairOrderId, Integer rating, Long roomId, Long userId);

    /**
     * 处理签到事件
     * @param roomId 房屋ID
     * @param userId 用户ID
     * @param continuousDays 连续签到天数
     */
    void handleCheckInEvent(Long roomId, Long userId, Integer continuousDays);

    /**
     * 处理社区活动参与事件
     * @param roomId 房屋ID
     * @param userId 用户ID
     * @param activityType 活动类型
     * @param activityId 活动ID
     */
    void handleActivityEvent(Long roomId, Long userId, String activityType, Long activityId);

    /**
     * 处理违规行为事件
     * @param roomId 房屋ID
     * @param userId 用户ID
     * @param violationType 违规类型
     * @param description 违规描述
     */
    void handleViolationEvent(Long roomId, Long userId, String violationType, String description);
}
