package com.jsonyao.distributelock.lock;

import com.jsonyao.distributelock.util.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;

import java.util.Arrays;
import java.util.UUID;

/**
 * 封装Redis分布式锁
 */
@Slf4j
public class RedisLock implements AutoCloseable{

    /**
     * RestTemplate
     */
    private RedisTemplate redisTemplate;

    /**
     * Redis key
     */
    private String key;

    /**
     * Redis value
     */
    private String value;

    /**
     * 过期时间/秒
     */
    private long expireSecondTime;

    /**
     * 构造方法
     * @param key
     * @param expireSecondTime
     */
    public RedisLock(String key, long expireSecondTime) {
        this.redisTemplate = ApplicationContextHolder.getApplicationContext().getBean("redisTemplate", RedisTemplate.class);
        this.key = key;
        this.value = UUID.randomUUID().toString();
        this.expireSecondTime = expireSecondTime;
    }

    /**
     * 获取Redis分布式锁
     * @return
     */
    public Boolean getLock(){
        RedisCallback<Boolean> redisCallback = connection -> {
            // 设置Key字节数组, Value字节数组, 过期时间, NX, 执行NX
            byte[] keyBytes = redisTemplate.getKeySerializer().serialize(key);
            byte[] valueBytes = redisTemplate.getValueSerializer().serialize(value);
            Expiration expiration = Expiration.seconds(expireSecondTime);
            RedisStringCommands.SetOption nxSetOption = RedisStringCommands.SetOption.ifAbsent();
            return connection.set(keyBytes, valueBytes, expiration, nxSetOption);
        };

        // 获取Redis分布式锁
        return (Boolean) redisTemplate.execute(redisCallback);
    }

    /**
     * 释放Redis分布式锁
     * @return
     */
    public Boolean unLock(){
        // 执行LUA脚本校验Redis锁的值是否为该线程的值, 是才删除, 防止删除了其他线程的锁
        String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                "    return redis.call(\"del\",KEYS[1])\n" +
                "else\n" +
                "    return 0\n" +
                "end";
        Boolean result = (Boolean) redisTemplate.execute(RedisScript.of(script, Boolean.class), Arrays.asList(key), value);
        log.info("释放锁的结果: " + result);
        return result;
    }

    /**
     * JDK 1.8 新特性: Try代码块结束时自动调用
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        this.unLock();
    }
}