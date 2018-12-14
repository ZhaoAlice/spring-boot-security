package com.test.web.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@EnableCaching
public class RedisUtils extends CachingConfigurerSupport {

    @Value("${spring.redis.host}")
    private String host;
    
    @Value("${spring.redis.port}")
    private int port;
    
    @Value("${spring.redis.pool.max-wait}")
    private int maxWait;
    
    @Value("${spring.redis.pool.max-idle}")
    private int maxIdle;
    
    @Value("${spring.redis.timeout}")
    private int timeOut;


    /**
     * @Description <br>
     *
     * @param  
     * @return JedisPool
     */
    @Bean
    public JedisPool redisPoolFactory() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWait);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, host, port, timeOut);
        System.out.println("jedis连接池注入成功");
        return jedisPool;
    }

    /**
     * @Description 缓存管理器 <br>
     *
     * @param redisTemplate
     * @return CacheManager
     */
    @Bean
    @ConditionalOnBean(name = "redisTemplate")
    public CacheManager cacheManager(RedisTemplate<?, ?> redisTemplate) {
        CacheManager cacheManager = new RedisCacheManager(redisTemplate);
        return cacheManager;
    }

    /**
     * @Description: 序列化模板配置，类似于JDBCtemplate<br>
     *
     * @param  
     * @return RedisTemplate<String,String>
     * @throws
     */
    @Bean
    @ConditionalOnBean(name = "redisConnectionFactory")
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(redisConnectionFactory);
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        stringRedisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        return stringRedisTemplate;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return (o, method, objects) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(o.getClass().getName());
            sb.append("." + method.getName() + "(");
            for (Object object : objects) {
                sb.append(object.toString());
            }
            sb.append(")");
            return sb.toString();
        };
    }

    public static void main(String[] args) throws UnknownHostException {
        // TODO Auto-generated method stub

        //获取本机IP地址
        System.out.println(InetAddress.getLocalHost().getHostAddress());
    }

}
