package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.vo.OrderSubmitVO;

/**
 * @Author: dy
 * @Date: 2023/9/6 21:10
 * @Description:
 */
public interface OrderService {
    /**
     * 用户提交订单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);
}
