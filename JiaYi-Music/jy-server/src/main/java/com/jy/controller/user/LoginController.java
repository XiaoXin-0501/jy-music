package com.jy.controller.user;

import com.jy.constant.CookieConstants;
import com.jy.domain.ApiResult;
import com.jy.service.UserService;
import com.jy.service.userService.LoginService;
import com.jy.utils.CookieUtils;
import com.jy.vo.requestVo.LoginBody;
import com.jy.vo.responseVo.UserInformation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
public class LoginController {
    @Autowired
    private LoginService loginService;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ApiResult<String> login(@RequestBody LoginBody loginBody, HttpServletResponse response) {
        Map<String, String> map = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(), loginBody.getUuid());
        String token = map.get("token");
        String csrfToken = map.get("csrfToken");
        if (token == null || token.isEmpty() || csrfToken == null || csrfToken.isEmpty()) {
            return ApiResult.error();
        }
        CookieUtils.set(response, CookieConstants.TOKEN, token);
        return ApiResult.success(csrfToken);
    }

    @GetMapping("/info")
    public ApiResult<UserInformation> info() {
        return ApiResult.success(userService.getUserInfo());
    }
}
