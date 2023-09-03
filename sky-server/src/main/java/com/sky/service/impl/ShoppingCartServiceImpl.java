package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: dy
 * @Date: 2023/9/3 18:22
 * @Description:
 */
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //  既然是添加购物车, 那么这里就先创建一个购物车对象
        ShoppingCart shoppingCart = new ShoppingCart();
        //  拷贝属性
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //  设置用户 id
        shoppingCart.setUserId(BaseContext.getCurrentId());

        //  判断当前商品是否已经在购物车
        //  这里的 list 方法是根据条件动态查询购物车数据, 得到的结果可能不止一个
        //  所以这里的返回值我们使用 List 来接收 (当然, 这里得到的结果最多只有一条, 但还是要用 List 来接收)
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        if (shoppingCartList != null && shoppingCartList.size() > 0) {
            //  如果已经存在, 那么我们就更新数量, 使数量加 1
            //  这里根据我们的条件来查询, 得到的仅仅只能是一个数据
            //  这里的购物车对象, 直接使用上面我们创建的购物车
            shoppingCart = shoppingCartList.get(0);
            shoppingCart.setNumber(shoppingCart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(shoppingCart);
        } else {
            //  数据未存在购物车, 数量为 1

            //  判断当前添加到购物车的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();

            if (dishId != null) {
                //  添加的数据为菜品
                Dish dish = dishMapper.getById(dishId);

                //  设置对应的属性
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {
                //添加到购物车的是套餐
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            //  公共数据的填充放到这里
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }


    }
}
