# Potato — 在线小说书城系统后端

基于 Spring Boot 4 + MyBatis-Plus + Sa-Token + MySQL 的在线小说书城后端，
面向读者端 / 作者端 / 管理员端三端提供 RESTful API。

## 技术栈

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Spring Boot | 4.1.1 | Web MVC + Validation + JDBC |
| MyBatis-Plus | 3.5.17 | ORM（spring-boot4-starter） |
| Sa-Token | 1.46.0 | 登录鉴权与角色权限（spring-boot4-starter） |
| MySQL | 8.0+ | InnoDB / utf8mb4 |
| RocketMQ | 5.x（可选） | 下单异步处理、超时关单（默认关闭） |
| Lombok | Boot 管理 | 简化实体代码 |

## 快速开始

1. **准备数据库**：创建数据库 `Potato`（与 `application.yml` 一致），导入建表脚本与测试数据：
   ```sql
   CREATE DATABASE Potato DEFAULT CHARSET utf8mb4;
   -- 在 Potato 库中执行 novel_bookstore.sql
   -- 再执行增量脚本（新增站内通知表）
   --   sql/upgrade_notification.sql
   ```
2. **修改数据源**：编辑 `src/main/resources/application.yml` 中的 `spring.datasource`（用户名/密码）。
3. **启动**：`mvnw spring-boot:run`（或 IDE 直接运行 `PotatoApplication`）。
4. 访问 `http://localhost:8080`（接口前缀 `/api`）。

## 测试账号（密码均为 123456）

| 账号 | 角色 | 说明 |
| --- | --- | --- |
| admin | 管理员 | 管理后台 |
| author01 | 作者 | 已通过入驻审核，含测试作品《星海征途》 |
| reader | 读者 | 余额 10000 分，可直接购买付费章节 |

## 关键约定

- **响应体**：`{ "code": 200, "message": "操作成功", "data": ... }`
- **鉴权**：登录后返回 `token`，前端请求头携带 `Authorization: <token>`（Sa-Token）
- **分页**：统一参数 `page` / `size`（size 上限 50），返回 `{records,total,current,size,pages}`
- **金额**：单位统一为“分”
- **密码**：BCrypt 加密存储
- **订单**：模拟支付。下单创建“待支付”订单 → 确认支付扣减余额并放行阅读权限；
  未支付订单 15 分钟（`potato.order.timeout-minutes`）后由定时任务自动关闭。

## RocketMQ（可选加分项）

默认 `potato.mq.enabled=false`，使用进程内异步线程完成“销量统计 + 站内通知”，
订单超时由定时任务兜底，**无需安装任何中间件即可完整演示**。

如需演示 RocketMQ：
1. 本地启动 RocketMQ（nameserver + broker）；
2. 修改 `application.yml`：`potato.mq.enabled=true`，并按需调整 `potato.rocketmq.*`；
3. 重启应用后，支付成功消息与订单超时延迟消息将走 RocketMQ 生产/消费链路。

## 接口文档

见项目根目录《接口文档-在线小说书城.md》（供前端开发使用）。
