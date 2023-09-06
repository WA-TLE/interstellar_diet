package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: dy
 * @Date: 2023/9/6 21:11
 * @Description:
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrdersMapper orderMapper;   //  操作订单表
    @Autowired
    private OrderDetailMapper orderDetailMapper;    //  操作订单明细表
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;  //  购物车相关操作
    @Autowired
    private AddressBookMapper addressBookMapper;    //  地址簿相关操作

    /**
     * 用户提交订单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //  先把基本的业务逻辑梳理一下

        //  判断用户提交的数据是否合法, 地址簿, 购物车数据是否存在

        //  判断地址簿是否存在
        //      怎么判断呢? OrdersSubmitDTO 对应的用 地址簿 id

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            //  地址簿为空, 抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //  判断购物车中的数据是否为空
        //  获取当前登陆用户 id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart
                .builder()
                .userId(userId)
                .build();
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);

        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            //  购物车商品为空, 不能提交订单, 抛出业务异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //  向订单表中插入一条数据

        //  我们传入的是 DTO, 而要向订单表里面插入的数据为 Order, 怎么办? 赋值呗
        Orders order = new Orders();
        //  属性拷贝
        //  ----------  这里偷个懒, 直接复制老师的代码了 ------------
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setUserId(userId);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setOrderTime(LocalDateTime.now());
        //  插入数据
        orderMapper.insert(order);

        //  向订单明细表中插入 n 条数据
        //  先把这些数据查询出来吧, 可是在上面我们已经查询过购物车的数据了
        //  既然没有抛异常, 那么就说明一定有值, 可以直接使用的
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : shoppingCarts) {
            //  这里得到的是购物车数据, 我们要把他改造为订单明细数据
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetails.add(orderDetail);
        }
        //  插入 n 条订单明细数据
        orderDetailMapper.insertBatch(orderDetails);



        //  清空购物车数据
        shoppingCartMapper.deleteShoppingCart(shoppingCarts.get(0));
        //  封装 vo 对象
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();
        //  返回 vo 对象
        return orderSubmitVO;
    }
}
