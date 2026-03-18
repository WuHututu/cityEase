-- 积分排行榜查询性能优化索引
-- Created: 2026/3/18

-- 为房屋积分余额添加索引，提升排行榜查询性能
ALTER TABLE `pms_room` ADD INDEX `idx_points_balance` (`points_balance` DESC);

-- 为区域ID添加索引，提升楼栋统计查询性能  
ALTER TABLE `pms_room` ADD INDEX `idx_area_id` (`area_id`);

-- 为积分日志表的房屋ID添加复合索引，提升积分历史统计性能
ALTER TABLE `gov_point_log` ADD INDEX `idx_room_id_change_type` (`room_id`, `change_type`);

-- 为积分日志表添加创建时间索引，支持按时间范围统计
ALTER TABLE `gov_point_log` ADD INDEX `idx_create_time` (`create_time`);

-- 为公共区域表的层级添加索引，提升楼栋查询性能
ALTER TABLE `pms_public_area` ADD INDEX `idx_level` (`level`);

-- 为公共区域表的父级ID添加索引，支持层级查询
ALTER TABLE `pms_public_area` ADD INDEX `idx_parent_id` (`parent_id`);
