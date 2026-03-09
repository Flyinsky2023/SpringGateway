package com.example.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 缓存工具类
 * 提供通用的缓存操作方法
 */
@Component
public class RedisCacheUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheUtil.class);
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private RedisLockUtil redisLockUtil;
    
    /**
     * 设置缓存值
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间（秒）
     */
    public void set(String key, Object value, long timeout) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            ops.set(key, value, timeout, TimeUnit.SECONDS);
            LOGGER.debug("设置缓存成功: key={}, timeout={}s", key, timeout);
        } catch (Exception e) {
            LOGGER.error("设置缓存失败: key={}", key, e);
            throw new RedisCacheException("设置缓存失败: " + key, e);
        }
    }
    
    /**
     * 设置缓存值（永不过期）
     * 
     * @param key 缓存键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            ops.set(key, value);
            LOGGER.debug("设置缓存成功（永不过期）: key={}", key);
        } catch (Exception e) {
            LOGGER.error("设置缓存失败: key={}", key, e);
            throw new RedisCacheException("设置缓存失败: " + key, e);
        }
    }
    
    /**
     * 获取缓存值
     * 
     * @param key 缓存键
     * @param <T> 返回值类型
     * @return 缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            Object value = ops.get(key);
            if (value == null) {
                LOGGER.debug("缓存未命中: key={}", key);
                return null;
            }
            LOGGER.debug("缓存命中: key={}", key);
            return (T) value;
        } catch (Exception e) {
            LOGGER.error("获取缓存失败: key={}", key, e);
            throw new RedisCacheException("获取缓存失败: " + key, e);
        }
    }
    
    /**
     * 获取缓存值，如果不存在则从数据源获取并设置缓存
     * 
     * @param key 缓存键
     * @param supplier 数据源获取函数
     * @param timeout 过期时间（秒）
     * @param <T> 返回值类型
     * @return 缓存值
     */
    public <T> T getOrLoad(String key, Supplier<T> supplier, long timeout) {
        T value = get(key);
        if (value != null) {
            return value;
        }
        
        // 使用分布式锁防止缓存击穿
        String lockKey = RedisConstants.LOCK_KEY_CACHE_REFRESH + key;
        return redisLockUtil.tryLockAndExecute(lockKey, () -> {
            // 双重检查
            T cachedValue = get(key);
            if (cachedValue != null) {
                return cachedValue;
            }
            
            // 从数据源获取
            T result = supplier.get();
            if (result != null) {
                set(key, result, timeout);
            }
            return result;
        });
    }
    
    /**
     * 获取缓存值，如果不存在则从数据源获取并设置缓存（使用默认过期时间）
     * 
     * @param key 缓存键
     * @param supplier 数据源获取函数
     * @param <T> 返回值类型
     * @return 缓存值
     */
    public <T> T getOrLoad(String key, Supplier<T> supplier) {
        return getOrLoad(key, supplier, RedisConstants.EXPIRE_TIME_THIRTY_MINUTES);
    }
    
    /**
     * 删除缓存
     * 
     * @param key 缓存键
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            LOGGER.debug("删除缓存成功: key={}", key);
        } catch (Exception e) {
            LOGGER.error("删除缓存失败: key={}", key, e);
            throw new RedisCacheException("删除缓存失败: " + key, e);
        }
    }
    
    /**
     * 批量删除缓存
     * 
     * @param keys 缓存键集合
     */
    public void delete(Collection<String> keys) {
        try {
            redisTemplate.delete(keys);
            LOGGER.debug("批量删除缓存成功: count={}", keys.size());
        } catch (Exception e) {
            LOGGER.error("批量删除缓存失败", e);
            throw new RedisCacheException("批量删除缓存失败", e);
        }
    }
    
    /**
     * 删除匹配模式的缓存
     * 
     * @param pattern 键模式
     */
    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                LOGGER.debug("按模式删除缓存成功: pattern={}, count={}", pattern, keys.size());
            }
        } catch (Exception e) {
            LOGGER.error("按模式删除缓存失败: pattern={}", pattern, e);
            throw new RedisCacheException("按模式删除缓存失败: " + pattern, e);
        }
    }
    
    /**
     * 检查缓存是否存在
     * 
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            LOGGER.error("检查缓存是否存在失败: key={}", key, e);
            throw new RedisCacheException("检查缓存是否存在失败: " + key, e);
        }
    }
    
    /**
     * 设置缓存过期时间
     * 
     * @param key 缓存键
     * @param timeout 过期时间（秒）
     * @return 是否设置成功
     */
    public boolean expire(String key, long timeout) {
        try {
            Boolean result = redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
            LOGGER.debug("设置缓存过期时间: key={}, timeout={}s, result={}", key, timeout, result);
            return result != null && result;
        } catch (Exception e) {
            LOGGER.error("设置缓存过期时间失败: key={}", key, e);
            throw new RedisCacheException("设置缓存过期时间失败: " + key, e);
        }
    }
    
    /**
     * 获取缓存剩余时间
     * 
     * @param key 缓存键
     * @return 剩余时间（秒），-1表示永不过期，-2表示不存在
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            LOGGER.error("获取缓存剩余时间失败: key={}", key, e);
            throw new RedisCacheException("获取缓存剩余时间失败: " + key, e);
        }
    }
    
    /**
     * 递增操作
     * 
     * @param key 缓存键
     * @param delta 增量
     * @return 递增后的值
     */
    public long increment(String key, long delta) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            Long result = ops.increment(key, delta);
            LOGGER.debug("递增操作: key={}, delta={}, result={}", key, delta, result);
            return result != null ? result : 0;
        } catch (Exception e) {
            LOGGER.error("递增操作失败: key={}, delta={}", key, delta, e);
            throw new RedisCacheException("递增操作失败: " + key, e);
        }
    }
    
    /**
     * 递减操作
     * 
     * @param key 缓存键
     * @param delta 减量
     * @return 递减后的值
     */
    public long decrement(String key, long delta) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            Long result = ops.decrement(key, delta);
            LOGGER.debug("递减操作: key={}, delta={}, result={}", key, delta, result);
            return result != null ? result : 0;
        } catch (Exception e) {
            LOGGER.error("递减操作失败: key={}, delta={}", key, delta, e);
            throw new RedisCacheException("递减操作失败: " + key, e);
        }
    }
    
    /**
     * 设置哈希表字段值
     * 
     * @param key 哈希表键
     * @param field 字段名
     * @param value 字段值
     */
    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            LOGGER.debug("设置哈希表字段值: key={}, field={}", key, field);
        } catch (Exception e) {
            LOGGER.error("设置哈希表字段值失败: key={}, field={}", key, field, e);
            throw new RedisCacheException("设置哈希表字段值失败: " + key + "." + field, e);
        }
    }
    
    /**
     * 获取哈希表字段值
     * 
     * @param key 哈希表键
     * @param field 字段名
     * @param <T> 返回值类型
     * @return 字段值
     */
    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String field) {
        try {
            Object value = redisTemplate.opsForHash().get(key, field);
            LOGGER.debug("获取哈希表字段值: key={}, field={}", key, field);
            return (T) value;
        } catch (Exception e) {
            LOGGER.error("获取哈希表字段值失败: key={}, field={}", key, field, e);
            throw new RedisCacheException("获取哈希表字段值失败: " + key + "." + field, e);
        }
    }
    
    /**
     * 删除哈希表字段
     * 
     * @param key 哈希表键
     * @param fields 字段名数组
     * @return 删除的字段数量
     */
    public long hDelete(String key, Object... fields) {
        try {
            Long result = redisTemplate.opsForHash().delete(key, fields);
            LOGGER.debug("删除哈希表字段: key={}, fields={}, count={}", key, Arrays.toString(fields), result);
            return result != null ? result : 0;
        } catch (Exception e) {
            LOGGER.error("删除哈希表字段失败: key={}, fields={}", key, Arrays.toString(fields), e);
            throw new RedisCacheException("删除哈希表字段失败: " + key, e);
        }
    }
    
    /**
     * 获取哈希表所有字段值
     * 
     * @param key 哈希表键
     * @return 字段值映射
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            Map<Object, Object> result = redisTemplate.opsForHash().entries(key);
            LOGGER.debug("获取哈希表所有字段值: key={}, size={}", key, result.size());
            return result;
        } catch (Exception e) {
            LOGGER.error("获取哈希表所有字段值失败: key={}", key, e);
            throw new RedisCacheException("获取哈希表所有字段值失败: " + key, e);
        }
    }
    
    /**
     * 添加元素到集合
     * 
     * @param key 集合键
     * @param values 元素数组
     * @return 添加的元素数量
     */
    public long sAdd(String key, Object... values) {
        try {
            Long result = redisTemplate.opsForSet().add(key, values);
            LOGGER.debug("添加元素到集合: key={}, values={}, count={}", key, Arrays.toString(values), result);
            return result != null ? result : 0;
        } catch (Exception e) {
            LOGGER.error("添加元素到集合失败: key={}, values={}", key, Arrays.toString(values), e);
            throw new RedisCacheException("添加元素到集合失败: " + key, e);
        }
    }
    
    /**
     * 获取集合所有元素
     * 
     * @param key 集合键
     * @return 元素集合
     */
    public Set<Object> sMembers(String key) {
        try {
            Set<Object> result = redisTemplate.opsForSet().members(key);
            LOGGER.debug("获取集合所有元素: key={}, size={}", key, result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            LOGGER.error("获取集合所有元素失败: key={}", key, e);
            throw new RedisCacheException("获取集合所有元素失败: " + key, e);
        }
    }
    
    /**
     * Redis 缓存异常类
     */
    public static class RedisCacheException extends RuntimeException {
        public RedisCacheException(String message) {
            super(message);
        }
        
        public RedisCacheException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}