package com.example.service;

import com.example.model.SystemInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;

/**
 * 系统信息服务类，用于获取类似 neofetch 的系统信息
 */
@Service
public class SystemInfoService {
    
    @Value("${spring.application.name:SpringGateway}")
    private String appName;
    
    @Value("${app.version:1.0.0}")
    private String appVersion;
    
    /**
     * 获取系统信息
     */
    public SystemInfo getSystemInfo() {
        SystemInfo info = new SystemInfo();
        
        // 操作系统信息
        info.setOsName(System.getProperty("os.name"));
        info.setOsVersion(System.getProperty("os.version"));
        info.setOsArch(System.getProperty("os.arch"));
        
        // Java 信息
        info.setJavaVersion(System.getProperty("java.version"));
        info.setJavaVendor(System.getProperty("java.vendor"));
        info.setJavaHome(System.getProperty("java.home"));
        
        // 用户信息
        info.setUserName(System.getProperty("user.name"));
        info.setUserHome(System.getProperty("user.home"));
        
        // 内存信息
        Runtime runtime = Runtime.getRuntime();
        info.setTotalMemory(runtime.totalMemory());
        info.setFreeMemory(runtime.freeMemory());
        info.setMaxMemory(runtime.maxMemory());
        info.setAvailableProcessors(runtime.availableProcessors());
        
        // 主机信息
        try {
            info.setHostname(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            info.setHostname("Unknown");
        }
        
        // 运行时间
        info.setUptime(getUptime());
        
        // Spring 和应用信息
        info.setSpringVersion(SpringBootVersion.getVersion());
        info.setAppName(appName);
        info.setAppVersion(appVersion);
        
        return info;
    }
    
    /**
     * 获取系统运行时间
     */
    private String getUptime() {
        try {
            RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
            long uptime = runtimeMxBean.getUptime();
            
            Duration duration = Duration.ofMillis(uptime);
            long days = duration.toDays();
            long hours = duration.toHours() % 24;
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;
            
            if (days > 0) {
                return String.format("%d days, %02d:%02d:%02d", days, hours, minutes, seconds);
            } else {
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    /**
     * 获取格式化的系统信息（类似 neofetch 的 ASCII 风格输出）
     */
    public String getFormattedSystemInfo() {
        SystemInfo info = getSystemInfo();
        
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔══════════════════════════════════════════════════════════╗\n");
        sb.append("║                    SYSTEM INFORMATION                     ║\n");
        sb.append("╚══════════════════════════════════════════════════════════╝\n");
        sb.append("\n");
        sb.append(String.format("  %-20s: %s\n", "Application", info.getAppName()));
        sb.append(String.format("  %-20s: %s\n", "Version", info.getAppVersion()));
        sb.append(String.format("  %-20s: %s\n", "Spring Boot", info.getSpringVersion()));
        sb.append("\n");
        sb.append(String.format("  %-20s: %s\n", "OS", info.getOsName()));
        sb.append(String.format("  %-20s: %s\n", "OS Version", info.getOsVersion()));
        sb.append(String.format("  %-20s: %s\n", "Architecture", info.getOsArch()));
        sb.append(String.format("  %-20s: %s\n", "Hostname", info.getHostname()));
        sb.append("\n");
        sb.append(String.format("  %-20s: %s\n", "Java Version", info.getJavaVersion()));
        sb.append(String.format("  %-20s: %s\n", "Java Vendor", info.getJavaVendor()));
        sb.append(String.format("  %-20s: %s\n", "User", info.getUserName()));
        sb.append("\n");
        sb.append(String.format("  %-20s: %d\n", "Processors", info.getAvailableProcessors()));
        sb.append(String.format("  %-20s: %s\n", "Total Memory", formatMemory(info.getTotalMemory())));
        sb.append(String.format("  %-20s: %s\n", "Free Memory", formatMemory(info.getFreeMemory())));
        sb.append(String.format("  %-20s: %s\n", "Max Memory", formatMemory(info.getMaxMemory())));
        sb.append("\n");
        sb.append(String.format("  %-20s: %s\n", "Server Time", info.getServerTime()));
        sb.append(String.format("  %-20s: %s\n", "Uptime", info.getUptime()));
        sb.append("\n");
        sb.append("╔══════════════════════════════════════════════════════════╗\n");
        sb.append("║                      END OF REPORT                       ║\n");
        sb.append("╚══════════════════════════════════════════════════════════╝\n");
        
        return sb.toString();
    }
    
    /**
     * 格式化内存大小
     */
    private String formatMemory(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}