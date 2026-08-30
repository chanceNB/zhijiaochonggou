<template>
  <section class="coach-conversation" data-testid="coach-conversation" aria-label="AI 教练对话">
    <div v-if="!messages.length" class="coach-conversation__welcome">
      <span class="coach-conversation__welcome-icon" aria-hidden="true">AI</span>
      <h2>从当前知识点开始</h2>
      <p>可以直接提问，也可以先开始一次两题诊断。诊断结果会进入后续练习流程。</p>
    </div>
    <article v-for="message in messages" :key="message.id" class="coach-message" :class="message.role === 'USER' ? 'is-user' : 'is-assistant'">
      <span class="coach-message__avatar" aria-hidden="true">{{ message.role === 'USER' ? '我' : 'AI' }}</span>
      <div class="coach-message__body">
        <span class="coach-message__role">{{ message.role === 'USER' ? '我' : 'AI 学习教练' }}</span>
        <p>{{ message.content }}</p>
        <div v-if="message.citations.length" class="coach-message__citations">
          <span>课程资料引用</span>
          <small v-for="citation in message.citations" :key="citation.chunkId">{{ citation.title }}</small>
        </div>
        <p v-else-if="message.role === 'ASSISTANT' && message.ragStatus === 'EMPTY'" class="coach-message__notice">未检索到课程资料，本次回答未引用课程知识库</p>
        <p v-else-if="message.role === 'ASSISTANT' && message.ragStatus === 'DEGRADED'" class="coach-message__notice">课程资料服务暂不可用，本次回答未引用课程知识库</p>
      </div>
    </article>
    <div v-if="sending" class="coach-conversation__typing" aria-live="polite">AI 正在整理回答...</div>
  </section>
</template>

<script setup lang="ts">
import type { CoachMessageVm } from '@/types/contracts/student'

defineProps<{ messages: CoachMessageVm[]; sending: boolean }>()
</script>

<style scoped>
.coach-conversation { display: grid; align-content: start; gap: 16px; min-height: 0; padding: 18px; overflow: auto; }
.coach-conversation__welcome { display: grid; justify-items: start; gap: 9px; padding: 22px; border: 1px solid #dce8fb; border-radius: 8px; background: #f8fbff; }
.coach-conversation__welcome-icon { display: grid; width: 38px; height: 38px; place-items: center; border-radius: 8px; color: #fff; background: var(--color-ai); font-size: 12px; font-weight: 800; }
.coach-conversation__welcome h2, .coach-conversation__welcome p { margin: 0; }
.coach-conversation__welcome h2 { color: var(--color-text); font-size: 18px; }
.coach-conversation__welcome p { max-width: 560px; color: var(--color-secondary); font-size: 13px; line-height: 1.65; }
.coach-message { display: grid; grid-template-columns: 34px minmax(0, 1fr); gap: 10px; align-items: start; max-width: 780px; }
.coach-message.is-user { align-self: end; }
.coach-message__avatar { display: grid; width: 32px; height: 32px; place-items: center; border: 1px solid #d1ddec; border-radius: 50%; color: #245fc9; background: #f0f5ff; font-size: 11px; font-weight: 800; }
.coach-message.is-user .coach-message__avatar { color: #35517f; background: #f4f7fb; }
.coach-message__body { min-width: 0; padding: 11px 13px; border: 1px solid #e0e7f1; border-radius: 8px; background: #fff; }
.coach-message.is-user .coach-message__body { border-color: #c6d9fb; background: #f1f6ff; }
.coach-message__role { color: var(--color-secondary); font-size: 11px; }
.coach-message__body > p { margin: 6px 0 0; color: var(--color-text); font-size: 14px; line-height: 1.65; white-space: pre-wrap; }
.coach-message__citations { display: grid; gap: 5px; margin-top: 10px; padding-top: 9px; border-top: 1px solid #edf0f5; }
.coach-message__citations span { color: var(--color-ai); font-size: 11px; font-weight: 700; }
.coach-message__citations small { color: var(--color-secondary); font-size: 11px; }
.coach-message__body > p.coach-message__notice { color: #b66a0c; font-size: 11px; }
.coach-conversation__typing { color: var(--color-secondary); font-size: 12px; }
</style>
