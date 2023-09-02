package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}
