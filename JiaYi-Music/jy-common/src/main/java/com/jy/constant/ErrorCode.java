package com.jy.constant;


import lombok.Getter;

@Getter
public enum ErrorCode {
    UNKNOWN_ERROR(1000, "未知异常"),
    SUCCESS(2000, "操作成功"),
    PERMISSION_EX(3000, "权限异常"),
    VALIDATE_EX(4000, "验证异常"),
    USER_EX(5000, "用户功能异常"),
    SONG_EX(6000, "歌曲功能异常"),
    DATA_EX(7000, "数据库异常"),
    CACHE_EX(8000, "缓存异常"),
    IO_EX(9000, "IO异常"),
    WS_EX(10000, "webSocket异常");

    private final Integer code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
