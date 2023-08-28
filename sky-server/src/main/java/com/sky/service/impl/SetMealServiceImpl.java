package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @Author: dy
 * @Date: 2023/8/25 19:21
 * @Description: 套餐相关业务实现
 */
@Service
@Slf4j
public class SetMealServiceImpl implements SetMealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetMealDishMapper setMealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐, 同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //  向套餐中添加数据
        setmealMapper.insert(setmeal);

        //  取出套餐生成的 id
        Long setmealId = setmeal.getId();

        //  取出绑定该套餐的菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        //  设置这些菜品所对应的套餐 id
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });

        //  保存套餐和菜品的关系
        setMealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //  mybatis 提供的分页插件
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);



        return new PageResult(page.getTotal(), page.getResult());
    }


    /**
     * 批量删除套餐
     * @param ids
     */
    public void deleteByIds(List<Long> ids) {

        //  这里假定套餐起售状态不能删除
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);

            if (Objects.equals(setmeal.getStatus(), StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }


        for (Long setmealId : ids) {
            setmealMapper.deleteByIds(setmealId);
            log.info("删除关联的数据: {}", setmealId);
            setMealDishMapper.deleteBySetMealId(setmealId);
        }


    }

    /**
     * 根据 id 查询套餐
     * @param id
     * @return
     */
    public SetmealVO getById(Long id) {

        //  根据 id 查询套餐
        Setmeal setmeal = setmealMapper.getById(id);

        //  根据套餐 id 查询套餐所对应的菜品
        List<SetmealDish> setMealDish = setMealDishMapper.getBySetMealId(id);

        //  创建视图对象
        SetmealVO setmealVO = new SetmealVO();

        //  拷贝属性
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setMealDish);

        return setmealVO;
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        //  获取菜品数据, 并拷贝到 setMeal
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //  得到套餐与菜品的数据
        List<SetmealDish> setMealDishes = setmealDTO.getSetmealDishes();

        //  设置菜品和套餐关系
        for (SetmealDish setMealDish : setMealDishes) {
            setMealDish.setSetmealId(setmeal.getId());
        }

        //  更新套餐数据
        setmealMapper.update(setmeal);

        //  先清理对应的套餐菜品数据
        setMealDishMapper.deleteBySetMealId(setmeal.getId());

        //  新建菜品套餐数据
        setMealDishMapper.insertBatch(setMealDishes);


    }

    /**
     * 套餐起售停售
     * @param id
     */
    public void startOrStop(Integer status, Long id) {

        if (Objects.equals(status, StatusConstant.ENABLE)) {
            List<Dish> dishList = dishMapper.getBySetMealId(id);

            if (dishList != null && dishList.size() > 0) {
                for (Dish dish : dishList) {
                    if (StatusConstant.DISABLE == dish.getStatus()) {
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                }
            }
        }

        //  创建套餐对象
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();

        //  根据套餐 id 修改套餐状态
        setmealMapper.update(setmeal);
        //  首先先判断当前套餐所包含的所用菜品是否起售

    }
}
