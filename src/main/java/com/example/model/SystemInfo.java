package com.example.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 系统信息实体类，类似 neofetch 的输出
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemInfo {
    
    private String osName;
    private String osVersion;
    private String osArch;
    private String javaVersion;
    private String javaVendor;
    private String javaHome;
    private String userHome;
    private String userName;
    private long totalMemory;
    private long freeMemory;
    private long maxMemory;
    private int availableProcessors;
    private String serverTime;
    private String uptime;
    private String hostname;
    private String springVersion;
    private String appName;
    private String appVersion;
    
    public SystemInfo() {
        // 设置时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.serverTime = LocalDateTime.now().format(formatter);
    }
    
    // Getters and Setters
    public String getOsName() {
        return osName;
    }
    
    public void setOsName(String osName) {
        this.osName = osName;
    }
    
    public String getOsVersion() {
        return osVersion;
    }
    
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }
    
    public String getOsArch() {
        return osArch;
    }
    
    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }
    
    public String getJavaVersion() {
        return javaVersion;
    }
    
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }
    
    public String getJavaVendor() {
        return javaVendor;
    }
    
    public void setJavaVendor(String javaVendor) {
        this.javaVendor = javaVendor;
    }
    
    public String getJavaHome() {
        return javaHome;
    }
    
    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }
    
    public String getUserHome() {
        return userHome;
    }
    
    public void setUserHome(String userHome) {
        this.userHome = userHome;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public long getTotalMemory() {
        return totalMemory;
    }
    
    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }
    
    public long getFreeMemory() {
        return freeMemory;
    }
    
    public void setFreeMemory(long freeMemory) {
        this.freeMemory = freeMemory;
    }
    
    public long getMaxMemory() {
        return maxMemory;
    }
    
    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }
    
    public int getAvailableProcessors() {
        return availableProcessors;
    }
    
    public void setAvailableProcessors(int availableProcessors) {
        this.availableProcessors = availableProcessors;
    }
    
    public String getServerTime() {
        return serverTime;
    }
    
    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }
    
    public String getUptime() {
        return uptime;
    }
    
    public void setUptime(String uptime) {
        this.uptime = uptime;
    }
    
    public String getHostname() {
        return hostname;
    }
    
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    public String getSpringVersion() {
        return springVersion;
    }
    
    public void setSpringVersion(String springVersion) {
        this.springVersion = springVersion;
    }
    
    public String getAppName() {
        return appName;
    }
    
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public String getAppVersion() {
        return appVersion;
    }
    
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
    
    @Override
    public String toString() {
        return "SystemInfo{" +
                "osName='" + osName + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", osArch='" + osArch + '\'' +
                ", javaVersion='" + javaVersion + '\'' +
                ", javaVendor='" + javaVendor + '\'' +
                ", userName='" + userName + '\'' +
                ", availableProcessors=" + availableProcessors +
                ", serverTime='" + serverTime + '\'' +
                ", appName='" + appName + '\'' +
                '}';
    }
}