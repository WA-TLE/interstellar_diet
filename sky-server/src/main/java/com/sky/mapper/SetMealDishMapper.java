package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: dy
 * @Date: 2023/8/23 18:46
 * @Description:
 */
@Mapper
public interface SetMealDishMapper {
    List<Long> getSetMealIdByDishIds(List<Long> dishIds);
}
