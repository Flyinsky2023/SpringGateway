# Spring Gateway 项目编码规范

## 1. 变量命名规范

### 1.1 常量命名（全大写+下划线分割）
所有常量（static final 变量）必须使用全大写字母和下划线分割的命名方式：

```java
// ✅ 正确示例
public static final String SECRET_KEY = "mySuperSecretKeyThatIsAtLeast32BytesLong123456";
public static final long EXPIRATION_TIME = 86400000; // 24小时
public static final int MAX_REQUESTS_PER_MINUTE = 60;
public static final String RATE_LIMIT_PREFIX = "rate_limit:";
public static final String TOKEN_PREFIX = "Bearer ";
public static final String HEADER_STRING = "Authorization";
public static final String DEFAULT_USERNAME = "admin";
public static final String DEFAULT_PASSWORD = "admin123";

// ❌ 错误示例
public static final String secretKey = "key";  // 不是全大写
public static final String SecretKey = "key";  // 不是全大写
public static final String SECRETKEY = "key";  // 没有下划线分割
```

### 1.2 枚举常量命名
枚举值也必须使用全大写+下划线分割：

```java
// ✅ 正确示例
public enum UserRole {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_SUPER_ADMIN
}

public enum ErrorCode {
    INVALID_PARAMETER,
    UNAUTHORIZED_ACCESS,
    RESOURCE_NOT_FOUND
}
```

### 1.3 配置属性命名
配置文件中的属性键名也应使用全大写+下划线分割：

```yaml
# ✅ 正确示例
spring:
  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD}
    database: 0

app:
  version: 1.0.0
  max_retry_count: 3
  timeout_seconds: 30

# 环境变量示例
REDIS_HOST: localhost
REDIS_PORT: 6379
JWT_SECRET_KEY: your-secret-key-here
API_RATE_LIMIT: 100
```

### 1.4 数据库表名和字段名
数据库相关的常量也应遵循此规范：

```java
// ✅ 正确示例
public static final String TABLE_USERS = "users";
public static final String COLUMN_USER_ID = "user_id";
public static final String COLUMN_USERNAME = "username";
public static final String COLUMN_CREATED_AT = "created_at";
public static final String COLUMN_UPDATED_AT = "updated_at";
```

## 2. 其他变量命名规范

### 2.1 类成员变量（实例变量）
使用驼峰命名法，以 `m` 开头表示成员变量（可选）：

```java
// ✅ 正确示例
private String username;
private String password;
private int retryCount;
private LocalDateTime createdAt;

// 或者使用 m 前缀（可选）
private String mUsername;
private String mPassword;
```

### 2.2 方法参数和局部变量
使用驼峰命名法：

```java
// ✅ 正确示例
public void processUserData(String userName, int maxRetryCount) {
    String formattedName = formatUserName(userName);
    int currentRetry = 0;
    
    while (currentRetry < maxRetryCount) {
        // 处理逻辑
        currentRetry++;
    }
}
```

### 2.3 集合类型变量
集合变量名应反映其内容：

```java
// ✅ 正确示例
private List<User> userList;
private Map<String, User> userMap;
private Set<String> permissionSet;
private Queue<Task> taskQueue;
```

## 3. 特殊场景

### 3.1 缓存键命名
缓存键应使用全大写+下划线分割，并包含命名空间：

```java
// ✅ 正确示例
public static final String CACHE_KEY_USER_PREFIX = "user:";
public static final String CACHE_KEY_PRODUCT_PREFIX = "product:";
public static final String CACHE_KEY_ORDER_PREFIX = "order:";
public static final String CACHE_KEY_SESSION_PREFIX = "session:";

// 使用方法
String userCacheKey = CACHE_KEY_USER_PREFIX + userId;
String productCacheKey = CACHE_KEY_PRODUCT_PREFIX + productId;
```

### 3.2 锁键命名
分布式锁的键名也应遵循此规范：

```java
// ✅ 正确示例
public static final String LOCK_KEY_USER_PREFIX = "lock:user:";
public static final String LOCK_KEY_ORDER_PREFIX = "lock:order:";
public static final String LOCK_KEY_INVENTORY_PREFIX = "lock:inventory:";
```

### 3.3 错误码和消息常量
错误相关的常量也应使用全大写+下划线分割：

```java
// ✅ 正确示例
public static final String ERROR_CODE_INVALID_TOKEN = "INVALID_TOKEN";
public static final String ERROR_MESSAGE_TOKEN_EXPIRED = "Token has expired";
public static final String ERROR_CODE_PERMISSION_DENIED = "PERMISSION_DENIED";
public static final String ERROR_MESSAGE_ACCESS_DENIED = "Access denied";
```

## 4. 项目现有常量检查清单

基于当前项目代码，需要更新的常量：

### 4.1 JwtUtil.java 中的常量
```java
// 当前代码
private static final String SECRET_KEY = "mySuperSecretKeyThatIsAtLeast32BytesLong123456";
private static final long EXPIRATION_TIME = 86400000; // 24小时

// ✅ 已符合规范
```

### 4.2 JwtAuthenticationFilter.java 中的常量
```java
// 当前代码
private static final String SECRET_KEY = "mySuperSecretKeyThatIsAtLeast32BytesLong123456";
private static final String TOKEN_PREFIX = "Bearer ";
private static final String HEADER_STRING = "Authorization";

// ✅ 已符合规范
```

### 4.3 RateLimitInterceptor.java 中的常量
```java
// 当前代码
private static final int MAX_REQUESTS_PER_MINUTE = 60;
private static final String RATE_LIMIT_PREFIX = "rate_limit:";

// ✅ 已符合规范
```

### 4.4 CacheUtils.java 中的常量
```java
// 当前代码
private static final Random RANDOM = new Random();

// ✅ 已符合规范
```

## 5. 代码审查要点

在代码审查时，请检查以下内容：

1. **所有 static final 变量**：是否使用全大写+下划线分割？
2. **枚举值**：是否使用全大写+下划线分割？
3. **配置文件键名**：是否使用全大写+下划线分割？
4. **缓存键和锁键**：是否使用全大写+下划线分割并有清晰的命名空间？
5. **错误码和消息**：是否使用全大写+下划线分割？

## 6. 自动检查工具

建议在项目中配置以下检查工具：

1. **Checkstyle**：配置常量命名规则检查
2. **SonarQube**：设置代码质量规则
3. **IDE 配置**：在 IDE 中设置代码模板和实时检查

## 7. 示例代码

### 7.1 正确的常量定义
```java
public class SecurityConstants {
    
    // JWT 相关常量
    public static final String JWT_SECRET_KEY = "your-secret-key-here";
    public static final long JWT_EXPIRATION_TIME = 86400000L; // 24小时
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final String JWT_HEADER_STRING = "Authorization";
    
    // 缓存相关常量
    public static final String CACHE_KEY_USER_PREFIX = "user:";
    public static final String CACHE_KEY_SESSION_PREFIX = "session:";
    public static final long CACHE_EXPIRATION_TIME = 3600L; // 1小时
    
    // 速率限制常量
    public static final int RATE_LIMIT_MAX_REQUESTS = 100;
    public static final long RATE_LIMIT_WINDOW_SECONDS = 60L;
    public static final String RATE_LIMIT_PREFIX = "rate_limit:";
    
    // 数据库相关常量
    public static final String TABLE_NAME_USERS = "users";
    public static final String COLUMN_NAME_USERNAME = "username";
    public static final String COLUMN_NAME_EMAIL = "email";
    
    // 错误码常量
    public static final String ERROR_CODE_UNAUTHORIZED = "UNAUTHORIZED";
    public static final String ERROR_MESSAGE_INVALID_CREDENTIALS = "Invalid credentials";
    
    // 私有构造函数防止实例化
    private SecurityConstants() {
        throw new IllegalStateException("Utility class");
    }
}
```

### 7.2 配置文件示例
```yaml
# application.yml
app:
  security:
    jwt:
      secret_key: ${JWT_SECRET_KEY}
      expiration_hours: 24
    rate_limit:
      max_requests: ${API_RATE_LIMIT:100}
      window_seconds: 60
    
  cache:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: ${REDIS_DATABASE:0}
    
  database:
    connection_pool:
      max_size: ${DB_MAX_POOL_SIZE:20}
      min_idle: ${DB_MIN_IDLE:5}
      timeout_ms: ${DB_TIMEOUT_MS:30000}
```

## 8. 总结

遵循全大写+下划线分割的常量命名规范有以下好处：

1. **可读性**：一眼就能识别出常量
2. **一致性**：整个项目保持统一的命名风格
3. **维护性**：便于查找和修改常量
4. **安全性**：避免意外修改常量值
5. **团队协作**：新成员能快速理解代码规范

请所有开发人员严格遵守此规范，并在代码审查时重点关注常量命名。