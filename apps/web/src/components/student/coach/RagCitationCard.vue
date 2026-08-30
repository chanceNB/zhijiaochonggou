<template>
  <article class="rag-citation-card" data-testid="rag-citation-card">
    <header><span>课程资料</span><small>{{ citation.score.toFixed(2) }}</small></header>
    <strong>{{ citation.title }}</strong>
    <p>{{ citation.excerpt }}</p>
  </article>
</template>

<script setup lang="ts">
import type { z } from 'zod'
import type { CitationSchema } from '@/types/contracts/student'

defineProps<{ citation: z.infer<typeof CitationSchema> }>()
</script>

<style scoped>
.rag-citation-card { display: grid; gap: 7px; padding: 11px 12px; border: 1px solid #cfe8db; border-radius: 8px; background: #f7fff9; }
.rag-citation-card header { display: flex; justify-content: space-between; gap: 8px; color: var(--color-ai); font-size: 11px; font-weight: 700; }
.rag-citation-card strong { overflow: hidden; color: var(--color-text); font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.rag-citation-card p { margin: 0; color: var(--color-secondary); font-size: 11px; line-height: 1.55; }
</style>
