package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: dy
 * @Date: 2023/8/23 18:46
 * @Description:
 */
@Mapper
public interface SetMealDishMapper {
    /**
     * 根据菜品 id 查询对应套餐 id
     * @param dishIds
     * @return
     */
    List<Long> getSetMealIdByDishIds(List<Long> dishIds);

    /**
     * 跟新套餐
     * @param setMeal
     */
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setMeal);
}
