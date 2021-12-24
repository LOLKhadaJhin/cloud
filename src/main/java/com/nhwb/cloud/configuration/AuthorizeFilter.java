package com.nhwb.cloud.configuration;

import com.nhwb.cloud.pojo.User;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器规则
 * 作者：B站「怒火无边」
 */
@Order(-1)
@Component
public class AuthorizeFilter implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User user = (User) request.getSession().getAttribute("user");
        if (user != null) {
            return true;
        }
        response.sendRedirect("/login");
        return false;
    }
}