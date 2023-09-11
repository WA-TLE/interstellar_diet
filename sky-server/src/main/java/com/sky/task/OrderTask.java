package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @Author: dy
 * @Date: 2023/9/10 20:50
 * @Description:
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder() {
        log.info("处理支付超时订单: {}", new Date());

        /*
            这里的业务逻辑为:
                订单超过 15 min 未支付, 那么我就就取消订单
            但是我们怎么实现这个方法呢? 不能说统计每个客户下单的时间, 然后再 15 min 后处理吧
            答:
                可以让这个方法没分钟执行一次, 从数据库中查询订单, 然后与当前时间对比
                判断该订单是否超时
         */

        //  获取当前时间之前的 15min 的时间 time, 在 time 之前的订单都为超时订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = ordersMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);

        //  遍历之前先判断是否为空, 这是个好习惯
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("订单超时, 自动取消");
                ordersMapper.update(orders);
            }
        }

    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder() {
        log.info("处理派送中订单: {}", new Date());
        //  获取临界时间
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = ordersMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                ordersMapper.update(orders);
            }
        }
    }


}
