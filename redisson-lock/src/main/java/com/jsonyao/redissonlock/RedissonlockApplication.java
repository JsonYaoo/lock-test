package com.jsonyao.redissonlock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * Redisson Lock测试应用
 */
@SpringBootApplication
@ImportResource(locations = "classpath:redisson.xml")
public class RedissonlockApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedissonlockApplication.class, args);
    }
}
