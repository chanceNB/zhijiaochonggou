<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/wrong-book/ReviewActionBar.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="review-actions" data-test="review-action-bar" aria-label="错题操作">
    <button
      class="action-button action-button--retry"
      data-test="review-retry-question"
      type="button"
      :disabled="disabled || Boolean(busyAction)"
      @click="$emit('retry')"
    >
      <RefreshRight class="action-icon action-icon--retry" aria-hidden="true" />
      <span>{{ busyAction === 'retry' ? '正在准备...' : '重新练这题' }}</span>
    </button>
    <button
      class="action-button action-button--similar"
      data-test="review-similar-questions"
      type="button"
      :disabled="disabled || Boolean(busyAction)"
      @click="$emit('similar')"
    >
      <MagicStick class="action-icon action-icon--similar" aria-hidden="true" />
      <span>{{ busyAction === 'similar' ? '生成中...' : '生成同类题' }}</span>
    </button>
    <button
      class="action-button action-button--knowledge"
      data-test="review-knowledge-only"
      type="button"
      :disabled="disabled || Boolean(busyAction)"
      @click="$emit('knowledgeOnly')"
    >
      <Aim class="action-icon action-icon--knowledge" aria-hidden="true" />
      <span>{{ busyAction === 'knowledge' ? '读取设置...' : '只练这个知识点' }}</span>
    </button>
    <button
      class="action-button action-button--mastery"
      data-test="review-mark-mastered"
      type="button"
      :disabled="disabled || Boolean(busyAction)"
      @click="$emit('markMastered')"
    >
      <CircleCheck class="action-icon action-icon--mastery" aria-hidden="true" />
      <span>我觉得已经掌握</span>
    </button>
  </section>
</template>

<style scoped lang="scss">
.review-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.action-button {
  display: inline-flex;
  min-height: 44px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid currentColor;
  border-radius: 8px;
  background: #fff;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition:
    background-color 160ms ease,
    border-color 160ms ease,
    box-shadow 160ms ease,
    transform 160ms ease;
}

.action-button:hover:not(:disabled) {
  background: #f7faff;
  box-shadow: 0 4px 12px rgb(37 99 235 / 10%);
  transform: translateY(-1px);
}

.action-button:focus-visible {
  outline: 3px solid rgb(37 99 235 / 20%);
  outline-offset: 2px;
}

.action-button:disabled {
  cursor: not-allowed;
  opacity: 0.52;
}

.action-button--retry {
  color: #2563eb;
}

.action-button--similar {
  color: #16a34a;
}

.action-button--knowledge {
  color: #d97706;
}

.action-button--mastery {
  color: #7c3aed;
}

.action-icon {
  width: 17px;
  height: 17px;
  flex: 0 0 17px;
}

@media (max-width: 767.98px) {
  .review-actions {
    grid-template-columns: 1fr;
  }
}
</style>
