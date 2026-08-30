<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/wrong-book/ErrorAnalysisPanel.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="analysis-panel" data-test="error-analysis-panel">
    <header>
      <div>
        <h2>错误原因</h2>
        <span class="compat-copy">错误归因</span>
      </div>
      <span v-if="item" class="selected-question"
        >当前选择：题 {{ item.questionCode.slice(-3) }}</span
      >
    </header>
    <p class="tip">分析原因，才能真正解决问题，而不是重复记错。</p>

    <div v-if="loading" class="analysis-skeleton" aria-label="正在加载错误原因">
      <span v-for="index in 3" :key="index" />
    </div>
    <template v-else-if="item">
      <article>
        <h3>为什么会错</h3>
        <p>{{ item.analysis.whyWrong }}</p>
      </article>
      <article>
        <h3>错误类型</h3>
        <b>{{ item.analysis.errorType }}</b>
      </article>
      <article>
        <h3>依赖的前置知识</h3>
        <ul>
          <li v-for="point in item.analysis.prerequisites" :key="point">{{ point }}</li>
        </ul>
      </article>
      <article class="review-suggestions">
        <h3>复习建议</h3>
        <div class="review-items">
          <button
            v-for="review in item.analysis.recommendedReviewItems"
            :key="review.title"
            type="button"
            @click="emit('openReview', review.title)"
          >
            <span>{{ review.title }}</span>
            <strong>{{ review.actionLabel }} <i aria-hidden="true">›</i></strong>
          </button>
        </div>
      </article>
      <p class="coach-note">针对你的薄弱点，先学透再练，效率更高。</p>
    </template>

<style scoped lang="scss">
.analysis-panel {
  display: grid;
  align-content: start;
  gap: 10px;
  padding: 16px;
  border: 1px solid #dfe7f2;
  border-radius: 8px;
  background: #fff;
}

header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

h2,
h3,
p,
ul {
  margin: 0;
}

h2 {
  color: #14213d;
  font-size: 18px;
  line-height: 1.3;
}

.compat-copy {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
}

.selected-question {
  padding: 5px 8px;
  border-radius: 6px;
  color: #2563eb;
  background: #edf4ff;
  font-size: 11px;
  white-space: nowrap;
}

.tip {
  padding: 10px 11px;
  border-radius: 6px;
  color: #496b9e;
  background: #f1f6ff;
  font-size: 12px;
  line-height: 1.55;
}

article {
  display: grid;
  gap: 7px;
  padding: 13px;
  border: 1px solid #e1e8f2;
  border-radius: 7px;
}

h3 {
  color: #14213d;
  font-size: 14px;
  line-height: 1.35;
}

article p,
li,
.empty {
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

article b {
  justify-self: start;
  padding: 4px 8px;
  border-radius: 6px;
  color: #7c3aed;
  background: #f2eafe;
  font-size: 11px;
}

ul {
  display: grid;
  gap: 4px;
  padding-left: 16px;
}

.review-items {
  display: grid;
  gap: 6px;
}

.review-items button {
  display: flex;
  min-height: 32px;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 0 8px;
  border: 1px solid #dfe7f2;
  border-radius: 6px;
  color: #486180;
  background: #fbfdff;
  font-size: 12px;
  text-align: left;
  cursor: pointer;
}

.review-items button:hover {
  border-color: #9ebcf2;
  background: #f5f9ff;
}

.review-items strong {
  flex: 0 0 auto;
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
}

.review-items i {
  font-style: normal;
  font-size: 16px;
}

.coach-note {
  padding: 10px;
  border: 1px solid #cbe6d3;
  border-radius: 6px;
  color: #28764c;
  background: #f2fbf4;
  font-weight: 700;
}

.analysis-skeleton {
  display: grid;
  gap: 8px;
}

.analysis-skeleton span {
  height: 74px;
  border: 1px solid #e5ebf4;
  border-radius: 7px;
  background: linear-gradient(90deg, #f6f8fc, #fff, #f6f8fc);
  background-size: 220% 100%;
  animation: analysis-shimmer 1.2s ease-in-out infinite;
}

@keyframes analysis-shimmer {
  to {
    background-position: -120% 0;
  }
}
</style>
