# RUN ONE TASK — 可直接复制给 Codex

把下面内容和一个 `task-packs/TASK-*.md` 一起发给 Codex。

```text
现在只执行我指定的 TASK-ID。

第一步先读取：
README_总包说明.md
00_GOLDEN_DEMO_V2_FREEZE.md
01_TECHNICAL_ARCHITECTURE.md
02_DOMAIN_BOUNDARIES_AND_CONTRACTS.md
03_DATA_MODEL_AND_SMARTBI_EXCHANGE.md
04_API_INTERFACE_SPEC.md
openapi/openapi.yaml
05_FRONTEND_CONTRACT.md
06_COMPONENT_LIBRARY.md
08_ERROR_AND_STATE_STANDARD.md
09_CODEX_MASTER_PROMPT.md
以及本次 TASK 文件。

预检：
git status --short
git branch --show-current
git rev-parse HEAD

如果存在与本 TASK 重叠的未提交修改，停止并报告。

实现规则：
1. 不扩大 TASK Scope。
2. 先写失败测试，再实现最小代码。
3. 不创建与文档冲突的第二套 DTO/Envelope/状态枚举。
4. 前端不计算业务核心指标。
5. SmartBI、LLM、RAG 都通过 Port/Adapter，真实平台能力未验证时不得造假。
6. 每次数据库结构变化只能新增 Flyway migration，不改已执行 migration。
7. 所有写命令检查幂等；状态迁移检查 version/If-Match。

完成后运行：
- 本 TASK targeted tests
- 受影响模块全量 tests
- frontend typecheck/build（若改前端）
- migration test（若改DB）
- OpenAPI parse/contract test（若改接口）
- git diff --check

最后只输出：
# <TASK-ID> REPORT
Repository
Files Changed
Contract Changes
Database Changes
Tests
Boundary Checks
Known Gaps
Decision = PASS / BLOCKED

完成后停止，不执行下一 TASK。
```
