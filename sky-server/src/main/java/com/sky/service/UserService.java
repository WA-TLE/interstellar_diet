package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

/**
 * @Author: dy
 * @Date: 2023/9/1 11:54
 * @Description:
 */

public interface UserService {

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);


}
