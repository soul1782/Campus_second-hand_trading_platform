package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ 关键：将 /images/** 映射到项目根目录下的 images/ 文件夹
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:images/");  // file: 表示外部目录
    }
}