package com.json.singlelock.test;

import com.jsonyao.singlelock.SinglelockApplication;
import com.jsonyao.singlelock.service.OrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SinglelockApplication.class})
public class SinglelockApplicationTests {

    @Autowired
    private OrderService orderService;

    @Test
    public void concurrentOrder() throws InterruptedException {
//        Thread.sleep(60000);
        CountDownLatch cdl = new CountDownLatch(5);
        CyclicBarrier cyclicBarrier = new CyclicBarrier(5);

        ExecutorService es = Executors.newFixedThreadPool(5);
        for (int i =0;i<5;i++){
            es.execute(()->{
                try {
                    // 等待所有线程初始化完成, 然后一起执行
                    cyclicBarrier.await();
                    Integer orderId = orderService.createOrder06();
                    System.out.println("订单id："+orderId);
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    cdl.countDown();
                }
            });
        }

        // 主程序等待线程执行完毕, 方法结束后关闭数据源
        cdl.await();

        // 关闭线程池
        es.shutdown();
    }

}
