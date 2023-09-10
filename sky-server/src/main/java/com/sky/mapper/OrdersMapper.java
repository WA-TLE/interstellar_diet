package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @Author: dy
 * @Date: 2023/9/6 21:22
 * @Description:
 */
@Mapper
public interface OrdersMapper {
    /**
     * 向订单表里插入一条数据
     *
     * @param order
     */
    public void insert(Orders order);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 根据订单 id 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    /**
     *  根据条件查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据订单 id 查询订单
     *
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);


    @Select("select * from orders")
    Page<Orders> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select count(id) from orders where status= #{status}")
    Integer countStatus(Integer status);
}
