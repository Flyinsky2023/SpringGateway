package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统信息控制器
 * 提供获取当前系统信息的接口
 */
@RestController
@RequestMapping("/api/system")
public class SystemInfoController {

    /**
     * 获取系统基本信息
     * @return 系统信息
     */
    @GetMapping("/info")
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        // 获取操作系统信息
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        
        // 系统基本信息
        systemInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        systemInfo.put("systemName", osBean.getName());
        systemInfo.put("systemVersion", osBean.getVersion());
        systemInfo.put("systemArch", osBean.getArch());
        systemInfo.put("availableProcessors", osBean.getAvailableProcessors());
        systemInfo.put("systemLoadAverage", osBean.getSystemLoadAverage());
        
        // JVM信息
        systemInfo.put("jvmName", runtimeBean.getVmName());
        systemInfo.put("jvmVendor", runtimeBean.getVmVendor());
        systemInfo.put("jvmVersion", runtimeBean.getVmVersion());
        systemInfo.put("jvmUptime", runtimeBean.getUptime());
        systemInfo.put("jvmStartTime", runtimeBean.getStartTime());
        
        // 内存信息
        Runtime runtime = Runtime.getRuntime();
        systemInfo.put("totalMemory", runtime.totalMemory());
        systemInfo.put("freeMemory", runtime.freeMemory());
        systemInfo.put("maxMemory", runtime.maxMemory());
        systemInfo.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        
        // 计算内存使用率
        double memoryUsage = ((double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.totalMemory()) * 100;
        systemInfo.put("memoryUsagePercentage", String.format("%.2f%%", memoryUsage));
        
        // 用户信息
        systemInfo.put("userName", System.getProperty("user.name"));
        systemInfo.put("userHome", System.getProperty("user.home"));
        systemInfo.put("userDir", System.getProperty("user.dir"));
        
        // Java环境信息
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("javaHome", System.getProperty("java.home"));
        systemInfo.put("javaVendor", System.getProperty("java.vendor"));
        
        // 系统属性
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        systemInfo.put("osArch", System.getProperty("os.arch"));
        
        // 响应状态
        systemInfo.put("code", 200);
        systemInfo.put("message", "System information retrieved successfully");
        
        return systemInfo;
    }

    /**
     * 获取系统健康状态
     * @return 健康状态信息
     */
    @GetMapping("/health")
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> healthInfo = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // 内存健康检查
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsage = ((double) usedMemory / maxMemory) * 100;
        
        Map<String, Object> memoryStatus = new HashMap<>();
        memoryStatus.put("usage", String.format("%.2f%%", memoryUsage));
        memoryStatus.put("status", memoryUsage > 90 ? "WARNING" : "HEALTHY");
        memoryStatus.put("maxMemory", maxMemory);
        memoryStatus.put("usedMemory", usedMemory);
        
        healthInfo.put("memory", memoryStatus);
        
        // CPU负载
        double systemLoad = osBean.getSystemLoadAverage();
        Map<String, Object> cpuStatus = new HashMap<>();
        cpuStatus.put("systemLoad", systemLoad >= 0 ? String.format("%.2f", systemLoad) : "N/A");
        cpuStatus.put("availableProcessors", osBean.getAvailableProcessors());
        cpuStatus.put("status", systemLoad > osBean.getAvailableProcessors() * 0.8 ? "WARNING" : "HEALTHY");
        
        healthInfo.put("cpu", cpuStatus);
        
        // 线程信息
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        while (rootGroup.getParent() != null) {
            rootGroup = rootGroup.getParent();
        }
        int threadCount = rootGroup.activeCount();
        
        Map<String, Object> threadStatus = new HashMap<>();
        threadStatus.put("activeThreads", threadCount);
        threadStatus.put("status", threadCount > 1000 ? "WARNING" : "HEALTHY");
        
        healthInfo.put("threads", threadStatus);
        
        // 响应状态
        healthInfo.put("code", 200);
        healthInfo.put("message", "System health check completed");
        
        return healthInfo;
    }

    /**
     * 获取系统环境变量
     * @return 环境变量信息
     */
    @GetMapping("/env")
    public Map<String, String> getEnvironmentVariables() {
        return System.getenv();
    }

    /**
     * 获取系统属性
     * @return 系统属性信息
     */
    @GetMapping("/properties")
    public Map<String, String> getSystemProperties() {
        Map<String, String> properties = new HashMap<>();
        System.getProperties().forEach((key, value) -> 
            properties.put(key.toString(), value.toString()));
        return properties;
    }
}