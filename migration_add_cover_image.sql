-- 为 sys_notice 表添加 cover_image 字段
-- 执行时间：2026-03-09
-- 说明：添加封面图字段，用于存储公告封面图片 URL

ALTER TABLE `sys_notice` 
ADD COLUMN `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图 URL' 
AFTER `creator_id`;
