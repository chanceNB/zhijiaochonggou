<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/today/LearningProgressCard.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="progress-card" data-testid="learning-progress-card">
    <h3>学习进度</h3>
    <p>本周学习概览</p>
    <div class="progress-body">
      <div class="ring" :style="{ '--progress': `${progress.weeklyCompletionRate}%` }">
        <strong>{{ progress.weeklyCompletionRate }}%</strong>
        <span>本周目标完成度</span>
      </div>
      <dl>
        <div>
          <dt>累计学习</dt>
          <dd>{{ progress.studyDays }} 天</dd>
        </div>
        <div>
          <dt>学习时长</dt>
          <dd>{{ progress.studyMinutes }} 分钟</dd>
        </div>
        <div>
          <dt>完成题目</dt>
          <dd>{{ progress.completedQuestions }} 道</dd>
        </div>
        <div>
          <dt>正确率</dt>
          <dd>{{ progress.accuracyRate }}%</dd>
        </div>
      </dl>
    </div>
    <button @click="$emit('report')">查看学习报告 ›</button>
  </section>
</template>

<style scoped lang="scss">
.progress-card {
  min-width: 0;
  padding: 16px;
  border: 1px solid #d2dff0;
  border-radius: 14px;
  background: rgb(255 255 255 / 92%);
}
h3 {
  margin: 0 0 4px;
  color: #135fd0;
}
p {
  margin: 0 0 10px;
  color: #667894;
}
.progress-body {
  display: grid;
  grid-template-columns: 90px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
}
.ring {
  width: 86px;
  aspect-ratio: 1;
  display: grid;
  place-items: center;
  align-content: center;
  border-radius: 50%;
  background: conic-gradient(#2f74ee var(--progress), #e7eef9 0);
  color: #2369dc;
}
.ring::before {
  content: '';
  position: absolute;
}
.ring strong,
.ring span {
  position: relative;
  z-index: 1;
}
.ring strong {
  font-size: 22px;
}
.ring span {
  max-width: 60px;
  color: #60748f;
  font-size: 12px;
  text-align: center;
}
.ring {
  box-shadow: inset 0 0 0 12px #fff;
}
dl {
  display: grid;
  min-width: 0;
  gap: 7px;
  margin: 0;
}
dl div {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 6px;
}
dt {
  min-width: 0;
  color: #687b96;
  font-size: 13px;
  white-space: nowrap;
}
dd {
  margin: 0;
  color: #253e66;
  font-size: 13px;
  font-weight: 800;
  white-space: nowrap;
}
button {
  margin-top: 10px;
  min-height: 36px;
  border: 0;
  background: transparent;
  color: #2563eb;
  font-weight: 800;
  cursor: pointer;
}
</style>
