package com.jy.constant;

import lombok.Getter;

@Getter
public enum RedisKey {
    //common
    //captcha
    CAPTCHA("captcha"),
    //song模块
    SONG("song"),
    SONG_CATEGORY("song:category"),
    SONG_ID_FILTER("song:id:filter"),
    SONG_IS_UPLOAD("song:is:upload"),
    SONG_PENDING("song:pending"),
    SONG_PLAY_TOP("song:play:top"),
    SONG_PLAY_COUNT("song:play:count"),
    SONG_COLLECT_TOP("song:collect:top"),
    SONG_COLLECT_COUNT("song:collect:count"),
    SONG_HOT_TOP("song:hot:top"),

    //user模块
    USER_LOGIN("user:login"),
    USER_TOKEN("user:token"),
    USER_CSRF_TOKEN("user:csrf_token"),
    USER_PWD_ERR("user:pwd:error"),
    USER_SONGS("user:songs"),
    USER_SONG_LIST("user:song:list"),
    //song_list模块
    LIST("list"),
    ;

    private final String key;

    RedisKey(String key) {
        this.key = key;
    }

    public String getKey(String suffix) {
        return key + ":" + suffix;
    }
}
