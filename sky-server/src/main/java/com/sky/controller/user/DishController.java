package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {

        //  构造 key
        String key = "dish_" + categoryId;

        //  先从缓存中查询数据, 看是否存在
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);

        if (list != null && list.size() > 0) {
            //  数据已经在缓存中了, 直接返回即可
            return Result.success(list);
        }

        //  数据不在缓存中, 从数据库中查询
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        //查询起售中的菜品
        dish.setStatus(StatusConstant.ENABLE);

        /*
            这里既然是通过分类 id 查询菜品, 那么为什么不直接传输一个 id? 而是要创建一个对象?
            答: 这样做代码逻辑更规范, 方便后续开发
                1. 我们查询菜品, 是不是菜品需要起售?
                2. 有时候我们是不是还会通过 name 进行模糊查询?
                我们这样做, 就是为了根据 dish 里面的 status, name, categoryId 来查询菜品
                也就是这一个接口做了很多事情, 后续方便复用
         */
        list = dishService.listWithFlavor(dish);

        //  将数据加入缓存
        redisTemplate.opsForValue().set(key, list);

        return Result.success(list);
    }

}
