package com.jsonyao.singlelock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 单体阶段锁测试
 */
@SpringBootApplication
@MapperScan("com.jsonyao.singlelock.dao")
public class SinglelockApplication {

    public static void main(String[] args) {
        SpringApplication.run(SinglelockApplication.class, args);
    }
}
