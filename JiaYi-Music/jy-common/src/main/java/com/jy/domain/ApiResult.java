package com.jy.domain;

import com.jy.constant.ErrorCode;
import lombok.Data;

@Data
public class ApiResult<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ApiResult<T> success() {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setCode(ErrorCode.SUCCESS.getCode());
        apiResult.setMsg("success");
        return apiResult;
    }

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setCode(ErrorCode.SUCCESS.getCode());
        apiResult.setMsg("success");
        apiResult.setData(data);
        return apiResult;
    }

    public static <T> ApiResult<T> success(T data, String msg) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setCode(ErrorCode.SUCCESS.getCode());
        apiResult.setMsg(msg);
        apiResult.setData(data);
        return apiResult;
    }

    public static <T> ApiResult<T> error() {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setCode(ErrorCode.UNKNOWN_ERROR.getCode());
        apiResult.setMsg("error");
        return apiResult;
    }

    public static <T> ApiResult<T> error(T data) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setCode(ErrorCode.UNKNOWN_ERROR.getCode());
        apiResult.setMsg("error");
        apiResult.setData(data);
        return apiResult;
    }

    public static <T> ApiResult<T> error(T data, String msg) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setCode(ErrorCode.UNKNOWN_ERROR.getCode());
        apiResult.setMsg(msg);
        apiResult.setData(data);
        return apiResult;
    }

    public static <T> ApiResult<T> error(T data, String msg, ErrorCode code) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setCode(code.getCode());
        apiResult.setMsg(msg);
        apiResult.setData(data);
        return apiResult;
    }
}

