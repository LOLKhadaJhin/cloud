package com.nhwb.cloud.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
/**
 * 管理员
 * 作者：B站「怒火无边」
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "admin")
public class Admin {
    private String userName = "root";
    private String password = "root";
    private boolean login = true;
    private boolean config = true;
    private boolean myVideo = true;
    private boolean myImgLazy = true;
    private boolean myCustom = true;
    private boolean myUpload = true;
    private List<String> custom;
    private String repository = "D:\\repository";
}
