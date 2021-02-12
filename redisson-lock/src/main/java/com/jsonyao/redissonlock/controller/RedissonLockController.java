package com.jsonyao.redissonlock.controller;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * Redisson Lock测试类
 */
@RestController
@Slf4j
public class RedissonLockController {

    @Autowired
    private RedissonClient springRedissonClient;

    @RequestMapping("/redissonLock")
    public String redissonLock(){
        log.info("我进入了方法！！");

        // 建立Redisson客户端连接
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress("redis://192.168.1.111:6379");
        singleServerConfig.setPassword("root");
        RedissonClient redissonClient = Redisson.create(config);

        // 获取Redis分布式锁
        RLock lock = redissonClient.getLock("/order");
        try {
            // 获取不到会阻塞, 30代表的是过期时间, 锁30s不放开则自动释放
            lock.lock(30, TimeUnit.SECONDS);
            log.info("我获得了锁！！！");
            Thread.sleep(10000);// 40000处理时间如果大于过期时间, 在后面释放锁的时候会抛出java.lang.IllegalMonitorStateException
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
            log.info("我释放了锁！！");
        }

        log.info("方法执行完成！！");
        return "方法执行完成！！";
    }

    /**
     * 测试Redisson分布式锁与Spring XML/SpringBoot
     * @return
     */
    @RequestMapping("/springRedissonLock")
    public String springRedissonLock(){
        log.info("我进入了方法！！");

        // 获取Redis分布式锁
        RLock lock = springRedissonClient.getLock("/order");
        try {
            // 获取不到会阻塞, 30代表的是过期时间, 锁30s不放开则自动释放
            lock.lock(30, TimeUnit.SECONDS);
            log.info("我获得了锁！！！");
            Thread.sleep(10000);// 40000处理时间如果大于过期时间, 在后面释放锁的时候会抛出java.lang.IllegalMonitorStateException
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
            log.info("我释放了锁！！");
        }

        log.info("方法执行完成！！");
        return "方法执行完成！！";
    }
}
