package com.jy.exception;

import com.jy.constant.ErrorCode;
import com.jy.constant.ErrorMessage;

public class CaptchaException extends BaseException {
    //非国际化
    public CaptchaException(ErrorMessage message) {
        super(message, ErrorCode.VALIDATE_EX);
    }

    public CaptchaException(ErrorMessage message, ErrorCode code) {
        super(message, code);
    }

    //国际化
    public CaptchaException(ErrorMessage message, Object... args) {
        super(message, ErrorCode.VALIDATE_EX, args);
    }

    public CaptchaException(ErrorMessage message, ErrorCode code, Object... args) {
        super(message, code, args);
    }
}
