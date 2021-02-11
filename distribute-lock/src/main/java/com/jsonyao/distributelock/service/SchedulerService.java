package com.jsonyao.distributelock.service;

import com.jsonyao.distributelock.lock.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 定时任务测试服务: 必须开启定时任务模块装配, 且任务交给Spring管理, 定时任务才能生效
 */
@Service
@Slf4j
public class SchedulerService {

    /**
     * 测试定时任务与分布式锁
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void sendSms(){
        try(RedisLock redisLock = new RedisLock("autoSms", 30L)) {
            if(redisLock.getLock()){
                log.info("向138xxxxxxxx发送短信！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
