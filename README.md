# Spring Gateway

一个基于 Spring Boot 的网关项目，提供计算器、系统信息查询等功能。

## 项目功能

### 1. 计算器功能
- 基本运算：加、减、乘、除
- 高级运算：幂运算、平方根
- 批量运算接口

### 2. 系统信息查询
- 系统基本信息
- JVM 信息
- 内存使用情况
- 健康检查

### 3. Neofetch 风格系统信息
- 类似 Linux neofetch 的格式化输出
- JSON 格式的系统信息

## 安全特性

### 1. 认证与授权
- **JWT 认证**：基于 Token 的无状态认证
- **Spring Security**：全面的安全框架
- **角色控制**：基于角色的访问控制
- **密码加密**：BCrypt 密码加密

### 2. API 安全
- **参数验证**：使用 Jakarta Validation API
- **输入过滤**：防止 SQL 注入和 XSS 攻击
- **速率限制**：防止暴力破解和 DDoS 攻击
- **CORS 配置**：跨域资源共享安全配置

### 3. 数据安全
- **敏感数据保护**：密码等敏感信息加密存储
- **请求日志**：完整的请求审计日志
- **异常处理**：统一的异常处理机制

## 技术栈

- **Spring Boot 3.2.0**
- **Spring Security** - 安全框架
- **JWT (JSON Web Token)** - 认证机制
- **Redis** - 缓存和分布式锁
- **Redisson** - Redis 客户端
- **Jakarta Validation** - 参数验证
- **Maven** - 项目管理

## 快速开始

### 1. 环境要求
- Java 17+
- Maven 3.6+
- Redis 6.0+

### 2. 安装依赖
```bash
mvn clean install
```

### 3. 配置应用
修改 `src/main/resources/application.yml`：
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    # 其他配置...
```

### 4. 运行应用
```bash
mvn spring-boot:run
```

## API 文档

### 认证接口

#### 1. 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

#### 2. 用户注册
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "confirmPassword": "password123"
}
```

### 受保护的接口（需要认证）

所有以下接口都需要在请求头中添加：
```
Authorization: Bearer <your-jwt-token>
```

#### 1. 计算器接口
```http
GET /api/calculator/subtract?a=10&b=5
GET /api/calculator/multiply?a=10&b=5
GET /api/calculator/divide?a=10&b=5
GET /api/calculator/power?base=2&exponent=3
GET /api/calculator/sqrt?number=16
```

#### 2. 系统信息接口
```http
GET /api/system/info
GET /api/system/health
GET /api/system/env
GET /api/system/properties
```

#### 3. Neofetch 接口
```http
GET /api/neofetch/info
GET /api/neofetch/json
```

### 公开接口（无需认证）

#### 1. Hello 接口
```http
GET /api/hello
GET /api/hello/name?name=World
```

## 安全配置

### 1. 默认用户
- 用户名：`admin`
- 密码：`admin123`

### 2. JWT 配置
- 密钥：`mySuperSecretKeyThatIsAtLeast32BytesLong123456`
- 有效期：24小时

### 3. CORS 配置
- 允许的源：`http://localhost:3000`, `http://localhost:8080`
- 允许的方法：GET, POST, PUT, DELETE, OPTIONS
- 允许的头部：所有

## 开发指南

### 1. 添加新的受保护接口
```java
@RestController
@RequestMapping("/api/your-api")
public class YourController {
    
    @GetMapping("/endpoint")
    public ResponseEntity<?> yourEndpoint() {
        // 需要认证的接口
        return ResponseEntity.ok("Your response");
    }
}
```

### 2. 添加参数验证
```java
@GetMapping("/validate")
public ResponseEntity<?> validateEndpoint(
        @RequestParam @NotNull(message = "参数不能为空") String param,
        @RequestParam @Min(1) @Max(100) Integer number) {
    // 参数会自动验证
    return ResponseEntity.ok("Validated");
}
```

### 3. 异常处理
系统已经配置了全局异常处理器，会自动处理：
- 参数验证异常
- 认证异常
- 权限异常
- 业务异常

## 部署说明

### 1. 生产环境配置
- 修改 JWT 密钥
- 配置 Redis 密码
- 启用 HTTPS
- 配置防火墙规则

### 2. 监控建议
- 启用 Spring Boot Actuator
- 配置日志收集
- 设置告警规则

## 许可证

MIT License

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 创建 Pull Request

## 联系方式

如有问题，请创建 Issue 或联系项目维护者。