# 在线小说书城系统 · 前端（Nut）

毕业设计「在线小说书城系统」前端工程，基于 **Vue 3 + Vite + Element Plus + Pinia + Axios** 构建，包含读者端、作者端、管理端三套页面。

## 技术栈

| 模块 | 技术 |
|---|---|
| 框架 | Vue 3（组合式 API + `<script setup>`） |
| 构建 | Vite 5 |
| UI | Element Plus（中文语言包，自定义“书卷红”主题） |
| 路由 | Vue Router 4（含登录 / 角色守卫） |
| 状态 | Pinia（用户信息、全局充值弹窗、未读消息数） |
| HTTP | Axios（统一拦截器：token 注入、401 跳登录、3001 余额不足弹充值） |

## 目录结构

```
Nut/
├── index.html
├── vite.config.js          # 开发代理：/api → http://localhost:8080
├── src/
│   ├── main.js             # 入口：注册 Element Plus / 图标 / 路由 / Pinia
│   ├── App.vue             # 根组件（挂载全局充值弹窗）
│   ├── styles/index.css    # 全局主题与通用样式
│   ├── assets/             # 默认封面 / 默认头像
│   ├── api/                # 接口封装（request.js 拦截器 + 各模块 API）
│   ├── store/              # Pinia：user（登录态）、app（弹窗/未读数）
│   ├── utils/format.js     # 金额分转元、时间格式化、状态文案映射
│   ├── router/index.js     # 路由 + 登录/角色守卫
│   ├── components/         # BookCard、RechargeDialog（全局充值弹窗）
│   ├── layouts/            # ReaderLayout / AuthorLayout / AdminLayout
│   └── views/
│       ├── reader/         # 读者端：首页、书城、详情、阅读器、书架、登录注册、
│       │                   #         个人中心、订单、评论、通知、作者入驻
│       ├── author/         # 作者端：数据看板、作品管理、章节管理
│       ├── admin/          # 管理端：用户、作者审核、小说、分类、订单、评论
│       └── error/NotFound.vue
└── README.md
```

## 快速开始

### 1. 启动后端（前置）

先启动 Spring Boot 后端（Potato 工程），确保 `http://localhost:8080/api` 可访问。

### 2. 安装依赖并启动

```bash
cd D:\graduation_project\Nut
npm install
npm run dev
```

浏览器访问 <http://localhost:5173>。

- 开发服务器已配置代理：前端统一请求 `/api/**`，由 Vite 转发到 `http://localhost:8080`（无需处理跨域）。
- 生产构建：`npm run build`，产物输出到 `dist/`，可用任意静态服务器托管，并配合 Nginx 将 `/api` 反代到后端。

### 3. 测试账号（密码均为 `123456`）

| 账号 | 角色 | 入口 |
|---|---|---|
| `reader` | 读者 | 读者端首页（余额 10000 分，已购《星海征途》部分章节） |
| `author01` | 作者 | 登录后右上角「作者中心」 |
| `admin` | 管理员 | 登录后右上角「管理后台」 |

也可在登录页点击测试账号标签自动填充。

## 关键设计说明

- **统一响应拦截**（`src/api/request.js`）：后端 HTTP 固定 200，业务以 `code` 为准。
  - `401` → 清理登录态并跳转登录页（带 `redirect` 回跳）；
  - `3001` 余额不足 → 自动弹出全局充值弹窗；
  - `3002` 已购买 → 提示后自动刷新（阅读器会重新拉取正文）；
  - 其余非 200 → `ElMessage` 展示后端 `message`。
- **金额**：后端单位“分”，前端统一 `fenToYuan()` 转为“元”展示。
- **付费阅读链路**：详情页/目录进入阅读器 → 免费章直接读；付费章未购买时后端返回 403 → 阅读器展示购买面板（支持单章 / 整本）→ 下单 → 确认支付 → 重新拉取正文。
- **阅读进度**：进入章节与滚动（防抖）时调用 `PUT /api/bookshelf/progress` 上报，书架支持“继续阅读”。
- **角色跳转**：登录后按角色进入对应端；入驻审核通过后，进入作者中心会自动刷新用户信息完成角色升级。

## 演示流程建议

1. 读者：注册/登录 → 首页浏览推荐与排行 → 搜索 → 详情 → 免费章节阅读 → 购买付费章节（余额不足触发充值）→ 书架继续阅读 → 评论。
2. 作者：申请入驻 → 管理员审核通过 → 新建作品 → 发布章节（可设免费/付费）→ 查看数据看板。
3. 管理员：用户禁用/启用 → 作者审核 → 小说上架/下架 → 分类管理 → 订单与评论管理。
