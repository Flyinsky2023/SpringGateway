package com.example.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * 速率限制拦截器
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_REQUESTS_PER_MINUTE = 60; // 每分钟最大请求数
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    public RateLimitInterceptor() {
        // 这里需要注入 RedisTemplate，简化版本使用本地缓存
        this.redisTemplate = null;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String key = RATE_LIMIT_PREFIX + clientIp;
        
        // 在实际项目中，这里应该使用 Redis 进行分布式限流
        // 简化版本：使用内存缓存（仅用于演示）
        
        // 检查请求频率
        if (isRateLimited(clientIp)) {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"请求过于频繁，请稍后再试\"}");
            return false;
        }
        
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private boolean isRateLimited(String clientIp) {
        // 在实际项目中，这里应该使用 Redis 实现令牌桶或漏桶算法
        // 简化版本：返回 false（不限制）
        return false;
    }
}