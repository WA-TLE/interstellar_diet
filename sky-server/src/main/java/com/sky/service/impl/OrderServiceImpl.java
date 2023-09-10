package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: dy
 * @Date: 2023/9/6 21:11
 * @Description:
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrdersMapper orderMapper;   //  操作订单表
    @Autowired
    private OrderDetailMapper orderDetailMapper;    //  操作订单明细表
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;  //  购物车相关操作
    @Autowired
    private AddressBookMapper addressBookMapper;    //  地址簿相关操作
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;

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
        //      怎么判断呢? OrdersSubmitDTO 对应的有地址簿 id

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

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        /*JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );*/

        JSONObject jsonObject = new JSONObject();


        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            log.info("订单已支付: ");
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //  这个目前没有用着
        HashMap<Object, Object> map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号: " + outTradeNo);


    }

    /**
     * 历史订单分页查询
     *
     * @param pageNUm
     * @param pageSize
     * @param status
     * @return
     */
    public PageResult pageQuery(Integer pageNUm, Integer pageSize, Integer status) {
        // 首先使用 Mybatis 的分页查询方法
        PageHelper.startPage(pageNUm, pageSize);
       /*
            关于这里的分页查询, 在做之前, 我们要搞明白, 返回的 Vo 对象是什么
            然后才能对应的封装对象
            就像这个, 前端需要的是 OrderVo 它继承了 Orders, 拥有它的全部属性
            另外还多了一个 private List<OrderDetail> orderDetailList; 属性
            把这些给前端, 他就可以展示了
        */

        //  这里咱们要做分页条件查询, 而条件我们使用 OrdersPageQueryDTO 来封装
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        //  返回结果是固定的
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        ArrayList<OrderVO> list = new ArrayList<>();

        if (page != null && page.size() > 0) {
            for (Orders orders : page) {
                //  根据订单查询订单明细
                //  获得订单 id
                Long ordersId = orders.getId();
                //  根据订单 id 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(ordersId);
                /*
                    这里终于是搞明白了
                    原来 OrderVo 继承了 Order!!!!
                    我说怎么从那里搞出来那么多属性
                    -------------------------------
                    另外, 这个接口的调用频率挺高的, 个人中心里面有最近订单, 历史订单里面也会调用
                 */
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);
                log.info("orderVo 的 orderDishes: {}", orderVO.getOrderDishes());
                log.info("orderVo 的 orderDetailList: {}", orderVO.getOrderDetailList());
                list.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), list);
    }

    /**
     * 查看订单详情
     *
     * @param id
     * @return
     */
    public OrderVO selectOrderDetailById(Long id) {
        //  -----------  这里忘记将订单的信息复制给 OrderVo 了 ------------

        OrderVO orderVO = new OrderVO();
        //  根据订单 id 查询订单
        Orders orders = orderMapper.getById(id);
        //  查询订单详情
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        //  将订单及其详情封装到 OrderVo
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 取消订单
     *
     * @param id
     */
    public void userCancelById(Long id) throws Exception {
        /*
            业务规则
            - 待支付和待接单状态下，用户可直接取消订单
            - 商家已接单状态下，用户取消订单需电话沟通商家
            - 派送中状态下，用户取消订单需电话沟通商家
            - 如果在待接单状态下取消订单，需要给用户退款
            - 取消订单后需要将订单状态修改为“已取消”
         */

        //  1. 查询订单, 确定其目前的状态
        Orders ordersDB = orderMapper.getById(id);

        //  2. 判断订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //  3. 判断订单状态
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(id);

        //  订单处于待接单状态下接单, 需要进行退款操作
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
           /* weChatPayUtil.refund(
                    ordersDB.getNumber(), //商户订单号
                    ordersDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额*/
            orders.setPayStatus(Orders.REFUND);
        }
        //  更新订单状态
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     *
     * @param id
     */
    @Transactional
    public void repetition(Long id) {
        /*
            这里是业务逻辑理解错了, 是因为我不常点外卖?
            这里的再来一单, 要求的不是重新提交订单, 然后支付
            而是把订单里面的数据重新加入购物车....
            我完全理解错业务逻辑了
         */

        /*  错误逻辑
        //  根据订单 id 查询订单
        Orders order = orderMapper.getById(id);
        //  根据订单 id 查询订单明细
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        //  插入订单数据
        orderMapper.insert(order);
        //  插入订单明细数据
        orderDetailMapper.insertBatch(orderDetails);

        //  封装 vo 对象
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();

        return orderSubmitVO;*/
        /*
         Java 8 中的 Stream API 写法
        //  查询当前用户 id
        Long userId = BaseContext.getCurrentId();

        //  根据订单 id 查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // 将订单详情对象转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());*/


        //  查询当前用户 id
        Long userId = BaseContext.getCurrentId();

        // 根据订单 id 查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // 创建一个空购物车对象列表
        List<ShoppingCart> shoppingCartList = new ArrayList<>();

        // 遍历订单详情列表
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            // 将购物车对象添加到购物车列表中
            shoppingCartList.add(shoppingCart);
        }

        shoppingCartMapper.insertBatch(shoppingCartList);


    }

    /**
     * 根据条件查询订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult conditionSearch1(OrdersPageQueryDTO ordersPageQueryDTO) {

        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.conditionSearch(ordersPageQueryDTO);

        ArrayList<OrderVO> list = new ArrayList<>();
        for (Orders orders : page) {
            OrderVO orderVO = new OrderVO();

            //  根据订单 id 查询订单详情
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());

            //  属性赋值
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDetailList(orderDetails);
            list.add(orderVO);
        }

        return new PageResult(page.getTotal(), list);
    }

    /**
     * 根据条件查询订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

    /**
     * 各个状态的订单数量统计
     *
     * @return
     */
    public OrderStatisticsVO statistics() {

        // 根据状态，分别查询出待接单、待派送、派送中的订单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        // 将查询出的数据封装到orderStatisticsVO中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;

    }

    /**
     * 商家接单
     * @param ordersConfirmDTO
     */
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        //  这里完全用不着属性拷贝, 仅仅只需要两个参数而已
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }
}
