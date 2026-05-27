# Contributing to Velox Web Pro

English | [简体中文](./CONTRIBUTING.zh-CN.md)

Thank you for contributing to Velox Web Pro. This guide describes the repository-level contribution workflow shared by the frontend and backend.

## Project Notice

The frontend of this project is developed on top of the open-source project Art Design Pro. The original repository commit history and contributor list are retained to preserve attribution and traceability.

## Before You Start

Please read:

- [Development Spec](./docs/DEVELOPMENT_SPEC.md)
- [Commit Spec](./docs/COMMIT_SPEC.md)
- [README](./README.md)

## Contribution Workflow

1. Create a working branch from the latest main branch.
2. Keep the change focused and avoid unrelated formatting or refactoring.
3. Run the checks required by the changed area.
4. Write the commit message according to the commit spec.
5. Open a pull request with the change summary, validation result, and impact scope.

Recommended branch names:

```text
feat/<short-name>
fix/<short-name>
docs/<short-name>
refactor/<short-name>
chore/<short-name>
```

## Local Validation

Backend changes should run at least:

```bash
cd backend
mvn clean compile
```

Frontend changes should run at least:

```bash
cd frontend
pnpm lint
pnpm build
```

Documentation or repository-governance changes should check links, paths, command examples, and language-pair consistency.

## Development Rules

- Root-level documents and configuration should only contain rules shared by both frontend and backend.
- Frontend-only rules, scripts, and tool configuration should remain under `frontend/`.
- Backend-only rules, scripts, and tool configuration should remain under `backend/`.
- Reuse existing project structure, naming, utilities, types, and component patterns.
- Do not mix unrelated feature, fix, formatting, and repository-governance changes in one commit.
- Do not commit local environment files, build outputs, dependency directories, logs, IDE files, or local agent workspaces.

## Commit Messages

Use Conventional Commits:

```text
<type>(<scope>): <subject>
```

Examples:

```text
feat(frontend): add file config status filter
fix(backend): handle expired reset password code
docs(root): add contribution guide
```

See [Commit Spec](./docs/COMMIT_SPEC.md) for the allowed types and detailed rules.

## Pull Request Requirements

A pull request should include:

- Change summary
- Impact scope
- Validation commands or test results
- Configuration, database, API, or compatibility notes
- Related issue, if any

For frontend-backend integration changes, describe whether API contracts, permission codes, error structures, and environment variables were updated on both sides.

## Documentation Maintenance

Root-level documentation is maintained in language pairs:

- `README.md` / `README.zh-CN.md`
- `CONTRIBUTING.md` / `CONTRIBUTING.zh-CN.md`
- `CHANGELOG.md` / `CHANGELOG.zh-CN.md`
- `docs/DEVELOPMENT_SPEC.md` / `docs/DEVELOPMENT_SPEC.zh-CN.md`
- `docs/COMMIT_SPEC.md` / `docs/COMMIT_SPEC.zh-CN.md`

When updating one language, update the paired document in the same change.

## Acknowledgements

The frontend is based on Art Design Pro:

https://github.com/Daymychen/art-design-pro
