package com.jy.exception;

import com.jy.constant.ErrorCode;
import com.jy.constant.ErrorMessage;

import java.io.Serial;

public class BaseException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 错误码,枚举类型
     */
    private final ErrorCode code;
    private final ErrorMessage message;
    private final Object[] args;
    private final boolean useI18n;

    public ErrorMessage getErrorMessage() {
        return message;
    }

    public boolean getUseI18n() {
        return useI18n;
    }

    public ErrorCode getCode() {
        return code;
    }

    public Object[] getArgs() {
        return args;
    }

    //非国际化
    public BaseException(ErrorMessage message, ErrorCode code) {
        super(message.getMessage());
        this.useI18n = false;
        this.code = code;
        this.message = message;
        this.args = null;
    }

    //国际化
    public BaseException(ErrorMessage message, ErrorCode code, Object... args) {
        super(message.getMessage());
        this.useI18n = true;
        this.code = code;
        this.message = message;
        this.args = args;
    }
    
}
