package com.nhwb.cloud.configuration;

import com.nhwb.cloud.pojo.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 电脑文件映射规则
 * 作者：B站「怒火无边」
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    public static Map<String, String> FilePath;
    public static Map<String, String> CustomPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Admin admin = WebAppConfigurer.ADMIN;
        //-----------------------所有文件映射--------------------------------
        if (admin.isConfig()) {
            System.out.println("----------默认开启全局文件共享----------");
            File[] roots = File.listRoots();
            FilePath = new HashMap<>();
            String mark = "/mark_";
            for (File root : roots) {
                String prefix = mark + String.valueOf(root.getPath().charAt(0)).toLowerCase();
                FilePath.put(prefix, root.getPath());
                registry.addResourceHandler(prefix + "x/**")
                        .addResourceLocations("file:" + root.getPath());
            }
        } else {
            System.out.println("----------全局文件共享已关闭----------");
        }
        List<String> customs = admin.getCustom();
        //------------------指定文件夹映射---------------------------
        if (admin.isMyCustom() && customs != null && customs.size() > 0) {
            System.out.println("----------自定义文件夹共享已开启----------");
            CustomPath = new HashMap<>();
            for (int i = 0; i < customs.size(); i++) {
                String s = customs.get(i);
                if (new File(s).isDirectory()) {
                    String prefix = "/custom_" + i;
                    CustomPath.put(prefix, s + "\\");
                    registry.addResourceHandler(prefix + "x/**")
                            .addResourceLocations("file:" + s + "\\");
                } else {
                    System.out.println("----------该文件夹不存在：" + s + "----------");
                }
            }
        }
    }
}