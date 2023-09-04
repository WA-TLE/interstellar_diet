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
     *
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

    /**
     * 查看购物车
     *
     * @return
     */
    public List<ShoppingCart> showShoppingCart() {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        return shoppingCartMapper.list(shoppingCart);
    }

    /**
     * 清空购物车
     */
    public void cleanShoppingCart() {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        shoppingCartMapper.deleteShoppingCart(shoppingCart);
    }

    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
     */
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //  设置当前用户 id
        shoppingCart.setId(BaseContext.getCurrentId());

        //  根据前端传入的条件, 查询当前登陆用户的的购物车数据
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        /*
            这里比较复杂的就是这了
            总体下来就是要判断一下用户删除的商品数据是否就只剩下一个
            如果剩余一个, 执行 delete 操作, 否者执行 update 操作
            当然, 从数据库中查询用户购物车相关菜品的数据是不可避免的
         */
        //  其实这里一定会有数据的, 而且查到的有且仅有一个数据
        if (shoppingCartList != null && shoppingCartList.size() > 0) {
            shoppingCart = shoppingCartList.get(0);
            if (shoppingCart.getNumber() > 1) {
                //  当前数据大于 1, 执行 update 操作
                shoppingCart.setNumber(shoppingCart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            } else {
                //  当前商品就剩一个了, 直接删除商品
                shoppingCartMapper.deleteById(shoppingCart);
            }
        }

    }
}
