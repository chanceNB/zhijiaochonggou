<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/ai-coach/CoachDiagnosticActionCard.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section data-testid="diagnostic-action" class="diagnostic-action-card">
    <strong data-testid="diagnostic-question">{{ action.stem }}</strong>

    <div v-if="action.questionType === 'MULTIPLE_CHOICE'" class="diagnostic-options">
      <button
        v-for="option in action.options"
        :key="option.code"
        data-testid="diagnostic-answer"
        :data-answer-code="option.code"
        :class="{ selected: answer === option.code }"
        type="button"
        :disabled="submitting"
        @click="choose(option.code)"
      >
        {{ option.label }}
      </button>
    </div>
    <input
      v-else
      v-model="answer"
      data-testid="diagnostic-answer"
      class="diagnostic-short-answer"
      type="text"
      :disabled="submitting"
      placeholder="输入你的答案"
      @keydown.enter.prevent="submit"
    />

    <div class="diagnostic-action-footer">
      <small>请在 {{ new Date(action.expiresAt).toLocaleTimeString('zh-CN') }} 前作答</small>
      <button data-testid="diagnostic-submit" type="button" :disabled="!canSubmit" @click="submit">
        {{ submitting ? '提交中...' : '提交答案' }}
      </button>
    </div>
  </section>
</template>

<style scoped lang="scss">
.diagnostic-action-card {
  display: grid;
  gap: 12px;
  padding: 14px;
  border: 1px solid #adc8f8;
  border-radius: 8px;
  color: #1f2a44;
  background: #f8fbff;
}
.diagnostic-options {
  display: grid;
  gap: 8px;
}
.diagnostic-options button,
.diagnostic-short-answer,
.diagnostic-action-footer button {
  min-height: 38px;
  padding: 8px 12px;
  border: 1px solid #adc8f8;
  border-radius: 8px;
  background: #ffffff;
}
.diagnostic-options button {
  color: #1849a9;
  text-align: left;
  cursor: pointer;
}
.diagnostic-options button.selected {
  border-color: #2563eb;
  background: #e8f1ff;
}
.diagnostic-action-footer {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}
.diagnostic-action-footer small {
  color: #667085;
}
.diagnostic-action-footer button {
  color: #ffffff;
  border-color: #2563eb;
  background: #2563eb;
  cursor: pointer;
}
button:disabled,
input:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
</style>
