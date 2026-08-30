# 智教慧评 Frontend Codex Prompt Pack V1

适用仓库：`chanceNB/zhijiaochonggou`

基线提交：`6079ad9991f558ad0aa34953143da2750260252a`
- 若仓库 HEAD 更新，Codex 必须先确认当前 HEAD 是该提交的后代，并重新读取当前契约，不能回退新事实。
- 验证命令：
  `git merge-base --is-ancestor 6079ad9991f558ad0aa34953143da2750260252a HEAD`

## 这个包解决什么

1. 把学生端和教师端 UI 设计冻结到当前已经确认的 Golden Demo 主链。
2. 修正现有前端契约中与当前设计不一致的地方。
3. 把旧项目页面代码只作为“视觉真源”，不复用旧业务。
4. 给 Codex 一组可以按顺序执行的开发提示词。
5. 把 SmartBI 改为“验证后 IFRAME 优先、NEW_TAB 兜底”，AIChat 继续保留 MANUAL_CAPTURE 边界。
6. 明确：诊断题固定 2 道，但答对/答错结果绝不固定；所有结果按真实业务数据展示。

## 使用顺序

把本目录放到新仓库根目录，例如：

`frontend_codex_pack/`

然后在 Codex 中按顺序：

1. 先发 `prompts/00_MASTER_PROMPT.md`
2. 再发 `prompts/F00_CONTRACT_AND_VISUAL_FOUNDATION.md`
3. F00 验收 PASS 后发 F01
4. 依次执行到 F06
5. `optional/` 下的任务不属于 Golden Demo 主链，除非你明确决定扩展后端契约

## 开发方式

- 一个 Fxx 一个工作树、一个分支、一个 PR/提交序列。
- 前端设计已经通过用户 Design Gate；Codex 不得重新设计。
- 同一 Fxx 预检通过后连续执行，普通编译/测试问题自行修复。
- 只有以下情况才停：
  1. 当前权威契约与真实后端产生无法自行判断的冲突；
  2. 会覆盖已有未提交修改；
  3. 需要破坏性 Git/数据操作；
  4. SmartBI 租户的真实 iframe/认证行为与约定不一致，且没有安全 fallback。

## 最重要的禁止事项

- 禁止复制旧项目 Store/API/Mock/业务算法。
- 禁止把旧项目页面里的班级、学生、百分比、题目、风险数据当新项目事实。
- 禁止把“两道诊断题”写死成“一对一错”。
- 禁止前端计算 mastery / predictedLift / actualLift。
- 禁止 Vue 重画 SmartBI Dashboard 冒充 SmartBI。
- 禁止从跨域 SmartBI iframe 读取 DOM 抓取 AIChat 文本。
- 禁止学生/教师页面直接调用 `/admin/knowledge/*` 绕权限。
- 禁止硬编码 API Key、JWT、SmartBI 凭据、resid。
