# RedisTemplate使用说明

## 1. 泛型解释

`RedisTemplate<String, Object>` 的含义：
- **第一个泛型 `<String>`**：Redis Key的数据类型，我们使用String作为Key
- **第二个泛型 `<Object>`**：Redis Value的数据类型，我们使用Object可以存储任何类型的数据

## 2. 为什么使用泛型

**类型安全**：
- 编译时检查类型，避免运行时错误
- IDE可以提供更好的代码提示

**灵活性**：
- Key通常是字符串，便于识别和管理
- Value使用Object可以存储复杂对象（如List、自定义对象等）

## 3. 常用操作示例

### 字符串操作
```java
// 存储字符串
redisTemplate.opsForValue().set("key", "value");

// 获取字符串
String value = (String) redisTemplate.opsForValue().get("key");
```

### 对象操作
```java
// 存储对象
PointRankingVO ranking = new PointRankingVO();
redisTemplate.opsForValue().set("ranking:1", ranking);

// 获取对象
PointRankingVO ranking = (PointRankingVO) redisTemplate.opsForValue().get("ranking:1");
```

### ZSet操作（排行榜）
```java
// 添加到排行榜
redisTemplate.opsForZSet().add("ranking", roomId, score);

// 获取排行榜前10名
Set<Object> top10 = redisTemplate.opsForZSet().reverseRange("ranking", 0, 9);

// 获取某个用户的排名
Long rank = redisTemplate.opsForZSet().reverseRank("ranking", userId);
```

### Hash操作
```java
// 存储Hash
redisTemplate.opsForHash().put("user:1", "name", "张三");
redisTemplate.opsForHash().put("user:1", "age", "25");

// 获取Hash
String name = (String) redisTemplate.opsForHash().get("user:1", "name");
```

## 4. 序列化说明

我们配置的序列化器：
- **Key**：StringRedisSerializer（字符串序列化）
- **Value**：GenericJackson2JsonRedisSerializer（JSON序列化）

这意味着：
- Key在Redis中存储为普通字符串
- Value在Redis中存储为JSON字符串
- 读取时自动反序列化为Java对象

## 5. 注意事项

1. **类型转换**：从Redis获取数据时需要类型转换
2. **null处理**：Redis中没有数据时返回null
3. **过期时间**：可以设置key的过期时间
4. **连接池**：配置了连接池参数，避免频繁创建连接

## 6. 错误排查

如果遇到Bean注入问题：
1. 确认Redis配置正确
2. 确认Redis服务启动
3. 检查网络连接
4. 查看日志错误信息
