package com.example.utils;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 缓存工具类
 * 解决缓存雪崩和缓存击穿问题
 */
@Component
public class CacheUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    private static final Random RANDOM = new Random();
    
    /**
     * 获取缓存值（解决缓存击穿）
     * 使用互斥锁防止大量请求同时访问数据库
     * 
     * @param key 缓存键
     * @param lockKey 分布式锁键
     * @param supplier 数据获取函数（当缓存不存在时调用）
     * @param timeout 缓存过期时间（秒）
     * @param <T> 返回值类型
     * @return 缓存值
     */
    public <T> T getWithMutex(String key, String lockKey, Supplier<T> supplier, long timeout) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        
        // 1. 尝试从缓存获取
        Object value = ops.get(key);
        if (value != null) {
            return (T) value;
        }
        
        // 2. 获取分布式锁
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 尝试获取锁，最多等待3秒，锁持有时间10秒
            boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                // 获取锁失败，等待一段时间后重试
                Thread.sleep(50);
                return getWithMutex(key, lockKey, supplier, timeout);
            }
            
            // 3. 双重检查，防止其他线程已经更新了缓存
            value = ops.get(key);
            if (value != null) {
                return (T) value;
            }
            
            // 4. 从数据源获取数据
            T result = supplier.get();
            if (result != null) {
                // 设置缓存，使用随机过期时间防止雪崩
                long randomTimeout = getRandomTimeout(timeout);
                ops.set(key, result, randomTimeout, TimeUnit.SECONDS);
            }
            return result;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取缓存时被中断", e);
        } finally {
            // 5. 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    /**
     * 获取缓存值（解决缓存雪崩）
     * 使用随机过期时间分散缓存失效时间
     * 
     * @param key 缓存键
     * @param supplier 数据获取函数
     * @param baseTimeout 基础过期时间（秒）
     * @param <T> 返回值类型
     * @return 缓存值
     */
    public <T> T getWithRandomExpire(String key, Supplier<T> supplier, long baseTimeout) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        
        // 1. 尝试从缓存获取
        Object value = ops.get(key);
        if (value != null) {
            return (T) value;
        }
        
        // 2. 从数据源获取数据
        T result = supplier.get();
        if (result != null) {
            // 3. 设置缓存，使用随机过期时间
            long randomTimeout = getRandomTimeout(baseTimeout);
            ops.set(key, result, randomTimeout, TimeUnit.SECONDS);
        }
        return result;
    }
    
    /**
     * 热点数据永不过期方案
     * 使用后台线程定期更新缓存
     * 
     * @param key 缓存键
     * @param supplier 数据获取函数
     * @param refreshInterval 刷新间隔（秒）
     * @param <T> 返回值类型
     * @return 缓存值
     */
    public <T> T getWithBackgroundRefresh(String key, Supplier<T> supplier, long refreshInterval) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        
        // 1. 尝试从缓存获取
        Object value = ops.get(key);
        if (value != null) {
            // 2. 异步检查是否需要刷新
            checkAndRefreshAsync(key, supplier, refreshInterval);
            return (T) value;
        }
        
        // 3. 缓存不存在，同步获取
        T result = supplier.get();
        if (result != null) {
            // 设置较长的过期时间
            ops.set(key, result, refreshInterval * 2, TimeUnit.SECONDS);
        }
        return result;
    }
    
    /**
     * 多级缓存方案（Redis + 本地缓存）
     * 需要配合 Caffeine 或 Guava Cache 使用
     * 
     * @param key 缓存键
     * @param localCache 本地缓存
     * @param supplier 数据获取函数
     * @param timeout 过期时间
     * @param <T> 返回值类型
     * @return 缓存值
     */
    public <T> T getWithMultiLevel(String key, 
                                   java.util.Map<String, T> localCache, 
                                   Supplier<T> supplier, 
                                   long timeout) {
        // 1. 检查本地缓存
        T value = localCache.get(key);
        if (value != null) {
            return value;
        }
        
        // 2. 检查 Redis 缓存
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        Object redisValue = ops.get(key);
        if (redisValue != null) {
            T result = (T) redisValue;
            // 更新本地缓存
            localCache.put(key, result);
            return result;
        }
        
        // 3. 从数据源获取
        T result = supplier.get();
        if (result != null) {
            // 更新 Redis 缓存
            long randomTimeout = getRandomTimeout(timeout);
            ops.set(key, result, randomTimeout, TimeUnit.SECONDS);
            
            // 更新本地缓存（设置较短的过期时间）
            localCache.put(key, result);
        }
        return result;
    }
    
    /**
     * 缓存预热
     * 在系统启动或低峰期预先加载热点数据
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间（秒）
     */
    public void preheatCache(String key, Object value, long timeout) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        long randomTimeout = getRandomTimeout(timeout);
        ops.set(key, value, randomTimeout, TimeUnit.SECONDS);
    }
    
    /**
     * 删除缓存
     * 
     * @param key 缓存键
     */
    public void deleteCache(String key) {
        redisTemplate.delete(key);
    }
    
    /**
     * 获取随机过期时间
     * 在基础时间上增加随机偏移，防止雪崩
     * 
     * @param baseTimeout 基础过期时间
     * @return 随机过期时间
     */
    private long getRandomTimeout(long baseTimeout) {
        // 在基础时间上增加 ±10% 的随机偏移
        double randomFactor = 0.9 + RANDOM.nextDouble() * 0.2; // 0.9 ~ 1.1
        return (long) (baseTimeout * randomFactor);
    }
    
    /**
     * 异步检查并刷新缓存
     */
    private <T> void checkAndRefreshAsync(String key, Supplier<T> supplier, long refreshInterval) {
        new Thread(() -> {
            try {
                // 模拟检查逻辑，实际应用中可以根据业务需求实现
                // 例如：检查缓存剩余时间，如果小于阈值则刷新
                Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (expire != null && expire < refreshInterval / 2) {
                    // 刷新缓存
                    T newValue = supplier.get();
                    if (newValue != null) {
                        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
                        ops.set(key, newValue, refreshInterval * 2, TimeUnit.SECONDS);
                    }
                }
            } catch (Exception e) {
                // 异步刷新失败不影响主流程
                e.printStackTrace();
            }
        }).start();
    }
}