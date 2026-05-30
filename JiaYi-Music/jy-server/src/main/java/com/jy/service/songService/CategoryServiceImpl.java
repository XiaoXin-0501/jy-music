package com.jy.service.songService;

import com.jy.cache.RedisService;
import com.jy.cache.RedissonService;
import com.jy.constant.ErrorMessage;
import com.jy.constant.LockKey;
import com.jy.constant.RedisKey;
import com.jy.exception.SongException;
import com.jy.mapper.CategoryMapper;
import com.jy.service.CategoryService;
import com.jy.vo.responseVo.CategoryInfo;
import org.redisson.api.RLock;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;
    private final RedisService redisService;
    private final RedissonService redissonService;

    public CategoryServiceImpl(CategoryMapper categoryMapper, RedisService redisService, RedissonService redissonService) {
        this.categoryMapper = categoryMapper;
        this.redisService = redisService;
        this.redissonService = redissonService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CategoryInfo> getCategoryList() {
        List<CategoryInfo> categoryList = (List<CategoryInfo>) redisService.get(RedisKey.SONG_CATEGORY.getKey(), List.class);
        if (categoryList != null) {
            return categoryList;
        }
        RLock lock = redissonService.getRedisson().getLock(LockKey.CATEGORY_LOCK.getKey());
        try {
            boolean isLock = lock.tryLock();
            if (!isLock) {
                Thread.sleep(50);
                for (int i = 0; i < 5; i++) {
                    categoryList = (List<CategoryInfo>) redisService.get(RedisKey.SONG_CATEGORY.getKey(), List.class);
                    if (categoryList != null) {
                        return categoryList;
                    }
                }
                throw new SongException(ErrorMessage.SONG_GET_CATEGORY_FAIL);
            }
            //双重检查
            categoryList = (List<CategoryInfo>) redisService.get(RedisKey.SONG_CATEGORY.getKey(), List.class);
            if (categoryList != null) {
                return categoryList;
            }
            categoryList = categoryMapper.getCategoryList();
            redisService.set(RedisKey.SONG_CATEGORY.getKey(), categoryList);
            return categoryList;
        } catch (Exception e) {
            throw new SongException(ErrorMessage.SONG_GET_CATEGORY_FAIL);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
