package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

/**
 * @Author: dy
 * @Date: 2023/9/6 21:23
 * @Description:
 */
@Mapper
public interface OrderDetailMapper {
    /**
     * 向订单明细表中插入 n 条数据
     * @param orderDetails
     */
    void insertBatch(ArrayList<OrderDetail> orderDetails);
}
