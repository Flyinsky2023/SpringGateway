package com.example.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Redis 速率限制器
 * 基于令牌桶算法实现API速率限制
 */
@Component
public class RedisRateLimiter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRateLimiter.class);
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Lua脚本：令牌桶算法实现
    private static final String LUA_SCRIPT = 
        "local key = KEYS[1]\n" +
        "local limit = tonumber(ARGV[1])\n" +
        "local window = tonumber(ARGV[2])\n" +
        "local current = redis.call('GET', key)\n" +
        "\n" +
        "if current == false then\n" +
        "    redis.call('SET', key, 1)\n" +
        "    redis.call('EXPIRE', key, window)\n" +
        "    return 1\n" +
        "else\n" +
        "    local currentCount = tonumber(current)\n" +
        "    if currentCount < limit then\n" +
        "        redis.call('INCR', key)\n" +
        "        return 1\n" +
        "    else\n" +
        "        return 0\n" +
        "    end\n" +
        "end";
    
    private final RedisScript<Long> rateLimitScript;
    
    public RedisRateLimiter() {
        this.rateLimitScript = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
    }
    
    /**
     * 尝试获取令牌（简单计数器算法）
     * 
     * @param key 限制键
     * @param limit 限制次数
     * @param window 时间窗口（秒）
     * @return 是否允许访问
     */
    public boolean tryAcquire(String key, int limit, long window) {
        try {
            String redisKey = RedisConstants.RATE_LIMIT_KEY_PREFIX + key;
            
            // 使用Lua脚本保证原子性
            List<String> keys = Arrays.asList(redisKey);
            Long result = redisTemplate.execute(rateLimitScript, keys, limit, window);
            
            boolean allowed = result != null && result == 1;
            LOGGER.debug("速率限制检查: key={}, limit={}/{}s, allowed={}", key, limit, window, allowed);
            return allowed;
        } catch (Exception e) {
            LOGGER.error("速率限制检查失败: key={}", key, e);
            // 发生异常时默认允许访问，避免影响正常业务
            return true;
        }
    }
    
    /**
     * 尝试获取令牌（使用默认配置）
     * 
     * @param key 限制键
     * @return 是否允许访问
     */
    public boolean tryAcquire(String key) {
        return tryAcquire(key, RedisConstants.RATE_LIMIT_MAX_REQUESTS, RedisConstants.RATE_LIMIT_WINDOW_SIZE);
    }
    
    /**
     * 尝试获取令牌（滑动窗口算法）
     * 
     * @param key 限制键
     * @param limit 限制次数
     * @param window 时间窗口（秒）
     * @return 是否允许访问
     */
    public boolean tryAcquireSlidingWindow(String key, int limit, long window) {
        try {
            String redisKey = RedisConstants.RATE_LIMIT_KEY_PREFIX + "SLIDING:" + key;
            long currentTime = System.currentTimeMillis();
            long windowStart = currentTime - (window * 1000);
            
            // 使用ZSET实现滑动窗口
            redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);
            
            Long currentCount = redisTemplate.opsForZSet().count(redisKey, windowStart, currentTime);
            if (currentCount != null && currentCount >= limit) {
                LOGGER.debug("滑动窗口速率限制: key={}, current={}, limit={}, allowed=false", key, currentCount, limit);
                return false;
            }
            
            // 添加当前请求
            redisTemplate.opsForZSet().add(redisKey, String.valueOf(currentTime), currentTime);
            redisTemplate.expire(redisKey, window, java.util.concurrent.TimeUnit.SECONDS);
            
            LOGGER.debug("滑动窗口速率限制: key={}, current={}, limit={}, allowed=true", key, 
                currentCount != null ? currentCount + 1 : 1, limit);
            return true;
        } catch (Exception e) {
            LOGGER.error("滑动窗口速率限制检查失败: key={}", key, e);
            return true;
        }
    }
    
    /**
     * 尝试获取令牌（漏桶算法）
     * 
     * @param key 限制键
     * @param capacity 桶容量
     * @param rate 流出速率（请求/秒）
     * @return 是否允许访问
     */
    public boolean tryAcquireLeakyBucket(String key, int capacity, int rate) {
        try {
            String redisKey = RedisConstants.RATE_LIMIT_KEY_PREFIX + "LEAKY:" + key;
            long currentTime = System.currentTimeMillis();
            
            // 获取桶的当前状态
            Object bucketData = redisTemplate.opsForValue().get(redisKey);
            long lastTime = currentTime;
            int currentWater = 0;
            
            if (bucketData != null) {
                String[] parts = bucketData.toString().split(":");
                if (parts.length == 2) {
                    lastTime = Long.parseLong(parts[0]);
                    currentWater = Integer.parseInt(parts[1]);
                }
            }
            
            // 计算漏出的水量
            long timePassed = currentTime - lastTime;
            int leakedWater = (int) (timePassed * rate / 1000);
            currentWater = Math.max(0, currentWater - leakedWater);
            
            // 检查是否可以加水
            if (currentWater >= capacity) {
                LOGGER.debug("漏桶算法速率限制: key={}, water={}, capacity={}, allowed=false", 
                    key, currentWater, capacity);
                return false;
            }
            
            // 加水并更新状态
            currentWater++;
            String newBucketData = currentTime + ":" + currentWater;
            redisTemplate.opsForValue().set(redisKey, newBucketData, RedisConstants.EXPIRE_TIME_ONE_HOUR, 
                java.util.concurrent.TimeUnit.SECONDS);
            
            LOGGER.debug("漏桶算法速率限制: key={}, water={}, capacity={}, allowed=true", 
                key, currentWater, capacity);
            return true;
        } catch (Exception e) {
            LOGGER.error("漏桶算法速率限制检查失败: key={}", key, e);
            return true;
        }
    }
    
    /**
     * 获取当前请求计数
     * 
     * @param key 限制键
     * @return 当前计数
     */
    public long getCurrentCount(String key) {
        try {
            String redisKey = RedisConstants.RATE_LIMIT_KEY_PREFIX + key;
            Object value = redisTemplate.opsForValue().get(redisKey);
            if (value == null) {
                return 0;
            }
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            LOGGER.error("获取当前请求计数失败: key={}", key, e);
            return 0;
        }
    }
    
    /**
     * 重置速率限制计数器
     * 
     * @param key 限制键
     */
    public void reset(String key) {
        try {
            String redisKey = RedisConstants.RATE_LIMIT_KEY_PREFIX + key;
            redisTemplate.delete(redisKey);
            LOGGER.debug("重置速率限制计数器: key={}", key);
        } catch (Exception e) {
            LOGGER.error("重置速率限制计数器失败: key={}", key, e);
        }
    }
    
    /**
     * 获取剩余请求次数
     * 
     * @param key 限制键
     * @param limit 限制次数
     * @return 剩余次数
     */
    public long getRemaining(String key, int limit) {
        long current = getCurrentCount(key);
        return Math.max(0, limit - current);
    }
    
    /**
     * 检查并获取令牌，如果被限制则抛出异常
     * 
     * @param key 限制键
     * @param limit 限制次数
     * @param window 时间窗口（秒）
     * @throws RateLimitException 如果被限制
     */
    public void checkAndAcquire(String key, int limit, long window) throws RateLimitException {
        if (!tryAcquire(key, limit, window)) {
            long remaining = getRemaining(key, limit);
            throw new RateLimitException("请求频率超过限制", limit, remaining, window);
        }
    }
    
    /**
     * 速率限制异常类
     */
    public static class RateLimitException extends RuntimeException {
        private final int limit;
        private final long remaining;
        private final long window;
        
        public RateLimitException(String message, int limit, long remaining, long window) {
            super(message);
            this.limit = limit;
            this.remaining = remaining;
            this.window = window;
        }
        
        public int getLimit() {
            return limit;
        }
        
        public long getRemaining() {
            return remaining;
        }
        
        public long getWindow() {
            return window;
        }
        
        @Override
        public String getMessage() {
            return String.format("%s (限制: %d次/%ds, 剩余: %d次)", 
                super.getMessage(), limit, window, remaining);
        }
    }
}