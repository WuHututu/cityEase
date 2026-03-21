-- gov 模块补充迁移脚本
-- 1) 积分流水索引优化
ALTER TABLE `gov_point_log`
    ADD INDEX `idx_user_change_time` (`user_id`, `change_type`, `create_time`),
    ADD INDEX `idx_room_time` (`room_id`, `create_time`);

-- 2) 积分商品图片路径统一为 OSS 路径（示例规则）
UPDATE `gov_point_goods`
SET `image_url` = CONCAT('wuhututu/goods/', `id`, '.png')
WHERE (`image_url` IS NULL OR `image_url` = '' OR `image_url` NOT LIKE 'wuhututu/%')
  AND `is_deleted` = 0;

-- 3) 修复绑定审核状态：超出枚举值的状态回收为待审核(0)
UPDATE `pms_bind_audit`
SET `status` = 0
WHERE `status` NOT IN (0, 1, 2);
