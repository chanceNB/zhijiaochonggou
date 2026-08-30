<template>
  <section class="learning-state-panel" data-testid="learning-state-panel">
    <header>
      <div>
        <p>当前学习状态</p>
        <h2>知识点掌握概览</h2>
      </div>
      <span class="learning-state-panel__status">实时</span>
    </header>
    <div class="learning-state-panel__metrics">
      <article>
        <span>掌握度</span>
        <strong>{{ formatPercent(state.mastery) }}</strong>
      </article>
      <article>
        <span>置信度</span>
        <strong>{{ formatPercent(state.confidence) }}</strong>
      </article>
      <article>
        <span>遗忘风险</span>
        <strong class="is-risk">{{ formatPercent(state.forgettingRisk) }}</strong>
      </article>
    </div>
    <footer>证据数 {{ state.evidenceCount }}</footer>
  </section>
</template>

<script setup lang="ts">
import type { TodayVm } from '@/types/contracts/student'

defineProps<{ state: TodayVm['learningState'] }>()

const formatPercent = (value: number) => `${Math.round(value * 100)}%`
</script>

<style scoped>
.learning-state-panel {
  display: grid;
  gap: 16px;
  padding: 18px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  background: var(--color-card);
  box-shadow: var(--shadow-card);
}

.learning-state-panel header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.learning-state-panel p,
.learning-state-panel h2 {
  margin: 0;
}

.learning-state-panel p {
  color: var(--color-secondary);
  font-size: 12px;
}

.learning-state-panel h2 {
  margin-top: 4px;
  color: var(--color-text);
  font-size: 17px;
}

.learning-state-panel__status {
  padding: 3px 7px;
  border-radius: 5px;
  color: var(--color-ai);
  background: #eaf8f1;
  font-size: 11px;
  font-weight: 700;
}

.learning-state-panel__metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.learning-state-panel__metrics article {
  display: grid;
  gap: 6px;
  min-width: 0;
  padding: 11px 10px;
  border-radius: 8px;
  background: #f7faff;
}

.learning-state-panel__metrics span {
  color: var(--color-secondary);
  font-size: 11px;
}

.learning-state-panel__metrics strong {
  color: #245fc9;
  font-size: 20px;
}

.learning-state-panel__metrics strong.is-risk { color: var(--color-risk); }

.learning-state-panel footer {
  padding-top: 10px;
  border-top: 1px solid #edf0f5;
  color: var(--color-secondary);
  font-size: 12px;
}
</style>
