package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统信息控制器
 * 提供获取当前系统信息的接口
 */
@RestController
public class SystemInfoController {
    
    /**
     * 获取当前系统信息
     * @return 包含系统信息的JSON对象
     */
    @GetMapping("/system/info")
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> result = new HashMap<>();
        
        // 操作系统信息
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        result.put("osName", osBean.getName());
        result.put("osVersion", osBean.getVersion());
        result.put("osArch", osBean.getArch());
        result.put("availableProcessors", osBean.getAvailableProcessors());
        result.put("systemLoadAverage", osBean.getSystemLoadAverage());
        
        // JVM信息
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        result.put("jvmName", runtimeBean.getVmName());
        result.put("jvmVendor", runtimeBean.getVmVendor());
        result.put("jvmVersion", runtimeBean.getVmVersion());
        result.put("jvmUptime", runtimeBean.getUptime());
        result.put("jvmStartTime", runtimeBean.getStartTime());
        
        // 内存信息
        Runtime runtime = Runtime.getRuntime();
        result.put("totalMemory", runtime.totalMemory());
        result.put("freeMemory", runtime.freeMemory());
        result.put("maxMemory", runtime.maxMemory());
        result.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        
        // 计算内存使用率
        double memoryUsage = (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.totalMemory() * 100;
        result.put("memoryUsagePercentage", String.format("%.2f%%", memoryUsage));
        
        // 系统时间
        result.put("currentTimeMillis", System.currentTimeMillis());
        result.put("timestamp", System.currentTimeMillis());
        
        // 响应状态
        result.put("code", 200);
        result.put("message", "System information retrieved successfully");
        
        return result;
    }
    
    /**
     * 获取系统健康状态
     * @return 系统健康状态信息
     */
    @GetMapping("/system/health")
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Runtime runtime = Runtime.getRuntime();
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            
            // 检查内存使用率
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsage = (double) usedMemory / totalMemory * 100;
            
            // 检查系统负载
            double systemLoad = osBean.getSystemLoadAverage();
            
            result.put("status", "UP");
            result.put("memoryUsage", String.format("%.2f%%", memoryUsage));
            result.put("systemLoad", systemLoad);
            result.put("availableProcessors", osBean.getAvailableProcessors());
            result.put("timestamp", System.currentTimeMillis());
            result.put("code", 200);
            result.put("message", "System is healthy");
            
            // 添加警告信息（如果内存使用率过高）
            if (memoryUsage > 80) {
                result.put("warning", "High memory usage detected");
            }
            if (systemLoad > osBean.getAvailableProcessors() * 0.8) {
                result.put("warning", "High system load detected");
            }
            
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            result.put("code", 500);
            result.put("message", "Failed to get system health");
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return result;
    }
    
    /**
     * 获取系统环境信息
     * @return 系统环境变量信息
     */
    @GetMapping("/system/env")
    public Map<String, Object> getSystemEnvironment() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取系统属性
        Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put("java.version", System.getProperty("java.version"));
        systemProperties.put("java.home", System.getProperty("java.home"));
        systemProperties.put("java.vendor", System.getProperty("java.vendor"));
        systemProperties.put("os.name", System.getProperty("os.name"));
        systemProperties.put("os.arch", System.getProperty("os.arch"));
        systemProperties.put("os.version", System.getProperty("os.version"));
        systemProperties.put("user.name", System.getProperty("user.name"));
        systemProperties.put("user.home", System.getProperty("user.home"));
        systemProperties.put("user.dir", System.getProperty("user.dir"));
        
        result.put("systemProperties", systemProperties);
        result.put("code", 200);
        result.put("message", "System environment information retrieved successfully");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
}