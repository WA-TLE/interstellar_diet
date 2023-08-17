package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 密码进行 md5 加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //密码比对
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        //  对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);

        //  设置其他状态

        //  账号状态
        employee.setStatus(StatusConstant.ENABLE);

        //  设置默认密码 123456, 后续员工可以自己更改
        //  使用 md5 加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //  设置当前记录的创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //  设置当前记录创建人和修改人的 id
        //  获取当前记录人的 id
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());


        //  持久层
        employeeMapper.insert(employee);



    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //  这里可以简化分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        //  用了别人的分页查询, 就应该按照他的规则来
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        //  从  PageHelper.startPage 返回的对象里取出我们需要的数据
        long total = page.getTotal();
        List<Employee> records = page.getResult();

        //  封装结果
        return new PageResult(total, records);
    }

    /**
     * 修改员工状态
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        //  这里我们采用新建一个 Employee 对象来传输数据
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();

        System.out.println("employee");
        employeeMapper.update(employee);
    }

    /**
     * 根据 id 查询员工信息
     * @param id
     * @return
     */
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);

        //  对密码进行隐藏
        employee.setPassword("******");
        return employee;
    }

    /**
     * 更新员工信息
     * @param employeeDTO
     */
    public void update(EmployeeDTO employeeDTO) {
        //  因为已经有过更新的 SQL 语句了, 这里我们之间调用即可
        //  当然, 前提是先将它转换为 Employee 类型的对象
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.update(employee);
    }

}
