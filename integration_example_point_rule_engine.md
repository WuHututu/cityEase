# 积分规则引擎集成示例

本文档展示如何在现有业务中集成积分规则引擎，实现自动化的积分奖惩。

## 1. 数据库迁移

执行以下SQL脚本创建积分规则相关表：

```sql
-- 执行排行榜索引优化
SOURCE migration_add_point_ranking_indexes.sql;

-- 执行积分规则表创建
SOURCE migration_add_point_rule_tables.sql;
```

## 2. 报修评价集成示例

在 `PmsRepairOrderServiceImpl.java` 中集成规则引擎：

```java
// 在类中注入规则引擎
@Autowired
private IGovPointRuleEngine pointRuleEngine;

// 修改评价方法，替换原有的硬编码积分发放
@Transactional(rollbackFor = Exception.class)
@Override
public void evaluate(RepairOrderEvaluateReq req) {
    long userId = StpUtil.getLoginIdAsLong();
    
    // 原有的评价逻辑...
    RepairOrderDO orderDO = this.getById(req.getOrderId());
    if (orderDO == null) {
        throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "报修工单不存在");
    }
    
    // 越权校验和状态校验...
    if (!orderDO.getUserId().equals(userId)) {
        throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "无权操作他人的工单");
    }
    
    if (orderDO.getStatus() != 2) {
        throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "工单当前状态无法评价");
    }
    
    // 星级合法性校验...
    if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
        throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "评价星级必须在1到5之间");
    }
    
    // 更新工单状态和评价信息
    orderDO.setStatus(3);
    orderDO.setRating(req.getRating());
    orderDO.setEvaluateContent(req.getEvaluateContent());
    this.updateById(orderDO);
    
    // 【关键改动】使用规则引擎替代硬编码积分发放
    pointRuleEngine.handleRepairRatingEvent(
        orderDO.getId(), 
        req.getRating(), 
        orderDO.getRoomId(), 
        userId
    );
}
```

## 3. 签到功能集成示例

如果需要实现签到积分功能：

```java
@RestController
@RequestMapping("/app/checkin")
public class AppCheckInController {
    
    @Autowired
    private IGovPointRuleEngine pointRuleEngine;
    
    @Autowired
    private UserCheckInService checkInService;
    
    @PostMapping("/daily")
    public ResVo<String> dailyCheckIn() {
        long userId = StpUtil.getLoginIdAsLong();
        
        // 获取用户房屋信息
        Long roomId = getUserRoomId(userId);
        
        // 执行签到逻辑
        int continuousDays = checkInService.doDailyCheckIn(userId);
        
        // 触发签到积分规则
        pointRuleEngine.handleCheckInEvent(roomId, userId, continuousDays);
        
        return ResVo.ok("签到成功，连续签到" + continuousDays + "天");
    }
}
```

## 4. 社区活动集成示例

```java
@RestController
@RequestMapping("/app/activity")
public class AppActivityController {
    
    @Autowired
    private IGovPointRuleEngine pointRuleEngine;
    
    @PostMapping("/participate")
    public ResVo<String> participateActivity(
            @RequestParam("activityId") Long activityId,
            @RequestParam("activityType") String activityType) {
        
        long userId = StpUtil.getLoginIdAsLong();
        Long roomId = getUserRoomId(userId);
        
        // 记录活动参与...
        activityService.recordParticipation(userId, activityId, activityType);
        
        // 触发活动参与积分规则
        pointRuleEngine.handleActivityEvent(roomId, userId, activityType, activityId);
        
        return ResVo.ok("活动参与成功");
    }
}
```

## 5. 违规行为记录示例

```java
@RestController
@RequestMapping("/admin/violation")
public class AdminViolationController {
    
    @Autowired
    private IGovPointRuleEngine pointRuleEngine;
    
    @PostMapping("/record")
    public ResVo<String> recordViolation(
            @RequestParam("roomId") Long roomId,
            @RequestParam("userId") Long userId,
            @RequestParam("violationType") String violationType,
            @RequestParam("description") String description) {
        
        // 记录违规行为...
        violationService.recordViolation(roomId, userId, violationType, description);
        
        // 触发违规扣减积分规则
        pointRuleEngine.handleViolationEvent(roomId, userId, violationType, description);
        
        return ResVo.ok("违规记录成功");
    }
}
```

## 6. 规则配置示例

通过管理后台配置规则：

### 报修评价奖励规则
```json
{
  "ruleName": "报修完成评价奖励",
  "ruleType": 1,
  "triggerCondition": "{\"rating_min\": 4}",
  "pointsAmount": 30,
  "maxDailyTimes": 1,
  "maxMonthlyTimes": 10,
  "status": 1,
  "description": "完成报修并给予4星以上评价奖励30积分"
}
```

### 连续签到奖励规则
```json
{
  "ruleName": "连续签到7天奖励",
  "ruleType": 2,
  "triggerCondition": "{\"continuous_days\": 7}",
  "pointsAmount": 50,
  "maxDailyTimes": 1,
  "maxMonthlyTimes": 4,
  "status": 1,
  "description": "连续签到7天奖励50积分"
}
```

### 社区活动参与奖励
```json
{
  "ruleName": "志愿活动参与奖励",
  "ruleType": 3,
  "triggerCondition": "{\"activity_type\": \"volunteer\"}",
  "pointsAmount": 20,
  "maxDailyTimes": 3,
  "maxMonthlyTimes": 10,
  "status": 1,
  "description": "参与社区志愿活动奖励20积分"
}
```

### 违规行为扣减规则
```json
{
  "ruleName": "违规垃圾投放扣减",
  "ruleType": 4,
  "triggerCondition": "{\"violation_type\": \"garbage\"}",
  "pointsAmount": -50,
  "maxDailyTimes": null,
  "maxMonthlyTimes": null,
  "status": 1,
  "description": "违规投放垃圾扣减50积分"
}
```

## 7. 测试验证

### 7.1 排行榜功能测试
```bash
# 获取房屋积分排行榜
GET /admin/gov/ranking/room?limit=10

# 获取指定房屋排名
GET /admin/gov/ranking/room/1900000000000000024

# 获取楼栋积分统计
GET /admin/gov/ranking/building
```

### 7.2 规则管理测试
```bash
# 获取规则列表
GET /admin/gov/rule/page

# 创建新规则
POST /admin/gov/rule/save
{
  "ruleName": "测试规则",
  "ruleType": 1,
  "pointsAmount": 10,
  "status": 1
}

# 手动触发规则
POST /admin/gov/rule/execute?ruleId=1&roomId=1&userId=1&triggerEvent=TEST
```

## 8. 注意事项

1. **事务一致性**：规则执行引擎与业务操作在同一个事务中，确保数据一致性
2. **异常处理**：规则执行失败不会影响主业务流程，只会记录执行日志
3. **性能考虑**：规则匹配使用了缓存和索引优化，性能影响最小
4. **扩展性**：支持自定义规则类型和触发条件，可根据业务需求灵活扩展

## 9. 后续优化建议

1. **缓存优化**：对频繁查询的规则和排行榜数据添加Redis缓存
2. **异步处理**：对于非实时要求的规则执行，可以考虑异步消息队列
3. **规则模板**：提供常用规则的模板配置，简化管理操作
4. **统计分析**：增加规则执行效果的统计分析功能
