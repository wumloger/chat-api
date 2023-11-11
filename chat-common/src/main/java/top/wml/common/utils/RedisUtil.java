package top.wml.common.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置 Key-Value
     * @param key   键
     * @param value 值
     * @param time  过期时间（单位：秒）
     */
    public void set(String key, Object value, long time) {
        ValueOperations<String, Object> ops = this.redisTemplate.opsForValue();
        ops.set(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * 获取 Key-Value
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        ValueOperations<String, Object> ops = this.redisTemplate.opsForValue();
        return ops.get(key);
    }

    /**
     * 删除 Key
     * @param key 键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}