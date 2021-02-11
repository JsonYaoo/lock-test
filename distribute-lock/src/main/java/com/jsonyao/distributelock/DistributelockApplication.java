package com.jsonyao.distributelock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 分布式锁应用
 */
@SpringBootApplication
public class DistributelockApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributelockApplication.class, args);
    }
}
