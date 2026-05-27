# Velox Web Pro 开发规范

[English](./DEVELOPMENT_SPEC.md) | 简体中文

本文档定义 Velox Web Pro 的仓库级开发规范。根目录文档和配置只放前后端共同适用的规则。仅前端或仅后端适用的细节应保留在对应子包内；如果合并到本文档，必须明确标注为对应子包规则。

## 1. 根目录职责

根目录负责：

- 项目入口文档。
- 贡献流程和提交规范。
- 通用忽略规则、编辑器规则和 Git 属性配置。
- 前后端协作契约。
- 子包架构摘要。

根目录不应承载：

- 仅前端可用的包管理、构建、lint、Vite、Vue、Husky、Commitizen 或类似工具配置。
- 仅后端可用的 Maven、Spring Boot、Java 检查或模块构建配置。
- 本地代理工作区或个人工具。

## 2. 仓库架构

- `frontend/`：前端应用。
- `backend/`：后端应用。

## 3. 前端子包

前端子包负责：

- 管理后台用户界面。
- 路由、状态管理、接口封装、权限展示和前端构建。
- 支持前后端本地联调的 API 代理配置。

前端专属脚本和工具配置保留在 `frontend/`，包括包管理脚本、lint 规则、格式化规则和前端提交辅助工具。

## 4. 后端子包

后端子包负责：

- 认证、鉴权、用户、角色、菜单、文件、邮件和基础能力。
- 面向前端的后端 API 和统一返回结构。
- Java 与 Spring Boot 构建配置。

后端由以下模块组成：

- `velox-dependencies`：依赖版本治理。
- `velox-common`：业务公共语言和公共模型。
- `velox-framework`：可插拔、可复用、配置驱动的能力 starter。
- `velox-infra`：产品级基建装配。
- `velox-system`：系统业务模块。
- `velox-server`：唯一应用启动模块。

后端依赖方向必须保持单向：

```text
velox-dependencies
-> velox-common
-> velox-framework/*
-> velox-infra/*
-> velox-system
-> velox-server
```

禁止反向依赖和循环依赖。

## 5. 后端模块规则

### 5.1 dependencies

`velox-dependencies` 只管理依赖版本、BOM 和依赖治理。不得包含业务代码、自动装配、API、SPI、实体、Mapper 或配置类。

### 5.2 common

`velox-common` 承载业务公共语言和公共模型，例如统一返回、异常、错误码、共享枚举、多语言文案和基础 DDD 抽象。它不是通用工具箱，不应包含产品用例、业务流程、provider 或应属于 starter 的可复用技术能力。

### 5.3 framework

`velox-framework` 承载可被外部项目直接消费的完整能力。每个 starter 必须具备明确边界、独立价值、配置驱动行为、显式启停语义，以及清晰的 API、SPI、NoOp 或自动装配入口。

Framework starter 必须：

- 能脱离当前产品复用。
- 只装配自己。
- 避免大范围组件扫描。
- disabled 时行为显式。
- 不依赖 `velox-common`。

禁止新增 `velox-core-starter`、`velox-all-starter` 这类无边界聚合模块。禁止新增对外无消费价值的纯 driver starter。

### 5.4 infra

`velox-infra` 负责产品级基建装配。它可以组合 framework 能力、定义产品级扫描规则，并承载少量不应通用化到 framework 的装配规则。

infra 不应包含业务用例、Controller、业务错误码、业务枚举、领域实体或业务 Mapper。

### 5.5 system

`velox-system` 承载产品业务能力和实现，消费 `common`、`framework`、`infra` 提供的能力。

当前业务范围包括认证、用户、角色、菜单、权限和文件资源管理。

system 模块不得新增 framework 组合逻辑、starter 自动装配逻辑、controller 到 Mapper 的直接依赖，或对另一个业务模块实现类的直接依赖。

### 5.6 server

`velox-server` 是唯一启动模块，承载 Spring Boot 启动入口、应用配置文件、环境配置和日志配置。不得包含业务实现、基础能力实现或可复用 starter。

## 6. 后端自动装配规则

带运行时装配逻辑的 starter 应提供：

- 自己的 `pom.xml`。
- 自己的 `AutoConfiguration`。
- 自己的 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。

禁止：

- 扫描整个 `com.velox`。
- framework starter 互相做大范围 `@ComponentScan`。
- 在自动装配类中写业务逻辑。

## 7. 后端配置规则

当前代码已经完成模块拆分，但尚未完成配置命名空间迁移。

现阶段仍保留已有低层配置入口：

- `velox.datasource.*`
- `velox.security.*`
- `velox.email.*`
- `velox.file.*`

不要在迁移实际完成前，把 `velox.infra.*` 记录为已实现配置入口。

## 8. 前后端协作契约

前后端联动变更必须保持以下契约清晰：

- API 路径、请求方法、请求参数和响应结构。
- 统一返回结构、错误码、错误消息和异常语义。
- 鉴权方式、Token 传递方式和 401 处理行为。
- 权限码、菜单数据、动态路由和按钮权限映射。
- 文件上传、预签名上传、下载和删除流程。
- 环境变量、代理地址、跨域和凭证行为。

涉及接口契约变化的 PR 必须说明前端和后端是否都已同步调整。

## 9. 通用规则

- 只有前后端共同适用的规则才放到根目录。
- 前端专属细节保留在 `frontend/`。
- 后端专属细节保留在 `backend/`。
- 不在一次提交中混合无关的前端、后端、文档、格式化和仓库治理变更。
- 不提交构建产物、依赖目录、日志、IDE 文件、本地环境文件或本地代理工作区。
- 复用现有项目结构、命名、工具和编码风格。
- 后端代码禁止使用 Lombok。

## 10. 验证要求

根据变更范围执行验证：

- 根文档或仓库治理变更：检查链接、路径、命令示例和中英文文档一致性。
- 前端变更：执行 `frontend/package.json` 中相关命令。
- 后端变更：执行相关 Maven 编译或测试命令。
- 前后端联动变更：验证接口调用、鉴权、权限码、菜单路由和错误处理。

## 11. 总结

根目录负责共同协作和治理；`frontend/` 负责 Web 控制台；`backend/` 负责后端服务和能力模块。
