package com.jy.controller.user;

import com.jy.domain.ApiResult;
import com.jy.securityUtils.SecurityUtils;
import com.jy.service.UserService;
import com.jy.vo.requestVo.UserInfoBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/avatar")
    public ApiResult<Map<String, String>> avatar(@RequestParam("avatarfile") MultipartFile file) throws Exception {
        String url = userService.updateAvatar(SecurityUtils.getUserId(), file);
        Map<String, String> map = new HashMap<>();
        map.put("avatar", url);
        return ApiResult.success(map);
    }

    @PostMapping("/updateUserInfo")
    public ApiResult<Map<String, String>> updateUserInfo(@RequestBody UserInfoBody userInfoBody) {
        if (userService.updateUserInfo(SecurityUtils.getUserId(), userInfoBody)) {
            return ApiResult.success();
        }
        return ApiResult.error(null, "上传失败");
    }

    @PostMapping("/logout/{id}")
    public ApiResult<Map<String, String>> logout(@PathVariable Long id) {
        userService.logout(id);
        return ApiResult.success();
    }
}
