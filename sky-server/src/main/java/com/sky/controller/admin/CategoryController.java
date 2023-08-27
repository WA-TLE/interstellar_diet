package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/admin/category")
@Api(tags = "分类相关接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result<String> save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类：{}", categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分页查询：{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除分类")
    public Result<String> deleteById(Long id){
        log.info("删除分类：{}", id);
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result<String> update(@RequestBody CategoryDTO categoryDTO){
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * 启用、禁用分类
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用分类") //  这里因为 status 和 {status} 名称一致, 所以可以不写参数, 但是 PathVariable 注解要写, 否者无法识别
    public Result<String> startOrStop(@PathVariable("status") Integer status, Long id, String test){
        categoryService.startOrStop(status,id);
        return Result.success();
    }

    /**
     * 根据类型查询分类<br>
     *
     * 2023-8-26 17:44:34<br>
     * 终于明白彻底明白这个接口的作用了
     * 所谓分类, 包括菜品分类, 套餐分类<br>
     * 菜品分类: 时令鲜蔬, 传统主食, 酒水饮料等......<br>
     * 套餐分类: 人气套餐, 商务套餐等....<br>
     * <p>
     *      而关键点在于 人气套餐, 商务套餐, 时令鲜蔬, 传统主食, 酒水饮料
     *      它们都在一张表里面存储着, 这个表叫做分类表!!!
     *      辨别他们的区分他们的办法就是他们的 type 字段
     *
     * </p>
     * <p>
     *     那么这个接口是什么时候调用呢??<br>
     *     答: 在新建菜品, 新建套餐的时候(当然修改, 用户端访问也会调用...)
     *     在我们进行着新建的时候, 就要根据"分类"来指定我们新建 菜品or套餐 的定位
     * </p>
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> list(Integer type){
        log.info("查询分类 {}", type);
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}
