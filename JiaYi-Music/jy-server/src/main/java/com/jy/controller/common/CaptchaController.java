package com.jy.controller.common;

import com.jy.domain.ApiResult;
import com.jy.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CaptchaController {
    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/captcha")
    public ApiResult<Object> captcha() {
        Map<String, String> captcha = captchaService.generateCaptcha();
        if (captcha != null) {
            return ApiResult.success(captcha);
        }
        return ApiResult.error();
    }
}
