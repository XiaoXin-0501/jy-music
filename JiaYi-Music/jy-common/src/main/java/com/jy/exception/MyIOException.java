package com.jy.exception;

import com.jy.constant.ErrorCode;
import com.jy.constant.ErrorMessage;

public class MyIOException extends BaseException {
    //非国际化
    public MyIOException(ErrorMessage message) {
        super(message, ErrorCode.IO_EX);
    }

    public MyIOException(ErrorMessage message, ErrorCode code) {
        super(message, code);
    }

    //国际化
    public MyIOException(ErrorMessage message, Object... args) {
        super(message, ErrorCode.IO_EX, args);
    }

    public MyIOException(ErrorMessage message, ErrorCode code, Object... args) {
        super(message, code, args);
    }
}
