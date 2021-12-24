package com.nhwb.cloud.configuration;

import com.nhwb.cloud.pojo.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 拦截器
 * 作者：B站「怒火无边」
 */
@Order(-1)
@Component
public class WebAppConfigurer implements WebMvcConfigurer {
    public static Admin ADMIN;
    @Autowired
    Admin admin;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        ADMIN = admin;
        System.out.println(ADMIN);
        System.out.println("欢迎使用我制作的私有云APP，自定义使用方法观看B站作者「怒火无边」的教程。");
        System.out.println("B站地址：https://www.bilibili.com/video/BV1oR4y1p7Zy/");
        System.out.println("可以设置账号密码，开启登入验证，还有指定共享文件夹，实现远程共享等。");

        if (ADMIN.isLogin()) {
            System.out.println("-------------开启登入认证-------------");
            InterceptorRegistration interceptorRegistration = registry.addInterceptor(new AuthorizeFilter());
            // 可添加多个
            interceptorRegistration.addPathPatterns("/**");
            interceptorRegistration.excludePathPatterns("/login", "/static/**");
        } else {
            System.out.println("-------------登入认证未启动-------------");
            if (ADMIN.isMyUpload()){
                ADMIN.setMyUpload(false);
                System.out.println("----为了安全考虑，上传功能需要登入认证，所以关闭上传功能被关闭----");
                System.out.println("----------需要上传功能，打开登入认证重新运行----------");
            }
        }

        if (ADMIN.isMyUpload()) {
            File file = new File(ADMIN.getRepository());
            if (file.isDirectory() || file.mkdirs()) {
                System.out.println("-------------上传功能已开启-------------");
                System.out.println("-----------缓存目录：" + "-----------");
            } else {
                System.out.println("-----------错误的缓存目录：" + "。-----------");
                System.out.println("-------------关闭上传功能-------------");
                ADMIN.setMyUpload(false);
            }
        }
    }
}