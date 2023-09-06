package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: dy
 * @Date: 2023/9/6 21:22
 * @Description:
 */
@Mapper
public interface OrdersMapper {
    /**
     * 向订单表里插入一条数据
     * @param order
     */
    public void insert(Orders order);
}
