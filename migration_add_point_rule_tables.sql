-- 积分规则系统数据库表结构
-- Created: 2026/3/18

-- 积分规则表
DROP TABLE IF EXISTS `gov_point_rule`;
CREATE TABLE `gov_point_rule` (
  `id` bigint NOT NULL COMMENT '规则ID',
  `rule_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '规则名称',
  `rule_type` tinyint NOT NULL COMMENT '规则类型：1-报修完成评价，2-连续签到，3-社区活动参与，4-违规行为扣减，5-自定义事件',
  `trigger_condition` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '触发条件JSON配置',
  `points_amount` int NOT NULL COMMENT '积分数量（正数为奖励，负数为扣减）',
  `max_daily_times` int NULL DEFAULT NULL COMMENT '每日最大触发次数，NULL表示无限制',
  `max_monthly_times` int NULL DEFAULT NULL COMMENT '每月最大触发次数，NULL表示无限制',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规则描述',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rule_type_status` (`rule_type`, `status`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC COMMENT = '积分规则配置表';

-- 积分规则执行记录表
DROP TABLE IF EXISTS `gov_point_rule_log`;
CREATE TABLE `gov_point_rule_log` (
  `id` bigint NOT NULL COMMENT '记录ID',
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `room_id` bigint NOT NULL COMMENT '房屋ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `trigger_event` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发事件',
  `trigger_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '触发事件数据JSON',
  `points_awarded` int NOT NULL COMMENT '实际发放积分数量',
  `execution_result` tinyint NOT NULL COMMENT '执行结果：1-成功，0-失败',
  `failure_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '失败原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rule_id` (`rule_id`) USING BTREE,
  INDEX `idx_room_id_create_time` (`room_id`, `create_time`) USING BTREE,
  INDEX `idx_user_id_create_time` (`user_id`, `create_time`) USING BTREE,
  INDEX `idx_create_time` (`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC COMMENT = '积分规则执行记录表';

-- 插入示例规则数据
INSERT INTO `gov_point_rule` VALUES 
(1900000000000000100, '报修完成评价奖励', 1, '{"rating_min": 4}', 30, 1, 10, 1, '完成报修并给予4星以上评价奖励30积分', 100, '2026-03-18 10:00:00', '2026-03-18 10:00:00', 0),
(1900000000000000101, '连续签到奖励', 2, '{"continuous_days": 7}', 50, 1, 4, 1, '连续签到7天奖励50积分', 90, '2026-03-18 10:00:00', '2026-03-18 10:00:00', 0),
(1900000000000000102, '社区活动参与奖励', 3, '{"activity_type": "volunteer"}', 20, 3, 10, 1, '参与社区志愿活动奖励20积分', 80, '2026-03-18 10:00:00', '2026-03-18 10:00:00', 0),
(1900000000000000103, '违规垃圾投放扣减', 4, '{"violation_type": "garbage"}', -50, NULL, NULL, 1, '违规投放垃圾扣减50积分', 70, '2026-03-18 10:00:00', '2026-03-18 10:00:00', 0);
