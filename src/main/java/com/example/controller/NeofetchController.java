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
 * Neofetch 风格系统信息控制器
 * 返回类似 Linux bash neofetch 的格式化系统信息
 */
@RestController
@RequestMapping("/api/neofetch")
public class NeofetchController {

    /**
     * 获取 neofetch 风格的系统信息
     * @return 格式化的系统信息
     */
    @GetMapping("/info")
    public String getNeofetchInfo() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        Runtime runtime = Runtime.getRuntime();
        
        // 计算内存使用率
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        double memoryUsagePercent = ((double) usedMemory / totalMemory) * 100;
        
        // 格式化内存大小
        String formatMemory = (size) -> {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        };
        
        // 构建 neofetch 风格输出
        StringBuilder neofetch = new StringBuilder();
        
        // ASCII 艺术 Logo (Spring Boot 风格)
        neofetch.append("\n");
        neofetch.append("     _____             _             ____              _   \n");
        neofetch.append("    / ____|           | |           |  _ \\            | |  \n");
        neofetch.append("   | (___  _ __   __ _| | _____ _ __| |_) | ___   ___ | |_ \n");
        neofetch.append("    \\___ \\| '_ \\ / _` | |/ / _ \\ '__|  _ < / _ \\ / _ \\| __|\n");
        neofetch.append("    ____) | |_) | (_| |   <  __/ |  | |_) | (_) | (_) | |_ \n");
        neofetch.append("   |_____/| .__/ \\__,_|_|\\_\\___|_|  |____/ \\___/ \\___/ \\__|\n");
        neofetch.append("          | |                                             \n");
        neofetch.append("          |_|                                             \n");
        neofetch.append("\n");
        neofetch.append("╔════════════════════════════════════════════════════════════════════════╗\n");
        neofetch.append("║                           SYSTEM INFORMATION                           ║\n");
        neofetch.append("╚════════════════════════════════════════════════════════════════════════╝\n");
        neofetch.append("\n");
        
        // 系统信息
        neofetch.append(String.format("  %-20s %s\n", "OS:", osBean.getName() + " " + osBean.getVersion()));
        neofetch.append(String.format("  %-20s %s\n", "Architecture:", osBean.getArch()));
        neofetch.append(String.format("  %-20s %d cores\n", "CPU:", osBean.getAvailableProcessors()));
        neofetch.append(String.format("  %-20s %.2f\n", "Load Average:", 
            osBean.getSystemLoadAverage() >= 0 ? osBean.getSystemLoadAverage() : 0.0));
        
        // Java/JVM 信息
        neofetch.append(String.format("  %-20s %s\n", "Java Version:", System.getProperty("java.version")));
        neofetch.append(String.format("  %-20s %s\n", "JVM:", runtimeBean.getVmName()));
        neofetch.append(String.format("  %-20s %s\n", "JVM Vendor:", runtimeBean.getVmVendor()));
        
        // 内存信息
        neofetch.append(String.format("  %-20s %s / %s (%.1f%%)\n", "Memory Usage:", 
            formatMemory.apply(usedMemory), 
            formatMemory.apply(totalMemory),
            memoryUsagePercent));
        neofetch.append(String.format("  %-20s %s\n", "Max Memory:", formatMemory.apply(maxMemory)));
        
        // 运行时信息
        neofetch.append(String.format("  %-20s %s\n", "Uptime:", formatUptime(runtimeBean.getUptime())));
        neofetch.append(String.format("  %-20s %s\n", "User:", System.getProperty("user.name")));
        neofetch.append(String.format("  %-20s %s\n", "Host:", getHostname()));
        
        // 时间信息
        neofetch.append(String.format("  %-20s %s\n", "Local Time:", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        
        neofetch.append("\n");
        neofetch.append("╔════════════════════════════════════════════════════════════════════════╗\n");
        neofetch.append("║                              COLOR SCHEME                              ║\n");
        neofetch.append("╚════════════════════════════════════════════════════════════════════════╝\n");
        neofetch.append("\n");
        
        // 颜色示例 (ANSI 颜色代码)
        neofetch.append("  \u001B[31m███\u001B[0m Red      \u001B[32m███\u001B[0m Green    \u001B[33m███\u001B[0m Yellow   \u001B[34m███\u001B[0m Blue\n");
        neofetch.append("  \u001B[35m███\u001B[0m Magenta  \u001B[36m███\u001B[0m Cyan     \u001B[37m███\u001B[0m White    \u001B[90m███\u001B[0m Gray\n");
        
        neofetch.append("\n");
        neofetch.append("╔════════════════════════════════════════════════════════════════════════╗\n");
        neofetch.append("║                              QUICK ACCESS                              ║\n");
        neofetch.append("╚════════════════════════════════════════════════════════════════════════╝\n");
        neofetch.append("\n");
        neofetch.append("  /api/neofetch/info     - This neofetch display\n");
        neofetch.append("  /api/system/info       - Detailed system information (JSON)\n");
        neofetch.append("  /api/system/health     - System health check\n");
        neofetch.append("  /api/hello             - Hello endpoint\n");
        neofetch.append("  /api/calculator        - Calculator endpoints\n");
        
        neofetch.append("\n");
        neofetch.append("╔════════════════════════════════════════════════════════════════════════╗\n");
        neofetch.append("║                              SPRING GATEWAY                            ║\n");
        neofetch.append("╚════════════════════════════════════════════════════════════════════════╝\n");
        neofetch.append("\n");
        neofetch.append("  Server is running on: http://localhost:8080\n");
        neofetch.append("  Spring Boot Version: 3.2.0\n");
        neofetch.append("  Java Version: 17\n");
        
        return neofetch.toString();
    }
    
    /**
     * 获取 neofetch 风格的 JSON 格式信息
     * @return JSON 格式的系统信息
     */
    @GetMapping("/json")
    public Map<String, Object> getNeofetchJson() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        Runtime runtime = Runtime.getRuntime();
        
        Map<String, Object> info = new HashMap<>();
        
        // 系统信息
        Map<String, Object> system = new HashMap<>();
        system.put("os", osBean.getName());
        system.put("version", osBean.getVersion());
        system.put("arch", osBean.getArch());
        system.put("cpuCores", osBean.getAvailableProcessors());
        system.put("loadAverage", osBean.getSystemLoadAverage());
        info.put("system", system);
        
        // Java 信息
        Map<String, Object> java = new HashMap<>();
        java.put("version", System.getProperty("java.version"));
        java.put("vendor", runtimeBean.getVmVendor());
        java.put("jvm", runtimeBean.getVmName());
        java.put("jvmVersion", runtimeBean.getVmVersion());
        info.put("java", java);
        
        // 内存信息
        Map<String, Object> memory = new HashMap<>();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        memory.put("total", formatBytes(totalMemory));
        memory.put("used", formatBytes(usedMemory));
        memory.put("free", formatBytes(freeMemory));
        memory.put("max", formatBytes(runtime.maxMemory()));
        memory.put("usagePercent", String.format("%.1f%%", ((double) usedMemory / totalMemory) * 100));
        info.put("memory", memory);
        
        // 运行时信息
        Map<String, Object> runtimeInfo = new HashMap<>();
        runtimeInfo.put("uptime", formatUptime(runtimeBean.getUptime()));
        runtimeInfo.put("user", System.getProperty("user.name"));
        runtimeInfo.put("host", getHostname());
        runtimeInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        info.put("runtime", runtimeInfo);
        
        // 应用信息
        Map<String, Object> app = new HashMap<>();
        app.put("name", "Spring Gateway");
        app.put("springBootVersion", "3.2.0");
        app.put("javaVersion", "17");
        app.put("endpoints", new String[] {
            "/api/neofetch/info",
            "/api/neofetch/json", 
            "/api/system/info",
            "/api/system/health",
            "/api/hello",
            "/api/calculator"
        });
        info.put("application", app);
        
        info.put("format", "neofetch-style");
        info.put("description", "System information in neofetch style");
        
        return info;
    }
    
    /**
     * 格式化运行时间
     */
    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d days, %d hours", days, hours % 24);
        } else if (hours > 0) {
            return String.format("%d hours, %d minutes", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", minutes, seconds % 60);
        } else {
            return String.format("%d seconds", seconds);
        }
    }
    
    /**
     * 格式化字节大小
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * 获取主机名
     */
    private String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}