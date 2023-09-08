package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * 根据订单 id 查询订单明细
     * @param ordersId
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long ordersId);
}
