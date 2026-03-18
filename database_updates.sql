-- 为系统公告表添加封面图字段
ALTER TABLE `sys_notice` 
ADD COLUMN `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图 URL' AFTER `creator_id`;

-- 示例：更新已有公告的封面图（可选）
-- UPDATE `sys_notice` SET `cover_image` = '/uploads/images/default_cover.jpg' WHERE `cover_image` IS NULL;
