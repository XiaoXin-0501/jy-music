package com.jy.service;

import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.core.codec.Base64;
import com.jy.cache.RedisService;
import com.jy.constant.ErrorMessage;
import com.jy.constant.RedisKey;
import com.jy.exception.CaptchaException;
import com.jy.utils.IdUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//暂未加限流等安全功能
@Service
public class CaptchaService {
    @Value("${security.captcha.width}")
    private int width;

    @Value("${security.captcha.height}")
    private int height;

    @Value("${security.captcha.count}")
    private int count;

    @Value("${security.captcha.thickness}")
    private int thickness;

    @Value("${security.captcha.expiration}")
    private int expiration;

    private final RedisService redisService;

    public CaptchaService(RedisService redisService) {
        this.redisService = redisService;
    }

    public Map<String, String> generateCaptcha() {
        String uuid = IdUtils.uuid();
        String captchaKey = RedisKey.CAPTCHA.getKey() + uuid;
        ShearCaptcha captcha = new ShearCaptcha(width, height, count, thickness);
        captcha.createCode();
        String code = captcha.getCode();
        redisService.set(captchaKey, code);
        redisService.expire(captchaKey, expiration, TimeUnit.MINUTES);

        String img = "";
        try {
            // 4. 将验证码图片转为Base64编码（前端可直接渲染）
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(captcha.getImage(), "png", outputStream);
            img = "data:image/png;base64," + Base64.encode(outputStream.toByteArray());
        } catch (IOException e) {
            return null;
        }
        // 5. 封装返回数据（uuid + base64图片）
        Map<String, String> resultData = new HashMap<>(2);
        resultData.put("uuid", uuid);
        resultData.put("img", img);
        // 6. 返回成功结果
        return resultData;
    }

    public void validateCaptcha(String code, String uuid) {
        String captchaKey = RedisKey.CAPTCHA.getKey() + uuid;
        String captcha = redisService.get(captchaKey, String.class);
        if (captcha == null) {
            throw new CaptchaException(ErrorMessage.CAPTCHA_EXPIRE);
        }
        redisService.delete(captchaKey);
        if (!code.equalsIgnoreCase(captcha)) {
            throw new CaptchaException(ErrorMessage.CAPTCHA_NOT_MATCH);
        }
    }
}
