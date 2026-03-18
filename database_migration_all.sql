-- ============================================
-- CityEase 项目数据库完整迁移脚本
-- 作者：AI Assistant
-- 日期：2026-03-09
-- 说明：修复所有缺失的字段
-- ============================================

USE `city_ease`;

-- ============================================
-- 1. 为 sys_notice 表添加 is_top 字段
-- ============================================
-- MySQL 8.0+ 支持 IF NOT EXISTS
ALTER TABLE `sys_notice` 
ADD COLUMN IF NOT EXISTS `is_top` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是' 
AFTER `status`;

-- 如果上面的语句报错（MySQL 版本不支持 IF NOT EXISTS），请使用以下语句：
-- ALTER TABLE `sys_notice` 
-- ADD COLUMN `is_top` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是' 
-- AFTER `status`;

-- 创建索引
CREATE INDEX IF NOT EXISTS `idx_is_top` ON `sys_notice`(`is_top`);


-- ============================================
-- 2. 为 sys_user 表添加 user_role 字段
-- ============================================
ALTER TABLE `sys_user` 
ADD COLUMN IF NOT EXISTS `user_role` tinyint NULL DEFAULT 0 COMMENT '用户角色：0-普通用户，1-超管，2-物业，3-业主，4-维修工' 
AFTER `third_account_id`;

-- 如果上面的语句报错，请使用以下语句：
-- ALTER TABLE `sys_user` 
-- ADD COLUMN `user_role` tinyint NULL DEFAULT 0 COMMENT '用户角色：0-普通用户，1-超管，2-物业，3-业主，4-维修工' 
-- AFTER `third_account_id`;

-- 更新现有用户的角色为默认值 0（普通用户）
UPDATE `sys_user` SET `user_role` = 0 WHERE `user_role` IS NULL;


-- ============================================
-- 3. 验证字段是否添加成功
-- ============================================
SELECT '===== sys_notice 表结构 =====' AS '';
DESC sys_notice;

SELECT '===== sys_user 表结构 =====' AS '';
DESC sys_user;

SELECT '===== 验证 sys_notice.is_top 字段 =====' AS '';
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'city_ease' 
  AND TABLE_NAME = 'sys_notice' 
  AND COLUMN_NAME = 'is_top';

SELECT '===== 验证 sys_user.user_role 字段 =====' AS '';
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'city_ease' 
  AND TABLE_NAME = 'sys_user' 
  AND COLUMN_NAME = 'user_role';

SELECT '===== 迁移完成 =====' AS '';
