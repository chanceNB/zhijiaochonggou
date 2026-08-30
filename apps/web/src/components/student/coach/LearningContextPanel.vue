<template>
  <aside class="learning-context-panel" data-testid="learning-context-panel">
    <header><p>当前上下文</p><h2>学习状态</h2></header>
    <dl v-if="context">
      <div><dt>掌握度</dt><dd>{{ formatPercent(context.mastery) }}</dd></div>
      <div><dt>置信度</dt><dd>{{ formatPercent(context.confidence) }}</dd></div>
      <div><dt>遗忘风险</dt><dd class="is-risk">{{ formatPercent(context.forgettingRisk) }}</dd></div>
    </dl>
    <div class="learning-context-panel__rag">
      <span>资料检索</span>
      <strong :class="`is-${ragStatus.toLowerCase()}`">{{ ragLabel }}</strong>
    </div>
    <div v-if="citations.length" class="learning-context-panel__citations">
      <RagCitationCard v-for="citation in citations" :key="citation.chunkId" :citation="citation" />
    </div>
    <p v-else class="learning-context-panel__empty">{{ ragStatus === 'DEGRADED' ? '课程资料服务暂不可用' : '未检索到课程资料，本次回答未引用课程知识库' }}</p>
  </aside>
</template>

<script setup lang="ts">
import RagCitationCard from '@/components/student/coach/RagCitationCard.vue'
import { computed } from 'vue'
import type { CoachContextVm } from '@/types/contracts/student'
import type { z } from 'zod'
import type { CitationSchema } from '@/types/contracts/student'

const props = defineProps<{ context: CoachContextVm | null; ragStatus: 'INDEXED' | 'EMPTY' | 'DEGRADED'; citations: z.infer<typeof CitationSchema>[] }>()
const formatPercent = (value: number) => `${Math.round(value * 100)}%`
const ragLabel = computed(() => props.ragStatus === 'INDEXED' ? '已引用' : props.ragStatus === 'DEGRADED' ? '服务降级' : '暂无资料')
</script>

<style scoped>
.learning-context-panel { display: grid; align-content: start; gap: 14px; min-height: 0; padding: 15px; border: 1px solid var(--color-border); border-radius: 8px; background: #fff; box-shadow: var(--shadow-card); }
.learning-context-panel header p, .learning-context-panel header h2 { margin: 0; }
.learning-context-panel header p { color: var(--color-secondary); font-size: 11px; }
.learning-context-panel header h2 { margin-top: 4px; color: var(--color-text); font-size: 17px; }
.learning-context-panel dl { display: grid; gap: 9px; margin: 0; }
.learning-context-panel dl div { display: flex; justify-content: space-between; gap: 12px; }
.learning-context-panel dt { color: var(--color-secondary); font-size: 12px; }
.learning-context-panel dd { margin: 0; color: #245fc9; font-size: 14px; font-weight: 800; }
.learning-context-panel dd.is-risk { color: var(--color-risk); }
.learning-context-panel__rag { display: flex; justify-content: space-between; gap: 8px; padding-top: 11px; border-top: 1px solid #edf0f5; color: var(--color-secondary); font-size: 12px; }
.learning-context-panel__rag strong.is-indexed { color: var(--color-ai); }
.learning-context-panel__rag strong.is-empty { color: #b66a0c; }
.learning-context-panel__rag strong.is-degraded { color: var(--color-warning); }
.learning-context-panel__citations { display: grid; gap: 8px; }
.learning-context-panel__empty { margin: 0; color: var(--color-secondary); font-size: 11px; line-height: 1.55; }
</style>
