<template>
  <section class="route-placeholder" data-testid="route-placeholder">
    <header class="route-placeholder__heading">
      <p class="route-placeholder__eyebrow">{{ roleLabel }} / Golden Demo V2</p>
      <h1>{{ title }}</h1>
      <p>{{ subtitle }}</p>
    </header>
    <div class="route-placeholder__panel">
      <p>页面结构已就绪，业务数据将在对应阶段通过真实 API 接入。</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const roleLabel = computed(() => (route.meta.role === 'teacher' ? '教师端' : '学生端'))
const title = computed(() => String(route.meta.title ?? '页面'))
const subtitle = computed(() => String(route.meta.subtitle ?? '视觉基础占位页'))
</script>

<style scoped>
.route-placeholder__eyebrow {
  margin: 0;
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}
</style>
