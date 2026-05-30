package com.jy.exception;

import com.jy.constant.ErrorCode;
import com.jy.constant.ErrorMessage;

public class CacheException extends BaseException {
    //非国际化
    public CacheException(ErrorMessage message) {
        super(message, ErrorCode.CACHE_EX);
    }

    public CacheException(ErrorMessage message, ErrorCode code) {
        super(message, code);
    }

    //国际化
    public CacheException(ErrorMessage message, Object... args) {
        super(message, ErrorCode.CACHE_EX, args);
    }

    public CacheException(ErrorMessage message, ErrorCode code, Object... args) {
        super(message, code, args);
    }
}

