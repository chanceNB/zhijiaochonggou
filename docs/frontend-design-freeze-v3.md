# Frontend Design Freeze V3

This document records the F00 visual foundation for the Golden Demo V2 student and teacher applications. It is a layout and interaction contract; it does not introduce business fixtures or replace API contracts.

## Visual language

- Page backgrounds: `#F5F7FB` for the teacher workspace and `#F5F9FF` for the student workspace.
- Surfaces are white with a light `#E2E8F1` border, an 8-10px radius, and restrained shadow `0 5px 18px rgb(37 61 99 / 7%)`.
- Primary blue is `#2563EB`; student primary is `#4F8EF7`. AI/success, warning, and risk use `#0FA779`, `#F59E0B`, and `#EF4444`.
- Text uses `Noto Sans SC, PingFang SC, Microsoft YaHei, sans-serif`; secondary text is `#6E7C92`.
- No large gradients, glassmorphism, neon dashboards, oversized hero typography, or heavy shadows.

## Student shell

- The sidebar is 186px wide and collapses to 76px. Navigation rows are 48px high with an 8px radius.
- Active navigation uses `#EEF2FF` with `#2F63DC` text. The six primary entries are 今日学习、AI学习教练、定向练习、错题本、我的成长、学习资料.
- Today keeps the high-density two-column workspace and may hide the regular topbar. Coach uses a 72px topbar. Practice uses a compact 58px topbar.
- All page bodies are responsive and must remain free of horizontal overflow at 1440x900 and 1672x941.

## Teacher shell

- The sidebar is 230px wide and collapses to 78px. The header is 56px high.
- Content is constrained to a maximum width of 1440px with `22px 24px 34px` padding on desktop.
- Primary navigation is 工作台、数据洞察、干预决策、干预结果、课程资源. Student profile and diagnosis case are secondary routes.
- Data Insights is the entry point for real SmartBI assets. Vue must not redraw SmartBI charts.

## Runtime and contract boundaries

- Pages orchestrate stores and ViewModels. API modules perform HTTP only, and adapters validate transport DTOs with Zod before presentation.
- Diagnostic sets always contain two questions, but result counts and accuracy are runtime-driven from real attempts and completion outcomes.
- SmartBI is verified IFRAME-first, with explicit NEW_TAB and UNVERIFIED fallbacks. AIChat recommendations remain MANUAL_CAPTURE and cross-origin iframe DOM is never read.
- Student and teacher resource upload remains unavailable until a role-scoped knowledge permission contract exists; neither role may call `/admin/knowledge/*` as a workaround.
- F00 pages use neutral placeholders only. They do not invent student metrics, class aggregates, intervention states, or practice outcomes.
