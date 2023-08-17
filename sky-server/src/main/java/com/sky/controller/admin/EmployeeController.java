package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("员工登陆")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工退出")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     * @return
     */
    @ApiOperation("新增员工")
    @PostMapping
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工: {}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")    //  这里不要忘记追加参数哦
    @ApiOperation("员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {

        log.info("员工分页查询, 参数为: {}", employeePageQueryDTO);
        //  我们规定, 所以分页查询的结果都使用 pageResult 来封装
        //  total records
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);

        //  直接使用 Result 的静态方法
        return Result.success(pageResult);
    }

    /**
     * 员工状态修改
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")    //  2023-8-17 21:06:23, 这里的请求写成了 Get!!! 调试了好久好久
    @ApiOperation("员工状态修改")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        //  打印一个日志
        log.info("启用员工账号: {}, {}", status, id);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 根据 id 查询员工
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据 id 查询员工")
    public Result<Employee> getById(@PathVariable Long id) {
        log.info("查询 id 为 {} 的员工", id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }


    /**
     * 更新员工信息
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("更新员工信息")
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("更新员工编号为 {} 的信息", employeeDTO.getId());
        employeeService.update(employeeDTO);
        return Result.success();
    }





}
