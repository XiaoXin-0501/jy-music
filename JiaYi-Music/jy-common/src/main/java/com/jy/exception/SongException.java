package com.jy.exception;

import com.jy.constant.ErrorCode;
import com.jy.constant.ErrorMessage;

public class SongException extends BaseException {
    //非国际化
    public SongException(ErrorMessage message) {
        super(message, ErrorCode.SONG_EX);
    }

    public SongException(ErrorMessage message, ErrorCode code) {
        super(message, code);
    }

    //国际化
    public SongException(ErrorMessage message, Object... args) {
        super(message, ErrorCode.SONG_EX, args);
    }

    public SongException(ErrorMessage message, ErrorCode code, Object... args) {
        super(message, code, args);
    }
}
