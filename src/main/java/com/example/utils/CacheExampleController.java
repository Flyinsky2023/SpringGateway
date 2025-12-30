package com.example.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存使用示例控制器
 */
@RestController
@RequestMapping("/api/cache")
public class CacheExampleController {

    @Autowired
    private CacheUtils cacheUtils;
    
    // 模拟本地缓存（实际项目中可以使用 Caffeine 或 Guava Cache）
    private final Map<String, Object> localCache = new ConcurrentHashMap<>();
    
    /**
     * 使用互斥锁解决缓存击穿
     */
    @GetMapping("/mutex/{id}")
    public String getWithMutex(@PathVariable String id) {
        String cacheKey = "user:" + id;
        String lockKey = "lock:user:" + id;
        
        return cacheUtils.getWithMutex(cacheKey, lockKey, () -> {
            // 模拟从数据库查询数据
            System.out.println("查询数据库获取用户数据: " + id);
            return "用户数据-" + id + "-" + System.currentTimeMillis();
        }, 300); // 缓存5分钟
    }
    
    /**
     * 使用随机过期时间解决缓存雪崩
     */
    @GetMapping("/random/{id}")
    public String getWithRandomExpire(@PathVariable String id) {
        String cacheKey = "product:" + id;
        
        return cacheUtils.getWithRandomExpire(cacheKey, () -> {
            // 模拟从数据库查询数据
            System.out.println("查询数据库获取产品数据: " + id);
            return "产品数据-" + id + "-" + System.currentTimeMillis();
        }, 600); // 缓存10分钟，实际会加上随机偏移
    }
    
    /**
     * 使用热点数据永不过期方案
     */
    @GetMapping("/hot/{id}")
    public String getWithBackgroundRefresh(@PathVariable String id) {
        String cacheKey = "hot:config:" + id;
        
        return cacheUtils.getWithBackgroundRefresh(cacheKey, () -> {
            // 模拟从数据库查询热点配置
            System.out.println("查询数据库获取热点配置: " + id);
            return "热点配置-" + id + "-" + System.currentTimeMillis();
        }, 1800); // 刷新间隔30分钟
    }
    
    /**
     * 使用多级缓存方案
     */
    @GetMapping("/multi-level/{id}")
    public String getWithMultiLevel(@PathVariable String id) {
        String cacheKey = "order:" + id;
        
        return cacheUtils.getWithMultiLevel(cacheKey, localCache, () -> {
            // 模拟从数据库查询订单数据
            System.out.println("查询数据库获取订单数据: " + id);
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", id);
            orderData.put("amount", 100.0);
            orderData.put("status", "PAID");
            orderData.put("timestamp", System.currentTimeMillis());
            return orderData;
        }, 900); // 缓存15分钟
    }
    
    /**
     * 缓存预热示例
     */
    @PostMapping("/preheat")
    public String preheatCache() {
        // 预热热点数据
        cacheUtils.preheatCache("hot:config:system", "系统配置数据", 3600);
        cacheUtils.preheatCache("hot:user:admin", "管理员用户数据", 1800);
        
        return "缓存预热完成";
    }
    
    /**
     * 删除缓存示例
     */
    @DeleteMapping("/{key}")
    public String deleteCache(@PathVariable String key) {
        cacheUtils.deleteCache(key);
        return "删除缓存成功: " + key;
    }
    
    /**
     * 获取缓存统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("localCacheSize", localCache.size());
        stats.put("timestamp", System.currentTimeMillis());
        stats.put("description", "缓存工具类运行状态");
        return stats;
    }
}