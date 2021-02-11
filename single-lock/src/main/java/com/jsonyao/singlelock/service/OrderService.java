package com.jsonyao.singlelock.service;

import com.jsonyao.singlelock.dao.OrderItemMapper;
import com.jsonyao.singlelock.dao.OrderMapper;
import com.jsonyao.singlelock.dao.ProductMapper;
import com.jsonyao.singlelock.model.Order;
import com.jsonyao.singlelock.model.OrderItem;
import com.jsonyao.singlelock.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

@Service
@Slf4j
public class OrderService {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private ProductMapper productMapper;
    //购买商品id
    private int purchaseProductId = 100100;
    //购买商品数量
    private int purchaseProductNum = 1;
//    @Autowired
//    private PlatformTransactionManager platformTransactionManager;
//    @Autowired
//    private TransactionDefinition transactionDefinition;

//    private Lock lock = new ReentrantLock();

    /**
     * 1. 超卖情况1: 不加锁, 直接使用spring事务, 方法内更新库存
     * => 出现超卖: 库存为0, 但有5个订单
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createOrder01() throws Exception{
        Product product = null;

        product = productMapper.selectByPrimaryKey(purchaseProductId);
        if (product==null){
            throw new Exception("购买商品："+purchaseProductId+"不存在");
        }

        //商品当前库存
        Integer currentCount = product.getCount();
        System.out.println(Thread.currentThread().getName()+"库存数："+currentCount);
        //校验库存
        if (purchaseProductNum > currentCount){
            throw new Exception("商品"+purchaseProductId+"仅剩"+currentCount+"件，无法购买");
        }

        // 1. 超卖情况1: 不加锁, 直接使用spring事务, 方法内更新库存
        Integer leftCount = currentCount - purchaseProductNum;
        product.setCount(leftCount);
        product.setUpdateTime(new Date());
        product.setUpdateUser("xxx");
        productMapper.updateByPrimaryKeySelective(product);

        Order order = getOrder(product);
        return order.getId();
    }

    /**
     * 2. 超卖情况2: 直接使用Spring事务, 利用数据库行锁, sql里增量更新库存
     * => 出现超卖: 库存为-4, 有5个订单
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createOrder02() throws Exception{
        Product product = null;

        product = productMapper.selectByPrimaryKey(purchaseProductId);
        if (product==null){
            throw new Exception("购买商品："+purchaseProductId+"不存在");
        }

        //商品当前库存
        Integer currentCount = product.getCount();
        System.out.println(Thread.currentThread().getName()+"库存数："+currentCount);
        //校验库存
        if (purchaseProductNum > currentCount){
            throw new Exception("商品"+purchaseProductId+"仅剩"+currentCount+"件，无法购买");
        }

        // 超卖情况2: 直接使用Spring事务, 利用数据库行锁, sql里增量更新库存
        productMapper.updateProductCount(purchaseProductNum,"xxx",new Date(),product.getId());

        Order order = getOrder(product);
        return order.getId();
    }

    /**
     * 3. 解决超卖情况1: 利用update行锁, 数据库增量更新后, 重新查询一次库存, 如果为负数则回滚事务
     * => 解决了超卖: 库存正常, 订单正常, 但是多次连接了数据库, 在高并发情况下是不建议的
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createOrder03() throws Exception{
        Product product = null;

        product = productMapper.selectByPrimaryKey(purchaseProductId);
        if (product==null){
            throw new Exception("购买商品："+purchaseProductId+"不存在");
        }

        //商品当前库存
        Integer currentCount = product.getCount();
        System.out.println(Thread.currentThread().getName()+"库存数："+currentCount);
        //校验库存
        if (purchaseProductNum > currentCount){
            throw new Exception("商品"+purchaseProductId+"仅剩"+currentCount+"件，无法购买");
        }

        // 3. 解决超卖情况1: 利用update行锁, 数据库增量更新后, 重新查询一次库存, 如果为负数则回滚事务
        productMapper.updateProductCount(purchaseProductNum,"xxx",new Date(),product.getId());
        product = productMapper.selectByPrimaryKey(purchaseProductId);
        if (product==null){
            throw new Exception("购买商品："+purchaseProductId + "不存在");
        }
        if(product.getCount() < 0){
            throw new Exception(purchaseProductId + "库存不能为负");
        }

        Order order = getOrder(product);
        return order.getId();
    }

    /**
     * 3. 超卖情况3: 加了Synchronized关键字对整个方法加锁
     * => 出现超卖: 虽然对方法加了Synchronized关键字, 但事务却没在锁的范围内, 所以会出现第二个线程查询缓存时, 第一个线程还没提交更新的事务, 因此出现了超卖现象
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized Integer createOrder04() throws Exception{
        Product product = null;

        product = productMapper.selectByPrimaryKey(purchaseProductId);
        if (product==null){
            throw new Exception("购买商品："+purchaseProductId+"不存在");
        }

        //商品当前库存
        Integer currentCount = product.getCount();
        System.out.println(Thread.currentThread().getName()+"库存数："+currentCount);

        //校验库存
        if (purchaseProductNum > currentCount){
            throw new Exception("商品"+purchaseProductId+"仅剩"+currentCount+"件，无法购买");
        }

        productMapper.updateProductCount(purchaseProductNum,"xxx",new Date(),product.getId());

        Order order = getOrder(product);
        return order.getId();
    }

////    @Transactional(rollbackFor = Exception.class)
//    public Integer createOrder() throws Exception{
//        Product product = null;
//
//        lock.lock();
//        try {
//            TransactionStatus transaction1 = platformTransactionManager.getTransaction(transactionDefinition);
//            product = productMapper.selectByPrimaryKey(purchaseProductId);
//            if (product==null){
//                platformTransactionManager.rollback(transaction1);
//                throw new Exception("购买商品："+purchaseProductId+"不存在");
//            }
//
//            //商品当前库存
//            Integer currentCount = product.getCount();
//            System.out.println(Thread.currentThread().getName()+"库存数："+currentCount);
//            //校验库存
//            if (purchaseProductNum > currentCount){
//                platformTransactionManager.rollback(transaction1);
//                throw
//                        new Exception("商品"+purchaseProductId+"仅剩"+currentCount+"件，无法购买");
//            }
//
//            productMapper.updateProductCount(purchaseProductNum,"xxx",new Date(),product.getId());
//            platformTransactionManager.commit(transaction1);
//        }finally {
//            lock.unlock();
//        }
//
//        TransactionStatus transaction = platformTransactionManager.getTransaction(transactionDefinition);
//        Order order = new Order();
//        order.setOrderAmount(product.getPrice().multiply(new BigDecimal(purchaseProductNum)));
//        order.setOrderStatus(1);//待处理
//        order.setReceiverName("xxx");
//        order.setReceiverMobile("13311112222");
//        order.setCreateTime(new Date());
//        order.setCreateUser("xxx");
//        order.setUpdateTime(new Date());
//        order.setUpdateUser("xxx");
//        orderMapper.insertSelective(order);
//
//        OrderItem orderItem = new OrderItem();
//        orderItem.setOrderId(order.getId());
//        orderItem.setProductId(product.getId());
//        orderItem.setPurchasePrice(product.getPrice());
//        orderItem.setPurchaseNum(purchaseProductNum);
//        orderItem.setCreateUser("xxx");
//        orderItem.setCreateTime(new Date());
//        orderItem.setUpdateTime(new Date());
//        orderItem.setUpdateUser("xxx");
//        orderItemMapper.insertSelective(orderItem);
//        platformTransactionManager.commit(transaction);
//        return order.getId();
//    }

    /**
     * 根据Product创建订单
     * @param product
     * @return
     */
    private Order getOrder(Product product) {
        Order order = new Order();
        order.setOrderAmount(product.getPrice().multiply(new BigDecimal(purchaseProductNum)));
        order.setOrderStatus(1);//待处理
        order.setReceiverName("xxx");
        order.setReceiverMobile("13311112222");
        order.setCreateTime(new Date());
        order.setCreateUser("xxx");
        order.setUpdateTime(new Date());
        order.setUpdateUser("xxx");
        orderMapper.insertSelective(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setProductId(product.getId());
        orderItem.setPurchasePrice(product.getPrice());
        orderItem.setPurchaseNum(purchaseProductNum);
        orderItem.setCreateUser("xxx");
        orderItem.setCreateTime(new Date());
        orderItem.setUpdateTime(new Date());
        orderItem.setUpdateUser("xxx");
        orderItemMapper.insertSelective(orderItem);
        return order;
    }

}
