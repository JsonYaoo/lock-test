package com.jsonyao.redissonlock.test;

import com.jsonyao.redissonlock.RedissonlockApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * Redisson Lock测试应用测试类
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RedissonlockApplication.class})
@Slf4j
public class RedissonlockApplicationTests {

    /**
     * 测试Redisson Lock
     */
    @Test
    public void testRedissonLock(){
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
    }
}
