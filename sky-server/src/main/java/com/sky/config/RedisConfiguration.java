package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @Author: dy
 * @Date: 2023/8/29 21:32
 * @Description:
 */
@Configuration
@Slf4j
public class RedisConfiguration {

    /*
        该配置类不是必须的, 实际上 SpringBoot 会自动装配 RedisTemplate 对象
        但是默认装配的序列化器为: JdkSerializationRedisSerializer
        导致我们存到 Redis 中后的数据和原始数据有差别，故设置为 StringRedisSerializer 序列化器。
     */
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建 redis 模板对象...");

        //  创建一个新的 redis 模板对象
        RedisTemplate redisTemplate = new RedisTemplate();

        //  设置 redis 的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        //  设置 redis key 的序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        return redisTemplate;

    }

}
