<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/wrong-book/WrongQuestionList.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="wrong-list" data-test="wrong-question-list">
    <header class="wrong-list__header">
      <div>
        <h2>错题列表</h2>
        <p>共 {{ items.length }} 题，优先复盘最近出错的题目</p>
      </div>
      <span class="wrong-list__legend">题号 / 错因 / 状态</span>
    </header>

    <div v-if="loading" class="wrong-list__skeleton" aria-label="正在加载错题列表">
      <span v-for="index in 3" :key="index" class="skeleton-row" />
    </div>
    <div v-else-if="items.length === 0" class="wrong-list__empty" data-test="wrong-question-empty">
      <strong>暂无匹配的错题</strong>
      <span>请调整筛选条件或搜索关键词后重试。</span>
    </div>
    <div v-else class="wrong-list__items">
      <article
        v-for="(item, index) in items"
        :key="item.id"
        data-test="wrong-question-card"
        role="button"
        tabindex="0"
        :aria-pressed="selectedId === item.id"
        :class="{ active: selectedId === item.id }"
        @click="$emit('select', item.id)"
        @keydown.enter="$emit('select', item.id)"
        @keydown.space.prevent="$emit('select', item.id)"
      >
        <div class="question-main">
          <span class="question-number">{{ String(index + 1).padStart(2, '0') }}</span>
          <div class="question-copy">
            <h3>{{ item.title }}</h3>
            <p>知识点：{{ item.knowledgePath.join(' > ') }}</p>
          </div>
          <small>{{ item.questionCode }}</small>
        </div>
        <footer>
          <b>{{ item.errorType }}</b>
          <span>错误次数：{{ item.errorCount }} 次</span>
          <span>最近错误：{{ item.lastWrongAt }}</span>
          <span :class="['status', item.masteryStatus]">
            <i aria-hidden="true" />{{ statusLabels[item.masteryStatus] }}
          </span>
        </footer>
      </article>
    </div>
  </section>
</template>

<style scoped lang="scss">
.wrong-list {
  display: grid;
  align-content: start;
  gap: 10px;
  min-width: 0;
}

.wrong-list__header,
.question-main,
footer {
  display: flex;
  align-items: center;
  gap: 10px;
}

.wrong-list__header {
  justify-content: space-between;
  min-height: 42px;
  padding: 0 2px;
}

h2,
h3,
p {
  margin: 0;
}

h2 {
  color: #14213d;
  font-size: 18px;
  line-height: 1.3;
}

.wrong-list__header p,
.wrong-list__legend,
small,
footer span {
  color: #64748b;
  font-size: 12px;
}

.wrong-list__header p {
  display: inline-block;
  margin-left: 10px;
}

.wrong-list__legend {
  color: #94a3b8;
  white-space: nowrap;
}

.wrong-list__items {
  display: grid;
  gap: 8px;
}

article {
  display: grid;
  gap: 9px;
  padding: 12px 14px;
  border: 1px solid #dfe7f2;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition:
    background-color 160ms ease,
    border-color 160ms ease,
    box-shadow 160ms ease;
}

article:hover {
  border-color: #b7c9e7;
  background: #fbfdff;
}

article:focus-visible {
  outline: 3px solid rgb(37 99 235 / 18%);
  outline-offset: 2px;
}

article.active {
  border-color: #77a3ff;
  background: #f4f8ff;
  box-shadow: 0 0 0 1px rgb(37 99 235 / 8%);
}

.question-main {
  align-items: flex-start;
}

.question-number {
  display: grid;
  width: 32px;
  height: 30px;
  flex: 0 0 32px;
  place-items: center;
  border-radius: 7px;
  color: #2563eb;
  background: #edf4ff;
  font-size: 12px;
  font-weight: 800;
}

.question-copy {
  min-width: 0;
  flex: 1;
}

h3 {
  overflow: hidden;
  color: #14213d;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.45;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.question-copy p {
  margin-top: 4px;
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
}

small {
  flex: 0 0 auto;
  color: #64748b;
  font-size: 11px;
}

footer {
  flex-wrap: wrap;
  padding-top: 9px;
  border-top: 1px solid #edf1f6;
}

footer b {
  padding: 3px 7px;
  border-radius: 6px;
  color: #7c3aed;
  background: #f2eafe;
  font-size: 11px;
  font-weight: 700;
}

.status {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-weight: 700;
}

.status i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
}

.status.needs_review {
  color: #ef4444;
}

.status.reviewing {
  color: #f59e0b;
}

.status.mastered {
  color: #16a34a;
}

.wrong-list__skeleton {
  display: grid;
  gap: 8px;
}

.skeleton-row {
  display: block;
  height: 88px;
  border: 1px solid #e5ebf4;
  border-radius: 8px;
  background: linear-gradient(90deg, #f6f8fc, #fff, #f6f8fc);
  background-size: 220% 100%;
  animation: skeleton-shimmer 1.2s ease-in-out infinite;
}

.wrong-list__empty {
  display: grid;
  gap: 6px;
  padding: 32px 18px;
  border: 1px dashed #cbd8eb;
  border-radius: 8px;
  color: #64748b;
  background: #fff;
  text-align: center;
}

.wrong-list__empty strong {
  color: #14213d;
}

@keyframes skeleton-shimmer {
  to {
    background-position: -120% 0;
  }
}

@media (max-width: 1100px) {
  .wrong-list__header {
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
  }

  .wrong-list__header p {
    margin-left: 0;
  }
}

@media (max-width: 767.98px) {
  .question-main {
    display: grid;
    grid-template-columns: 32px minmax(0, 1fr);
  }

  .question-main small {
    grid-column: 2;
  }

  h3 {
    white-space: normal;
  }
}
</style>
