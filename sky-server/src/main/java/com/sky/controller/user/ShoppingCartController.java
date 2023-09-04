package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: dy
 * @Date: 2023/9/3 18:16
 * @Description: 购物车
 */
@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api("c端-购物车接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车: {}", shoppingCartDTO);

        shoppingCartService.add(shoppingCartDTO);

        return Result.success();
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list() {
        /*
            自己实现出现的问题
            查看购物车, 应该是当前用户的购物车, 那么我们就需要 userId 这个字段
            但是前端请求并没有给我们对应的参数 (这并不意味着要我们查询购物车里的全部数据!)
            所有的数据都在购物车表里面储存着, 我们不能把别人的数据也一块查询出来吧
            所以应该加个限制条件 userId
            然后直接调用我们写的动态查询购物车的方法 list
         */

        log.info("查看购物车");
        List<ShoppingCart> shoppingCartList =  shoppingCartService.showShoppingCart();
        return Result.success(shoppingCartList);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result clean() {
        log.info("清空购物车");
        shoppingCartService.cleanShoppingCart();
        return Result.success();
    }


}
