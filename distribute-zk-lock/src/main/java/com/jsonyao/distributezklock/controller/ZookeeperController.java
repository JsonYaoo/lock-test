package com.jsonyao.distributezklock.controller;

import com.jsonyao.distributezklock.lock.ZkLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * Zookeeper分布式锁测试类
 */
@RestController
@Slf4j
public class ZookeeperController {

    @Autowired
    private CuratorFramework curatorClient;

    /**
     * 自己实现的Zookeeper分布式锁测试
     * @return
     */
    @RequestMapping("/zkLock")
    public String zkLock(){
        log.info("我进入了方法！");
        try (ZkLock zkLock = new ZkLock()){
            if (zkLock.getLock("order")){
                log.info("我获得了锁");
                Thread.sleep(10000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("方法执行完成！");
        return "方法执行完成！";
    }

    /**
     * Curator实现的Zookeeper分布式锁测试
     * @return
     */
    @RequestMapping("/curatorLock")
    public String curatorLock(){
        log.info("我进入了方法！");

        // 创建分布式锁
        InterProcessMutex lock = new InterProcessMutex(curatorClient, "/order");

        // 获取分布式锁
        try {
            if(lock.acquire(30, TimeUnit.SECONDS)){
                try {
                    log.info("我获得了锁!!!");
                    Thread.sleep(10000);
                }finally {
                    lock.release();
                    log.info("我释放了锁!!!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("方法执行完成！");
        return "方法执行完成！";
    }
}
