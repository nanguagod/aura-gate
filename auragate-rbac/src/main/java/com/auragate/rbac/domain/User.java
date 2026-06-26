package com.auragate.rbac.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户对象 user
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    //用户ID
    private Long userId;

    //用户名
    private String userName;

    //用户性别
    private Integer sex;

    //用户头像
    private String avatar;

    //密码
    private String password;

    //创建时间
    // @JsonFormat 可以控制JSON格式转换
    // pattern = "yyyy-MM-dd HH:mm:ss" 表示格式化为: xxxx-xx-xx xx:xx:xx
    // 作用: 当这个对象转为json返回给我们的前端时, 日期会按这个格式进行显示
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    //角色ID (该字段用于关联角色信息)
    private Long roleId;

    //角色名称 (该字段用于关联查询)
    private String roleName;

}
