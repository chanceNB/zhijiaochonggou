<template>
  <section class="assignment-card" data-testid="teacher-assignment-card">
    <header>
      <span class="assignment-card__label">教师安排</span>
      <span class="assignment-card__status">{{ assignment.status }}</span>
    </header>
    <h2>定向练习</h2>
    <p>完成教师下发的专项练习，结果会回到当前学习状态。</p>
    <dl>
      <div><dt>预计用时</dt><dd>按任务设置</dd></div>
      <div><dt>截止时间</dt><dd>{{ formatDate(assignment.dueAt) }}</dd></div>
    </dl>
    <button type="button" @click="emit('start')">进入定向练习</button>
  </section>
</template>

<script setup lang="ts">
import type { TodayVm } from '@/types/contracts/student'

defineProps<{ assignment: NonNullable<TodayVm['teacherAssignment']> }>()
const emit = defineEmits<{ start: [] }>()
const formatDate = (value: string) => new Intl.DateTimeFormat('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(value))
</script>

<style scoped>
.assignment-card { display: grid; gap: 13px; padding: 18px; border: 1px solid #cbdcf7; border-radius: 10px; background: #fff; box-shadow: var(--shadow-card); }
.assignment-card header { display: flex; justify-content: space-between; gap: 10px; align-items: center; }
.assignment-card__label { color: #245fc9; font-size: 12px; font-weight: 800; }
.assignment-card__status { padding: 3px 7px; border-radius: 5px; color: #147b56; background: #eaf8f1; font-size: 11px; font-weight: 700; }
.assignment-card h2, .assignment-card p { margin: 0; }
.assignment-card h2 { color: var(--color-text); font-size: 17px; }
.assignment-card p { color: var(--color-secondary); font-size: 13px; line-height: 1.55; }
.assignment-card dl { display: grid; gap: 8px; margin: 0; padding-top: 10px; border-top: 1px solid #edf0f5; }
.assignment-card dl div { display: flex; justify-content: space-between; gap: 10px; }
.assignment-card dt { color: var(--color-secondary); font-size: 12px; }
.assignment-card dd { margin: 0; color: var(--color-text); font-size: 12px; font-weight: 700; }
.assignment-card button { min-height: 38px; border: 1px solid var(--color-student-primary); border-radius: 8px; color: #fff; background: var(--color-student-primary); cursor: pointer; font-size: 13px; font-weight: 700; }
</style>
