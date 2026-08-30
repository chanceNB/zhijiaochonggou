<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/today/QuickAskCard.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="quick-ask" data-testid="quick-ask-card">
    <el-icon class="bot" aria-hidden="true"><Cpu /></el-icon>
    <label for="quick-ask-input"
      >有问题？ 问 AI 教练 <span>快速答疑 / 讲题 / 知识点梳理</span></label
    >
    <input
      id="quick-ask-input"
      v-model="question"
      placeholder="输入你的问题，比如：BFS 为什么使用队列？"
      @keyup.enter="submit"
    />
    <button
      type="button"
      aria-label="向 AI 教练提问"
      data-testid="quick-ask-submit"
      :disabled="!canSubmit"
      @click="submit"
    >
      ➤
    </button>
  </section>
</template>

<style scoped lang="scss">
.quick-ask {
  min-height: 70px;
  padding: 10px 14px;
  display: grid;
  grid-template-columns: 34px minmax(190px, auto) minmax(220px, 1fr) 48px;
  gap: 10px;
  align-items: center;
  border: 1.5px solid #80aef8;
  border-radius: 12px;
  background: rgb(255 255 255 / 90%);
}
.bot {
  font-size: 24px;
}
label {
  color: #1262ca;
  font-size: 16px;
  font-weight: 900;
}
label span {
  margin-left: 6px;
  color: #6b7d94;
  font-size: 12px;
  font-weight: 500;
}
input {
  width: 100%;
  min-height: 42px;
  padding: 0 16px;
  border: 1px solid #cbd9ee;
  border-radius: 11px;
  color: #1f355d;
}
button {
  min-height: 44px;
  border: 0;
  border-radius: 12px;
  background: #2f74ee;
  color: #fff;
  font-size: 19px;
  cursor: pointer;
}
button:disabled {
  cursor: not-allowed;
  opacity: 0.48;
}
@media (max-width: 1024px) {
  .quick-ask {
    grid-template-columns: 40px 1fr 58px;
  }
  input {
    grid-column: 2 / 4;
  }
}
</style>
