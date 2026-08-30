<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/today/DailyPlanCard.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="plan-card" data-testid="daily-plan-card">
    <div class="plan-heading">
      <div>
        <p class="eyebrow">
          <el-icon aria-hidden="true"><Cpu /></el-icon> 今日 AI 学习计划 <span>为你量身定制</span>
        </p>
        <h2>推荐重点：{{ plan.focus }}</h2>
      </div>
      <div class="duration-box">
        <strong>约 {{ plan.durationMinutes }} 分钟</strong>
        <span>预计掌握提升</span>
        <b>从 {{ plan.expectedMasteryGain.from }}% → {{ plan.expectedMasteryGain.to }}%+</b>
      </div>
    </div>
    <p class="reason"><strong>推荐理由：</strong>{{ plan.reason }}</p>
    <h3>3 步学习路径</h3>
    <LearningStepTimeline :steps="plan.steps" :active-step-id="activeStepId" />
    <div class="plan-actions">
      <button
        class="primary"
        data-testid="start-learning"
        :disabled="isActionLoading"
        @click="$emit('action', 'start-learning')"
      >
        开始学习
      </button>
      <button
        data-testid="practice-first"
        :disabled="isActionLoading"
        @click="$emit('action', 'practice-first')"
      >
        先做题
      </button>
      <button
        data-testid="explanation-first"
        :disabled="isActionLoading"
        @click="$emit('action', 'explanation-first')"
      >
        先看讲解
      </button>
      <button
        data-testid="change-plan"
        :disabled="isActionLoading"
        @click="$emit('action', 'change-plan')"
      >
        换一个计划 ↻
      </button>
      <button
        data-testid="too-hard"
        :disabled="isActionLoading"
        @click="$emit('action', 'too-hard')"
      >
        太难了 ☹
      </button>
      <button
        data-testid="already-mastered"
        :disabled="isActionLoading"
        @click="$emit('action', 'already-mastered')"
      >
        我已经会了
      </button>
    </div>
    <p
      v-if="feedback.status !== 'idle'"
      class="action-feedback"
      :class="feedback.status"
      data-testid="action-feedback"
    >
      {{ feedback.message }}
    </p>
  </section>
</template>

<style scoped lang="scss">
.plan-card {
  border: 1.5px solid #80aef8;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 5px 14px rgb(51 103 205 / 9%);
  padding: 16px;
}
.plan-heading {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 198px;
  gap: 12px;
  align-items: stretch;
}
.eyebrow {
  margin: 0 0 8px;
  color: #0f2b61;
  font-size: 21px;
  font-weight: 900;
}
.eyebrow span {
  margin-left: 8px;
  padding: 4px 9px;
  border-radius: 999px;
  background: #e7f1ff;
  color: #3476e4;
  font-size: 12px;
}
h2,
h3 {
  margin: 0;
  color: #10285c;
}
h2 {
  font-size: 17px;
}
h3 {
  margin: 12px 0 10px;
  font-size: 17px;
}
.duration-box {
  display: grid;
  align-content: center;
  gap: 6px;
  padding: 12px;
  border: 1px solid #d5e2f3;
  border-radius: 11px;
  background: #fff;
}
.duration-box strong {
  font-size: 16px;
  color: #142a56;
}
.duration-box span {
  color: #71819a;
}
.duration-box b {
  color: #1aa34a;
  font-size: 17px;
}
.reason {
  margin: 8px 0 0;
  color: #53657f;
  line-height: 1.55;
  font-size: 14px;
}
.plan-actions {
  display: grid;
  grid-template-columns: 1.2fr repeat(5, minmax(0, 1fr));
  gap: 8px;
  margin-top: 14px;
}
button {
  min-height: 40px;
  border: 1.5px solid #a9c1e7;
  border-radius: 8px;
  background: #fff;
  padding: 0 6px;
  color: #2463c8;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.2;
  white-space: nowrap;
  cursor: pointer;
}
button:disabled {
  cursor: wait;
  opacity: 0.58;
}
button.primary {
  background: #2f74ee;
  color: #fff;
  box-shadow: 0 8px 18px rgb(47 116 238 / 24%);
}
.action-feedback {
  margin: 8px 0 0;
  padding: 10px 12px;
  border-radius: 8px;
  color: #2c5d9f;
  background: #eaf3ff;
}
.action-feedback.success {
  color: #177855;
  background: #e5f8ef;
}
@media (max-width: 1280px) {
  .plan-actions {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
@media (max-width: 1024px) {
  .plan-heading {
    grid-template-columns: 1fr;
  }
  .plan-actions {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
