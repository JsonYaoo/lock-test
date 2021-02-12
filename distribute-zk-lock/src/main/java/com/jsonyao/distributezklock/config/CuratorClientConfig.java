package com.jsonyao.distributezklock.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Curator Client配置类
 */
@Configuration
public class CuratorClientConfig {

    /**
     * 需要注意的是, 这样CuratorClient交给了Spring管理, 生命周期就是Spring的生命周期了, 也就是CuratorClient会一直保持长连接状态
     * @return
     */
    @Bean(name = "curatorClient", initMethod = "start", destroyMethod = "close")
    public CuratorFramework curatorClient(){
        // 重试策略: 每隔1s, 重试3次
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);

        // 创建Curator客户端
        CuratorFramework curatorClient = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);
        return curatorClient;
    }
}
