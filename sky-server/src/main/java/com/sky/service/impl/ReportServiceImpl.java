package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: dy
 * @Date: 2023/9/12 10:27
 * @Description:
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrdersMapper ordersMapper;

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
}
