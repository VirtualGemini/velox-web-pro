# Velox Web Pro Development Spec

English | [简体中文](./DEVELOPMENT_SPEC.zh-CN.md)

This document defines repository-level development rules for Velox Web Pro. Root-level documents and configuration should only contain rules shared by both frontend and backend. Frontend-only and backend-only details should stay in their own subpackages unless they are merged into this document as clearly scoped subpackage rules.

## 1. Root Responsibilities

The repository root is responsible for:

- Project entry documents.
- Contribution workflow and commit rules.
- Common ignore, editor, and Git attributes configuration.
- Frontend-backend collaboration contracts.
- Subpackage architecture summaries.

The repository root should not contain:

- Frontend-only package manager, build, lint, Vite, Vue, Husky, Commitizen, or similar tool configuration.
- Backend-only Maven, Spring Boot, Java check, or module build configuration.
- Local agent workspaces or personal tooling.

## 2. Repository Architecture

- `frontend/`: frontend application.
- `backend/`: backend application.

## 3. Frontend Package

The frontend package is responsible for:

- Admin console UI.
- Routing, state management, API wrappers, permission display, and frontend build.
- Local API proxy configuration for frontend-backend development.

Frontend-specific scripts and tool configuration stay under `frontend/`, including package manager scripts, lint rules, formatting rules, and frontend commit helpers.

## 4. Backend Package

The backend package is responsible for:

- Authentication, authorization, users, roles, menus, files, mail, and foundational capabilities.
- Backend APIs and the unified response model consumed by the frontend.
- Java and Spring Boot build configuration.

The backend is organized into these modules:

- `velox-dependencies`: dependency version management.
- `velox-common`: shared business language and common models.
- `velox-framework`: pluggable, reusable, configuration-driven capability starters.
- `velox-infra`: product-level infrastructure wiring.
- `velox-system`: system business modules.
- `velox-server`: the only application startup module.

Backend dependencies must remain one-way:

```text
velox-dependencies
-> velox-common
-> velox-framework/*
-> velox-infra/*
-> velox-system
-> velox-server
```

Reverse dependencies and cyclic dependencies are forbidden.

## 5. Backend Module Rules

### 5.1 dependencies

`velox-dependencies` only manages dependency versions, BOMs, and dependency governance. It must not contain business code, auto-configuration, APIs, SPIs, entities, mappers, or configuration classes.

### 5.2 common

`velox-common` contains shared business language and common models, such as result wrappers, exceptions, error codes, shared enums, message definitions, and base DDD abstractions. It is not a generic utility bucket and must not contain product use cases, business flows, providers, or reusable technical capabilities that belong in starters.

### 5.3 framework

`velox-framework` contains complete capabilities that can be consumed by external projects. Each starter must have a clear boundary, independent value, configuration-driven behavior, explicit enable/disable semantics, and clear API, SPI, NoOp, or auto-configuration entry points.

Framework starters must:

- Be reusable outside the current product.
- Wire only themselves.
- Avoid broad component scanning.
- Behave explicitly when disabled.
- Avoid depending on `velox-common`.

Do not add boundaryless aggregate modules such as `velox-core-starter` or `velox-all-starter`. Do not add driver-only starters without external consumption value.

### 5.4 infra

`velox-infra` handles product-level infrastructure wiring. It may combine framework capabilities, define product-level scanning rules, and hold a small number of wiring rules that should not be generalized into framework modules.

It must not contain business use cases, controllers, business error codes, business enums, domain entities, or business mappers.

### 5.5 system

`velox-system` contains product business capabilities and implementations. It consumes capabilities from `common`, `framework`, and `infra`.

Current business areas include authentication, users, roles, menus, permissions, and file resource management.

System modules must not add framework composition logic, starter auto-configuration logic, controller-to-mapper direct dependencies, or direct dependencies on another business module's implementation classes.

### 5.6 server

`velox-server` is the only startup module. It contains the Spring Boot entry point, application configuration files, environment configuration, and logging configuration. It must not contain business implementations, foundational capability implementations, or reusable starters.

## 6. Backend Auto-Configuration Rules

A starter with runtime wiring should provide:

- Its own `pom.xml`.
- Its own `AutoConfiguration`.
- Its own `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

Forbidden:

- Scanning all of `com.velox`.
- Broad `@ComponentScan` across framework starters.
- Business logic inside auto-configuration classes.

## 7. Backend Configuration Rules

The current code has completed module separation, but not configuration namespace migration.

The current low-level configuration entries remain valid:

- `velox.datasource.*`
- `velox.security.*`
- `velox.email.*`
- `velox.file.*`

Do not document `velox.infra.*` as implemented until the migration is actually completed.

## 8. Frontend-Backend Contract

Frontend-backend integration changes must keep these contracts clear:

- API path, method, request parameters, and response shape.
- Unified response structure, error codes, messages, and exception semantics.
- Authentication method, token transport, and 401 handling.
- Permission codes, menu data, dynamic routes, and button permission mapping.
- File upload, presigned upload, download, and delete flows.
- Environment variables, proxy target, CORS, and credential behavior.

Pull requests that change API contracts must state whether both frontend and backend were updated.

## 9. General Rules

- Put root-level rules in the root only when they apply to both frontend and backend.
- Keep frontend-only details under `frontend/`.
- Keep backend-only details under `backend/`.
- Do not mix unrelated frontend, backend, documentation, formatting, and repository-governance changes in one commit.
- Do not commit build outputs, dependency directories, logs, IDE files, local environment files, or local agent workspaces.
- Reuse existing project structure, naming, tools, and coding style.
- Backend code must not use Lombok.

## 10. Validation

Run validation according to the changed area:

- Root documentation or repository governance: check links, paths, command examples, and language-pair consistency.
- Frontend changes: run the relevant commands defined in `frontend/package.json`.
- Backend changes: run the relevant Maven compile or test commands.
- Frontend-backend integration changes: validate API calls, authentication, permission codes, menu routes, and error handling.

## 11. Summary

The root owns shared collaboration and governance. `frontend/` owns the web console. `backend/` owns backend services and capability modules.
