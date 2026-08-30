<template>
  <section class="learning-path" data-testid="learning-path">
    <header>
      <h2>今日学习路径</h2>
      <span>按当前行动推进</span>
    </header>
    <div class="learning-path__steps">
      <article v-for="(step, index) in steps" :key="step.title" :class="{ 'is-active': index === 0 }">
        <span class="learning-path__number">{{ index + 1 }}</span>
        <div>
          <strong>{{ step.title }}</strong>
          <small>{{ step.description }}</small>
        </div>
        <span class="learning-path__kind">{{ step.kind }}</span>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
const steps = [
  { title: '理解当前知识点', description: '梳理概念和关键关系', kind: '讲解' },
  { title: '完成定向诊断', description: '用结构化题目确认薄弱处', kind: '练习' },
  { title: '巩固并验证', description: '根据真实结果安排下一步', kind: '验证' },
]
</script>

<style scoped>
.learning-path {
  display: grid;
  gap: 14px;
  padding: 18px 20px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  background: var(--color-card);
  box-shadow: var(--shadow-card);
}

.learning-path header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.learning-path h2 { margin: 0; color: var(--color-text); font-size: 17px; }
.learning-path header span { color: var(--color-secondary); font-size: 12px; }

.learning-path__steps {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.learning-path article {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr);
  gap: 9px;
  align-items: center;
  min-width: 0;
  min-height: 88px;
  padding: 12px;
  border: 1px solid #d9e2f3;
  border-radius: 8px;
  background: #fbfcff;
}

.learning-path article.is-active { border-color: #8db0ee; background: #f1f6ff; }
.learning-path__number { display: grid; width: 28px; height: 28px; place-items: center; border-radius: 50%; color: #fff; background: var(--color-student-primary); font-size: 13px; font-weight: 800; }
.learning-path article:not(.is-active) .learning-path__number { color: #38517c; background: #edf2fa; }
.learning-path article div { min-width: 0; }
.learning-path strong { display: block; overflow: hidden; color: #203a6a; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.learning-path small { display: block; margin-top: 6px; color: var(--color-secondary); font-size: 11px; line-height: 1.4; }
.learning-path__kind { grid-column: 2; color: var(--color-student-primary); font-size: 11px; }

@media (max-width: 840px) { .learning-path__steps { grid-template-columns: 1fr; } }
</style>
