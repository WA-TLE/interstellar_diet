package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    /**
     *  批量保存套餐和菜品的关联关系
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id删除套餐和菜品的关联关系
     * @param setMealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setMealId}")
    void deleteBySetMealId(Long setMealId);


    /**
     * 根据套餐 id 查询套餐所对应的菜品
     * @param setMealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setMealId}")
    List<SetmealDish> getBySetMealId(Long setMealId);
}
