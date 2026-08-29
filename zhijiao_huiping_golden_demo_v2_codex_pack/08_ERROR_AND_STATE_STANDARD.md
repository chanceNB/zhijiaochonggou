# 错误码与 UI 状态标准

## HTTP 与业务码

| HTTP | code | UI 行为 |
|---:|---|---|
| 200/201 | `OK` | READY / SUCCESS |
| 400 | `VALIDATION_ERROR` | 字段级提示 |
| 401 | `AUTH_REQUIRED` | 跳转登录 |
| 403 | `FORBIDDEN` | PermissionState |
| 404 | `RESOURCE_NOT_FOUND` | 友好不存在页 |
| 409 | `STATE_CONFLICT` | 重新拉取最新状态，不覆盖 |
| 412 | `PRECONDITION_FAILED` | version/If-Match 冲突 |
| 422 | `DOMAIN_RULE_VIOLATION` | 展示业务规则说明 |
| 429 | `AI_RATE_LIMITED` | 保留上下文，延迟重试 |
| 502 | `AI_UPSTREAM_ERROR` | AI degraded，不影响已保存业务事实 |
| 503 | `SMARTBI_UNAVAILABLE` | SmartBI unavailable，不出现 Vue 假图 |
| 503 | `RAG_UNAVAILABLE` | Coach 可降级回答但无 citation，必须显式提示 |
| 504 | `UPSTREAM_TIMEOUT` | 允许用户重试 |

## 统一 Envelope

```json
{
  "code": "OK",
  "message": "success",
  "requestId": "req-01J...",
  "data": {},
  "timestamp": "2026-08-29T08:00:00Z"
}
```

错误：

```json
{
  "code": "STATE_CONFLICT",
  "message": "资源状态已更新，请刷新后重试",
  "requestId": "req-01J...",
  "data": null,
  "details": {"currentVersion": 4},
  "timestamp": "2026-08-29T08:00:00Z"
}
```

## 规则

- 不向用户显示 stacktrace、SQL、`HTTP_CONTRACT_NOT_AVAILABLE`、raw exception。
- `requestId` 可放入“复制诊断信息”。
- LLM/RAG失败不能篡改已成功的 PracticeAttempt。
- SmartBI 失败不能阻塞学生学习与教师正式业务闭环。
