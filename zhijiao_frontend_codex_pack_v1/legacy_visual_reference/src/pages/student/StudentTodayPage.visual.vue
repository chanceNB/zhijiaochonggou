<!-- VISUAL REFERENCE ONLY.
Source: src/pages/student/StudentTodayPage.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <div class="student-today-page">
    <div v-if="isInitialLoading" class="state-card" data-testid="today-loading">
      正在生成你的今日学习计划...
    </div>

    <section v-else-if="today.error" class="state-card error-state" data-testid="today-error">
      <el-alert :title="today.error" type="error" show-icon />
      <button type="button" data-testid="today-error-retry" @click="retryWithPlan">重新加载</button>
    </section>

    <section
      v-else-if="today.data && !today.hasTasks"
      class="state-card empty"
      data-testid="today-empty"
    >
      <h2>今天还没有任务</h2>
      <p>{{ today.emptyMessage }}</p>
      <button data-testid="today-retry" @click="retryWithPlan">让 AI 生成新的学习计划</button>
    </section>

    <template v-else-if="today.plan && today.progress">
      <section class="today-layout">
        <main class="today-main">
          <section class="best-action-card" data-testid="daily-plan-card">
            <div class="best-action-content">
              <header class="card-heading best-action-heading">
                <div>
                  <p class="section-kicker">你的下一最佳学习行动 <span>今日 AI 学习计划</span></p>
                  <div class="action-title-row">
                    <span class="step-badge">1</span>
                    <h1>{{ displayFocus }} <small>（概念讲解）</small></h1>
                  </div>
                  <div class="action-meta">
                    <span
                      ><el-icon aria-hidden="true"><Clock /></el-icon> 本步预计
                      {{ today.plan.steps[0]?.durationMinutes ?? 15 }} 分钟</span
                    >
                    <i></i>
                    <span>全计划约 {{ today.plan.durationMinutes }} 分钟</span>
                    <i></i>
                    <span>关键子主题：图像、性质、顶点式、对称轴</span>
                  </div>
                </div>
              </header>

              <ul class="action-evidence">
                <li v-for="evidence in safePlanEvidence.slice(0, 2)" :key="evidence.id">
                  <el-icon class="check-dot" aria-hidden="true"><Check /></el-icon>
                  <span
                    ><strong>{{ evidence.value }}</strong> {{ evidence.detail }}</span
                  >
                </li>
              </ul>

              <div class="best-action-buttons">
                <button
                  class="primary-button"
                  data-testid="start-learning"
                  type="button"
                  :disabled="actionDisabled"
                  @click="handlePlanAction('start-learning')"
                >
                  <el-icon aria-hidden="true"><VideoPlay /></el-icon> 开始第 1 步学习
                </button>
                <button class="secondary-button" type="button" :disabled="actionDisabled">
                  查看计划详情
                </button>
                <button
                  class="compat-action"
                  data-testid="practice-first"
                  type="button"
                  :disabled="actionDisabled"
                  @click="handlePlanAction('practice-first')"
                >
                  先做题
                </button>
                <button
                  class="compat-action"
                  data-testid="explanation-first"
                  type="button"
                  :disabled="actionDisabled"
                  @click="handlePlanAction('explanation-first')"
                >
                  先看讲解
                </button>
              </div>
            </div>
            <div class="action-illustration" aria-hidden="true">
              <div class="illustration-board">
                <span class="illustration-bar illustration-bar--one"></span>
                <span class="illustration-bar illustration-bar--two"></span>
                <span class="illustration-bar illustration-bar--three"></span>
                <span class="illustration-check"></span>
              </div>
            </div>
            <aside class="mastery-box" data-test="v3-today-confidence">
              <div class="confidence-box">
                <span>学习计划</span>
                <strong>{{ today.plan.durationMinutes }} 分钟</strong>
              </div>
              <p>按步骤完成今日学习任务</p>
            </aside>
            <p
              v-if="today.actionFeedback.status !== 'idle'"
              class="action-feedback"
              :class="today.actionFeedback.status"
              data-testid="action-feedback"
            >
              {{ today.actionFeedback.message }}
            </p>
          </section>

          <section class="learning-path-card" data-testid="learning-step-timeline">
            <header class="card-heading compact-heading">
              <h2>
                今日学习路径 <small>（共 {{ today.plan.steps.length }} 步）</small>
              </h2>
            </header>
            <div class="learning-path">
              <template v-for="(step, index) in today.plan.steps" :key="step.id">
                <article
                  class="learning-step"
                  :class="{ active: activeStepId === step.id || (!activeStepId && index === 0) }"
                  data-testid="learning-step"
                >
                  <span class="learning-step__number">{{ step.order }}</span>
                  <div class="learning-step__body">
                    <div class="learning-step__title">
                      <strong>{{ step.title }}</strong>
                      <span v-if="index === 0" class="recommended-tag">当前推荐</span>
                    </div>
                    <span class="learning-step__time">{{ step.durationMinutes }} 分钟</span>
                    <small>重点：{{ step.description }}</small>
                  </div>
                  <span class="learning-step__icon" aria-hidden="true">
                    {{
                      step.type === 'explanation'
                        ? '讲解'
                        : step.type === 'practice'
                          ? '练习'
                          : '验证'
                    }}
                  </span>
                </article>
                <span
                  v-if="index < today.plan.steps.length - 1"
                  class="path-arrow"
                  aria-hidden="true"
                  >→</span
                >
              </template>

<style scoped lang="scss">
.student-today-page {
  --today-blue: #2f5be7;
  --today-ink: #14244a;
  --today-muted: #67748d;
  --today-line: #e0e6f2;
  display: grid;
  gap: 16px;
  color: var(--today-ink);
}

.today-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) clamp(322px, 24vw, 364px);
  gap: 16px;
  align-items: start;
}

@media (min-width: 1281px) {
  .today-layout {
    grid-template-columns: minmax(0, 1fr) 364px;
  }
}

.today-main {
  display: grid;
  gap: 15px;
  min-width: 0;
}

.today-side {
  display: grid;
  gap: 14px;
  min-width: 0;
}

.best-action-card,
.learning-path-card,
.learning-loop-card,
.evidence-panel,
.feedback-panel,
.side-card,
.state-card {
  border: 1px solid var(--today-line);
  border-radius: 15px;
  background: #fff;
  box-shadow: 0 8px 28px rgb(40 65 115 / 6%);
}

.best-action-card {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 170px 236px;
  gap: 16px;
  min-height: 296px;
  padding: 20px 24px 17px;
}

.best-action-content {
  min-width: 0;
}

.card-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.section-kicker {
  margin: 0 0 10px;
  color: var(--today-ink);
  font-size: 18px;
  font-weight: 800;
}

.section-kicker span {
  display: inline-block;
  margin-left: 8px;
  padding: 4px 9px;
  border-radius: 6px;
  color: #159060;
  background: #e8f7ef;
  font-size: 12px;
  font-weight: 800;
}

.action-title-row {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
}

.step-badge {
  display: grid;
  width: 48px;
  height: 48px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 10px;
  color: #fff;
  background: var(--today-blue);
  font-size: 26px;
  font-weight: 800;
}

.action-title-row h1 {
  min-width: 0;
  margin: 0;
  color: #111e43;
  font-size: clamp(22px, 1.65vw, 29px);
  font-weight: 800;
  line-height: 1.25;
}

.action-title-row h1 small {
  font-size: 16px;
  font-weight: 700;
}

.action-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 13px;
  align-items: center;
  margin: 14px 0 12px 60px;
  color: #53627c;
  font-size: 12px;
}

.action-meta i {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #a8b2c3;
}

.confidence-box {
  display: grid;
  grid-template-columns: auto auto;
  gap: 6px;
  align-items: center;
  justify-content: space-between;
  padding: 9px 12px;
  border-radius: 10px;
  color: #147b56;
  background: #eff9f4;
  white-space: nowrap;
}

.confidence-box span {
  font-size: 12px;
}

.confidence-box strong {
  font-size: 14px;
}

.action-illustration {
  display: grid;
  min-width: 0;
  place-items: center;
  align-self: stretch;
}

.illustration-board {
  position: relative;
  width: 144px;
  height: 112px;
  border: 1px solid #dbe6ff;
  border-radius: 10px;
  background: #f7faff;
  box-shadow: 18px 13px 0 -8px #edf3ff;
  opacity: 0.92;
}

.illustration-board::before,
.illustration-board::after {
  position: absolute;
  display: block;
  content: '';
  border-radius: 4px;
  background: #dbe8ff;
}

.illustration-board::before {
  right: 14px;
  bottom: 17px;
  width: 98px;
  height: 2px;
  transform: rotate(-20deg);
  transform-origin: right center;
}

.illustration-board::after {
  right: 15px;
  bottom: 16px;
  width: 2px;
  height: 62px;
}

.illustration-bar {
  position: absolute;
  bottom: 18px;
  width: 12px;
  border-radius: 4px 4px 2px 2px;
  background: #9fbaff;
}

.illustration-bar--one {
  right: 40px;
  height: 29px;
}

.illustration-bar--two {
  right: 62px;
  height: 47px;
  background: #6e96f5;
}

.illustration-bar--three {
  right: 84px;
  height: 67px;
  background: #c1d2ff;
}

.illustration-check {
  position: absolute;
  top: 21px;
  left: 22px;
  width: 22px;
  height: 13px;
  border-bottom: 4px solid #88cdb8;
  border-left: 4px solid #88cdb8;
  transform: rotate(-35deg);
}

.confidence-box :deep(.confidence-badge) {
  color: #0d8d5b;
  font-size: 20px;
}

.action-evidence {
  display: grid;
  gap: 8px;
  padding: 0;
  margin: 0;
  list-style: none;
}

.action-evidence li {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  color: #44516d;
  font-size: 13px;
  line-height: 1.5;
}

.action-evidence li strong {
  color: #2d3f69;
}

.check-dot {
  display: inline-grid;
  width: 16px;
  height: 16px;
  flex: 0 0 auto;
  place-items: center;
  margin-top: 1px;
  border-radius: 50%;
  color: #fff;
  background: #1b9e6b;
  font-size: 11px;
  font-weight: 800;
}

.best-action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-top: 16px;
}

.primary-button,
.secondary-button,
.compat-action,
.loop-footer button,
.wrong-side-card button,
.state-card button {
  min-height: 44px;
  padding: 0 16px;
  border-radius: 9px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.primary-button {
  border: 1px solid var(--today-blue);
  color: #fff;
  background: var(--today-blue);
  box-shadow: 0 6px 15px rgb(47 91 231 / 22%);
}

.secondary-button {
  border: 1px solid #7f9cff;
  color: #3159d7;
  background: #fff;
}

.compat-action {
  min-height: 44px;
  padding: 0 22px;
  border: 1px solid #dce6ff;
  color: #3159d7;
  background: #fff;
  font-size: 13px;
}

button:disabled {
  cursor: wait;
  opacity: 0.55;
}

.mastery-box {
  display: grid;
  align-content: start;
  gap: 5px;
  padding: 17px 14px 14px;
  border: 1px solid #dbe3f3;
  border-radius: 10px;
  background: #fcfdff;
}

.mastery-box p,
.mastery-updated {
  margin: 0;
  color: #6a7790;
  font-size: 12px;
}

.mastery-values {
  display: flex;
  align-items: center;
  justify-content: space-between;
  line-height: 1;
  color: #2f5be7;
}

.mastery-values strong {
  font-size: 24px;
  line-height: 1;
}

.mastery-values span {
  color: #48ac8a;
  font-size: 29px;
  line-height: 1;
}

.mastery-values .target {
  color: #118d5d;
}

.mastery-probability,
.mastery-updated {
  padding-top: 7px;
  border-top: 1px solid #edf0f5;
  line-height: 1.35;
}

.mastery-probability b {
  float: right;
  color: #253657;
}

.compat-metadata {
  padding-top: 6px;
  border-top: 1px solid #edf0f5;
  color: #6a7790;
  font-size: 11px;
  line-height: 1.35;
}

.action-feedback {
  grid-column: 1 / -1;
  padding: 8px 11px;
  margin: -4px 0 0;
  border-radius: 7px;
  color: #2252b4;
  background: #eef4ff;
  font-size: 13px;
}

.action-feedback.success {
  color: #16764e;
  background: #eaf8f0;
}
.action-feedback.error {
  color: #b42318;
  background: #fff0ee;
}

.learning-path-card,
.learning-loop-card {
  padding: 16px 20px;
}

.learning-path-card {
  padding-block: 12px;
}

.learning-loop-card {
  padding-block: 14px;
}

.compact-heading h2,
.panel-heading h2,
.side-card h2 {
  margin: 0;
  color: var(--today-ink);
  font-size: 18px;
  font-weight: 800;
}

.compact-heading h2 small {
  color: #6b7890;
  font-size: 13px;
  font-weight: 500;
}

.learning-path {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 28px minmax(0, 1fr) 28px minmax(0, 1fr);
  gap: 8px;
  align-items: center;
  margin-top: 14px;
}

.learning-step {
  position: relative;
  display: grid;
  grid-template-columns: 30px minmax(0, 1fr) auto;
  gap: 8px;
  min-height: 108px;
  align-items: center;
  padding: 14px 12px 12px;
  border: 1px solid #d9e2f3;
  border-radius: 11px;
  background: #fff;
}

.learning-step.active {
  border-color: #2f5be7;
  box-shadow: inset 0 0 0 1px #2f5be7;
}

.learning-step__number {
  display: grid;
  width: 27px;
  height: 27px;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  background: #2f5be7;
  font-size: 15px;
  font-weight: 800;
}

.learning-step:not(.active) .learning-step__number {
  color: #273657;
  background: #eef2fa;
}

.learning-step__body {
  min-width: 0;
}
.learning-step__title {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}
.learning-step__title strong {
  color: #17264d;
  font-size: 15px;
}
.recommended-tag {
  padding: 2px 5px;
  border-radius: 4px;
  color: #2f5be7;
  background: #edf2ff;
  font-size: 10px;
}
.learning-step__time {
  display: block;
  margin-top: 7px;
  color: #53627c;
  font-size: 12px;
}
.learning-step__body small {
  display: block;
  margin-top: 5px;
  color: #6f7d96;
  font-size: 11px;
  line-height: 1.35;
}
.learning-step__icon {
  color: #2f5be7;
  font-size: 22px;
}
.path-arrow {
  color: #8191ad;
  font-size: 27px;
  text-align: center;
}

.loop-flow {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 28px minmax(0, 1fr) 28px minmax(0, 1fr) 28px minmax(0, 1fr);
  gap: 8px;
  align-items: center;
  margin-top: 14px;
}

.loop-node {
  display: grid;
  gap: 5px;
  min-height: 106px;
  place-items: center;
  padding: 12px 8px;
  border: 1px solid #d9e2f3;
  border-radius: 10px;
  text-align: center;
  background: #f9fbff;
}

.loop-node.positive {
  border-color: #c9eadb;
  background: #f6fcf9;
}
.loop-node.deviation {
  border-color: #ffd8d4;
  background: #fffafa;
}
.loop-node span,
.loop-node small {
  color: #5d6d87;
  font-size: 12px;
}
.loop-node strong {
  color: #2f5be7;
  font-size: 25px;
}
.loop-node.positive strong {
  color: #159466;
}
.loop-node.deviation strong {
  color: #dc3833;
}
.loop-arrow {
  color: #8795ad;
  font-size: 26px;
  text-align: center;
}

.loop-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  color: #697892;
  font-size: 12px;
}
.loop-footer button {
  min-height: 34px;
  border: 1px solid #6f8eff;
  color: #3159d7;
  background: #fff;
}

.today-bottom-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 14px;
}
.evidence-panel,
.feedback-panel {
  min-width: 0;
  padding: 17px 19px;
}

.evidence-panel,
.feedback-panel {
  padding-bottom: 4px;
}
.panel-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}
.panel-heading span {
  color: #718099;
  font-size: 11px;
}
.evidence-panel ol {
  display: grid;
  gap: 12px;
  padding: 0;
  margin: 16px 0 14px;
  list-style: none;
}
.evidence-panel li {
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr);
  gap: 8px;
  align-items: start;
  color: #53617b;
  font-size: 12px;
  line-height: 1.5;
}
.evidence-panel li b {
  display: grid;
  width: 20px;
  height: 20px;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  background: #2f5be7;
}
.evidence-panel li strong {
  color: #263858;
}
.evidence-panel > a {
  color: #2f5be7;
  font-size: 12px;
  font-weight: 700;
}
.domain-evidence {
  display: grid;
  gap: 6px;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #edf0f5;
  color: #6b7891;
  font-size: 11px;
}
.domain-evidence button {
  justify-self: start;
  border: 0;
  color: #2f5be7;
  background: transparent;
  font-size: 11px;
  cursor: pointer;
}
.feedback-options {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  margin-top: 16px;
}
.feedback-options button {
  display: grid;
  min-width: 0;
  min-height: 126px;
  gap: 4px;
  place-items: center;
  padding: 15px 6px;
  border: 1px solid #e0e6f1;
  border-radius: 9px;
  color: #17264d;
  background: #fff;
  cursor: pointer;
}
.feedback-options strong {
  font-size: 12px;
}
.feedback-options small {
  color: #7c8799;
  font-size: 10px;
}
.feedback-face {
  position: relative;
  display: grid;
  width: 26px;
  height: 26px;
  place-items: center;
  border: 2px solid currentcolor;
  border-radius: 50%;
  font-size: 0;
}

.feedback-face.easy::before,
.feedback-face.hard::before {
  position: absolute;
  top: 7px;
  left: 6px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: currentcolor;
  box-shadow: 8px 0 currentcolor;
  content: '';
}

.feedback-face.easy::after,
.feedback-face.hard::after {
  position: absolute;
  bottom: 5px;
  left: 6px;
  width: 10px;
  height: 5px;
  border: 2px solid currentcolor;
  border-top: 0;
  border-radius: 0 0 10px 10px;
  content: '';
}

.feedback-face.hard::after {
  bottom: 4px;
  border-top: 2px solid currentcolor;
  border-bottom: 0;
  border-radius: 10px 10px 0 0;
}
.feedback-face.easy {
  color: #17a26b;
}
.feedback-face.hard {
  color: #e1443e;
}
.feedback-face.known {
  color: #315be7;
}
.feedback-face.change {
  color: #7d57dc;
  border: 0;
  font-size: 24px;
}

.feedback-quick-ask {
  display: none;
  margin-top: 12px;
}
.feedback-quick-ask :deep(.quick-ask) {
  display: grid;
  grid-template-columns: 26px minmax(0, auto) minmax(120px, 1fr) 34px;
  min-height: 42px;
  gap: 7px;
  padding: 5px 8px;
  border: 1px solid #dfe6f2;
  border-radius: 8px;
  background: #fafcff;
}
.feedback-quick-ask :deep(.bot) {
  font-size: 18px;
}
.feedback-quick-ask :deep(label) {
  color: #335bd2;
  font-size: 12px;
}
.feedback-quick-ask :deep(label span) {
  display: none;
}
.feedback-quick-ask :deep(input) {
  min-height: 30px;
  padding: 0 8px;
  border: 1px solid #e0e6f2;
  border-radius: 6px;
  font-size: 11px;
}
.feedback-quick-ask :deep(button) {
  min-height: 30px;
  border-radius: 6px;
  font-size: 15px;
}

.side-card {
  padding: 17px 16px;
}

.goal-card {
  min-height: 227px;
}

.progress-side-card {
  min-height: 0;
  padding: 18px 18px 14px;
  border-color: #e2e8f3;
  border-radius: 17px;
  box-shadow: 0 10px 28px rgb(40 65 115 / 7%);
}

.progress-side-card > .progress-side-heading {
  display: grid;
  gap: 3px;
  align-items: start;
  margin-bottom: 0;
}

.progress-side-heading h2 {
  color: var(--today-ink);
  font-size: 18px;
}

.progress-side-heading span {
  color: #718099;
  font-size: 12px;
}

.wrong-side-card {
  min-height: 222px;
}
.side-card header {
  display: flex;
  gap: 9px;
  align-items: center;
  margin-bottom: 16px;
}
.side-icon {
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  font-size: 24px;
  font-weight: 700;
}
.side-icon.blue {
  color: #315be7;
}
.side-icon.red {
  color: #dd3b3b;
}
.side-icon.slate {
  color: #31456c;
}
.side-card dl {
  display: grid;
  gap: 14px;
  margin: 0;
}
.side-card dl div {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: baseline;
}
.side-card dt {
  color: #66738b;
  font-size: 13px;
}
.side-card dd {
  margin: 0;
  color: #202f50;
  font-size: 14px;
  font-weight: 700;
  text-align: right;
}
.side-card dd.green {
  color: #169764;
  font-size: 17px;
}
.side-card > a,
.progress-side-card > a {
  display: block;
  color: #2f5be7;
  font-size: 12px;
  font-weight: 700;
  text-align: center;
}
.side-progress {
  height: 8px;
  margin: 16px 0 12px;
  overflow: hidden;
  border-radius: 99px;
  background: #e7ebf3;
}
.side-progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #2f5be7;
}
.progress-side-body {
  display: grid;
  grid-template-columns: 108px minmax(0, 1fr);
  gap: 16px;
  align-items: center;
}
.ring {
  position: relative;
  display: grid;
  width: 108px;
  height: 108px;
  place-items: center;
  align-content: center;
  border-radius: 50%;
  border: 1px solid #eef2f8;
  background: conic-gradient(from -90deg, #2f5be7 0 var(--progress), #e9edf5 var(--progress) 100%);
  box-shadow: 0 5px 14px rgb(40 65 115 / 7%);
}
.ring::before {
  position: absolute;
  inset: 13px;
  content: '';
  border: 1px solid #eef2f8;
  border-radius: 50%;
  background: #fff;
}
.ring strong,
.ring span {
  position: relative;
  z-index: 1;
}
.ring strong {
  color: #2f5be7;
  font-size: 24px;
  font-variant-numeric: tabular-nums;
}
.ring span {
  color: #66738b;
  font-size: 12px;
}
.progress-side-card dl {
  gap: 0;
}
.progress-side-card dl div {
  min-height: 28px;
  padding: 6px 0;
  border-bottom: 1px solid #eff2f7;
}
.progress-side-card dl div:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}
.progress-side-card dt {
  color: #66738b;
  font-size: 12px;
}
.progress-side-card dd {
  color: #1e3154;
  font-size: 14px;
  font-variant-numeric: tabular-nums;
}
.progress-side-card > .progress-report-link {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-height: 30px;
  padding-top: 11px;
  border-top: 1px solid #eff2f7;
  margin-top: 0;
  color: #2f5be7;
  font-size: 13px;
  transition: color 160ms ease;
}
.progress-side-card > .progress-report-link:hover {
  color: #244fcf;
}
.wrong-side-card {
  border-color: #f6dfdd;
  background: #fffdfd;
}
.wrong-side-card p {
  margin: 0 0 12px;
  color: #4d5e79;
  font-size: 12px;
  line-height: 1.5;
}
.wrong-side-card p strong {
  color: #dd3b3b;
}
.wrong-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}
.wrong-tags span {
  padding: 5px 8px;
  border-radius: 6px;
  color: #ad3836;
  background: #fff0ef;
  font-size: 11px;
}
.wrong-tags b {
  margin-left: 3px;
  font-weight: 800;
}
.wrong-side-card button {
  min-height: 32px;
  padding: 0;
  margin-top: 10px;
  border: 0;
  color: #2f5be7;
  background: transparent;
}
.task-side-card ul {
  display: grid;
  gap: 10px;
  padding: 0;
  margin: 0;
  list-style: none;
}

.task-side-card {
  padding-block: 13px;
}
.compat-test-label {
  display: none;
}
.task-side-card li {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr) auto;
  gap: 7px;
  align-items: center;
}
.task-side-card li.active {
  padding: 7px;
  border-radius: 7px;
  background: #f1f4ff;
}
.task-check {
  display: grid;
  width: 16px;
  height: 16px;
  place-items: center;
  border: 1px solid #aab8d2;
  border-radius: 4px;
  color: #fff;
  background: #fff;
  font-size: 10px;
}
.task-side-card li.active .task-check {
  border-color: #315be7;
  background: #315be7;
}
.task-side-card li > div {
  min-width: 0;
}
.task-side-card strong,
.task-side-card small {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.task-side-card strong {
  color: #273858;
  font-size: 11px;
}
.task-side-card small {
  margin-top: 3px;
  color: #7a879b;
  font-size: 10px;
}
.task-side-card li button {
  min-height: 25px;
  padding: 0 6px;
  border: 1px solid #dce3ef;
  border-radius: 5px;
  color: #60708a;
  background: #f9fbff;
  font-size: 10px;
  cursor: pointer;
}
.task-side-card li.active button {
  border-color: #97adf7;
  color: #315be7;
  background: #fff;
}
.task-side-card footer {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 8px;
  align-items: center;
  margin-top: 14px;
  color: #6f7c94;
  font-size: 11px;
}
.task-progress {
  height: 6px;
  overflow: hidden;
  border-radius: 99px;
  background: #e7ebf3;
}
.task-progress i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #315be7;
}
.task-side-card footer b {
  color: #475a84;
}

.state-card {
  padding: 34px;
  text-align: center;
}
.state-card h2 {
  margin: 0 0 8px;
}
.state-card p {
  color: var(--today-muted);
}
.error-state {
  display: grid;
  gap: 14px;
}
.state-card button {
  justify-self: center;
  border: 0;
  color: #fff;
  background: var(--today-blue);
}

@media (max-width: 1280px) {
  .today-layout {
    grid-template-columns: minmax(0, 1fr) 322px;
    gap: 12px;
  }
  .best-action-card {
    grid-template-columns: minmax(0, 1fr) 140px 210px;
    gap: 12px;
  }
  .action-title-row h1 {
    font-size: 22px;
  }
  .learning-step {
    padding-inline: 9px;
  }
}

@media (max-width: 1024px) {
  .today-layout {
    grid-template-columns: minmax(0, 1fr) 278px;
  }
  .best-action-card {
    grid-template-columns: 1fr;
    min-height: 0;
  }
  .action-illustration {
    display: none;
  }
  .best-action-content {
    grid-row: 1;
  }
  .mastery-box {
    grid-row: 2;
    grid-column: 1;
  }
  .today-bottom-grid {
    grid-template-columns: 1fr;
  }
  .loop-flow {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
  .loop-arrow {
    display: none;
  }
}

@media (max-width: 760px) {
  .today-layout {
    grid-template-columns: 1fr;
  }
  .today-side {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .progress-side-card {
    grid-column: 1 / -1;
  }
  .task-side-card {
    grid-column: 1 / -1;
  }
  .learning-path {
    grid-template-columns: 1fr;
  }
  .path-arrow {
    display: none;
  }
  .loop-flow {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .feedback-options {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
