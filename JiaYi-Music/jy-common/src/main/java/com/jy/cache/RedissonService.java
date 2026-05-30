package com.jy.cache;

import com.jy.constant.RedisKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RCuckooFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Component
public class RedissonService {
    private final RedissonClient redisson;
    private final RCuckooFilter<String> songIdFilter;

    public RedissonService(RedissonClient redisson) {
        this.redisson = redisson;
        this.songIdFilter = redisson.getCuckooFilter(RedisKey.SONG_ID_FILTER.getKey());
        if (!songIdFilter.isExists()) {
            songIdFilter.init(1000000L);
        }
    }

    public void songIdFilterAdd(String element) {
        boolean result = songIdFilter.add(element);
        if (!result) {
            log.warn("文件上传过滤器已满");
        }
    }
}
