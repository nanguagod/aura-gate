下次启动项目所需命令
前提条件
MySQL 服务已启动，且 fast 数据库已创建
数据库初始化：在 MySQL 中执行 fast.sql 建表
1️⃣ 启动后端
在 springboot/ 目录下：


cd springboot
mvnw spring-boot:run
或直接运行已构建的 jar：


cd springboot
java -jar target/springboot-0.0.1-SNAPSHOT.jar
2️⃣ 启动前端
在 vue/ 目录下：


cd vue
npm run dev
访问地址
前端页面: http://localhost:90
后端接口: http://localhost:8080
API 代理: 前端通过 /base 前缀自动转发到后端 8080
默认管理员账号：admin / admin，登录密码是 admin（数据库中已预置）。