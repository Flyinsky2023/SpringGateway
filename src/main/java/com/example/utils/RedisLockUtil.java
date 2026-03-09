package com.example.utils;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 分布式锁工具类
 * 提供基于 Redisson 的分布式锁功能
 */
@Component
public class RedisLockUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLockUtil.class);
    
    @Autowired
    private RedissonClient redissonClient;
    
    /**
     * 尝试获取分布式锁并执行操作
     * 
     * @param lockKey 锁的键名
     * @param waitTime 等待锁的最大时间（毫秒）
     * @param leaseTime 锁的持有时间（毫秒）
     * @param operation 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T tryLockAndExecute(String lockKey, long waitTime, long leaseTime, Supplier<T> operation) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        
        try {
            // 尝试获取锁
            locked = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
            
            if (locked) {
                LOGGER.debug("成功获取分布式锁: {}", lockKey);
                // 执行操作
                return operation.get();
            } else {
                LOGGER.warn("获取分布式锁失败: {}", lockKey);
                throw new RedisLockException("获取分布式锁失败: " + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("获取分布式锁时被中断: {}", lockKey, e);
            throw new RedisLockException("获取分布式锁时被中断: " + lockKey, e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                LOGGER.debug("释放分布式锁: {}", lockKey);
            }
        }
    }
    
    /**
     * 尝试获取分布式锁并执行操作（无返回值）
     * 
     * @param lockKey 锁的键名
     * @param waitTime 等待锁的最大时间（毫秒）
     * @param leaseTime 锁的持有时间（毫秒）
     * @param operation 要执行的操作
     */
    public void tryLockAndExecute(String lockKey, long waitTime, long leaseTime, Runnable operation) {
        tryLockAndExecute(lockKey, waitTime, leaseTime, () -> {
            operation.run();
            return null;
        });
    }
    
    /**
     * 尝试获取分布式锁并执行操作（使用默认配置）
     * 
     * @param lockKey 锁的键名
     * @param operation 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T tryLockAndExecute(String lockKey, Supplier<T> operation) {
        return tryLockAndExecute(lockKey, 
            RedisConstants.DEFAULT_TIMEOUT, 
            RedisConstants.EXPIRE_TIME_ONE_MINUTE * 1000, 
            operation);
    }
    
    /**
     * 尝试获取分布式锁并执行操作（无返回值，使用默认配置）
     * 
     * @param lockKey 锁的键名
     * @param operation 要执行的操作
     */
    public void tryLockAndExecute(String lockKey, Runnable operation) {
        tryLockAndExecute(lockKey, 
            RedisConstants.DEFAULT_TIMEOUT, 
            RedisConstants.EXPIRE_TIME_ONE_MINUTE * 1000, 
            operation);
    }
    
    /**
     * 获取公平锁并执行操作
     * 
     * @param lockKey 锁的键名
     * @param waitTime 等待锁的最大时间（毫秒）
     * @param leaseTime 锁的持有时间（毫秒）
     * @param operation 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T tryFairLockAndExecute(String lockKey, long waitTime, long leaseTime, Supplier<T> operation) {
        RLock lock = redissonClient.getFairLock(lockKey);
        boolean locked = false;
        
        try {
            locked = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
            
            if (locked) {
                LOGGER.debug("成功获取公平分布式锁: {}", lockKey);
                return operation.get();
            } else {
                LOGGER.warn("获取公平分布式锁失败: {}", lockKey);
                throw new RedisLockException("获取公平分布式锁失败: " + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("获取公平分布式锁时被中断: {}", lockKey, e);
            throw new RedisLockException("获取公平分布式锁时被中断: " + lockKey, e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                LOGGER.debug("释放公平分布式锁: {}", lockKey);
            }
        }
    }
    
    /**
     * 获取读写锁并执行读操作
     * 
     * @param lockKey 锁的键名
     * @param waitTime 等待锁的最大时间（毫秒）
     * @param leaseTime 锁的持有时间（毫秒）
     * @param readOperation 读操作
     * @param <T> 返回值类型
     * @return 读操作结果
     */
    public <T> T tryReadLockAndExecute(String lockKey, long waitTime, long leaseTime, Supplier<T> readOperation) {
        RLock lock = redissonClient.getReadWriteLock(lockKey).readLock();
        boolean locked = false;
        
        try {
            locked = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
            
            if (locked) {
                LOGGER.debug("成功获取读锁: {}", lockKey);
                return readOperation.get();
            } else {
                LOGGER.warn("获取读锁失败: {}", lockKey);
                throw new RedisLockException("获取读锁失败: " + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("获取读锁时被中断: {}", lockKey, e);
            throw new RedisLockException("获取读锁时被中断: " + lockKey, e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                LOGGER.debug("释放读锁: {}", lockKey);
            }
        }
    }
    
    /**
     * 获取读写锁并执行写操作
     * 
     * @param lockKey 锁的键名
     * @param waitTime 等待锁的最大时间（毫秒）
     * @param leaseTime 锁的持有时间（毫秒）
     * @param writeOperation 写操作
     * @param <T> 返回值类型
     * @return 写操作结果
     */
    public <T> T tryWriteLockAndExecute(String lockKey, long waitTime, long leaseTime, Supplier<T> writeOperation) {
        RLock lock = redissonClient.getReadWriteLock(lockKey).writeLock();
        boolean locked = false;
        
        try {
            locked = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
            
            if (locked) {
                LOGGER.debug("成功获取写锁: {}", lockKey);
                return writeOperation.get();
            } else {
                LOGGER.warn("获取写锁失败: {}", lockKey);
                throw new RedisLockException("获取写锁失败: " + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("获取写锁时被中断: {}", lockKey, e);
            throw new RedisLockException("获取写锁时被中断: " + lockKey, e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                LOGGER.debug("释放写锁: {}", lockKey);
            }
        }
    }
    
    /**
     * 检查锁是否存在
     * 
     * @param lockKey 锁的键名
     * @return 是否存在
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }
    
    /**
     * 强制释放锁（危险操作，慎用）
     * 
     * @param lockKey 锁的键名
     */
    public void forceUnlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isLocked()) {
            lock.forceUnlock();
            LOGGER.warn("强制释放分布式锁: {}", lockKey);
        }
    }
    
    /**
     * Redis 锁异常类
     */
    public static class RedisLockException extends RuntimeException {
        public RedisLockException(String message) {
            super(message);
        }
        
        public RedisLockException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}