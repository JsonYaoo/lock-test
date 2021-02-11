package com.jsonyao.distributelock.controller;

import com.jsonyao.distributelock.dao.DistributeLockMapper;
import com.jsonyao.distributelock.model.DistributeLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 分布式环境SQL分布式锁测试
 */
@RestController
@Slf4j
public class SqlDistributelockController {

    @Resource
    private DistributeLockMapper distributeLockMapper;

    /**
     * SQL分布式锁测试
     *      1) 可以锁住, 但不加Spring@Transactional默认是使用每一句的事务, 查询出来就提交了, 所以必须使用方法级别的Spring@Transactional,
     *         且必要时设置rollbackFor为Exception.class, 因为默认是RuntimeException.class
     *      2) 优点: 简单易用
     *      3) 缺点: 高并发情况下, 对数据库压力比较大
     *      4) 建议: 要使用的话, 使用锁的表/库与业务表/库分开
     * @return
     * @throws Exception
     */
    @RequestMapping("/sqlDistributeLock")
    @Transactional(rollbackFor = Exception.class)
    public String sqlDistributeLock() throws Exception {
        log.info("我进入了方法！");
        DistributeLock distributeLock = distributeLockMapper.selectSqlDistributeLock("demo");
        if(distributeLock == null){
            throw new Exception("分布式锁不存在");
        }
        log.info("我进入了锁！");
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "我已经执行完成！";
    }
}
