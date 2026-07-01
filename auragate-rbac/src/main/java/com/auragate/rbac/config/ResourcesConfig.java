package com.auragate.rbac.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Collections;

/**
 * 资源处理配置类
 *
 * 这个类主要负责两件事情
 * 1. 配置静态资源访问路径(比如用户上传的图片、文件等等)
 * 2. 配置跨域访问规则(允许前端应用访问后端API)
 *
 * 可以把这个类看做是系统的"接待处"和"通行证办理处"
 */
@Configuration //告诉spring: 这是一个配置类, 启动时要加载我
public class ResourcesConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理器
     *
     * 这个方法负责告诉spring: 当用户请求某些路径时,
     * 应该去服务器的哪个文件夹里找到对应的文件
     *
     * 比如: 用户在我们的网站上传了一张头像: 我们把它存在服务器的 ./file/avatar.png
     * 当用户访问 http://localhost:8080/profile/avatar.png 时
     * 系统会自动取 ./file 文件夹下面找到avatar.png
     *
     * @param registry 资源处理器注册器, 相当于"文件管家"
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //本地文件上传路径配置
        //解释: 当请求以/profile/ 开头的URL时...
        registry.addResourceHandler("/profile/**")
                //...就去本地文件系统指定的路径下寻找文件
                //"file:" 表示这是本地文件系统路径 (不是类路径)
                //AuraGateConfig.getProfile() 是获取配置文件中的上传路径
                //比如 file:./file
                .addResourceLocations("file:" + AuraGateConfig.getProfile() + "/");
    }

    /**
     * 跨域配置源
     *
     * 这个方法配置了哪些"外部的"网站可以访问我们的后端API
     * 跨域就像是"跨省通行证"
     * 默认情况下, 我们的浏览器不允许一个网站(如: http://localhost:90)
     * 去访问另外一个网站(如: http://localhost:8080) 的api
     *
     * 这个配置就是告诉浏览器: "允许某些外部网站可以访问我"
     *
     */
    @Value("${app.cors.allowed-origins:http://localhost:5175}")
    private String allowedOrigins;

    @Bean //告诉spring: 这个方法返回的对象要放到容器里进行管理
    public CorsConfigurationSource corsConfigurationSource() {
        //创建一个跨域配置对象, 相当与"通行证的规则手册"
        CorsConfiguration config = new CorsConfiguration();

        //1.允许哪些来源访问（从配置读取，默认开发环境 localhost:5175）
        config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));

        //2.允许携带哪些请求头
        config.setAllowedHeaders(Collections.singletonList("*"));

        //3.允许哪些HTTP方法(GET POST PUT DELETE OPTIONS)
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        //4.是否允许发送凭证(如: cookies HTTP认证信息)
        config.setAllowCredentials(true);

        //5.预检请求的缓存时间(单位:秒) — 1 小时
        config.setMaxAge(3600L);

        //创建基于URL的跨域配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        //注册配置: 对所有路径(/**)应用上面的跨域规则
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}
