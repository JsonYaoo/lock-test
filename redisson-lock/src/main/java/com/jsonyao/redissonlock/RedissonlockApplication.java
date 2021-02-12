package com.jsonyao.redissonlock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Redisson Lock测试应用
 */
@SpringBootApplication
public class RedissonlockApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedissonlockApplication.class, args);
    }
}
