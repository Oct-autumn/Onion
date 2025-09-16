package com.onion.onionserver.service;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Set a value in Redis
     *
     * @param key   the key
     * @param value the value
     */
    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Set a value in Redis with expiration time
     *
     * @param key       the key
     * @param value     the value
     * @param timeout   the expiration time in seconds
     */
    public void setValueWithExpiration(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    /**
     * Get a value from Redis
     *
     * @param key   the key
     * @return the value
     * @throws org.springframework.dao.DataAccessException if a data access error occurs
     */
    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Check if a key exists in Redis
     *
     * @param key   the key
     * @return true if the key exists, false otherwise
     */
    public boolean contains(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * Delete a value from Redis
     *
     * @param key   the key
     * @return true if the key was deleted, false otherwise
     */
    public boolean deleteValue(String key) {
        return redisTemplate.delete(key);
    }
}
