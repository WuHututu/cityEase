# Redis配置清理说明

## 问题分析

原始配置中存在重复的Redis配置，可能导致配置冲突：

### 1. application.yml 中的重复配置
```yaml
# 旧的配置（已删除）
cache:
  redis:
    host: 192.168.200.147
    port: 6379
    timeout: 10s

# 新的配置（保留）
spring:
  redis:
    host: 192.168.200.147
    port: 6379
    timeout: 10000ms
    lettuce:
      pool:
        max-active: 200
        max-wait: -1ms
        max-idle: 20
        min-idle: 5
```

### 2. application-dal.yml 中的重复配置
```yaml
# 旧的配置（已删除）
spring:
  redis:
    host: ${cache.redis.host}
    port: ${cache.redis.port}
    timeout: ${cache.redis.timeout}
    lettuce:
      pool:
        max-active: 200
        max-wait: -1ms
        max-idle: 10
        min-idle: 0
```

## 清理后的配置

### application.yml（主配置文件）
```yaml
server:
  port: 8080

#数据库配置
database:
  host: 192.168.200.147
  port: 3306
  name: city_ease
  username: root
  password: 123456

# mybatis 相关统一配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0

security:
  salt: city_ease
  salt-index: 3

spring:
  config:
    import: application-dal.yml,application-login.yml
  redis:
    host: 192.168.200.147
    port: 6379
    # password:
    timeout: 10000ms
    lettuce:
      pool:
        max-active: 200
        max-wait: -1ms
        max-idle: 20
        min-idle: 5

  rabbitmq:
    host: 192.168.200.147
    port: 5672
    username: guest
    password: guest
    virtual-host: /
```

### application-dal.yml（数据访问层配置）
```yaml
spring:
  datasource:
    url: jdbc:mysql://${database.host}:${database.port}/${database.name}?TimeZone=Asia/Shanghai
    username: ${database.username}
    password: ${database.password}
```

## 配置优先级说明

Spring Boot的配置加载顺序：
1. `application.yml`（主配置文件）
2. `application-dal.yml`（通过import引入）
3. `application-login.yml`（通过import引入）

**Redis配置最终生效：**
- 位置：`application.yml`中的`spring.redis.*`配置
- 优先级：最高，因为定义在主配置文件中

## 为什么要这样配置？

### 1. 避免配置冲突
- 移除了重复的Redis配置
- 统一配置位置，便于管理

### 2. 符合Spring Boot规范
- 使用标准的`spring.redis.*`配置前缀
- 支持Spring Boot的自动配置

### 3. 配置集中化
- 所有Redis相关配置都在一个地方
- 便于维护和修改

### 4. 环境变量支持
- 保留了对环境变量的支持
- 便于不同环境的部署

## 验证配置

启动应用后，可以通过以下方式验证Redis配置：

### 1. 检查启动日志
```bash
# 查看Redis连接信息
grep -i redis application.log
```

### 2. 检查Bean注入
```java
@Autowired
private RedisTemplate<String, Object> redisTemplate;

// 如果注入成功，说明配置正确
```

### 3. 测试Redis连接
```java
@Test
public void testRedisConnection() {
    redisTemplate.opsForValue().set("test", "value");
    String value = (String) redisTemplate.opsForValue().get("test");
    assertEquals("value", value);
}
```

## 注意事项

1. **配置文件顺序**：确保`application.yml`中的`spring.config.import`正确引用其他配置文件
2. **配置覆盖**：后加载的配置会覆盖先加载的同名配置
3. **环境变量**：生产环境中建议使用环境变量覆盖敏感配置
4. **连接池配置**：根据实际需求调整连接池参数

## 总结

通过这次配置清理：
- ✅ 移除了重复的Redis配置
- ✅ 统一了配置位置
- ✅ 符合Spring Boot最佳实践
- ✅ 便于维护和部署

现在Redis配置清晰且无冲突，应用应该可以正常启动和运行。
