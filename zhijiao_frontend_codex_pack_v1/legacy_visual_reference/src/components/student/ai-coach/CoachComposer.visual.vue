<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/ai-coach/CoachComposer.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <form class="coach-composer" @submit.prevent="submit()">
    <button class="tool-button" type="button" aria-label="添加题目图片或附件">
      <el-icon><Paperclip /></el-icon>
    </button>
    <textarea
      v-model="question"
      data-test="coach-composer-input"
      data-testid="coach-composer-input"
      rows="2"
      :disabled="sending"
      placeholder="继续提问吧...（Shift + Enter 换行）"
      @keydown.enter.exact.prevent="submit()"
    ></textarea>
    <button
      data-testid="coach-diagnose"
      class="diagnose-button"
      type="button"
      :disabled="sending || !question.trim()"
      @click="submit('PERSONALIZED_DIAGNOSIS')"
    >
      诊断
    </button>
    <button
      data-testid="coach-send"
      class="send-button"
      type="button"
      :disabled="sending || !question.trim()"
      aria-label="发送"
      @click="submit('GENERAL_TUTORING')"
    >
      <el-icon><Promotion /></el-icon>
    </button>
  </form>
</template>

<style scoped lang="scss">
.coach-composer {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) 64px 52px;
  gap: 12px;
  align-items: end;
  padding: 14px;
  border: 1px solid #73a7ff;
  border-radius: 8px;
  background: #ffffff;
}
textarea {
  min-height: 54px;
  max-height: 144px;
  resize: vertical;
  border: 0;
  outline: 0;
  color: #1d2939;
  font: inherit;
  line-height: 1.5;
}
.tool-button,
.diagnose-button,
.send-button {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  border: 1px solid #d4e2f7;
  border-radius: 8px;
  cursor: pointer;
}
.diagnose-button {
  width: 64px;
  color: #1849a9;
  background: #ffffff;
}
.tool-button {
  color: #667085;
  background: #ffffff;
}
.send-button {
  color: #ffffff;
  border-color: #2563eb;
  background: #2563eb;
}
.diagnose-button:disabled,
.send-button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
@media (max-width: 760px) {
  .coach-composer {
    grid-template-columns: 40px minmax(0, 1fr) 56px 44px;
    gap: 8px;
  }
  .tool-button {
    width: 40px;
  }
  .diagnose-button {
    width: 56px;
  }
  .send-button {
    width: 44px;
  }
}
</style>
