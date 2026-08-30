<template>
  <section class="diagnostic-start-card" data-testid="diagnostic-start-card">
    <div><span class="diagnostic-start-card__icon" aria-hidden="true">2</span><div><strong>开始两题诊断</strong><p>用两道结构化题目确认当前知识点。</p></div></div>
    <button type="button" :disabled="disabled" @click="emit('start')">{{ loading ? '创建中...' : '开始诊断' }}</button>
    <p v-if="error" class="diagnostic-start-card__error">{{ error }}</p>
  </section>
</template>

<script setup lang="ts">
defineProps<{ disabled?: boolean; loading?: boolean; error?: string | null }>()
const emit = defineEmits<{ start: [] }>()
</script>

<style scoped>
.diagnostic-start-card { display: grid; gap: 10px; padding: 13px 14px; border: 1px solid #b8cdfc; border-radius: 8px; background: #f8fbff; }
.diagnostic-start-card > div { display: grid; grid-template-columns: 32px minmax(0, 1fr); gap: 10px; align-items: center; }
.diagnostic-start-card__icon { display: grid; width: 32px; height: 32px; place-items: center; border-radius: 8px; color: #fff; background: var(--color-student-primary); font-size: 14px; font-weight: 800; }
.diagnostic-start-card strong { color: var(--color-text); font-size: 13px; }
.diagnostic-start-card p { margin: 4px 0 0; color: var(--color-secondary); font-size: 11px; line-height: 1.5; }
.diagnostic-start-card button { min-height: 36px; border: 1px solid var(--color-student-primary); border-radius: 7px; color: #fff; background: var(--color-student-primary); cursor: pointer; font-size: 13px; font-weight: 700; }
.diagnostic-start-card button:disabled { cursor: wait; opacity: .55; }
.diagnostic-start-card__error { color: var(--color-risk) !important; }
</style>
