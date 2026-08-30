<template>
  <form class="coach-composer" data-testid="coach-composer" @submit.prevent="submit">
    <textarea v-model="value" aria-label="向 AI 学习教练提问" :disabled="sending" placeholder="输入你的问题，例如：BFS 为什么使用队列？" @keydown.enter.exact.prevent="submit"></textarea>
    <button type="submit" aria-label="发送消息" :disabled="sending || !value.trim()"><ArrowUp aria-hidden="true" /></button>
  </form>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ArrowUp } from '@element-plus/icons-vue'

defineProps<{ sending: boolean }>()
const emit = defineEmits<{ submit: [message: string] }>()
const value = ref('')

defineExpose({
  clear: () => {
    value.value = ''
  },
})

const submit = () => {
  const message = value.value.trim()
  if (!message) return
  emit('submit', message)
}
</script>

<style scoped>
.coach-composer { display: grid; grid-template-columns: minmax(0, 1fr) 42px; gap: 8px; align-items: end; padding: 9px; border: 1px solid var(--color-border); border-radius: 8px; background: #fff; box-shadow: 0 3px 12px rgb(25 52 87 / 5%); }
.coach-composer textarea { min-height: 48px; resize: vertical; padding: 10px 11px; border: 1px solid #d7e1ef; border-radius: 7px; outline: none; color: var(--color-text); background: #fbfdff; font: inherit; font-size: 13px; line-height: 1.5; }
.coach-composer textarea:focus { border-color: #8db0ee; box-shadow: 0 0 0 2px #e6efff; }
.coach-composer button { display: grid; width: 42px; height: 42px; place-items: center; border: 1px solid var(--color-primary); border-radius: 8px; color: #fff; background: var(--color-primary); cursor: pointer; }
.coach-composer button:disabled { cursor: not-allowed; opacity: .5; }
</style>
