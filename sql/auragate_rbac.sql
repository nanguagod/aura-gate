-- AuraGate RBAC 数据库初始化脚本
-- 数据库: auragate

CREATE DATABASE IF NOT EXISTS `auragate` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `auragate`;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_name` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像地址',
  `status` tinyint DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_name` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色权限字符串',
  `status` tinyint DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单表
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `path` varchar(200) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) DEFAULT NULL COMMENT '菜单图标',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `menu_type` tinyint DEFAULT '0' COMMENT '菜单类型(0目录 1菜单 2按钮)',
  `status` tinyint DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 用户角色关联表
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色菜单关联表
DROP TABLE IF EXISTS `role_menu`;
CREATE TABLE `role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 初始化数据
INSERT INTO `user` (`user_id`, `user_name`, `password`, `nickname`, `status`) VALUES
(1, 'admin', '123456', '管理员', 0);

INSERT INTO `role` (`id`, `role_name`, `role_key`, `status`) VALUES
(1, '管理员', 'admin', 0);

INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (1, 1);

INSERT INTO `menu` (`id`, `menu_name`, `parent_id`, `path`, `component`, `perms`, `icon`, `order_num`, `menu_type`) VALUES
(1, '系统管理', 0, '/system', NULL, NULL, 'Setting', 1, 0),
(2, '用户管理', 1, '/system/user', 'system/user/index', 'system:user:list', 'User', 1, 1),
(3, '角色管理', 1, '/system/role', 'system/role/index', 'system:role:list', 'Avatar', 2, 1),
(4, '菜单管理', 1, '/system/menu', 'system/menu/index', 'system:menu:list', 'Menu', 3, 1);
