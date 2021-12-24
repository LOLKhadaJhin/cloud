package com.nhwb.cloud.controller;

import com.nhwb.cloud.configuration.WebAppConfigurer;
import com.nhwb.cloud.configuration.WebMvcConfig;
import com.nhwb.cloud.pojo.Admin;
import com.nhwb.cloud.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Web映射规则
 * 作者：B站「怒火无边」
 */
@Controller
public class CloudController {
    @Autowired
    Admin admin;

    //首页
    @RequestMapping({"/home", "/"})
    public String home(Model model) {
        model.addAttribute("title", "私有云 --B站「怒火无边」");
        if (admin.isConfig()) {
            model.addAttribute("mark", WebMvcConfig.FilePath);
        }
        if (admin.isMyCustom()) {
            model.addAttribute("custom", WebMvcConfig.CustomPath);
        }
        return "home";
    }

    //登入
    @RequestMapping("/login")
    public String login(@Nullable User user, Model model, HttpSession session) {
        if (user == null || user.getUserName() == null || user.getPassword() == null) {
            //跳转登入页面
            return "login";
        } else if (user.getUserName().equals(admin.getUserName()) && user.getPassword().equals(admin.getPassword())) {
            //验证成功，跳转首页
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(60 * 60 * 2);
            return "redirect:/home";
        } else {
            //账号或密码不正确，重新输入
            model.addAttribute("className", "show");
            return "login";
        }
    }

    @RequestMapping("/video/**")
    public String video(Model model, String videoUrl, String videoName) {
        model.addAttribute("videoUrl", videoUrl);
        model.addAttribute("videoName", videoName);
        return "video";
    }

    @RequestMapping("/favicon.ico")
    @ResponseBody
    public void returnNoFavicon() {
    }

    //本地映射访问
    @RequestMapping("/cloud/{define}/**")
    public String cloud(@PathVariable("define") String define, Model model, @Nullable String directory, HttpServletRequest request) {
        model.addAttribute("title", "私有云 --B站「怒火无边」");
        //文件的根目录
        String realURLPrefix;
        if (define.startsWith("mark_") && WebMvcConfig.FilePath != null) {
            realURLPrefix = WebMvcConfig.FilePath.get("/" + define);
        } else if (define.startsWith("custom_") && WebMvcConfig.CustomPath != null) {
            realURLPrefix = WebMvcConfig.CustomPath.get("/" + define);
        } else {
            System.out.println("没用找到:" + define);
            return "redirect:/home";
        }
        //文件访问地址前缀
        String uriPathPrefix = "/" + define + "x";
        directory = directory != null ? directory : "";
        String fileSuffix = directory.replace("/", "\\");
        String filePrefix = (realURLPrefix + fileSuffix).replace("\\\\", "\\");
        File file = new File(filePrefix);
        if (file.isDirectory()) {
            model.addAttribute("define", define)
                    .addAttribute("fileSuffix", fileSuffix)
                    .addAttribute("urlPrefix",
                            request.getRequestURL().toString().replace(request.getServletPath(), ""));
            request.getSession().setAttribute("filePrefix",filePrefix);
            String[] list = file.list();
            Map<String, String> directoryList = new HashMap<>();
            Map<String, String> fileList = new HashMap<>();
            Map<String, String> videoList = new HashMap<>();
            Map<String, String> imgList = new HashMap<>();
            //list必不为null，因为他的爹必存在。即使length为0也可以遍历。
            for (String s : list) {
                String[] strings = {"jpg", "png", "bmp", "jpeg", "gif", "xbm", "xpm"};
                //当前文件(夹)完整真实路径 = 真·爹 + 子文件(夹)
                String file1 = filePrefix + "\\" + s;
                //映射的完整路径 = 前缀 + 目录 + 子文件(夹)
                String Url = uriPathPrefix + directory + "/" + s;
                //目录访问地址前缀
                String uriPrefix = "/cloud/" + define;
                if (new File(file1).isDirectory()) {
                    directoryList.put(s, uriPrefix + "?directory=" + directory + "/" + s);
                } else if (admin.isMyVideo() && (s).split("\\.")[(s).split("\\.").length - 1].equalsIgnoreCase("mp4")) {
                    videoList.put(s, "/video?videoUrl=" + Url + "&videoName=" + s);
                } else if (admin.isMyImgLazy() && equalsValueList((s).split("\\.")[(s).split("\\.").length - 1], strings)) {
                    imgList.put(s, Url);
                } else {
                    fileList.put(s, Url);
                }
            }
            model.addAttribute("directoryList", directoryList)
                    .addAttribute("fileList", fileList)
                    .addAttribute("videoList", videoList)
                    .addAttribute("imgList", imgList);
            if (WebAppConfigurer.ADMIN.isMyUpload()){
            return "cloud";}else {
                return "demo";
            }
        }
        return "redirect:/home";
    }

    private boolean equalsValueList(String value, String... array) {
        boolean flag = false;
        for (String a : array) {
            if (a.equalsIgnoreCase(value)) {
                flag = true;
                break;
            }
        }
        return flag;
    }
}
