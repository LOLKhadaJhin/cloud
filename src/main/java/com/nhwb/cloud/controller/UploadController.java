package com.nhwb.cloud.controller;


import com.nhwb.cloud.configuration.WebAppConfigurer;
import com.nhwb.cloud.configuration.WebMvcConfig;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Controller
public class UploadController {

    private final static String utf8 = "utf-8";

    @RequestMapping("/upload")
    @ResponseBody
    public void upload(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!WebAppConfigurer.ADMIN.isMyUpload()) {
            return;
        }
        String realURLPrefix = null;
        String fileSuffix = null;
        String repository = WebAppConfigurer.ADMIN.getRepository();
        //分片
        response.setCharacterEncoding(utf8);
        Integer schunk = null;
        Integer schunks = null;
        String name = null;
        BufferedOutputStream os = null;
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(1024);
            factory.setRepository(new File(repository));
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(5L * 1024L * 1024L * 1024L);
            upload.setSizeMax(10L * 1024L * 1024L * 1024L);
            List<FileItem> items = upload.parseRequest(request);

            for (FileItem item : items) {
                if (item.isFormField()) {
                    if ("chunk".equals(item.getFieldName())) {
                        schunk = Integer.parseInt(item.getString(utf8));
                    }
                    if ("chunks".equals(item.getFieldName())) {
                        schunks = Integer.parseInt(item.getString(utf8));
                    }
                    if ("name".equals(item.getFieldName())) {
                        name = item.getString(utf8);
                    }
                    if ("fileSuffix".equals(item.getFieldName())) {
                        fileSuffix = item.getString(utf8);
                    }
                    if ("define".equals(item.getFieldName())) {
                        String define = item.getString(utf8);
                        //文件的根目录
                        if (define.startsWith("mark_") && WebMvcConfig.FilePath != null) {
                            realURLPrefix = WebMvcConfig.FilePath.get("/" + define);
                        } else if (define.startsWith("custom_") && WebMvcConfig.CustomPath != null) {
                            realURLPrefix = WebMvcConfig.CustomPath.get("/" + define);
                        } else {
                            System.out.println("没用找到:" + define);
                        }
                    }
                }
            }
            String uploadPath = (realURLPrefix + fileSuffix).replace("\\\\", "\\");
            for (FileItem item : items) {
                if (!item.isFormField()) {
                    String temFileName = name;
                    if (name != null) {
                        File temFile;
                        if (schunk != null) {
                            temFileName = schunk + "_" + name;
                            temFile = new File(repository, temFileName);
                        } else {
                            temFile = new File(uploadPath, temFileName);
                        }

                        if (!temFile.exists()) {//断点续传
                            item.write(temFile);
                        }
                    }
                }
            }
            //文件合并

            if (schunk != null && schunks != null && schunk == schunks - 1) {
                File tempFile = new File(uploadPath, name);
                os = new BufferedOutputStream(new FileOutputStream(tempFile));

                for (int i = 0; i < schunks; i++) {
                    File file = new File(repository, i + "_" + name);
                    while (!file.exists()) {
                        Thread.sleep(100);
                    }
                    byte[] bytes = FileUtils.readFileToByteArray(file);
                    os.write(bytes);
                    os.flush();
                    file.delete();
                }
                os.flush();
            }
            response.getWriter().write("上传成功" + name);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
