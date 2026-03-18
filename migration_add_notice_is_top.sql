-- ============================================
-- 数据库迁移脚本：添加 sys_notice 表的 is_top 字段
-- 作者：AI Assistant
-- 日期：2026-03-09
-- 说明：为 sys_notice 表添加置顶字段
-- ============================================

-- 检查字段是否已存在，不存在则添加
ALTER TABLE `sys_notice` 
ADD COLUMN IF NOT EXISTS `is_top` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是' 
AFTER `status`;

-- 如果 IF NOT EXISTS 不支持，使用以下替代方案（注释掉上面的 ALTER，使用下面的）
-- ALTER TABLE `sys_notice` 
-- ADD COLUMN `is_top` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是' 
-- AFTER `status`;

-- 创建索引（如果不存在）
CREATE INDEX IF NOT EXISTS `idx_is_top` ON `sys_notice`(`is_top`);

-- 验证字段是否添加成功
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'city_ease' 
  AND TABLE_NAME = 'sys_notice' 
  AND COLUMN_NAME = 'is_top';

-- 查看表结构
DESC sys_notice;
