package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: dy
 * @Date: 2023/8/21 21:04
 * @Description: 自定义注解, 用于标识某个方法需要进行功能字段自动填充处理
 */

@Target(ElementType.METHOD) //  设置运行时的位置
@Retention(RetentionPolicy.RUNTIME) //  注解的生命周期
public @interface AutoFill {
    /**
     * 在使用 AutoFill 的时候必须给定一个参数
     * 这个参数的类型要为 OperationType
     * @return
     */
    OperationType value();
}
