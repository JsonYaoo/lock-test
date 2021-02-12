package com.jsonyao.distributezklock.test;

import com.jsonyao.distributezklock.DistributezklockApplication;
import com.jsonyao.distributezklock.lock.ZkLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * Zookeeper实现分布式锁应用测试类
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DistributezklockApplication.class})
@Slf4j
public class DistributezklockApplicationTests {

    /**
     * 测试自己实现的Zookeeper分布式锁
     */
    @Test
    public void testZkLock() {
        try (ZkLock zkLock = new ZkLock()){
            boolean result = zkLock.getLock("order");
            log.info("获得锁的结果："+ result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试Curator实现的Zookeeper分布式锁
     */
    @Test
    public void testCuratorLock() {
        // 重试策略: 每隔1s, 重试3次
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);

        // 创建Curator客户端
        CuratorFramework curatorClient = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);

        // 启动连接
        curatorClient.start();
        log.info("我打开了Curator连接!!!");

        // 创建分布式锁
        InterProcessMutex lock = new InterProcessMutex(curatorClient, "/order");

        // 获取分布式锁
        try {
            if(lock.acquire(30, TimeUnit.SECONDS)){
                try {
                    log.info("我获得了锁!!!");
                }finally {
                    lock.release();
                    log.info("我释放了锁!!!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(curatorClient != null){
                curatorClient.close();
                log.info("我关闭了Curator连接!!!");
            }
        }
    }
}
