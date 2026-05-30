package com.jy.handle;

import com.jy.domain.ApiResult;
import com.jy.exception.BaseException;
import com.jy.exception.CaptchaException;
import com.jy.exception.SongException;
import com.jy.exception.UserException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(value = UserException.class)
    public ApiResult<Object> handleUserException(BaseException e) {
        return ApiResult.error(null, getMessage(e), e.getCode());
    }

    @ExceptionHandler(value = CaptchaException.class)
    public ApiResult<Object> handleCaptchaException(BaseException e) {
        return ApiResult.error(null, getMessage(e), e.getCode());
    }

    @ExceptionHandler(value = SongException.class)
    public ApiResult<Object> handleSongException(BaseException e) {
        return ApiResult.error(null, getMessage(e), e.getCode());
    }

    private String getMessage(BaseException e) {
        if (e.getUseI18n()) {
            return messageSource.getMessage(e.getErrorMessage().getMessageKey(), e.getArgs(), LocaleContextHolder.getLocale());
        }
        return e.getMessage();
    }
}
