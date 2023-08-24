package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Autowired
    private SetMealDishMapper setMealDishMapper;

    /**
     * 新增菜品和对应口味
     *
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

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {

        //  Mybatis 提供的分页查询
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
//

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 删除菜品
     *
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //  在 service 层先填写逻辑, 然后再使用 Java 代码 "翻译"

        //  先判断能否删除---菜品是否起售
        //  这里采用的是但凡有一个菜品起售, 该删除操作就不能完成
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);

            if (Objects.equals(dish.getStatus(), StatusConstant.ENABLE)) {
                //  当前套餐处于起售状态, 抛出异常
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //  先判断能否删除---是否关联套餐
        List<Long> setMealIds = setMealDishMapper.getSetMealIdByDishIds(ids);

        if (setMealIds != null && setMealIds.size() > 0) {
            //  要删除的菜品关联的套餐,无法删除, 抛出相关异常
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //  可以删除菜品
        //  将口味一块删除
        //  直接在口味表中查询是否有相关菜品的 id
        for (Long id : ids) {
            //  删除菜品
            dishMapper.deleteById(id);

            //  删除菜品相关口味
            dishFlavorMapper.deleteByDishId(id);
        }


    }

    /**
     * 根据 id 查询菜品
     *
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id) {

        //  根据 id 查询菜品
        Dish dish = dishMapper.getById(id);
        //  根据 id 查询所关联的口味

        List<DishFlavor> dishFlavors = dishFlavorMapper.getById(id);

        //  将数据封装到 VO 对象
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);

        //  返回对象
        return dishVO;
    }

    /**
     * 修改菜品
     *
     * @param dishDTO
     */
    @Transactional  //  这里要开启事务注解
    public void updateWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //  修改菜品
        dishMapper.update(dish);

        //  修改口味
        //  得到提交到的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();

        //  先将原有的口味删除
        dishFlavorMapper.deleteByDishId(dish.getId());

        //  设置所提交口味的 dishId 属性值
        if (flavors != null && flavors.size() > 0) {
            for (DishFlavor dishFlavor : flavors) {
                dishFlavor.setDishId(dishDTO.getId());
            }
            //  批量插入口味数据
            dishFlavorMapper.insertBatch(flavors);
        }
        //  没了???
    }

    /**
     * 修改菜品状态
     *
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {

        //  这里动用查询操作不太好, 我们仅仅是更新状态
        //  没有必要从数据库中把所有要更改的数据拿出来
        //  这里我们采用直接 new 一个的方式 (从数据库中查询也需要时间)
        //  Dish dish = dishMapper.getById(id);

        //  这里才是正规的操作, 在更改状态的时候, 因为之前已经写过 Update 了
        //  所以这里我们之间用即可 (否者的话, 修改时间你怎么办???)
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
        //  dishMapper.setStatus(status, id);

        //  如果该菜品停售了, 那么与该菜品关联的套餐也要停售
        if (Objects.equals(status, StatusConstant.DISABLE)) {

            ArrayList<Long> dishIds = new ArrayList<>();
            dishIds.add(id);

            //  这里得到的是所有与该菜品关联的套餐 id
            List<Long> setMeanIds = setMealDishMapper.getSetMealIdByDishIds(dishIds);

            //  这里要做的就是根据套餐的 id 来跟新套餐的状态了
            if (setMeanIds != null && setMeanIds.size() > 0) {

                /*
                    这里我们依旧不采用从数据库中查询的手段
                    而是直接 builder 一个套餐对象, 把他对应的 id 和 status 给他
                    之后直接跟新套餐即可
                 */
                for (Long setMeanId : setMeanIds) {
                    Setmeal setMeal = Setmeal.builder()
                            .id(setMeanId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    //  创建好套餐对象后直接更新套餐
                    setMealDishMapper.update(setMeal);
                }
            }


        }


    }
}













