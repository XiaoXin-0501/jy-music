package com.jy.constant;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ErrorMessage {
    // 默认
    DEFAULT_MSG("默认信息", "default.message"),

    // 验证码
    CAPTCHA_NOT_MATCH("验证码错误", "captcha.not.match"),
    CAPTCHA_EXPIRE("验证码过期", "captcha.expire"),

    // 文件
    FILE_DEFAULT("文件异常", "file.default"),
    FILE_PATH_NOT_EXIST("文件路径不存在", "file.path.not.exist"),
    FILE_DELETE_FAIL("文件删除异常", "file.delete.fail"),
    FILE_CREATE_FAIL("文件创建失败", "file.create.fail"),
    FILE_IS_EMPTY("文件夹为空", "file.is.empty"),
    FILE_WRITE_FAIL("文件写入失败", "file.write.fail"),
    FILE_CLEAR_TEMP_FAIL("临时文件清除失败", "file.clear.temp.fail"),
    FILE_MERGE_FAIL("文件合并失败", "file.merge.fail"),
    FILE_GET_CHUNKS_FAIL("分片获取失败", "file.get.chunks.fail"),
    FILE_FIND_FAIL("文件查找失败", "file.find.fail"),

    // 用户
    USER_GET_INFO_FAIL("获取用户信息失败", "user.get.info.failed"),
    USER_IS_LOCKED("用户已被锁定", "user.is.locked"),
    USER_NOT_EXIST("用户不存在", "user.not.exist"),
    USER_PASSWORD_RETRY_LIMIT_EXCEED("密码重试次数过多，请稍后再试", "user.password.retry.limit.exceed"),
    USER_PASSWORD_NOT_MATCH("密码错误", "user.password.not.match"),
    USER_TOKEN_ERROR("认证失败", "user.token.error"),
    USER_TOKEN_EXPIRE("登录过期", "user.token.expire"),
    USER_GET_SONG_LIST_FAIL("用户歌单获取失败", "user.get.song.list.fail"),


    //歌曲
    SONG_GET_CATEGORY_FAIL("歌曲分类获取失败", "song.get.category.fail"),
    SONG_UPLOAD_FAIL("歌曲上传失败", "song.upload.fail"),
    SONG_UPDATE_FAIL("歌曲更新失败", "song.update.fail"),
    SONG_REMOVE_FAIL("歌曲删除失败", "song.rmove.fail"),
    SONG_ADD_TO_LIST_FAIL("歌曲添加失败", "song.add.to.list.fail"),
    SONG_NOT_EXIST("歌曲不存在", "song.not.exist"),
    SONG_REMOVE_FROM_LIST_FAIL("歌曲移除失败", "song.remove.from.list.fail"),
    SONG_LIST_CREATE_FAIL("歌单创建失败", "song.list.create.fail"),
    SONG_LIST_DELETE_FAIL("歌单删除失败", "song.list.delete.fail"),
    SONG_LIST_UPDATE_FAIL("歌单更新失败", "song.list.update.fail"),


    //缓存
    //redis
    REDIS_SET_FAIL("redis缓存设置失败", "redis.set.fail"),
    REDIS_GET_FAIL("redis缓存获取失败", "redis.get.fail"),
    REDIS_EXPIRE_SET_FAIL("redis过期时间设置失败", "redis.expire.set.fail"),
    REDIS_DEL_KEY_FAIL("redis删除key失败", "redis.del.key.fail"),
    REDIS_EXIST_EX("redis存在判断异常", "redis.exist.ex"),
    REDIS_GET_ALL_FILED("redis获取hash结构所有键值对失败", "redis.get.all.filed.fail"),

    //webSocket异常
    WS_ERROR("ws异常", "ws.error"),
    WS_SEND_FAIL("ws信息发送失败", "ws.send.fail"),
    WS_SEND_ALL_FAIL("ws广播失败", "ws.send.all.fail"),
    WS_TOPIC_EX("topic异常", "ws.topic.ex"),
    WS_STREAM_EX("stream异常", "ws.stream.ex"),

    //Topic
    TP_EX("topic异常", "topic.ex"),
    ;
    //非国际化信息
    private final String message;
    //国际化信息的key
    private final String messageKey;

    ErrorMessage(String message, String messageKey) {
        this.message = message;
        this.messageKey = messageKey;
    }

    public static ErrorMessage getByMessage(String message) {
        return Arrays.stream(values())
                .filter(e -> e.getMessage().equals(message))
                .findFirst()
                // 找不到 → 默认枚举
                .orElse(DEFAULT_MSG);
    }

}
