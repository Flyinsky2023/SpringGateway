package com.example.controller;

import com.example.config.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 模拟用户数据库
    private static final Map<String, String> userDatabase = new HashMap<>();
    
    static {
        // 初始化一个测试用户
        userDatabase.put("admin", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV8Ujq"); // 密码: admin123
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        String storedPassword = userDatabase.get(request.getUsername());
        
        if (storedPassword == null || !passwordEncoder.matches(request.getPassword(), storedPassword)) {
            return ResponseEntity.status(401).body(createErrorResponse("认证失败", "用户名或密码错误"));
        }
        
        String token = jwtUtil.generateToken(request.getUsername());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "登录成功");
        response.put("token", token);
        response.put("username", request.getUsername());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        if (userDatabase.containsKey(request.getUsername())) {
            return ResponseEntity.status(400).body(createErrorResponse("注册失败", "用户名已存在"));
        }
        
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.status(400).body(createErrorResponse("注册失败", "两次输入的密码不一致"));
        }
        
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        userDatabase.put(request.getUsername(), encodedPassword);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "注册成功");
        response.put("username", request.getUsername());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 验证 Token
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody TokenRequest request) {
        try {
            String username = jwtUtil.extractUsername(request.getToken());
            boolean isValid = jwtUtil.validateToken(request.getToken(), username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Token 验证成功");
            response.put("valid", isValid);
            response.put("username", username);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(createErrorResponse("Token 验证失败", "无效的 Token"));
        }
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody TokenRequest request) {
        try {
            String username = jwtUtil.extractUsername(request.getToken());
            boolean isValid = jwtUtil.validateToken(request.getToken(), username);
            
            if (!isValid) {
                return ResponseEntity.status(401).body(createErrorResponse("Token 刷新失败", "Token 已过期"));
            }
            
            String newToken = jwtUtil.generateToken(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Token 刷新成功");
            response.put("token", newToken);
            response.put("username", username);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(createErrorResponse("Token 刷新失败", "无效的 Token"));
        }
    }

    private Map<String, Object> createErrorResponse(String message, String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", message);
        response.put("error", error);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    // 请求对象定义
    public static class LoginRequest {
        private String username;
        private String password;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private String confirmPassword;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }

    public static class TokenRequest {
        private String token;

        // Getters and Setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}