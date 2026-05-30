package com.jy.exception;

import com.jy.constant.ErrorCode;
import com.jy.constant.ErrorMessage;

public class WebSocketException extends BaseException {
    //非国际化
    public WebSocketException(ErrorMessage message) {
        super(message, ErrorCode.WS_EX);
    }

    public WebSocketException(ErrorMessage message, ErrorCode code) {
        super(message, code);
    }

    //国际化
    public WebSocketException(ErrorMessage message, Object... args) {
        super(message, ErrorCode.WS_EX, args);
    }

    public WebSocketException(ErrorMessage message, ErrorCode code, Object... args) {
        super(message, code, args);
    }
}
