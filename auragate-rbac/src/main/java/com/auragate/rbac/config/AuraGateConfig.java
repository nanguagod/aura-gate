package com.auragate.rbac.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 *
 * 这个类是一个"配置读取器", 专门负责读取application.yml
 * 配置文件中的自定义配置项 可以把这看做系统的"配置文件翻译官"
 */
@Component //告诉spring: "我是一个组件, 请把我放到容器里管理"
@ConfigurationProperties(prefix = "fast") //核心注解: 告诉spring: "请读取项目配置文件中以fast开头的配置项"
public class AuraGateConfig {

    /**
     * 文件上传路径配置
     *
     * 这个变量对应配置文件中的: fast.profile
     * 注意: 这里使用率static静态变量, 这样可以通过类名直接访问
     * 比如: AuraGateConfig.profile
     */
    private static String profile;  // 存储上传文件的基本路径

    public static String getProfile() {
        return profile;
    }

    /**
     * 设置上传路径
     *
     * spring会自动调用这个方法, 把配置文件中的值传进来
     * 过程:
     * 1. spring启动时, 读取配置文件
     * 2. 找到fast.profile = ./file
     * 3. 创建AuraGateConfig对象
     * 4. 调用setProfile("./file")
     *
     * @param profile 从配置文件中读取到的上传路径
     */
    public void setProfile(String profile) {
        // 把配置值设置到静态变量中
        AuraGateConfig.profile = profile;
    }
}
