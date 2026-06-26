package com.auragate.rbac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// exclude = { DataSourceAutoConfiguration.class } 表示排除了数据源的自动配置
//为什么要排除? 因为我们要自定义数据源的配置
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class SpringbootApplication {

    //main方法: java程序的入口点
    public static void main(String[] args) {
        //启动 SpringBoot应用
        SpringApplication.run(SpringbootApplication.class, args);

        //在控制台中打印成功启动信息
        System.out.println("后端启动成功~\n " +
                " | |__  _   _  __ _  ___ __ _(_)\n" +
                " | '_ \\| | | |/ _` |/ __/ _` | |\n" +
                " | | | | |_| | (_| | (_| (_| | |\n" +
                " |_| |_|\\__,_|\\__,_|\\___\\__,_|_|");
    }

}
