package com.sky.service;

import com.sky.dto.DishDTO;

/**
 * @Author: dy
 * @Date: 2023/8/22 20:51
 * @Description:
 */
public interface DishService {
    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);
}
