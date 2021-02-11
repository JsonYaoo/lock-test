package com.jsonyao.distributelock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 分布式锁应用
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.jsonyao.distributelock.dao"})
@EnableScheduling
public class DistributelockApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributelockApplication.class, args);
    }
}
