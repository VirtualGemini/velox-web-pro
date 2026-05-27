# Velox Web Pro 贡献指南

[English](./CONTRIBUTING.md) | 简体中文

感谢你参与 Velox Web Pro。本指南描述前后端共同遵守的仓库级贡献流程。

## 项目说明

本项目的前端基于开源项目 Art Design Pro 二次开发。项目保留原仓库所有提交记录以及贡献者名单，以保留署名和历史可追溯性。

## 开始之前

请先阅读：

- [开发规范](./docs/DEVELOPMENT_SPEC.zh-CN.md)
- [提交规范](./docs/COMMIT_SPEC.zh-CN.md)
- [README](./README.zh-CN.md)

## 贡献流程

1. 从最新主干创建工作分支。
2. 保持变更聚焦，避免混入无关格式化或重构。
3. 根据变更范围执行必要检查。
4. 按提交规范编写 commit message。
5. 提交 Pull Request，并说明变更摘要、验证结果和影响范围。

推荐分支命名：

```text
feat/<short-name>
fix/<short-name>
docs/<short-name>
refactor/<short-name>
chore/<short-name>
```

## 本地验证

后端变更至少执行：

```bash
cd backend
mvn clean compile
```

前端变更至少执行：

```bash
cd frontend
pnpm lint
pnpm build
```

文档或仓库治理变更应检查链接、路径、命令示例和中英文文档一致性。

## 开发规则

- 根目录文档和配置只放前后端共同适用的规则。
- 前端专属规则、脚本和工具配置保留在 `frontend/`。
- 后端专属规则、脚本和工具配置保留在 `backend/`。
- 优先复用现有项目结构、命名、工具函数、类型定义和组件模式。
- 不在一次提交中混合无关的功能、修复、格式化和仓库治理变更。
- 不提交本地环境文件、构建产物、依赖目录、日志、IDE 文件或本地代理工作区。

## 提交信息

提交信息使用 Conventional Commits：

```text
<type>(<scope>): <subject>
```

示例：

```text
feat(frontend): add file config status filter
fix(backend): handle expired reset password code
docs(root): add contribution guide
```

允许的 `type` 和详细规则见 [提交规范](./docs/COMMIT_SPEC.zh-CN.md)。

## Pull Request 要求

PR 应包含：

- 变更摘要
- 影响范围
- 验证命令或测试结果
- 配置、数据库、接口或兼容性说明
- 关联 issue，若有

涉及前后端联动时，应说明接口契约、权限码、错误结构和环境变量是否已经两端同步。

## 文档维护

根目录文档按中英文成对维护：

- `README.md` / `README.zh-CN.md`
- `CONTRIBUTING.md` / `CONTRIBUTING.zh-CN.md`
- `CHANGELOG.md` / `CHANGELOG.zh-CN.md`
- `docs/DEVELOPMENT_SPEC.md` / `docs/DEVELOPMENT_SPEC.zh-CN.md`
- `docs/COMMIT_SPEC.md` / `docs/COMMIT_SPEC.zh-CN.md`

更新一种语言时，应在同一变更中同步更新对应文档。

## 致谢

前端基于 Art Design Pro：

https://github.com/Daymychen/art-design-pro
