package com.sky.service.impl;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author: dy
 * @Date: 2023/8/22 20:52
 * @Description:
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;


    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    //  因为这里是新增的菜品和口味, 两者绑定一块了
    //  所以应该使用事务, 让他们具有原子性
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //  向菜品中插入 1 条数据
        dishMapper.insert(dish);

        //  获取 insert 语句生产的主键值
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
                log.info("设置菜品 id ....");
                log.info("设置菜品 id ....");
            });
            //  向口味中插入数据
            dishFlavorMapper.insertBatch(flavors);
        }


    }
}
