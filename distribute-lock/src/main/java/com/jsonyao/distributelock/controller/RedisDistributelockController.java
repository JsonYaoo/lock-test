package com.jsonyao.distributelock.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.UUID;

/**
 * 分布式环境Redis分布式锁测试
 */
@RestController
@Slf4j
public class RedisDistributelockController {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * Redis分布式锁测试
     * @return
     * @throws Exception
     */
    @RequestMapping("/redisDistributeLock")
    public String redisDistributeLock() throws Exception {
        log.info("我进入了方法！");

        String key = "redisKey";
        String value = UUID.randomUUID().toString();

        // connection -> {..} 相当于方法实现
//        RedisCallback<Boolean> redisCallback = new RedisCallback<Boolean>() {
//            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
//                // 设置Key字节数组
//                byte[] keyBytes = redisTemplate.getKeySerializer().serialize(key);
//                // 设置Value字节数组
//                byte[] valueBytes = redisTemplate.getStringSerializer().serialize(value);
//                // 设置过期时间
//                Expiration expiration = Expiration.seconds(30);
//                // 设置NX
//                RedisStringCommands.SetOption nxSetOption = RedisStringCommands.SetOption.ifAbsent();
//                // 执行NX
//                Boolean result = connection.set(keyBytes, valueBytes, expiration, nxSetOption);
//                return result;
//            }
//        };

        RedisCallback<Boolean> redisCallback = connection -> {
            // 设置Key字节数组
            byte[] keyBytes = redisTemplate.getKeySerializer().serialize(key);
            // 设置Value字节数组
            byte[] valueBytes = redisTemplate.getStringSerializer().serialize(value);
            // 设置过期时间
            Expiration expiration = Expiration.seconds(30);
            // 设置NX
            RedisStringCommands.SetOption nxSetOption = RedisStringCommands.SetOption.ifAbsent();
            // 执行NX
            Boolean result = connection.set(keyBytes, valueBytes, expiration, nxSetOption);
            return result;
        };

        // 获取Redis分布式锁
        boolean lock = (boolean) redisTemplate.execute(redisCallback);
        if(lock){
            log.info("我进入了锁！");
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                // 执行LUA脚本校验Redis锁的值是否为该线程的值, 是才删除, 防止删除了其他线程的锁
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                                "    return redis.call(\"del\",KEYS[1])\n" +
                                "else\n" +
                                "    return 0\n" +
                                "end";
                RedisScript<Boolean> redisScript = RedisScript.of(script, Boolean.class);
                redisTemplate.execute(redisScript, Arrays.asList(key), value);
            }
        }

        log.info("我已经执行完成！");
        return "我已经执行完成！";
    }
}
