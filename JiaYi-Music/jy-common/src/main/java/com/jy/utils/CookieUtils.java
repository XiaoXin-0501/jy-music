package com.jy.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

public class CookieUtils {
    /**
     * 生效域名
     * 本地开发：localhost
     * 线上环境：.你的域名.com（前面带. 表示所有子域名共享）
     */
    private static final String DOMAIN = "localhost";

    /**
     * 生效路径
     * / ：整个项目所有接口都能访问
     */
    private static final String PATH = "/";

    /**
     * HttpOnly：true 禁止前端 JS 读取 Cookie，防 XSS 攻击
     * 线上必须 true
     */
    private static final boolean HTTP_ONLY = true;

    /**
     * Secure：true 仅 HTTPS / WSS 传输
     * 本地 HTTP 环境必须 false
     * 线上 HTTPS 环境必须 true
     */
    private static final boolean SECURE = false;

    /**
     * 过期时间（秒）
     * 7天 = 604800
     * 0 = 立即删除
     * -1 = 浏览器关闭即失效
     */
    private static final int MAX_AGE = 604800;

    /**
     * SameSite 防 CSRF 配置
     * 三个可选值：Strict / Lax / None
     * WebSocket 必须用 Lax
     * 配置方式：setAttribute("SameSite", "Lax")
     */
    private static final String SAME_SITE = "Lax";

    /**
     * 设置 Cookie（使用全局默认配置）
     */
    public static void set(HttpServletResponse response, String name, String value) {
        set(response, name, value, MAX_AGE);
    }

    /**
     * 设置 Cookie（自定义过期时间）
     */
    public static void set(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setDomain(DOMAIN);
        cookie.setPath(PATH);
        cookie.setHttpOnly(HTTP_ONLY);
        cookie.setSecure(SECURE);
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", SAME_SITE);
        response.addCookie(cookie);
    }

    public static String get(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static void delete(HttpServletResponse response, String name) {
        // 删除原理：覆盖同名Cookie，值为空，立即过期
        Cookie cookie = new Cookie(name, "");
        cookie.setDomain(DOMAIN);
        cookie.setPath(PATH);
        cookie.setHttpOnly(HTTP_ONLY);
        cookie.setSecure(SECURE);
        cookie.setMaxAge(0); // 0 = 立即删除
        cookie.setAttribute("SameSite", SAME_SITE);
        response.addCookie(cookie);
    }
}
