package com.reciprocityledger.backend.common.config;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 全局跨域配置。
 * 这里允许本地 Flutter Web 调试页访问后端，避免 Chrome Dev Server 使用随机端口时被浏览器拦截。
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origin-patterns}")
    private String allowedOriginPatterns;

    /**
     * 使用 origin pattern 而不是固定 origin，目的是兼容 Flutter Web 本地调试时的随机端口。
     * 仅放开 POST 和 OPTIONS，保证当前接口约定不变，同时满足浏览器预检请求。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> originPatternList = StrUtil.splitTrim(allowedOriginPatterns, ',');
        registry.addMapping("/**")
                .allowedOriginPatterns(originPatternList.toArray(new String[0]))
                .allowedMethods("POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
