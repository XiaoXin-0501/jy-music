package com.jy.exception;

import com.jy.constant.ErrorCode;
import com.jy.constant.ErrorMessage;

public class UserException extends BaseException {
    //非国际化
    public UserException(ErrorMessage message) {
        super(message, ErrorCode.USER_EX);
    }

    public UserException(ErrorMessage message, ErrorCode code) {
        super(message, code);
    }

    //国际化
    public UserException(ErrorMessage message, Object... args) {
        super(message, ErrorCode.USER_EX, args);
    }

    public UserException(ErrorMessage message, ErrorCode code, Object... args) {
        super(message, code, args);
    }
}
