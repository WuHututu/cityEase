/*
 Navicat Premium Data Transfer

 Source Server         : 20251110实训
 Source Server Type    : MySQL
 Source Server Version : 80407
 Source Host           : 192.168.200.147:3306
 Source Schema         : city_ease

 Target Server Type    : MySQL
 Target Server Version : 80407
 File Encoding         : 65001

 Date: 09/03/2026 15:55:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for gov_point_goods
-- ----------------------------
DROP TABLE IF EXISTS `gov_point_goods`;
CREATE TABLE `gov_point_goods`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT ' (5L)',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'URL',
  `points_price` int NOT NULL,
  `stock` int NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT ': 1-, 0-',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gov_point_goods
-- ----------------------------
INSERT INTO `gov_point_goods` VALUES (1900000000000000087, '5L食用油', '5L食用油兑换商品', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png', 500, 20, 1, '2025-11-04 12:00:00', '2026-03-09 02:16:06', 0);
INSERT INTO `gov_point_goods` VALUES (1900000000000000088, '洗衣液', '洗衣液兑换商品', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png', 200, 50, 1, '2025-11-04 12:00:00', '2026-03-09 02:16:06', 0);
INSERT INTO `gov_point_goods` VALUES (1900000000000000089, '纸巾套装', '纸巾套装兑换商品', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png', 150, 100, 1, '2025-11-04 12:00:00', '2026-03-09 02:16:06', 0);
INSERT INTO `gov_point_goods` VALUES (1900000000000000090, '充电宝', '充电宝兑换商品', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png', 800, 10, 1, '2025-11-04 12:00:00', '2026-03-09 02:16:06', 0);
INSERT INTO `gov_point_goods` VALUES (1900000000000000091, '雨伞', '雨伞兑换商品', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png', 120, 30, 1, '2025-11-04 12:00:00', '2026-03-09 02:16:06', 0);
INSERT INTO `gov_point_goods` VALUES (1900000000000000092, '米面粮油礼包', '米面粮油礼包兑换商品', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png', 1000, 5, 1, '2025-11-04 12:00:00', '2026-03-09 02:16:06', 0);
INSERT INTO `gov_point_goods` VALUES (2030909001110827010, '对联', '', '', 50, 100, 1, '2026-03-09 15:30:13', '2026-03-09 15:30:13', 0);

-- ----------------------------
-- Table structure for gov_point_log
-- ----------------------------
DROP TABLE IF EXISTS `gov_point_log`;
CREATE TABLE `gov_point_log`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `room_id` bigint NOT NULL COMMENT 'ID ()',
  `user_id` bigint NOT NULL COMMENT 'ID (/)',
  `change_type` tinyint NOT NULL COMMENT ': 1-(), 2-()',
  `change_amount` int NOT NULL COMMENT ' ()',
  `after_balance` int NOT NULL COMMENT ' ()',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT ' ()',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gov_point_log
-- ----------------------------
INSERT INTO `gov_point_log` VALUES (1900000000000000096, 1900000000000000024, 1900000000000000004, 1, 300, 300, '工单1900000000000000042评价奖励', '2026-03-02 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `gov_point_log` VALUES (1900000000000000097, 1900000000000000025, 1900000000000000005, 1, 300, 300, '工单1900000000000000043评价奖励', '2026-03-02 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `gov_point_log` VALUES (1900000000000000098, 1900000000000000024, 1900000000000000004, 1, 300, 600, '工单1900000000000000048评价奖励', '2026-03-02 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `gov_point_log` VALUES (1900000000000000100, 1900000000000000024, 1900000000000000004, 2, 500, 100, '兑换商品5L食用油', '2026-03-02 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `gov_point_log` VALUES (1900000000000000102, 1900000000000000025, 1900000000000000005, 2, 200, 100, '兑换商品洗衣液', '2026-03-02 12:00:00', '2026-03-03 12:00:00', 0);

-- ----------------------------
-- Table structure for gov_point_order
-- ----------------------------
DROP TABLE IF EXISTS `gov_point_order`;
CREATE TABLE `gov_point_order`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `goods_id` bigint NOT NULL COMMENT 'ID',
  `goods_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '()',
  `room_id` bigint NOT NULL COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '(ID)',
  `points_paid` int NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT ': 0-(), 1-()',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gov_point_order
-- ----------------------------
INSERT INTO `gov_point_order` VALUES (1900000000000000099, 1900000000000000087, '5L食用油', 1900000000000000024, 1900000000000000004, 500, 1, '2026-03-02 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `gov_point_order` VALUES (1900000000000000101, 1900000000000000088, '洗衣液', 1900000000000000025, 1900000000000000005, 200, 1, '2026-03-03 12:00:00', '2026-03-03 12:00:00', 0);

-- ----------------------------
-- Table structure for gov_public_affairs
-- ----------------------------
DROP TABLE IF EXISTS `gov_public_affairs`;
CREATE TABLE `gov_public_affairs`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `type` tinyint NULL DEFAULT NULL,
  `title` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `content` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gov_public_affairs
-- ----------------------------
INSERT INTO `gov_public_affairs` VALUES (1900000000000000103, 1, '垃圾分类宣传', '垃圾分类宣传内容示例', '2026-02-02 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `gov_public_affairs` VALUES (1900000000000000104, 1, '消防演练通知', '消防演练通知内容示例', '2026-02-03 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `gov_public_affairs` VALUES (1900000000000000105, 1, '停车管理调整', '停车管理调整内容示例', '2026-02-04 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `gov_public_affairs` VALUES (1900000000000000106, 1, '电动车充电规范', '电动车充电规范内容示例', '2026-02-05 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `gov_public_affairs` VALUES (1900000000000000107, 1, '小区绿化养护', '小区绿化养护内容示例', '2026-02-06 12:00:00', '2026-03-03 12:00:00', 0);

-- ----------------------------
-- Table structure for pms_bind_audit
-- ----------------------------
DROP TABLE IF EXISTS `pms_bind_audit`;
CREATE TABLE `pms_bind_audit`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `owner_id` bigint NOT NULL COMMENT '业主 ID',
  `room_id` bigint NOT NULL COMMENT '房屋 ID',
  `owner_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业主姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号',
  `room_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '房屋信息',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-已通过，2-已拒绝',
  `apply_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `auditor_id` bigint NULL DEFAULT NULL COMMENT '审核人 ID',
  `auditor_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核人姓名',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `idx_room_id`(`room_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pms_bind_audit
-- ----------------------------
INSERT INTO `pms_bind_audit` VALUES (2026030900000000001, 1900000000000000001, 1900000000000000021, '张三', '13800001001', '1 栋 1 单元 1101 室', 1, '2026-03-08 09:15:00', NULL, NULL, NULL, '首次申请绑定', '2026-03-08 09:15:00', '2026-03-08 19:44:07', 0);
INSERT INTO `pms_bind_audit` VALUES (2026030900000000002, 1900000000000000002, 1900000000000000022, '李四', '13800001002', '1 栋 1 单元 1102 室', 1, '2026-03-08 10:30:00', NULL, NULL, NULL, '家属关系绑定', '2026-03-08 10:30:00', '2026-03-08 19:44:28', 0);
INSERT INTO `pms_bind_audit` VALUES (2026030900000000003, 1900000000000000003, 1900000000000000023, '王五', '13800001003', '1 栋 1 单元 1201 室', 0, '2026-03-08 14:20:00', NULL, NULL, NULL, '', '2026-03-08 14:20:00', '2026-03-09 05:58:47', 0);
INSERT INTO `pms_bind_audit` VALUES (2026030900000000004, 1900000000000000004, 1900000000000000024, '赵六', '13800001004', '1 栋 1 单元 1202 室', 1, '2026-03-08 15:45:00', NULL, NULL, NULL, '业主本人申请', '2026-03-08 15:45:00', '2026-03-09 05:52:14', 0);
INSERT INTO `pms_bind_audit` VALUES (2026030900000000005, 1900000000000000005, 1900000000000000025, '钱七', '13800001005', '1 栋 2 单元 2101 室', 0, '2026-03-09 08:00:00', NULL, NULL, NULL, '', '2026-03-09 08:00:00', '2026-03-09 05:58:42', 0);
INSERT INTO `pms_bind_audit` VALUES (2026030900000000006, 1900000000000000006, 1900000000000000026, '孙八', '13800001006', '1 栋 2 单元 2102 室', 2, '2026-03-09 09:30:00', NULL, NULL, NULL, '', '2026-03-09 09:30:00', '2026-03-09 05:57:48', 0);
INSERT INTO `pms_bind_audit` VALUES (2026030900000000007, 1900000000000000001, 1900000000000000027, '张三', '13800001001', '1 栋 2 单元 2201 室', 0, '2026-03-09 10:15:00', NULL, NULL, NULL, '第二套房产绑定', '2026-03-09 10:15:00', '2026-03-09 10:15:00', 0);
INSERT INTO `pms_bind_audit` VALUES (2026030900000000008, 1900000000000000002, 1900000000000000028, '李四', '13800001002', '1 栋 2 单元 2202 室', 0, '2026-03-09 11:00:00', NULL, NULL, NULL, '投资房产申请', '2026-03-09 11:00:00', '2026-03-09 11:00:00', 0);
INSERT INTO `pms_bind_audit` VALUES (2026030900000000009, 1900000000000000003, 1900000000000000029, '王五', '13800001003', '2 栋 1 单元 3101 室', 1, '2026-03-05 09:00:00', 1900000000000000007, '管理员', '2026-03-05 14:30:00', '资料齐全，已审核通过', '2026-03-05 09:00:00', '2026-03-05 14:30:00', 0);
INSERT INTO `pms_bind_audit` VALUES (2026030900000000010, 1900000000000000004, 1900000000000000030, '赵六', '13800001004', '2 栋 1 单元 3102 室', 1, '2026-03-06 10:20:00', 1900000000000000007, '管理员', '2026-03-06 15:00:00', NULL, '2026-03-06 10:20:00', '2026-03-06 15:00:00', 0);
INSERT INTO `pms_bind_audit` VALUES (2026030900000000011, 1900000000000000005, 1900000000000000021, '钱七', '13800001005', '1 栋 1 单元 1101 室', 1, '2026-03-04 11:30:00', 1900000000000000007, '管理员', '2026-03-04 16:45:00', '业主直系亲属', '2026-03-04 11:30:00', '2026-03-04 16:45:00', 0);
INSERT INTO `pms_bind_audit` VALUES (2026030900000000012, 1900000000000000006, 1900000000000000022, '孙八', '13800001006', '1 栋 1 单元 1102 室', 1, '2026-03-07 08:45:00', 1900000000000000007, '管理员', '2026-03-07 10:15:00', '快速审批通道', '2026-03-07 08:45:00', '2026-03-07 10:15:00', 0);
INSERT INTO `pms_bind_audit` VALUES (2026030900000000013, 1900000000000000001, 1900000000000000023, '张三', '13800001001', '1 栋 1 单元 1201 室', 2, '2026-03-03 14:00:00', 1900000000000000007, '管理员', '2026-03-03 16:30:00', '房产证明不清晰，需重新提交', '2026-03-03 14:00:00', '2026-03-03 16:30:00', 0);
INSERT INTO `pms_bind_audit` VALUES (2026030900000000014, 1900000000000000002, 1900000000000000024, '李四', '13800001002', '1 栋 1 单元 1202 室', 2, '2026-03-02 09:30:00', 1900000000000000007, '管理员', '2026-03-02 11:00:00', '申请人信息不符', '2026-03-02 09:30:00', '2026-03-02 11:00:00', 0);
INSERT INTO `pms_bind_audit` VALUES (2026030900000000015, 1900000000000000003, 1900000000000000025, '王五', '13800001003', '1 栋 2 单元 2101 室', 2, '2026-03-01 15:20:00', 1900000000000000007, '管理员', '2026-03-01 17:00:00', '该房屋已被其他用户绑定', '2026-03-01 15:20:00', '2026-03-01 17:00:00', 0);

-- ----------------------------
-- Table structure for pms_fee_bill
-- ----------------------------
DROP TABLE IF EXISTS `pms_fee_bill`;
CREATE TABLE `pms_fee_bill`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `room_id` bigint NOT NULL COMMENT '房屋ID',
  `fee_month` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '计费月份 (格式: 2026-05)',
  `amount` decimal(10, 2) NOT NULL COMMENT '应收金额',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '缴费状态: 0-待缴费, 1-已缴费',
  `payer_id` bigint NULL DEFAULT NULL COMMENT '实际支付人的用户ID',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '实际支付时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '账单生成时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_room_month`(`room_id` ASC, `fee_month` ASC, `is_deleted` ASC) USING BTREE COMMENT '防重复生成：同一房屋每月只能有一条有效账单',
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '物业费账单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pms_fee_bill
-- ----------------------------
INSERT INTO `pms_fee_bill` VALUES (1900000000000000049, 1900000000000000021, '2026-02', 120.50, 1, 1900000000000000001, '2026-02-22 12:00:00', '2026-01-23 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_fee_bill` VALUES (1900000000000000050, 1900000000000000021, '2026-03', 120.50, 0, NULL, NULL, '2026-01-23 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_fee_bill` VALUES (1900000000000000051, 1900000000000000022, '2026-02', 120.50, 1, 1900000000000000002, '2026-03-09 15:11:07', '2026-01-23 12:00:00', '2026-03-09 15:11:07', 0);
INSERT INTO `pms_fee_bill` VALUES (1900000000000000052, 1900000000000000022, '2026-03', 120.50, 0, NULL, NULL, '2026-01-23 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_fee_bill` VALUES (1900000000000000053, 1900000000000000023, '2026-02', 120.50, 1, 1900000000000000003, '2026-02-22 12:00:00', '2026-01-23 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_fee_bill` VALUES (1900000000000000054, 1900000000000000023, '2026-03', 120.50, 0, NULL, NULL, '2026-01-23 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_fee_bill` VALUES (1900000000000000055, 1900000000000000024, '2026-02', 120.50, 1, 1900000000000000004, '2026-02-22 12:00:00', '2026-01-23 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_fee_bill` VALUES (1900000000000000056, 1900000000000000024, '2026-03', 120.50, 0, NULL, NULL, '2026-01-23 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_fee_bill` VALUES (1900000000000000057, 1900000000000000025, '2026-02', 120.50, 1, 1900000000000000005, '2026-02-22 12:00:00', '2026-01-23 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_fee_bill` VALUES (1900000000000000058, 1900000000000000025, '2026-03', 120.50, 0, NULL, NULL, '2026-01-23 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_fee_bill` VALUES (1900000000000000059, 1900000000000000026, '2026-02', 120.50, 1, 1900000000000000006, '2026-02-22 12:00:00', '2026-01-23 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_fee_bill` VALUES (1900000000000000060, 1900000000000000026, '2026-03', 120.50, 0, NULL, NULL, '2026-01-23 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_fee_bill` VALUES (2030904275724054530, 1900000000000000027, '2026-03', 120.00, 0, NULL, NULL, '2026-03-09 15:11:26', '2026-03-09 15:11:43', 0);
INSERT INTO `pms_fee_bill` VALUES (2030904275724054531, 1900000000000000028, '2026-03', 120.00, 0, NULL, NULL, '2026-03-09 15:11:26', '2026-03-09 15:11:47', 0);
INSERT INTO `pms_fee_bill` VALUES (2030904275724054532, 1900000000000000029, '2026-03', 120.00, 0, NULL, NULL, '2026-03-09 15:11:26', '2026-03-09 15:11:53', 0);
INSERT INTO `pms_fee_bill` VALUES (2030904275786969089, 1900000000000000030, '2026-03', 120.00, 0, NULL, NULL, '2026-03-09 15:11:26', '2026-03-09 15:11:26', 0);

-- ----------------------------
-- Table structure for pms_public_area
-- ----------------------------
DROP TABLE IF EXISTS `pms_public_area`;
CREATE TABLE `pms_public_area`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT 'ID',
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `level` tinyint NULL DEFAULT NULL COMMENT ': 1-, 2-...',
  `sort` int NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pms_public_area
-- ----------------------------
INSERT INTO `pms_public_area` VALUES (1900000000000000017, NULL, 'CityEase花园', 1, 1, '2025-08-16 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_public_area` VALUES (1900000000000000018, 1900000000000000017, '1号楼', 3, 1, '2025-08-16 12:00:00', '2026-03-09 05:05:25', 0);
INSERT INTO `pms_public_area` VALUES (1900000000000000019, 1900000000000000017, '2号楼', 3, 2, '2025-08-16 12:00:00', '2026-03-09 05:05:26', 0);
INSERT INTO `pms_public_area` VALUES (1900000000000000020, 1900000000000000017, '3号楼', 3, 3, '2025-08-16 12:00:00', '2026-03-09 05:05:32', 0);
INSERT INTO `pms_public_area` VALUES (1900000000000000021, NULL, '静心亭', 6, 0, '2026-03-09 02:06:52', '2026-03-09 05:05:34', 0);
INSERT INTO `pms_public_area` VALUES (2030872974061178881, 1900000000000000019, '3楼', 4, 0, '2026-03-09 05:07:01', '2026-03-09 05:07:01', 0);
INSERT INTO `pms_public_area` VALUES (2030873191078662146, 0, '南师小区', 1, 0, '2026-03-09 05:07:53', '2026-03-09 05:17:55', 1);

-- ----------------------------
-- Table structure for pms_repair_order
-- ----------------------------
DROP TABLE IF EXISTS `pms_repair_order`;
CREATE TABLE `pms_repair_order`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '()ID',
  `room_id` bigint NULL DEFAULT NULL COMMENT 'ID()',
  `repair_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '(: )',
  `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `images` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'URL(JSON)',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT ': 0-, 1-, 2-, 3-, 4-',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '()ID',
  `handle_time` datetime NULL DEFAULT NULL,
  `handle_result` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '/',
  `handle_images` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '(JSON)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  `rating` tinyint NULL DEFAULT NULL COMMENT '(1-5)',
  `evaluate_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pms_repair_order
-- ----------------------------
INSERT INTO `pms_repair_order` VALUES (1900000000000000039, 1900000000000000001, 1900000000000000021, '水电维修', '水电维修：问题描述示例1', NULL, 0, NULL, NULL, NULL, NULL, '2026-03-03 12:00:00', '2026-03-05 05:55:50', 0, NULL, NULL);
INSERT INTO `pms_repair_order` VALUES (1900000000000000040, 1900000000000000002, 1900000000000000022, '门锁更换', '门锁更换：问题描述示例2', NULL, 1, 1900000000000000008, '2026-02-28 12:00:00', NULL, NULL, '2026-03-02 12:00:00', '2026-03-05 05:55:43', 0, NULL, NULL);
INSERT INTO `pms_repair_order` VALUES (1900000000000000041, 1900000000000000003, 1900000000000000023, '下水道疏通', '下水道疏通：问题描述示例3', NULL, 2, 1900000000000000008, '2026-03-01 12:00:00', NULL, NULL, '2026-03-02 12:00:00', '2026-03-05 05:55:37', 0, NULL, NULL);
INSERT INTO `pms_repair_order` VALUES (1900000000000000042, 1900000000000000004, 1900000000000000024, '电梯故障', '电梯故障：问题描述示例4', NULL, 1, 1900000000000000008, '2026-03-02 12:00:00', '已修复', NULL, '2026-03-04 12:00:00', '2026-03-05 08:52:01', 0, 5, '服务很好，处理迅速');
INSERT INTO `pms_repair_order` VALUES (1900000000000000043, 1900000000000000005, 1900000000000000025, '水电维修', '水电维修：问题描述示例5', NULL, 2, 1900000000000000008, '2026-03-05 18:01:25', '好', NULL, '2026-03-05 12:00:00', '2026-03-05 08:52:01', 0, 5, '服务很好，处理迅速');
INSERT INTO `pms_repair_order` VALUES (1900000000000000044, 1900000000000000006, 1900000000000000026, '门锁更换', '门锁更换：问题描述示例6', NULL, 0, 1900000000000000008, '2026-03-04 12:00:00', NULL, NULL, '2026-03-02 12:00:00', '2026-03-05 08:52:01', 0, NULL, NULL);
INSERT INTO `pms_repair_order` VALUES (1900000000000000045, 1900000000000000001, 1900000000000000021, '下水道疏通', '下水道疏通：问题描述示例7', NULL, 0, 1900000000000000008, '2026-03-05 12:00:00', NULL, NULL, '2026-03-03 12:00:00', '2026-03-05 08:52:01', 0, NULL, NULL);
INSERT INTO `pms_repair_order` VALUES (1900000000000000046, 1900000000000000002, 1900000000000000022, '电梯故障', '电梯故障：问题描述示例8', NULL, 0, NULL, NULL, NULL, NULL, '2026-03-03 12:00:00', '2026-03-05 05:54:44', 0, NULL, NULL);
INSERT INTO `pms_repair_order` VALUES (1900000000000000047, 1900000000000000003, 1900000000000000023, '水电维修', '水电维修：问题描述示例9', NULL, 0, 1900000000000000008, '2026-03-07 12:00:00', '已修复', NULL, '2026-02-28 12:00:00', '2026-03-05 08:52:01', 0, NULL, NULL);
INSERT INTO `pms_repair_order` VALUES (1900000000000000048, 1900000000000000004, 1900000000000000024, '门锁更换', '门锁更换：问题描述示例10', NULL, 0, 1900000000000000008, '2026-03-08 12:00:00', '已修复', NULL, '2026-02-28 12:00:00', '2026-03-05 08:52:01', 0, 5, '服务很好，处理迅速');

-- ----------------------------
-- Table structure for pms_room
-- ----------------------------
DROP TABLE IF EXISTS `pms_room`;
CREATE TABLE `pms_room`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `area_id` bigint NOT NULL COMMENT 'pms_public_area.id',
  `room_num` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '(:2001)',
  `points_balance` int NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pms_room
-- ----------------------------
INSERT INTO `pms_room` VALUES (1900000000000000021, 1900000000000000018, '1101', 0, '2025-09-05 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_room` VALUES (1900000000000000022, 1900000000000000018, '1102', 0, '2025-09-05 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_room` VALUES (1900000000000000023, 1900000000000000018, '1201', 0, '2025-09-05 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_room` VALUES (1900000000000000024, 1900000000000000018, '1202', 100, '2025-09-05 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_room` VALUES (1900000000000000025, 1900000000000000019, '2101', 100, '2025-09-05 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_room` VALUES (1900000000000000026, 1900000000000000019, '2102', 0, '2025-09-05 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_room` VALUES (1900000000000000027, 1900000000000000019, '2201', 0, '2025-09-05 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_room` VALUES (1900000000000000028, 1900000000000000019, '2202', 0, '2025-09-05 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_room` VALUES (1900000000000000029, 1900000000000000020, '3101', 0, '2025-09-05 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_room` VALUES (1900000000000000030, 1900000000000000020, '3102', 0, '2025-09-05 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_room` VALUES (2030879668367466498, 2030872974061178881, '3105', 0, '2026-03-09 05:33:37', '2026-03-09 05:51:10', 1);

-- ----------------------------
-- Table structure for pms_user_room_rel
-- ----------------------------
DROP TABLE IF EXISTS `pms_user_room_rel`;
CREATE TABLE `pms_user_room_rel`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT 'ID ( sys_user_info.user_id)',
  `room_id` bigint NOT NULL COMMENT 'ID ( pms_room.id)',
  `relation_type` tinyint NOT NULL COMMENT ': 1-, 2-, 3-',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT ': 0-, 1-, 2-',
  `attachments` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT ' (/URLJSON)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_room`(`user_id` ASC, `room_id` ASC, `is_deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pms_user_room_rel
-- ----------------------------
INSERT INTO `pms_user_room_rel` VALUES (1900000000000000031, 1900000000000000001, 1900000000000000021, 1, 1, NULL, '业主认证通过', '2026-01-03 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_user_room_rel` VALUES (1900000000000000032, 1900000000000000002, 1900000000000000022, 1, 1, NULL, '业主认证通过', '2026-01-04 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_user_room_rel` VALUES (1900000000000000033, 1900000000000000003, 1900000000000000023, 1, 1, NULL, '业主认证通过', '2026-01-05 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_user_room_rel` VALUES (1900000000000000034, 1900000000000000004, 1900000000000000024, 1, 1, NULL, '业主认证通过', '2026-01-06 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_user_room_rel` VALUES (1900000000000000035, 1900000000000000005, 1900000000000000025, 1, 1, NULL, '业主认证通过', '2026-01-07 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_user_room_rel` VALUES (1900000000000000036, 1900000000000000006, 1900000000000000026, 1, 1, NULL, '业主认证通过', '2026-01-08 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_user_room_rel` VALUES (1900000000000000037, 1900000000000000002, 1900000000000000021, 2, 1, NULL, '家属', '2026-01-09 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `pms_user_room_rel` VALUES (1900000000000000038, 1900000000000000003, 1900000000000000022, 3, 1, NULL, '租户', '2026-01-10 12:00:00', '2026-03-03 12:00:00', 0);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `dict_type` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT '',
  `dict_label` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT '',
  `dict_value` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT '',
  `dict_sort` int NULL DEFAULT 0,
  `status` tinyint NULL DEFAULT 1 COMMENT '1 0',
  `remark` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 'pms_area_level', '小区', '1', 1, 1, NULL, '2026-02-13 16:23:02', NULL, 0);
INSERT INTO `sys_dict_data` VALUES (2, 'pms_area_level', '分期', '2', 2, 1, NULL, '2026-02-13 16:23:02', '2026-03-09 05:03:52', 0);
INSERT INTO `sys_dict_data` VALUES (3, 'pms_area_level', '楼栋', '3', 3, 1, NULL, '2026-02-13 16:23:02', '2026-03-09 05:04:41', 0);
INSERT INTO `sys_dict_data` VALUES (4, 'pms_area_level', '单元', '4', 4, 1, NULL, '2026-02-13 16:23:02', '2026-03-09 05:04:44', 0);
INSERT INTO `sys_dict_data` VALUES (5, 'pms_area_level', '楼层', '5', 5, 1, NULL, '2026-02-13 16:23:02', '2026-03-09 05:04:48', 0);
INSERT INTO `sys_dict_data` VALUES (6, 'pms_area_level', '公共区域', '6', 6, 1, NULL, '2026-02-13 16:23:02', '2026-03-09 02:01:51', 0);
INSERT INTO `sys_dict_data` VALUES (1900000000000000080, 'repair_type', '水电维修', '1', 1, 1, NULL, '2025-12-04 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_dict_data` VALUES (1900000000000000081, 'repair_type', '门锁更换', '2', 2, 1, NULL, '2025-12-04 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_dict_data` VALUES (1900000000000000082, 'repair_type', '下水道疏通', '3', 3, 1, NULL, '2025-12-04 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_dict_data` VALUES (1900000000000000083, 'repair_type', '电梯故障', '4', 4, 1, NULL, '2025-12-04 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_dict_data` VALUES (1900000000000000084, 'notice_type', '通知', '1', 1, 1, NULL, '2025-12-04 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_dict_data` VALUES (1900000000000000085, 'notice_type', '公告', '2', 2, 1, NULL, '2025-12-04 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_dict_data` VALUES (1900000000000000086, 'notice_type', '活动', '3', 3, 1, NULL, '2025-12-04 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_dict_data` VALUES (2030906987400310785, 'pms_area_level', '123', '123', 3, 0, '', '2026-03-09 07:22:11', '2026-03-09 07:22:11', 0);

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `dict_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT '',
  `dict_type` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT '',
  `status` tinyint NULL DEFAULT 1 COMMENT '1 0',
  `remark` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '区域类型1', 'pms_area_level', 1, '公共区域类型', '2026-02-13 16:23:02', NULL, 0);
INSERT INTO `sys_dict_type` VALUES (1900000000000000078, '报修类型', 'repair_type', 1, '报修工单类型', '2025-11-24 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_dict_type` VALUES (1900000000000000079, '公告类型', 'notice_type', 1, '系统公告类型', '2025-11-24 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_dict_type` VALUES (2030910242712891393, '啊·', '打算', 1, '', '2026-03-09 07:35:07', NULL, 1);

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `notice_title` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `notice_type` tinyint NOT NULL COMMENT ': 1-, 2-, 3-',
  `notice_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '(HTML)',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT ': 0-, 1-',
  `is_top` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `creator_id` bigint NOT NULL COMMENT '(ID)',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图 URL',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1900000000000000061, '停水通知', 1, '<p>停水通知内容示例</p>', 1, 1, 1900000000000000007, '2026-03-09 12:00:00', NULL, 0);
INSERT INTO `sys_notice` VALUES (1900000000000000062, '电梯检修通知', 1, '<p>电梯检修通知内容示例</p>', 1, 1, 1900000000000000007, '2026-02-26 12:00:00', '2026-03-08 19:48:35', 0);
INSERT INTO `sys_notice` VALUES (1900000000000000063, '社区活动报名', 1, '<p>社区活动报名内容示例</p>', 1, 0, 1900000000000000007, '2026-02-27 12:00:00', NULL, 0);
INSERT INTO `sys_notice` VALUES (2030823210858799106, '新年好', 1, '<p>好好好</p>', 1, 0, 2020751400691752961, '2026-03-09 01:49:17', '2026-03-09 04:42:49', 1);
INSERT INTO `sys_notice` VALUES (2030823673842851842, '111', 1, '<p>1</p>', 0, 0, 2020751400691752961, '2026-03-09 01:51:08', '2026-03-09 04:42:47', 1);
INSERT INTO `sys_notice` VALUES (2030823765853298690, '111', 1, '<p>1</p>', 0, 0, 2020751400691752961, '2026-03-09 01:51:30', '2026-03-09 04:42:29', 1);
INSERT INTO `sys_notice` VALUES (2030903286040276994, '阿松大是的', 3, '<p>新年好</p>', 1, 1, 2020751400691752961, '2026-03-09 07:07:28', '2026-03-09 07:10:36', 1);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `phone` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '()',
  `password` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '()',
  `status` tinyint NULL DEFAULT 0 COMMENT ': 0-, 1-',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT ': 0-, 1-',
  `login_type` tinyint NOT NULL DEFAULT 1 COMMENT ': 0-1-',
  `third_account_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'ID',
  `user_role` tinyint NULL DEFAULT 0 COMMENT '用户角色：0-普通用户，1-超管，2-物业，3-业主，4-维修工',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1900000000000000001, '13800001001', '$2a$10$testpasswordhashxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 0, '2026-02-02 12:00:00', '2026-03-09 07:41:34', 0, 1, '', 3);
INSERT INTO `sys_user` VALUES (1900000000000000002, '13800001002', '$2a$10$testpasswordhashxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 0, '2026-02-03 12:00:00', '2026-03-09 07:41:34', 0, 1, '', 3);
INSERT INTO `sys_user` VALUES (1900000000000000003, '13800001003', '$2a$10$testpasswordhashxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 0, '2026-02-04 12:00:00', '2026-03-09 07:41:34', 0, 1, '', 3);
INSERT INTO `sys_user` VALUES (1900000000000000004, '13800001004', '$2a$10$testpasswordhashxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 0, '2026-02-05 12:00:00', '2026-03-09 07:41:34', 0, 1, '', 3);
INSERT INTO `sys_user` VALUES (1900000000000000005, '13800001005', '$2a$10$testpasswordhashxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 0, '2026-02-06 12:00:00', '2026-03-09 07:41:34', 0, 1, '', 3);
INSERT INTO `sys_user` VALUES (1900000000000000006, '13800001006', '$2a$10$testpasswordhashxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 0, '2026-02-07 12:00:00', '2026-03-09 07:41:34', 0, 1, '', 3);
INSERT INTO `sys_user` VALUES (1900000000000000007, '13900000001', '$2a$10$adminhashxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 0, '2026-02-08 12:00:00', '2026-03-09 07:41:34', 0, 1, '', 1);
INSERT INTO `sys_user` VALUES (1900000000000000008, '13900000002', '$2a$10$workerhashxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 0, '2026-02-09 12:00:00', '2026-03-09 07:41:34', 0, 1, '', 4);
INSERT INTO `sys_user` VALUES (2020751400691752961, '15538020575', 'c9ceacaa051e1962c1b475049bb86462', 0, '2026-02-09 14:47:32', '2026-03-09 07:41:34', 0, 1, '', 0);
INSERT INTO `sys_user` VALUES (2030909853246599170, '12345687', '$2a$10$mNnzjyElsxqsW2jKetuKluKP93ckziBTDhYoEaAN8jrHTbYJIX4G6', 0, '2026-03-09 15:33:36', '2026-03-09 15:33:58', 0, 1, '', 1);

-- ----------------------------
-- Table structure for sys_user_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_info`;
CREATE TABLE `sys_user_info`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT 'sys_user.id',
  `username` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT '',
  `avatar` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'URL',
  `real_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `gender` tinyint NULL DEFAULT 0 COMMENT ': 0-, 1-, 2-',
  `birthday` datetime NULL DEFAULT NULL,
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT ': 0-, 1-',
  `user_role` int NOT NULL DEFAULT 0 COMMENT '0  1 ',
  `ip` json NOT NULL COMMENT 'ip',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_info
-- ----------------------------
INSERT INTO `sys_user_info` VALUES (1900000000000000009, 1900000000000000001, 'user_1001', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png', '张三', 1, '1995-01-01 00:00:00', 0, 3, '[\"127.0.0.1\"]', '2026-03-09 07:32:27', '2026-02-12 12:00:00');
INSERT INTO `sys_user_info` VALUES (1900000000000000010, 1900000000000000002, 'user_1002', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png', '李四', 1, '1995-01-01 00:00:00', 0, 3, '[\"127.0.0.1\"]', '2026-03-09 07:32:27', '2026-02-13 12:00:00');
INSERT INTO `sys_user_info` VALUES (1900000000000000011, 1900000000000000003, 'user_1003', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png', '王五', 1, '1995-01-01 00:00:00', 0, 3, '[\"127.0.0.1\"]', '2026-03-09 07:32:27', '2026-02-14 12:00:00');
INSERT INTO `sys_user_info` VALUES (1900000000000000012, 1900000000000000004, 'user_1004', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png', '赵六', 1, '1995-01-01 00:00:00', 0, 3, '[\"127.0.0.1\"]', '2026-03-09 07:32:27', '2026-02-15 12:00:00');
INSERT INTO `sys_user_info` VALUES (1900000000000000013, 1900000000000000005, 'user_1005', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png', '钱七', 1, '1995-01-01 00:00:00', 0, 3, '[\"127.0.0.1\"]', '2026-03-09 07:32:27', '2026-02-16 12:00:00');
INSERT INTO `sys_user_info` VALUES (1900000000000000014, 1900000000000000006, 'user_1006', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png', '孙八', 1, '1995-01-01 00:00:00', 0, 3, '[\"127.0.0.1\"]', '2026-03-09 07:32:27', '2026-02-17 12:00:00');
INSERT INTO `sys_user_info` VALUES (1900000000000000015, 1900000000000000007, 'user_0001', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png', '管理员', 1, '1995-01-01 00:00:00', 0, 1, '[\"127.0.0.1\"]', '2026-03-09 07:32:23', '2026-02-18 12:00:00');
INSERT INTO `sys_user_info` VALUES (1900000000000000016, 1900000000000000008, 'user_0002', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png', '维修师傅', 1, '1995-01-01 00:00:00', 0, 4, '[\"127.0.0.1\"]', '2026-03-09 07:32:20', '2026-02-19 12:00:00');
INSERT INTO `sys_user_info` VALUES (2020751400767250434, 2020751400691752961, '王铁但', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png', NULL, 0, NULL, 0, 0, '{\"firstIp\": \"192.168.200.1\", \"latestIp\": \"192.168.200.1\", \"firstRegion\": \"\", \"latestRegion\": \"\"}', '2026-03-04 09:43:57', '2026-02-09 14:47:32');
INSERT INTO `sys_user_info` VALUES (2030909853246599171, 2030909853246599170, 'A', NULL, 'A千问', 0, NULL, 0, 0, '{\"firstIp\": null, \"latestIp\": null, \"firstRegion\": null, \"latestRegion\": null}', '2026-03-09 15:33:58', '2026-03-09 15:33:36');

-- ----------------------------
-- Table structure for sys_user_message
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_message`;
CREATE TABLE `sys_user_message`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `receive_user_id` bigint NOT NULL COMMENT '接收人ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息标题',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息内容',
  `related_biz_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联业务ID (如工单ID，方便前端点击跳转)',
  `notify_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息类型 (如: REPAIR_DISPATCH)',
  `read_status` tinyint NOT NULL DEFAULT 0 COMMENT '阅读状态: 0-未读, 1-已读',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receive_user`(`receive_user_id` ASC, `read_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户个人系统消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_message
-- ----------------------------
INSERT INTO `sys_user_message` VALUES (1900000000000000064, 1900000000000000002, '报修工单已受理', '您的工单1900000000000000040已派单，维修师傅将尽快处理。', '1900000000000000040', 'REPAIR_DISPATCH', 0, '2026-03-01 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_user_message` VALUES (1900000000000000065, 1900000000000000003, '报修工单已受理', '您的工单1900000000000000041已派单，维修师傅将尽快处理。', '1900000000000000041', 'REPAIR_DISPATCH', 0, '2026-03-02 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_user_message` VALUES (1900000000000000066, 1900000000000000004, '报修工单已受理', '您的工单1900000000000000042已派单，维修师傅将尽快处理。', '1900000000000000042', 'REPAIR_DISPATCH', 0, '2026-03-03 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_user_message` VALUES (1900000000000000067, 1900000000000000005, '报修工单已受理', '您的工单1900000000000000043已派单，维修师傅将尽快处理。', '1900000000000000043', 'REPAIR_DISPATCH', 0, '2026-03-01 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_user_message` VALUES (1900000000000000068, 1900000000000000006, '报修工单已受理', '您的工单1900000000000000044已派单，维修师傅将尽快处理。', '1900000000000000044', 'REPAIR_DISPATCH', 0, '2026-03-02 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_user_message` VALUES (1900000000000000069, 1900000000000000001, '报修工单已受理', '您的工单1900000000000000045已派单，维修师傅将尽快处理。', '1900000000000000045', 'REPAIR_DISPATCH', 0, '2026-03-03 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_user_message` VALUES (1900000000000000070, 1900000000000000003, '报修工单已受理', '您的工单1900000000000000047已派单，维修师傅将尽快处理。', '1900000000000000047', 'REPAIR_DISPATCH', 0, '2026-03-01 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_user_message` VALUES (1900000000000000071, 1900000000000000004, '报修工单已受理', '您的工单1900000000000000048已派单，维修师傅将尽快处理。', '1900000000000000048', 'REPAIR_DISPATCH', 0, '2026-03-02 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_user_message` VALUES (1900000000000000072, 1900000000000000001, '物业费待缴提醒', '房屋1900000000000000021 2026-03物业费￥120.5待缴。', '1900000000000000050', 'FEE_BILL', 0, '2026-03-03 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_user_message` VALUES (1900000000000000073, 1900000000000000002, '物业费待缴提醒', '房屋1900000000000000022 2026-03物业费￥120.5待缴。', '1900000000000000052', 'FEE_BILL', 0, '2026-03-01 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_user_message` VALUES (1900000000000000074, 1900000000000000003, '物业费待缴提醒', '房屋1900000000000000023 2026-03物业费￥120.5待缴。', '1900000000000000054', 'FEE_BILL', 0, '2026-03-02 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_user_message` VALUES (1900000000000000075, 1900000000000000004, '物业费待缴提醒', '房屋1900000000000000024 2026-03物业费￥120.5待缴。', '1900000000000000056', 'FEE_BILL', 0, '2026-03-03 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_user_message` VALUES (1900000000000000076, 1900000000000000005, '物业费待缴提醒', '房屋1900000000000000025 2026-03物业费￥120.5待缴。', '1900000000000000058', 'FEE_BILL', 0, '2026-03-01 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_user_message` VALUES (1900000000000000077, 1900000000000000006, '物业费待缴提醒', '房屋1900000000000000026 2026-03物业费￥120.5待缴。', '1900000000000000060', 'FEE_BILL', 0, '2026-03-02 12:00:00', '2026-03-03 12:00:00', 0);
INSERT INTO `sys_user_message` VALUES (2029492114040713218, 1900000000000000008, '新工单派发提醒', '您有一个新的报修工单待处理，单号：1900000000000000043', '1900000000000000043', 'REPAIR_DISPATCH', 0, '2026-03-05 09:40:00', '2026-03-05 09:40:00', 0);
INSERT INTO `sys_user_message` VALUES (2030832241811873793, 1900000000000000008, '新工单派发提醒', '您有一个新的报修工单待处理，单号：1900000000000000042', '1900000000000000042', 'REPAIR_DISPATCH', 0, '2026-03-09 02:25:10', '2026-03-09 02:25:10', 0);

SET FOREIGN_KEY_CHECKS = 1;
