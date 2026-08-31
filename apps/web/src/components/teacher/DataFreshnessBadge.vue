<template>
  <span class="freshness-badge" :class="`freshness-badge--${tone}`" data-testid="data-freshness-badge">
    <span class="freshness-badge__dot" aria-hidden="true"></span>
    {{ label }}
    <small v-if="lagSeconds > 0">{{ lagSeconds }} 秒</small>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  status?: 'FRESH' | 'STALE' | 'NO_DATA' | 'DEGRADED' | 'LOADING'
  lagSeconds?: number
}>(), {
  status: 'LOADING',
  lagSeconds: 0,
})

const label = computed(() => ({
  FRESH: '数据新鲜',
  STALE: '数据可能滞后',
  NO_DATA: '暂无分析数据',
  DEGRADED: '分析服务暂不可用',
  LOADING: '正在检查数据',
}[props.status]))
const tone = computed(() => props.status.toLowerCase().replace('_', '-'))
</script>

<style scoped>
.freshness-badge {
  display: inline-flex;
  min-height: 28px;
  align-items: center;
  gap: 7px;
  padding: 0 10px;
  border: 1px solid #d9e3f1;
  border-radius: 999px;
  color: #526782;
  background: #f8fbff;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.freshness-badge__dot { width: 7px; height: 7px; border-radius: 50%; background: #94a3b8; }
.freshness-badge small { color: inherit; font-size: 10px; font-weight: 600; }
.freshness-badge--fresh { border-color: #bfe7d4; color: #18704e; background: #f0fbf5; }
.freshness-badge--fresh .freshness-badge__dot { background: #22a06b; }
.freshness-badge--stale { border-color: #f2d39d; color: #9a6415; background: #fff9ed; }
.freshness-badge--stale .freshness-badge__dot { background: #e79a19; }
.freshness-badge--no-data,
.freshness-badge--degraded { border-color: #ead7d4; color: #9b4d46; background: #fff7f6; }
.freshness-badge--no-data .freshness-badge__dot,
.freshness-badge--degraded .freshness-badge__dot { background: #c96b62; }
</style>
