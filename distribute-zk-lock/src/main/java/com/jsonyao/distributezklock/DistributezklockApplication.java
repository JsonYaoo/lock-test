package com.jsonyao.distributezklock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Zookeeper实现分布式锁应用
 */
@SpringBootApplication
public class DistributezklockApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributezklockApplication.class, args);
    }
}
