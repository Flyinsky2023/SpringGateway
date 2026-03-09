package com.example.utils;

/**
 * Redis 常量定义类
 * 遵循项目编码规范：所有常量必须使用全大写字母和下划线分割
 */
public class RedisConstants {
    
    // Redis 键前缀常量
    public static final String CACHE_KEY_PREFIX = "CACHE:";
    public static final String LOCK_KEY_PREFIX = "LOCK:";
    public static final String SESSION_KEY_PREFIX = "SESSION:";
    public static final String RATE_LIMIT_KEY_PREFIX = "RATE_LIMIT:";
    public static final String TOKEN_KEY_PREFIX = "TOKEN:";
    public static final String USER_KEY_PREFIX = "USER:";
    public static final String ORDER_KEY_PREFIX = "ORDER:";
    public static final String PRODUCT_KEY_PREFIX = "PRODUCT:";
    
    // 缓存键常量
    public static final String CACHE_KEY_USER_INFO = CACHE_KEY_PREFIX + "USER_INFO:";
    public static final String CACHE_KEY_SYSTEM_CONFIG = CACHE_KEY_PREFIX + "SYSTEM_CONFIG";
    public static final String CACHE_KEY_API_RATE_LIMIT = CACHE_KEY_PREFIX + "API_RATE_LIMIT:";
    public static final String CACHE_KEY_HOT_DATA = CACHE_KEY_PREFIX + "HOT_DATA:";
    
    // 分布式锁键常量
    public static final String LOCK_KEY_USER_OPERATION = LOCK_KEY_PREFIX + "USER_OPERATION:";
    public static final String LOCK_KEY_ORDER_CREATE = LOCK_KEY_PREFIX + "ORDER_CREATE:";
    public static final String LOCK_KEY_INVENTORY_UPDATE = LOCK_KEY_PREFIX + "INVENTORY_UPDATE:";
    public static final String LOCK_KEY_CACHE_REFRESH = LOCK_KEY_PREFIX + "CACHE_REFRESH:";
    
    // 过期时间常量（单位：秒）
    public static final long EXPIRE_TIME_ONE_MINUTE = 60L;
    public static final long EXPIRE_TIME_FIVE_MINUTES = 300L;
    public static final long EXPIRE_TIME_TEN_MINUTES = 600L;
    public static final long EXPIRE_TIME_THIRTY_MINUTES = 1800L;
    public static final long EXPIRE_TIME_ONE_HOUR = 3600L;
    public static final long EXPIRE_TIME_ONE_DAY = 86400L;
    public static final long EXPIRE_TIME_ONE_WEEK = 604800L;
    public static final long EXPIRE_TIME_ONE_MONTH = 2592000L;
    
    // JWT Token 相关常量
    public static final long TOKEN_EXPIRE_TIME = EXPIRE_TIME_ONE_DAY;
    public static final String TOKEN_BLACKLIST_KEY = TOKEN_KEY_PREFIX + "BLACKLIST:";
    
    // 速率限制常量
    public static final long RATE_LIMIT_WINDOW_SIZE = EXPIRE_TIME_ONE_MINUTE;
    public static final int RATE_LIMIT_MAX_REQUESTS = 100;
    
    // 默认配置常量
    public static final int DEFAULT_DATABASE = 0;
    public static final long DEFAULT_TIMEOUT = 3000L;
    public static final int DEFAULT_MAX_RETRIES = 3;
    
    // 错误码常量
    public static final String ERROR_CODE_REDIS_CONNECTION_FAILED = "REDIS_001";
    public static final String ERROR_CODE_REDIS_TIMEOUT = "REDIS_002";
    public static final String ERROR_CODE_REDIS_LOCK_FAILED = "REDIS_003";
    public static final String ERROR_CODE_REDIS_KEY_NOT_FOUND = "REDIS_004";
    
    // 消息常量
    public static final String MESSAGE_REDIS_CONNECTION_SUCCESS = "Redis连接成功";
    public static final String MESSAGE_REDIS_CONNECTION_FAILED = "Redis连接失败";
    public static final String MESSAGE_REDIS_LOCK_ACQUIRED = "分布式锁获取成功";
    public static final String MESSAGE_REDIS_LOCK_FAILED = "分布式锁获取失败";
    
    // 私有构造函数防止实例化
    private RedisConstants() {
        throw new IllegalStateException("常量类不允许实例化");
    }
}