<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/today/LearningStepTimeline.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <div class="step-timeline" data-testid="learning-step-timeline">
    <article
      v-for="step in steps"
      :key="step.id"
      class="step-card"
      :class="{ active: activeStepId === step.id }"
      data-testid="learning-step"
    >
      <span class="step-order">{{ step.order }}</span>
      <div class="step-icon">
        {{ step.type === 'explanation' ? '讲解' : step.type === 'practice' ? '练习' : '验证' }}
      </div>
      <div>
        <h4>{{ step.title }}</h4>
        <p>{{ step.description }}</p>
        <small>约 {{ step.durationMinutes }} 分钟</small>
      </div>
    </article>
  </div>
</template>

<style scoped lang="scss">
.step-timeline {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.step-card {
  position: relative;
  min-height: 88px;
  padding: 16px 12px 10px;
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  border: 1.5px solid #c8d7ee;
  border-radius: 11px;
  background: rgb(255 255 255 / 84%);
  box-shadow: 0 3px 10px rgb(65 118 210 / 7%);
}
.step-card::after {
  content: '';
  position: absolute;
  right: -16px;
  top: 50%;
  width: 20px;
  height: 2px;
  background: #4f8ef7;
}
.step-card:last-child::after {
  display: none;
}
.step-card.active {
  border-color: #2f74ee;
  background: #eef6ff;
}
.step-order {
  position: absolute;
  top: -9px;
  left: 14px;
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #3578ed;
  color: #fff;
  font-weight: 800;
}
.step-icon {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border: 2px solid #3a7df2;
  border-radius: 9px;
  color: #2563eb;
  font-weight: 900;
}
h4 {
  margin: 0 0 4px;
  color: #132a59;
  font-size: 15px;
}
.step-card > div:last-child {
  min-width: 0;
}
p,
small {
  margin: 0;
  color: #62738f;
}
@media (max-width: 1024px) {
  .step-timeline {
    grid-template-columns: 1fr;
  }
  .step-card::after {
    display: none;
  }
}
</style>
