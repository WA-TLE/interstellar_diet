package com.sky.service;

import com.sky.dto.ShoppingCartDTO;

/**
 * @Author: dy
 * @Date: 2023/9/3 18:19
 * @Description:
 */
public interface ShoppingCartService {

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void add(ShoppingCartDTO shoppingCartDTO);
}
