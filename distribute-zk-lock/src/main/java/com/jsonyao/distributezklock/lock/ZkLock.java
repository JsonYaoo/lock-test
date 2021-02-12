package com.jsonyao.distributezklock.lock;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Zookeeper实现的分布式锁
 */
@Slf4j
public class ZkLock implements AutoCloseable, Watcher {

    /**
     * Zookeeper连接
     */
    private ZooKeeper zooKeeper;

    /**
     * Zookeeper结点: /order/order_00000001
     */
    private String znode;

    /**
     * Zookeeper路径分割符
     */
    private static final String PATH_SPLITTER = "/";

    /**
     * 构造方法
     */
    public ZkLock() {
        try {
            this.zooKeeper = new ZooKeeper("localhost:2181", 10000, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取分布式锁
     * @param businessCode
     * @return
     */
    public boolean getLock(String businessCode){
        try {
            // 创建业务根结点: 无需密码登录、持久无序结点
            String rootPath = PATH_SPLITTER + businessCode;
            Stat stat = zooKeeper.exists(rootPath, false);
            if(stat == null){
                zooKeeper.create(rootPath, businessCode.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            // 创建瞬时有序结点: /order/order_00000001
            znode = zooKeeper.create(rootPath + PATH_SPLITTER + businessCode + "_", businessCode.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            // 获取业务根节点下所有的子结点"/order/order_00000001", 然后升序排序
            List<String> childrenNodes = zooKeeper.getChildren(rootPath, false);
            Collections.sort(childrenNodes);

            // 判断是否为序号最小的子结点, 如果是则返回true, 表示获取到分布式锁
            String lastNode = null;
            if(znode.endsWith(lastNode = childrenNodes.get(0))){
                return true;
            }

            // 加synchronized关键字是: wait()的通用写法, 同时为了与notify互斥, 避免监听~wait过程中, 前一个结点就删除了导致notify比wait方法执行早,
            // 从而导致的wait()无限阻塞下去, 从而提前了监听前一个结点的代码, 保证了监听与wait的原子性
            synchronized (this) {
                // 如果不是第一个子节点, 则需要监听前一个子结点
                for (String node : childrenNodes) {
                    if(!znode.endsWith(node)){
                        lastNode = node;
                    }else {
                        zooKeeper.exists(rootPath + PATH_SPLITTER + lastNode, true);
                        break;
                    }
                }

                // 监听后该线程进入阻塞状态
                this.wait();
                
                // 线程被唤醒后, 说明前一个结点已被删除: 由于在并发情况下, 各结点是顺序监听, 所以这里也就是代表本结点成为了第一个结点, 因此可以返回true, 说明获取到了分布式锁
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 释放分布式锁
     */
    public boolean unlock(){
        try {
            if(StringUtils.hasText(znode)){
                // -1代表匹配任意版本
                zooKeeper.delete(znode, -1);
                log.info("我已经释放了锁");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(zooKeeper != null){
                try {
                    // 关闭Zookeeper连接
                    zooKeeper.close();
                    log.info("我已经关闭了Zookeeper连接");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    /**
     * JDK 1.8特性: Try结束时自动调用该方法
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        this.unlock();
    }

    /**
     * Zookeeper观察器监听方法
     * @param watchedEvent
     */
    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent != null && Event.EventType.NodeDeleted == watchedEvent.getType()){
            // 加synchronized关键字是: notify()的通用写法, 同时为了与wait互斥, 避免监听~wait过程中, 前一个结点就删除了导致notify比wait方法执行早, 从而导致的wait()无限阻塞下去
            synchronized (this) {
                this.notify();
            }
        }
    }
}
