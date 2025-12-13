# SpringGateway - Spring Boot Hello World 项目

这是一个基于 Maven 管理的 Spring Boot 项目，包含一个简单的 Hello World 接口。

## 项目结构

```
SpringGateway/
├── pom.xml                    # Maven 配置文件
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/
│   │   │       ├── Application.java          # Spring Boot 主启动类
│   │   │       └── controller/
│   │   │           └── HelloController.java  # Hello World 控制器
│   │   └── resources/
│   │       └── application.yml               # 配置文件
│   └── test/
│       └── java/                             # 测试代码
└── README.md
```

## 技术栈

- **Spring Boot 3.2.0**
- **Java 17**
- **Maven**

## 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/Flyinsky2023/SpringGateway.git
cd SpringGateway
```

### 2. 构建项目
```bash
mvn clean package
```

### 3. 运行项目
```bash
mvn spring-boot:run
```

或者运行打包后的 JAR 文件：
```bash
java -jar target/spring-gateway-1.0.0-SNAPSHOT.jar
```

## API 接口

项目启动后，默认端口为 `8080`。

### 1. Hello World 接口
- **URL**: `GET /hello`
- **响应**: `Hello World!`

### 2. 带参数的 Hello 接口
- **URL**: `GET /hello/name?name={name}`
- **参数**: `name` (可选，默认为 "World")
- **响应**: JSON 格式
  ```json
  {
    "code": 200,
    "message": "Hello, {name}!",
    "timestamp": 1234567890123
  }
  ```

## 示例请求

### 使用 curl
```bash
# 基础 Hello World
curl http://localhost:8080/hello

# 带参数的 Hello
curl http://localhost:8080/hello/name
curl http://localhost:8080/hello/name?name=SpringBoot
```

### 使用浏览器
1. 打开浏览器访问 `http://localhost:8080/hello`
2. 访问 `http://localhost:8080/hello/name?name=YourName`

## 开发说明

### 项目配置
- **主类**: `com.example.Application`
- **配置文件**: `src/main/resources/application.yml`
- **端口**: 8080 (可在配置文件中修改)

### 依赖管理
项目使用 Maven 进行依赖管理，主要依赖包括：
- `spring-boot-starter-web`: Web 应用支持
- `spring-boot-starter-test`: 测试支持

## 许可证

MIT License
