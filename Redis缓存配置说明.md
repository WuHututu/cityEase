# Redis缓存配置说明

## 概述

为了提升积分排行榜的查询性能，我们已经实现了Redis缓存机制。缓存系统采用多级缓存策略，确保数据一致性和系统性能。

## 缓存架构

### 1. 缓存层级
```
请求 → Redis缓存 → 数据库查询 → 更新缓存
```

### 2. 缓存Key设计
- `point:ranking:room` - 房屋积分排行榜（ZSet）
- `point:ranking:room:detail:{roomId}` - 房屋详细信息（String）
- `point:stats:building` - 楼栋积分统计（List）
- `point:ranking:building:room:{areaId}` - 楼栋内房屋排行（List）
- `point:cache:status` - 缓存状态信息（String）

### 3. 缓存过期时间
- 排行榜数据：30分钟
- 楼栋统计：30分钟
- 缓存状态：24小时

## 自动刷新机制

### 1. 定时任务
```java
@Scheduled(cron = "0 */10 * * * ?") // 每10分钟执行
public void refreshAllRankings() {
    refreshRoomRanking();
    refreshBuildingStats();
}
```

### 2. 实时更新
当积分发生变动时，立即更新相关缓存：
```java
// 在GovPointServiceImpl.changePoints()方法中
cacheService.updateRoomPoints(roomId, newRoom.getPointsBalance());
```

## 缓存策略

### 1. 读取策略
- **优先从缓存读取**：提升查询性能
- **缓存降级**：缓存异常时自动降级到数据库查询
- **缓存预热**：缓存为空时自动触发刷新

### 2. 写入策略
- **原子操作**：使用Redis ZSet确保排名一致性
- **批量更新**：定时任务批量刷新全量数据
- **增量更新**：积分变动时增量更新相关缓存

### 3. 一致性保证
- **最终一致性**：通过定时任务保证数据最终一致
- **实时性**：积分变动时立即更新缓存
- **容错性**：缓存失败不影响主业务流程

## 性能优势

### 1. 查询性能提升
- **排行榜查询**：从O(n log n)降低到O(log n)
- **并发处理**：Redis天然支持高并发
- **内存访问**：比磁盘数据库快10-100倍

### 2. 数据库保护
- **减少查询**：避免频繁的复杂排序查询
- **降低负载**：保护数据库不被排行榜查询拖垮
- **提升稳定性**：数据库故障时排行榜仍可访问

## 管理接口

### 1. 缓存管理API
```bash
# 刷新所有缓存
POST /admin/gov/cache/refresh/all

# 刷新房屋排行榜缓存
POST /admin/gov/cache/refresh/room

# 刷新楼栋统计缓存
POST /admin/gov/cache/refresh/building

# 清除指定房屋缓存
POST /admin/gov/cache/remove/room?roomId=xxx

# 获取缓存状态
GET /admin/gov/cache/status
```

### 2. 前端管理界面
- **缓存状态监控**：实时显示缓存状态
- **手动刷新**：支持手动触发缓存刷新
- **缓存清理**：支持清理指定缓存项

## Redis配置要求

### 1. 基本配置
```properties
# Redis连接配置
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=your_password
spring.redis.database=0

# 连接池配置
spring.redis.jedis.pool.max-active=200
spring.redis.jedis.pool.max-idle=20
spring.redis.jedis.pool.min-idle=5
```

### 2. 序列化配置
```properties
# JSON序列化配置
spring.redis.serializer.default-serializer=org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
spring.redis.serializer.key-serializer=org.springframework.data.redis.serializer.StringRedisSerializer
```

## 监控和告警

### 1. 缓存命中率监控
```java
// 在缓存服务中添加监控
@EventListener
public void handleCacheHit(CacheHitEvent event) {
    // 记录缓存命中
    metrics.incrementCounter("cache.hit", event.getKey());
}

@EventListener  
public void handleCacheMiss(CacheMissEvent event) {
    // 记录缓存未命中
    metrics.incrementCounter("cache.miss", event.getKey());
}
```

### 2. 性能指标
- **缓存命中率**：目标 > 90%
- **平均响应时间**：目标 < 50ms
- **缓存更新频率**：每10分钟一次

## 故障处理

### 1. 缓存故障降级
```java
public List<PointRankingVO> getRoomRankingFromCache(Integer limit) {
    try {
        // 尝试从缓存获取
        return getCachedRanking(limit);
    } catch (Exception e) {
        // 缓存异常时降级到数据库查询
        log.error("缓存获取失败，降级到数据库查询", e);
        return rankingService.getRoomPointRanking(limit);
    }
}
```

### 2. 数据一致性检查
```java
// 定期检查缓存与数据库的一致性
@Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
public void checkDataConsistency() {
    // 检查缓存数据与数据库的一致性
    // 发现不一致时记录日志并刷新缓存
}
```

## 优化建议

### 1. 短期优化
- **缓存预热**：系统启动时预加载热点数据
- **布隆过滤器**：防止缓存穿透
- **本地缓存**：减少Redis网络开销

### 2. 长期优化
- **分片缓存**：按楼栋分片存储，提升扩展性
- **读写分离**：使用Redis Cluster实现读写分离
- **智能预取**：根据用户行为预取相关数据

## 注意事项

1. **内存使用**：监控Redis内存使用情况，避免内存溢出
2. **网络延迟**：考虑Redis网络延迟对性能的影响
3. **数据备份**：定期备份Redis数据，防止数据丢失
4. **安全配置**：配置Redis密码和访问控制
5. **版本兼容**：确保Redis版本与Spring Boot兼容

通过以上Redis缓存机制，积分排行榜系统的性能将得到显著提升，同时保证数据的一致性和系统的稳定性。
