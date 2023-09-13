package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * @Author: dy
 * @Date: 2023/9/1 17:35
 * @Description:
 */
@Mapper
public interface UserMapper {
    /**
     * 根据 openid 查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 创建用户
     * @param user
     */
    void insert(User user);

    /**
     * 根据用户 id 查询相应用户
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 根据动态条件, 查询用户数据
     * @param map
     * @return
     */
    Integer getUserStatistics(Map<Object, Object> map);
}
