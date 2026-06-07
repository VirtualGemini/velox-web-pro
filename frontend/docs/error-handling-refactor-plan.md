# 前端错误处理统一改造 · 需求方向计划

> 状态：已实施（2026-06-07）｜vue-tsc / ESLint / 生产构建均通过
> 范围：`frontend/` 全项目错误处理链路

---

## 1. 背景与问题

控制台频繁出现红色 `console.error`，但其中绝大多数是**预期内、已处理**的业务结果（如「发件分组名称已存在」「未登录 401」「表单未填」），并非真正的异常。它们污染控制台、干扰排查，将来接入错误上报平台还会产生大量假告警。

### 1.1 一条业务错误为何产生 2~3 条 error

以「新建发件分组，名称重复」为例，后端返回 `{code: 400, msg: '发件分组名称已存在'}`：

```
api.post()
  └─ response 拦截器：code!==200 → throw HttpError        (index.ts:92)
       └─ request() catch：showError(error)               (index.ts:192)
            ├─ ElMessage.error('发件分组名称已存在')        ✅ 用户已看到
            └─ console.error('[HTTP Error]', ...)          ❌ 噪音#1 (error.ts:161)
       └─ Promise.reject(error) 继续抛出
            └─ MailGroupDialog.handleSubmit 只有 try/finally，无 catch
                 └─ 冒泡到 Vue 全局 errorHandler
                      └─ console.error('[VueError]', ...)  ❌ 噪音#2 (error-handle.ts:75)
```

### 1.2 三类共性问题（全项目）

| 类型 | 现象 | 代表位置 |
| --- | --- | --- |
| A. HTTP 层一律 error 级 | `showError` 对所有错误（含 4xx 业务）都 `console.error` | `utils/http/error.ts:161` |
| B. 调用处冗余日志 | `catch (e) { console.error('xxx失败', e) }`，而错误已被 HTTP 层提示 | `register`、`forget-password`、`system/account`、`mfa-challenge` 等 |
| C. 表单校验被当成错误 | `validate()` 与接口调用同处一个 try，校验失败的 `{field:[...]}` 被记成 error | `login/index.vue:260`、`code-login-verify` `handleSubmit` |
| D. 路由 401 误报 | 初始化时 401（正常未登录）先被无条件 `console.error`，再判断是否 401 | `router/guards/beforeEach.ts:380` |

---

## 2. 设计目标与原则

1. **单一职责**：HTTP 层是「用户提示 + 开发日志」的唯一入口；调用方的 `try/catch` 只负责**流程控制**（保持弹窗、停 loading、回滚 UI），不负责再次提示或打日志。
2. **已处理 ≠ 异常**：凡是经过 HTTP 层并已 `ElMessage` 提示的 `HttpError`，视为「已处理」，不应再以红色 error 出现在控制台或全局处理器中。
3. **校验不是错误**：element-plus 表单校验失败是正常交互分支，绝不进入错误日志。
4. **真错误要留得住**：网络中断、5xx、非 HttpError 的代码异常，仍需可见、可上报。
5. **统一出口**：所有 `console.*` 收敛到统一 `logger`，便于将来一处接入监控上报。

---

## 3. 决策记录（已与用户对齐）

- **Q1 已处理 HttpError 的日志级别 → 仅 DEV 输出**：开发环境保留 `logger.warn` 留痕，生产静默。
- **Q2 改造范围 → 全面改造 + 统一 logger**：中心化改造 + 逐个清理业务页 + 校验单独识别 + 补齐缺失 catch + 引入统一 `logger` 封装（为错误上报预留）。

---

## 4. 方案详解

### 4.1 统一 logger（新增 `src/utils/sys/logger.ts`）

环境策略：DEV 全部输出；PROD 仅 `error` 输出，并把 `warn/error` 交给可注册的上报器。

```ts
type LogLevel = 'debug' | 'info' | 'warn' | 'error'
type Reporter = (level: LogLevel, args: unknown[]) => void

const isDev = import.meta.env.DEV
let reporter: Reporter | null = null
export const setLogReporter = (fn: Reporter | null) => { reporter = fn }

function write(level: LogLevel, args: unknown[]) {
  if (isDev) console[level](...args)
  else if (level === 'error') console.error(...args)
  if (!isDev && (level === 'warn' || level === 'error')) reporter?.(level, args) // 预留上报
}

export interface Logger { debug: F; info: F; warn: F; error: F } // F = (...a:unknown[])=>void
export function createLogger(scope?: string): Logger { /* 给每条加 scope 前缀，如 '[HTTP]' */ }
export const logger = createLogger()
```

- 「已处理错误」一律走 `logger.warn` → 满足 Q1「仅 DEV 输出」。
- 「真异常」走 `logger.error` → DEV/PROD 都可见，生产可上报。

### 4.2 HTTP 层 `showError` 改造（`utils/http/error.ts`）

```ts
export function showError(error: HttpError, showMessage = true): void {
  if (showMessage) ElMessage.error(error.message)
  // 已提示用户 → 视为已处理：按错误性质分级
  if (isServerOrNetworkError(error.code)) logger.error('[HTTP]', error.toLogData()) // 5xx/网络/超时：真故障
  else logger.warn('[HTTP]', error.toLogData())                                     // 4xx/业务：仅 DEV
}
```

> 见 §6「待确认」：4xx=warn / 5xx=error 的分级，是否采纳。

### 4.3 全局错误处理器（`utils/sys/error-handle.ts`）

- `vueErrorHandler`：`isHttpError(err)` → 已处理，`logger.warn` 后 return；`isFormValidationError(err)` → 忽略；否则 `logger.error('[VueError]', ...)`。
- `registerPromiseErrorHandler`：`HttpError`/校验对象 → `event.preventDefault()` 并忽略（消除「Uncaught (in promise)」）；否则 `logger.error('[PromiseError]', ...)`。
- `scriptErrorHandler` / `registerResourceErrorHandler`：保留现有忽略名单，输出改走 `logger.error`。
- 新增 `isFormValidationError(e)`：识别 element-plus 校验对象 `{ field: [{message, field}] }` 形态，作为兜底安全网（主修在调用处）。

### 4.4 路由守卫（`router/guards/beforeEach.ts`）

调整 catch 顺序：**先判 401**（正常未登录）→ `logger.warn` + `next(false)`；其余才 `logger.error` + 跳 500。消除初始化期 401 的红色误报。

### 4.5 业务页统一范式

```ts
async function handleSubmit() {
  // ① 校验单独处理，失败直接 return —— 不进入下面的 try，不进日志
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await fetchXxx(payload)
    ElMessage.success(...)
    // …成功收尾
  } catch {
    // HttpError 已由 HTTP 层提示并分级记录；这里只做流程控制，不再 console.error
  } finally {
    submitting.value = false
  }
}
```

需按此范式清理（删冗余日志 / 分离校验 / 必要处补 catch）：

- `views/auth/login/index.vue`（去掉把校验对象记成 `[Login] Unexpected error`）
- `views/auth/register/index.vue`
- `views/auth/forget-password/index.vue`
- `views/auth/code-login-verify/index.vue`（`handleSubmit` 校验分离）
- `views/auth/mfa-challenge/index.vue`
- `views/system/account/index.vue`
- `views/config/mail-account/components/MailGroupDialog.vue`（补 `handleToggleActive` 失败回滚开关等）
- `views/config/mail-account/components/MailChannelDialog.vue`、`.../index.vue`（工作区已改动，一并对齐）

### 4.6 全项目 console 收敛（sweep）

将其余**合法**的 `console.error/warn`（WebSocket、ECharts、storage、upgrade、router/core、worktab、各组件）机械迁移到 `logger.error/warn`，语义不变，仅统一出口。涉及约 100 处，纯替换、低风险。

---

## 5. 文件清单（分阶段 checklist）

**阶段一 · 核心（必须）**
- [ ] 新增 `src/utils/sys/logger.ts`
- [ ] `src/utils/http/error.ts`：`showError` 分级 + 走 logger
- [ ] `src/utils/sys/error-handle.ts`：HttpError/校验抑制 + 走 logger
- [ ] `src/router/guards/beforeEach.ts`：401 优先、降级

**阶段二 · 业务页范式（高价值）**
- [ ] 7 个 auth/system 视图 + mail-account 三个组件（见 §4.5）

**阶段三 · 全局收敛（机械 sweep）**
- [ ] `utils/socket`、`hooks/core/useChart`、`utils/storage`、`utils/sys/upgrade`、`router/core/*`、`store/modules/worktab`、各 `components/core/*`、`utils/table/*`

---

## 6. 待确认的设计点

**错误分级**：是否采纳「4xx/业务 → `logger.warn`（仅 DEV）；5xx/网络/超时 → `logger.error`（始终 + 可上报）」？
- 采纳：生产环境仍能捕获真实后端故障/断网，更利于运维。
- 不采纳：严格按 Q1，所有已提示的 HttpError 一律 warn（仅 DEV），生产对 5xx 也静默（依赖后端监控）。
- 倾向：**采纳**（更稳妥，且不违背「业务错误不刷屏」的初衷）。

---

## 7. 验证方案（改造后逐项手测）

| 场景 | 期望 |
| --- | --- |
| 新建重名分组 | 仅 ElMessage 红条；控制台无 `[HTTP Error]`/`[VueError]`（DEV 一条 warn） |
| 未登录访问受控页 | 仅浏览器原生 401 行；无红色 `[RouteGuard]`/`[HTTP Error]`（DEV warn） |
| 登录留空提交 | 字段下校验提示；无 `[Login] Unexpected error` |
| 后端 500 / 断网 | ElMessage 提示 + `logger.error`（生产可见、可上报） |
| 真代码异常（非 HttpError） | 照常 `logger.error('[VueError]')`，不被误抑制 |

---

## 8. 风险与回滚

- **风险**：全局抑制 HttpError 可能掩盖「本应让调用方感知」的错误 → 通过「调用方 catch 仍可拿到 reject、只是不打日志」规避；抑制只针对**日志**，不改变 Promise 行为。
- **风险**：校验对象形态识别为启发式 → 仅作兜底，主修在调用处分离校验。
- **回滚**：改动按阶段提交，logger 为新增文件；如需回退，恢复 `showError`/`error-handle` 两文件即可还原旧行为。

---

## 9. 实施记录（2026-06-07）

- **§6 错误分级**：已采纳「4xx/业务 → warn(仅 DEV)；5xx/网络/超时 → error(始终+可上报)」。`error.ts` 新增 `isServerLevelError`，并给 `HttpError` 增加 `isNetworkError` 标记以区分「网络层无响应」与「业务 400」。
- **统一 logger**：新增 `utils/sys/logger.ts`，全项目 33 个文件的 `console.error/warn/info/debug/log` 统一收敛到 `logger.*`（`log` → `debug`）。刻意保留：`utils/sys/console.ts` 的 ASCII banner、JSDoc 注释中的示例。
- **踩坑修正**：`hooks/core/useTable.ts` 与 `utils/table/tableUtils.ts` 内部存在**同名局部 `logger` 调试包装对象**，机械替换会造成自递归 / 调用不存在的方法。已将局部对象更名为 `tableLogger`，其内部委托到全局 `logger`（仍受 `enableLog` 控制）。
- **校验分离**：`login` 等表单提交统一用 `validate().catch(() => false)`，使校验失败不再进入 catch、不再误记为错误。
- **验证**：`vue-tsc --noEmit` 通过；改动文件 ESLint 通过；`npm run build` 生产构建通过。浏览器内的实际接口/鉴权场景需在本地完整前后端环境下按 §7 手测。
