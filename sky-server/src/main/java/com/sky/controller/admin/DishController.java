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
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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











}
