# Spring Gateway

一个基于 Spring Boot 的网关项目，集成了 Redis 缓存解决方案。

## 项目特性

- Spring Boot 3.2.0
- Java 17
- Redis 缓存支持
- 分布式锁（Redisson）
- 缓存雪崩和击穿解决方案

## 新增的 Redis 缓存工具类

项目新增了一套完整的 Redis 缓存工具类，用于解决缓存雪崩和缓存击穿问题。

### 主要功能

#### 1. 缓存击穿解决方案
- **互斥锁（Mutex Lock）**：使用 Redisson 分布式锁，防止大量请求同时访问数据库
- **双重检查**：获取锁后再次检查缓存，避免重复更新
- **锁超时控制**：防止死锁，自动释放

#### 2. 缓存雪崩解决方案
- **随机过期时间**：在基础过期时间上增加随机偏移，分散缓存失效时间
- **缓存预热**：在系统启动或低峰期预先加载热点数据
- **多级缓存**：Redis + 本地缓存组合，提高访问速度

#### 3. 热点数据优化
- **后台刷新**：热点数据永不过期，后台线程定期更新
- **异步检查**：非阻塞式缓存刷新，不影响主流程

### 工具类说明

#### CacheUtils
核心缓存工具类，提供以下方法：

1. **getWithMutex()** - 使用互斥锁解决缓存击穿
2. **getWithRandomExpire()** - 使用随机过期时间解决缓存雪崩
3. **getWithBackgroundRefresh()** - 热点数据后台刷新
4. **getWithMultiLevel()** - 多级缓存方案
5. **preheatCache()** - 缓存预热
6. **deleteCache()** - 删除缓存

#### RedisConfig
Redis 配置类：
- 配置 Redis 连接工厂
- 配置 RedisTemplate（支持 JSON 序列化）
- 配置 Redisson 客户端（分布式锁）

### 使用示例

#### API 端点

1. **互斥锁方案**
   ```
   GET /api/cache/mutex/{id}
   ```

2. **随机过期时间方案**
   ```
   GET /api/cache/random/{id}
   ```

3. **热点数据方案**
   ```
   GET /api/cache/hot/{id}
   ```

4. **多级缓存方案**
   ```
   GET /api/cache/multi-level/{id}
   ```

5. **缓存预热**
   ```
   POST /api/cache/preheat
   ```

6. **删除缓存**
   ```
   DELETE /api/cache/{key}
   ```

### 配置说明

#### application.yml
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0

cache:
  default-timeout: 300          # 默认缓存过期时间（秒）
  hot-refresh-interval: 1800    # 热点数据刷新间隔（秒）
  preheat-enabled: true         # 是否启用缓存预热
```

#### pom.xml 依赖
```xml
<!-- Spring Boot Redis Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Redis 客户端 Lettuce -->
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
</dependency>

<!-- 分布式锁 Redisson -->
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.23.5</version>
</dependency>
```

### 缓存问题解决方案对比

| 问题类型 | 解决方案 | 实现方法 | 适用场景 |
|---------|---------|---------|---------|
| 缓存击穿 | 互斥锁 | `getWithMutex()` | 热点数据，并发访问高 |
| 缓存雪崩 | 随机过期时间 | `getWithRandomExpire()` | 批量缓存，同时失效风险 |
| 热点数据 | 后台刷新 | `getWithBackgroundRefresh()` | 配置数据，访问频繁 |
| 性能优化 | 多级缓存 | `getWithMultiLevel()` | 超高并发，低延迟要求 |

### 最佳实践

1. **选择合适的方案**
   - 热点数据：使用互斥锁 + 后台刷新
   - 批量数据：使用随机过期时间
   - 配置数据：使用多级缓存

2. **监控与调优**
   - 监控缓存命中率
   - 调整过期时间策略
   - 定期清理无效缓存

3. **容错处理**
   - Redis 连接失败降级
   - 锁获取超时处理
   - 异步刷新异常捕获

### 运行要求

1. 安装 Redis 服务器（默认 localhost:6379）
2. Java 17 或更高版本
3. Maven 3.6+

### 启动项目

```bash
# 启动 Redis
redis-server

# 编译项目
mvn clean package

# 运行项目
java -jar target/spring-gateway-1.0.0-SNAPSHOT.jar
```

### 测试缓存功能

访问以下端点测试缓存功能：

```bash
# 测试互斥锁方案
curl http://localhost:8080/api/cache/mutex/123

# 测试随机过期时间方案
curl http://localhost:8080/api/cache/random/456

# 测试缓存预热
curl -X POST http://localhost:8080/api/cache/preheat
```

## 项目结构

```
src/main/java/com/example/
├── utils/
│   ├── CacheUtils.java          # 缓存工具类
│   ├── RedisConfig.java         # Redis 配置
│   └── CacheExampleController.java # 使用示例
├── controller/
├── service/
├── model/
└── Application.java
```

## 许可证

MIT License