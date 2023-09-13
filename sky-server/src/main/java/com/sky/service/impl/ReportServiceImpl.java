package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: dy
 * @Date: 2023/9/12 10:27
 * @Description:
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        //  获取 dateList 数据
        //  这里采用的思路为: 先将 begin ~ end 日期的数据添加到 list 集合里
        //  然后再将集合里的数据转化为 String, 中间以 ',' 分隔
        ArrayList<LocalDate> dataList = new ArrayList<>();
        dataList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dataList.add(begin);
        }
        String str = StringUtils.join(dataList, ',');


        ArrayList<Double> turnoverList = new ArrayList<>();
        //  获取 turnoverList 数据
        for (LocalDate localDate : dataList) {
            //  遍历日期, 获取查询起始时间, 终止时间
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            //  这里采用构建一个 Map 的方式来传输对象
            Map<Object, Object> hashMap = new HashMap<>();
            hashMap.put("begin", beginTime);
            hashMap.put("end", endTime);
            hashMap.put("status", Orders.COMPLETED);

            Double turnover = ordersMapper.getSumTurnover(hashMap);

            //  假如当天没有营业
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        String turnoverStr = StringUtils.join(turnoverList, ',');

        return TurnoverReportVO.builder()
                .dateList(str)
                .turnoverList(turnoverStr)
                .build();
    }

    /**
     * 用户统计接口
     *
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        //  构建日期列表
        List<LocalDate> dataList = new ArrayList<>();
        dataList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dataList.add(begin);
        }

        //  构建新增用户列表
        //  select count(id) from user where create_time > ? and create_time < ?
        List<Integer> newUserList = new ArrayList<>();

        //  构建用户总数列表
        //  select count(id) from user where create_time < ?
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dataList) {
            //  转化为 LocalDateTime 类型的数据
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //  封装 Map 对象进行查询
            Map<Object, Object> map = new HashMap<>();
            map.put("end", endTime);

            //  这里先查询总的用户数量
            Integer totalUser = userMapper.getUserStatistics(map);

            //  这里查询当天新增用户数量
            map.put("begin", beginTime);
            Integer newUser = userMapper.getUserStatistics(map);

            newUserList.add(newUser);
            totalUserList.add(totalUser);

        }

        log.info("新增用户数量列表: {}", newUserList);
        log.info("用户总数列表: {}", totalUserList);

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dataList, ','))
                .newUserList(StringUtils.join(newUserList, ','))
                .totalUserList(StringUtils.join(totalUserList, ','))
                .build();

    }

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {

        //  构建日期列表
        List<LocalDate> dataList = new ArrayList<>();
        dataList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dataList.add(begin);
        }
        Integer totalOrders = 0;
        Integer validOrders = 0;
        ArrayList<Integer> orderCountList = new ArrayList<>();
        ArrayList<Integer> validOrderCountList = new ArrayList<>();

        //  根据日期查询订单
        for (LocalDate date : dataList) {
            //  转化为 LocalDateTime 类型
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //  需要用到的查询条件封装到 Map 集合里面
            HashMap<Object, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);

            //  查询当天所有订单数
            Integer todayTotalOrder = ordersMapper.getByStatusAndOrderTime(map);

            //  查询当天有效订单数
            map.put("status", Orders.COMPLETED);
            Integer todayValidOrder = ordersMapper.getByStatusAndOrderTime(map);

            //  计算订单总数/有效订单数 (当天订单数相加)
            totalOrders += todayTotalOrder;
            validOrders += todayValidOrder;

            //  封装到集合
            orderCountList.add(todayTotalOrder);
            validOrderCountList.add(todayValidOrder);
        }

//        //时间区间内的总订单数
//        Integer totalOrders = orderCountList.stream().reduce(Integer::sum).get();
//        //时间区间内的总有效订单数
//        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();


        //  1. 你这种 int 转 double 的方法是算法竞赛时的做法
        //  2. 如果总营业额为 0 呢? 是不是就会抛异常了

        double orderCompletionRate = 0.0;
        if (totalOrders != 0) {
//            orderCompletionRate =  ((double)validOrders / (double) totalOrders);
            orderCompletionRate = validOrders.doubleValue() / totalOrders;
        }

        //  封装 VO 对象, 并返回
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dataList, ','))
                .orderCountList(StringUtils.join(orderCountList, ','))
                .validOrderCountList(StringUtils.join(validOrderCountList, ','))
                .orderCompletionRate(orderCompletionRate)
                .totalOrderCount(totalOrders)
                .validOrderCount(validOrders)
                .build();
    }
}
