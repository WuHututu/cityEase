-- ============================================
-- 数据库迁移脚本：添加 user_role 字段
-- 作者：AI Assistant
-- 日期：2026-03-09
-- 说明：为 sys_user 表添加用户角色字段
-- ============================================

-- 检查字段是否已存在，不存在则添加
ALTER TABLE `sys_user` 
ADD COLUMN `user_role` tinyint NULL DEFAULT 0 COMMENT '用户角色：0-普通用户，1-超管，2-物业，3-业主，4-维修工' 
AFTER `third_account_id`;

-- 验证字段是否添加成功
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT, COLUMN_DEFAULT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'city_ease' 
  AND TABLE_NAME = 'sys_user' 
  AND COLUMN_NAME = 'user_role';

-- 更新现有用户的角色（可选，根据实际需求）
-- 将所有现有用户的角色设置为 0（普通用户）
UPDATE `sys_user` SET `user_role` = 0 WHERE `user_role` IS NULL;

-- 验证数据
SELECT id, phone, user_role FROM sys_user LIMIT 10;
