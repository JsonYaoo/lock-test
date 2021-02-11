package com.jsonyao.distributelock.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 分布式环境单体锁测试
 */
@RestController
@Slf4j
public class SinglelockController {

    /**
     * 单例可重入锁
     */
    private Lock lock = new ReentrantLock();

    /**
     * 单体锁测试
     * @return
     * @throws Exception
     */
    @RequestMapping("/singleLock")
    public String singleLock() throws Exception {
        log.info("我进入了方法！");
        lock.lock();
        log.info("我进入了锁！");
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lock.unlock();
        return "我已经执行完成！";
    }
}
