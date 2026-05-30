package com.jy.cache;

import com.jy.constant.ErrorMessage;
import com.jy.exception.CacheException;
import lombok.Getter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 普通封装
 * 适用于低并发，无竞争的请求
 */
@Component
public class RedisService {
    @Getter
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * String类型set
     *
     * @param key   String
     * @param value 任意
     * @param <T>   泛型
     */
    public <T> void set(String key, T value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json);
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_SET_FAIL);
        }
    }

    public <T> void sAdd(String key, T value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForSet().add(key, json);
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_SET_FAIL);
        }
    }

    public <T> void lPush(String key, T value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForList().leftPush(key, json);
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_SET_FAIL);
        }
    }

    public <T> void lPushAll(String key, List<T> values) {
        try {
            if (values == null || values.isEmpty()) {
                return;
            }
            List<String> jsonList = new java.util.ArrayList<>();
            for (T value : values) {
                jsonList.add(objectMapper.writeValueAsString(value));
            }
            redisTemplate.opsForList().leftPushAll(key, jsonList);
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_SET_FAIL);
        }
    }

    public <T> void hSet(String key, String field, T value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForHash().put(key, field, json);
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_SET_FAIL);
        }
    }

    public void zAdd(String key, String field, double score) {
        try {
            redisTemplate.opsForZSet().add(key, field, score);
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_SET_FAIL);
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return null;
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_GET_FAIL);
        }
    }

    public <T> List<T> sPop(String key, int num, Class<T> clazz) {
        try {
            List<String> jsonList = redisTemplate.opsForSet().pop(key, num);
            if (jsonList == null || jsonList.isEmpty()) {
                return Collections.emptyList();
            }
            List<T> resultList = new ArrayList<>(jsonList.size());
            for (String json : jsonList) {
                if (json != null) {
                    resultList.add(objectMapper.readValue(json, clazz));
                }
            }
            return resultList;
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_GET_FAIL);
        }
    }

    public <T> List<T> lRangeAll(String key, Class<T> clazz) {
        try {
            List<String> jsonList = redisTemplate.opsForList().range(key, 0, -1);
            if (jsonList == null || jsonList.isEmpty()) {
                return Collections.emptyList();
            }
            List<T> resultList = new ArrayList<>(jsonList.size());
            for (String json : jsonList) {
                if (json != null) {
                    resultList.add(objectMapper.readValue(json, clazz));
                }
            }
            return resultList;
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_GET_FAIL);
        }
    }

    public <T> T hGet(String key, String field, Class<T> clazz) {
        try {
            String json = (String) redisTemplate.opsForHash().get(key, field);
            if (json == null) return null;
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_GET_FAIL);
        }
    }

    public <T> Map<String, T> hGetAll(String key, Class<T> clazz) {
        try {
            Map<String, T> resultMap = new HashMap<>();
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                String field = entry.getKey().toString();
                String json = entry.getValue().toString();
                if (json == null) return null;
                resultMap.put(field, objectMapper.readValue(json, clazz));
            }
            return resultMap;
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_GET_ALL_FILED);
        }
    }

    public Map<String, Double> zRange(String key, int start, int end) {
        try {
            Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().rangeWithScores(key, start, end);
            if (typedTuples == null || typedTuples.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, Double> resultMap = new HashMap<>();
            for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
                resultMap.put(typedTuple.getValue(), typedTuple.getScore());
            }
            return resultMap;
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_GET_FAIL);
        }
    }


    public boolean hDelete(String key, String... fields) {
        try {
            if (key == null || key.isEmpty() || fields == null || fields.length == 0) return false;
            return redisTemplate.opsForHash().delete(key, (Object[]) fields) > 0;
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_DEL_KEY_FAIL);
        }
    }

    public boolean delete(String... keys) {
        try {
            if (keys == null || keys.length == 0) {
                return false;
            }
            return redisTemplate.delete(Arrays.asList(keys)) == keys.length;
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_DEL_KEY_FAIL);
        }
    }

    public boolean lRemove(String key, Object value, int count) {
        try {
            if (key == null || key.isEmpty() || value == null) {
                return false;
            }
            String json = objectMapper.writeValueAsString(value);
            return redisTemplate.opsForList().remove(key, count, json) > 0;
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_DEL_KEY_FAIL);
        }
    }

    public boolean lRemove(String key, Object value) {
        return lRemove(key, value, 1);
    }

    public boolean sRem(String key, Object value) {
        try {
            if (key == null || key.isEmpty() || value == null) {
                return false;
            }
            String json = objectMapper.writeValueAsString(value);
            return redisTemplate.opsForSet().remove(key, json) > 0;
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_DEL_KEY_FAIL);
        }
    }

    public boolean zRem(String key, Object value) {
        try {
            if (key == null || key.isEmpty() || value == null) {
                return false;
            }
            String json = objectMapper.writeValueAsString(value);
            return redisTemplate.opsForZSet().remove(key, json) > 0;
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_DEL_KEY_FAIL);
        }
    }

    public boolean expire(String key, int time, TimeUnit timeUnit) {
        try {
            return redisTemplate.expire(key, time, timeUnit);
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_SET_FAIL);
        }
    }

    public void incr(String key) {
        redisTemplate.opsForValue().increment(key);
    }

    public void zIncrby(String key, String field, double score) {
        redisTemplate.opsForZSet().incrementScore(key, field, score);
    }

    public void decr(String key) {
        redisTemplate.opsForValue().decrement(key);
    }

    public boolean exist(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            throw new CacheException(ErrorMessage.REDIS_EXIST_EX);
        }
    }
}
