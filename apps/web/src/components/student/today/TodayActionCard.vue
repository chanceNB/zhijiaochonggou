<template>
  <section class="today-action-card" data-testid="today-action-card">
    <div class="today-action-card__main">
      <p class="today-action-card__eyebrow">
        <span class="today-action-card__eyebrow-dot" aria-hidden="true"></span>
        下一步学习行动
        <span class="today-action-card__tag">{{ assignment ? '教师任务' : 'AI 学习教练' }}</span>
      </p>
      <div class="today-action-card__title-row">
        <span class="today-action-card__step" aria-hidden="true">1</span>
        <h1>{{ action.title }}</h1>
      </div>
      <p class="today-action-card__summary">
        {{ assignment ? '按照教师安排完成本次定向练习。' : '先用两道诊断题确认当前知识点，再进入针对性学习。' }}
      </p>
      <div class="today-action-card__meta">
        <span>{{ action.estimatedMinutes }} 分钟</span>
        <span aria-hidden="true"></span>
        <span>当前知识点</span>
      </div>
      <button class="today-action-card__primary" type="button" :disabled="busy" @click="emit('start')">
        {{ assignment ? '进入定向练习' : '开始 AI 诊断' }}
      </button>
    </div>
    <div class="today-action-card__illustration" aria-hidden="true">
      <span></span><span></span><span></span><i></i>
    </div>
    <div class="today-action-card__duration">
      <span>预计用时</span>
      <strong>{{ action.estimatedMinutes }} 分钟</strong>
      <small>按步骤完成即可</small>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { TodayVm } from '@/types/contracts/student'

defineProps<{
  action: TodayVm['nextAction']
  assignment: TodayVm['teacherAssignment']
  busy?: boolean
}>()

const emit = defineEmits<{ start: [] }>()
</script>

<style scoped>
.today-action-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 160px 185px;
  gap: 20px;
  min-height: 270px;
  padding: 22px 24px;
  border: 1px solid #cbdcf7;
  border-radius: 10px;
  background: var(--color-card);
  box-shadow: var(--shadow-card);
}

.today-action-card__main {
  min-width: 0;
}

.today-action-card__eyebrow {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 18px;
  color: var(--color-text);
  font-size: 17px;
  font-weight: 800;
}

.today-action-card__eyebrow-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-student-primary);
}

.today-action-card__tag {
  padding: 4px 8px;
  border-radius: 6px;
  color: var(--color-ai);
  background: #e8f7ef;
  font-size: 12px;
}

.today-action-card__title-row {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
}

.today-action-card__step {
  display: grid;
  width: 44px;
  height: 44px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 9px;
  color: #fff;
  background: var(--color-student-primary);
  font-size: 22px;
  font-weight: 800;
}

.today-action-card h1 {
  min-width: 0;
  margin: 0;
  color: #182f5d;
  font-size: clamp(20px, 1.65vw, 28px);
  font-weight: 800;
  line-height: 1.3;
}

.today-action-card__summary {
  max-width: 620px;
  margin: 18px 0 10px 56px;
  color: var(--color-secondary);
  font-size: 14px;
  line-height: 1.6;
}

.today-action-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-left: 56px;
  color: #53627c;
  font-size: 12px;
}

.today-action-card__meta span:nth-child(2) {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #a8b2c3;
}

.today-action-card__primary {
  min-height: 42px;
  margin-top: 22px;
  padding: 0 18px;
  border: 1px solid var(--color-student-primary);
  border-radius: 8px;
  color: #fff;
  background: var(--color-student-primary);
  box-shadow: 0 6px 15px rgb(79 142 247 / 22%);
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
}

.today-action-card__primary:disabled {
  cursor: wait;
  opacity: 0.6;
}

.today-action-card__illustration {
  position: relative;
  display: grid;
  align-self: center;
  width: 145px;
  height: 116px;
  place-items: end center;
  border: 1px solid #dbe6ff;
  border-radius: 10px;
  background: #f7faff;
  box-shadow: 17px 13px 0 -8px #edf3ff;
}

.today-action-card__illustration span {
  position: absolute;
  bottom: 18px;
  width: 14px;
  border-radius: 4px 4px 2px 2px;
  background: #9fbaff;
}

.today-action-card__illustration span:nth-child(1) { right: 38px; height: 30px; }
.today-action-card__illustration span:nth-child(2) { right: 61px; height: 48px; background: #6e96f5; }
.today-action-card__illustration span:nth-child(3) { right: 84px; height: 68px; background: #c1d2ff; }
.today-action-card__illustration i {
  position: absolute;
  top: 22px;
  left: 22px;
  width: 22px;
  height: 14px;
  border-bottom: 4px solid #88cdb8;
  border-left: 4px solid #88cdb8;
  transform: rotate(-35deg);
}

.today-action-card__duration {
  display: grid;
  align-content: start;
  gap: 7px;
  min-width: 0;
  padding: 15px 14px;
  border: 1px solid #dbe3f3;
  border-radius: 9px;
  background: #fcfdff;
}

.today-action-card__duration span,
.today-action-card__duration small {
  color: var(--color-secondary);
  font-size: 12px;
}

.today-action-card__duration strong {
  color: var(--color-ai);
  font-size: 21px;
}

@media (max-width: 1024px) {
  .today-action-card { grid-template-columns: minmax(0, 1fr) 160px; }
  .today-action-card__duration { grid-column: 1 / -1; }
}

@media (max-width: 680px) {
  .today-action-card { grid-template-columns: 1fr; padding: 18px; }
  .today-action-card__illustration { display: none; }
  .today-action-card__summary,
  .today-action-card__meta { margin-left: 0; }
}
</style>
