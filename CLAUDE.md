# Fintax-ai - 项目说明

这个是一个加密货币账户和交易查询的项目

## 项目概述

本项目采用双 Agent 架构模式，旨在解决 AI Agent 在跨越多个上下文窗口工作时的一致性和效率问题。

## 架构设计

### 双 Agent 模式

#### 1. Initializer Agent (初始化 Agent)
- **职责**：在首次运行时设置环境
- **输出**：
  - `init.sh` - 启动开发服务器的脚本
  - `claude-progress.txt` - 记录 Agent 进度的日志文件
  - `feature_list.json` - 功能需求列表（初始状态为 failing）
  - 初始 Git commit - 展示添加的文件

#### 2. Coding Agent (编码 Agent)
- **职责**：在每个会话中逐步推进项目进度
- **工作流程**：
  1. 获取项目当前状态
  2. 选择一个未完成的功能进行工作
  3. 实现功能并进行测试
  4. 提交 Git commit 并更新进度文件

## 核心组件

### 功能列表 (feature_list.json)
```json
{
  "category": "functional",
  "description": "功能描述",
  "steps": ["步骤1", "步骤2", ...],
  "passes": false
}
```
- 包含详细的端到端功能描述
- 初始状态为 `passes: false`
- Coding Agent 只修改 `passes` 字段，不允许删除或编辑测试

### 进度追踪 (claude-progress.txt)
- 记录已完成的工作
- 提供清晰的项目状态概览
- 帮助新会话快速理解上下文

### Git 提交规范
- 每个会话结束后创建描述性的 commit
- 允许通过 git 恢复到工作状态
- 保持代码库的可追溯性

### 初始化脚本 (init.sh)
- 提供启动开发服务器的标准化方式
- 包含基本端到端测试
- 确保 Agent 快速上手项目

## 技术栈

后端：
- Java 21
- Spring Boot 3
- MyBatis-Flex
- PostgreSQL
- Maven
- Lombok
- Fastjson

前端：
- TypeScript
- Vue 3
- Vite
- UI框架你自己看着来，欧美风格
- Tailwind CSS
- 路由你也自己来


## 项目架构规则

- 使用标准 MVC 分层架构：
  Controller → Service → Mapper → Database

- Controller 层只负责：
  - 请求接收
  - 参数校验
  - 返回响应

- 严禁在 Controller 中编写业务逻辑

- 每个 Controller 方法必须添加清晰的 JavaDoc 注释，说明：
  - 功能说明
  - 入参说明
  - 返回值说明

## 命名规范

- Java 类名使用 PascalCase（大驼峰）
- DTO 类统一使用 `*DTO` 作为后缀

Controller 层规范：
- 请求参数对象使用 `*Req`
- 返回结果对象使用 `*Resp`



## Git Commit 规范

格式：

type(scope): description

类型：
- feat
- fix
- refactor
- test
- docs

## API 响应规范

统一结构：

{
  code: number
  message: string
  data: any
}

## Coding Agent 会话流程

每个会话开始时，Agent 执行以下步骤：

1. **获取基本信息**
   ```
   pwd  # 查看工作目录
   ```

2. **了解项目进度**
   - 读取 `claude-progress.txt`
   - 读取 `feature_list.json`
   - 查看 git 日志

3. **启动并验证**
   - 运行 `init.sh` 启动开发服务器
   - 执行基本端到端测试确保应用正常工作

4. **实施新功能**
   - 选择一个未完成的功能
   - 逐步实现
   - 完整测试验证

5. **提交并记录**
   - 创建描述性 Git commit
   - 更新 `claude-progress.txt`
   - 更新 `feature_list.json` 中对应功能的 `passes` 状态

## 失败模式及解决方案

| 问题 | Initializer Agent | Coding Agent |
|------|-------------------|--------------|
| 过早宣称完成 | 设置功能列表文件 | 读取功能列表，选择单个功能工作 |
| 留下有bug的环境 | 创建初始git仓库和进度文件 | 会话开始读取进度和git日志，运行基本测试 |
| 过早标记功能完成 | 设置功能列表文件 | 自我验证所有功能后才标记为passing |
| 花时间搞清楚如何运行应用 | 编写init.sh脚本 | 会话开始读取init.sh |

## 最佳实践

1. **增量开发**：每次只处理一个功能，避免一次性完成太多工作
2. **清洁状态**：每个会话结束时，代码应该是可合并到主分支的状态
3. **完整测试**：必须使用端到端测试验证功能，不能只依赖代码审查
4. **清晰文档**：保持代码有序且文档完善
5. **进度可见**：使用 `feature_list.json` 和 `claude-progress.txt` 跟踪进度

## 目录结构

```
fitnax-ai/
├── CLAUDE.md              # 项目说明（本文件）
├── agent.md               # Agent 详细配置和提示
├── init.sh                # 初始化脚本（由 Initializer Agent 创建）
├── claude-progress.txt    # 进度日志（由 Initializer Agent 创建）
├── feature_list.json      # 功能需求列表（由 Initializer Agent 创建）
└── src/                   # 源代码目录
```

## 参考资源

- [Effective Harnesses for Long-Running Agents - Anthropic Engineering](https://www.anthropic.com/engineering/effective-harnesses-for-long-running-agents)
