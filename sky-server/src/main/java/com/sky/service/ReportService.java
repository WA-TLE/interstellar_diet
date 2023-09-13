package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

/**
 * @Author: dy
 * @Date: 2023/9/12 10:25
 * @Description:
 */

public interface ReportService {

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户统计接口
     *
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);


    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);

    /**
     * 查询销量排名top10
     *
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getTop10Statistics(LocalDate begin, LocalDate end);
}
