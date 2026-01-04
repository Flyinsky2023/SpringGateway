package com.example.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * 请求日志过滤器
 */
@Component
public class RequestLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        long startTime = System.currentTimeMillis();
        
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录请求日志
            logRequest(httpRequest, httpResponse, duration);
            
            // 记录敏感操作
            logSensitiveOperations(httpRequest, httpResponse);
        }
    }

    private void logRequest(HttpServletRequest request, HttpServletResponse response, long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUri = queryString == null ? uri : uri + "?" + queryString;
        int status = response.getStatus();
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        logger.info("请求日志 - IP: {}, 方法: {}, URI: {}, 状态: {}, 耗时: {}ms, User-Agent: {}",
                clientIp, method, fullUri, status, duration, userAgent);
        
        // 记录慢请求
        if (duration > 1000) { // 超过1秒的请求
            logger.warn("慢请求警告 - IP: {}, 方法: {}, URI: {}, 耗时: {}ms",
                    clientIp, method, fullUri, duration);
        }
        
        // 记录错误请求
        if (status >= 400) {
            logger.error("错误请求 - IP: {}, 方法: {}, URI: {}, 状态: {}",
                    clientIp, method, fullUri, status);
        }
    }

    private void logSensitiveOperations(HttpServletRequest request, HttpServletResponse response) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        
        // 记录敏感操作
        if (uri.contains("/api/auth/login") || uri.contains("/api/auth/register")) {
            String clientIp = getClientIp(request);
            logger.info("认证操作 - IP: {}, 方法: {}, URI: {}, 时间: {}",
                    clientIp, method, uri, new Date());
        }
        
        // 记录计算器操作
        if (uri.contains("/api/calculator")) {
            String clientIp = getClientIp(request);
            String queryString = request.getQueryString();
            logger.info("计算器操作 - IP: {}, 方法: {}, URI: {}, 参数: {}",
                    clientIp, method, uri, queryString);
        }
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
}