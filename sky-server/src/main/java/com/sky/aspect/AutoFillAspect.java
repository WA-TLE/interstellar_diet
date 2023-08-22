package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * @Author: dy
 * @Date: 2023/8/21 21:12
 * @Description: 自定义切面，实现公共字段自动填充处理逻辑
 */
@Aspect
@Component
@Slf4j  // 用于调试日志
public class AutoFillAspect {

    /**
     * 定义切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut(){};

    @Before("autoFillPointcut()")
    public void autofill(JoinPoint joinPoint) {
        log.info("开始进行公共字段的自动填充");

        //  这里可以将要做的步骤写出来

        //  0.1 获取当前被拦截的方法上的数据库操作类型

        //  获取我们拦截方法的方法签名, 并且把他转为为 MethodSignature 类型
        //  方法签名里面包括方法的一些信息, 包括参数类型, 方法名, 注解等...
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //  1. 首先获取拦截到的方法对象
        //  2. 然后通过 getAnnotation(AutoFill.class) 获取该方法上 AutoFill.class 注解的值
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        //  将获取到的 AutoFill 注解的值赋值给 operationType, 方便后续操作
        OperationType operationType = autoFill.value();

        //  0.2 获取当前被拦截方法的参数
        //  这里规定, Employee 的参数放在第一位
        Object[] args = joinPoint.getArgs();

        //  保险起见, 先判断得到的参数是否为空
        if (args == null || args.length == 0) {
            return;
        }

        //  获得所拦截方法的参数
        Object entity = args[0];

        //  1. 获取要填充的值
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //  2. 判断要填充那些值
        if (operationType == OperationType.INSERT) {
            //为4个公共字段赋值
            try {
                //  1. 通过 getClass() 方法获取获取我们 entity 的类 (执行反射的前一步)
                //  2. 通过 getDeclaredMethod(*, *) 获取该对象中对应名称和参数的方法
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性赋值
                //  在这里调用我们刚才获取的四个方法, 分别为他们赋值
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);

                /*
                概述:
                    1. 我们通过 joinPoint 获取我们所拦截的方法的签名
                    2. 通过所获得的方法签名先得到该方法使用的 AutoFill 注解中的属性值 (insert or update)
                    3. 获取方法中的第一个参数对象 entity (这里规定, 将实体类放在第一个参数的位置)
                    4. entity.getClass() 获取该对象的类 (方便后续反射调用)
                    5. 通过 getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class) 获取该对象中的特定方法
                    6. setCreateTime.invoke(entity,now) 执行方法, 并传入对应的参数
                 */

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        } else if (operationType == OperationType.UPDATE) {
            //为2个公共字段赋值
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //  3. 填充




    }

}




















