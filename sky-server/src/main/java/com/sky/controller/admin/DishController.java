package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @Author: dy
 * @Date: 2023/8/22 20:45
 * @Description: 菜品相关接口
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /*
        为了解决缓存一致性, 这里需要更改代码
        但凡数据中的数据发生了改变, 我们就直接删除对应的缓存 (简单暴力)
     */

    /**
     * 新增菜品
     *
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")

    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品: {}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        // 将与之对应的缓存删除
        //  获取对应的分类 id (咱们这里, 一个菜品只能对应一个分类)
        Long categoryId = dishDTO.getCategoryId();

        //  构造 key
        String key = "dish_" + categoryId;

        //  删除对应的缓存
        cleanCache(key);

        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> queryPage(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询: {}", dishPageQueryDTO);

        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("删除菜品: {}", ids);
        dishService.deleteBatch(ids);

        /*
            因为涉及批量删除, 更改缓存太麻烦
            这里我们之间删除所有与 dish 相关的缓存
         */

        //将所有的菜品缓存数据清理掉，所有以dish_开头的key
        cleanCache("dish_*");


        return Result.success();
    }

    /**
     * 根据 id 查询菜品 <br>
     * 这里查询的菜品是用来展示的, 所以我们用的是 dishVo 对象
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据 id 查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据 id 查询菜品: {}", id);

        DishVO dishVO = dishService.getByIdWithFlavor(id);

        return Result.success(dishVO);
    }


    /**
     * 修改菜品
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")   //  这里的参数类型最开始写错了, 我使用的是 VO (以后要具体分析了)
    public Result update(@RequestBody DishDTO dishDTO) {
        /*
            其实这里用什么参数的话, 我们可以这样分析
            VO 和 DTO 的差别主要就是
            VO 展示的有 更新时间, 分类没名称
            像这两个属性, 在数据传输的时候压根就用不着
            更新时间会自动更改的
            分类名称不需要, 我们有分类 id
         */
        log.info("修改菜品: {}", dishDTO);

        dishService.updateWithFlavor(dishDTO);

        //将所有的菜品缓存数据清理掉，所有以dish_开头的key
        cleanCache("dish_*");

        return Result.success();
    }


    /**
     * 修改菜品状态
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("修改菜品状态: status = {}, id = {}", status, id);

        dishService.startOrStop(status, id);

        //将所有的菜品缓存数据清理掉，所有以dish_开头的key
        cleanCache("dish_*");

        return Result.success();
    }


    /**
     * 根据分类 id 查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类 id 查询菜品")
    public Result<List<Dish>> list(Long categoryId) {
        log.info("根据分类 id 查询菜品: {}", categoryId);
        List<Dish> list = dishService.list(categoryId);

        return Result.success(list);
    }


    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        //  根据 keys 集合删除对应的缓存
        redisTemplate.delete(keys);
    }







}
