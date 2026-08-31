<template>
  <section class="smartbi-page" data-testid="teacher-smartbi-asset-page">
    <header class="page-heading">
      <div><p class="eyebrow">SmartBI platform asset</p><h1>{{ asset?.displayName ?? '数据洞察资产' }}</h1><p>通过真实平台查看当前分析资产，业务数据仍由本地系统负责。</p></div>
      <DataFreshnessBadge :status="freshnessBadgeStatus" :lag-seconds="smartbi.freshness?.lagSeconds ?? 0" />
    </header>
    <section v-if="context" class="context-card"><div><p class="eyebrow">Current analysis context</p><h2>当前分析对象</h2></div><div class="context-grid"><div><span>当前学生</span><strong>{{ context.displayName }}</strong></div><div><span>课程</span><strong>{{ context.courseName }}</strong></div><div><span>班级</span><strong>{{ context.className }}</strong></div></div></section>
    <section v-else class="empty-context"><strong>暂无当前分析对象</strong><span>当前没有 ACTIVE demo run，无法绑定分析上下文。</span></section>
    <SmartBiEmbedPanel :asset="asset" :loading="smartbi.assetState === 'LOADING'" :error-state="smartbi.assetState === 'DEGRADED' ? 'DEGRADED' : smartbi.assetState === 'FORBIDDEN' ? 'FORBIDDEN' : null" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import DataFreshnessBadge from '@/components/teacher/DataFreshnessBadge.vue'
import SmartBiEmbedPanel from '@/components/teacher/SmartBiEmbedPanel.vue'
import { useSmartBiStore } from '@/stores/smartbiStore'
import { useTeacherStore } from '@/stores/teacherStore'

const route = useRoute()
const smartbi = useSmartBiStore()
const teacher = useTeacherStore()
const asset = computed(() => smartbi.asset)
const context = computed(() => teacher.workbench?.currentStudent ?? null)
const freshnessBadgeStatus = computed(() => smartbi.freshnessState === 'DEGRADED' ? 'DEGRADED' as const : smartbi.freshnessState === 'LOADING' || smartbi.freshnessState === 'INITIAL' ? 'LOADING' as const : smartbi.freshness?.status ?? 'NO_DATA')

async function load() {
  await teacher.loadWorkbench()
  await Promise.all([smartbi.loadAsset(String(route.params.assetKey), true), smartbi.loadFreshness()])
}

onMounted(() => void load())
</script>

<style scoped>
.smartbi-page { display: grid; gap: 22px; min-width: 0; color: #172238; }
.page-heading { display: flex; align-items: end; justify-content: space-between; gap: 20px; }
.page-heading h1 { margin: 4px 0 5px; color: #172a4b; font-size: 28px; }
.page-heading p:last-child { margin: 0; color: #6e7c92; font-size: 13px; }
.eyebrow { margin: 0; color: #2563eb; font-size: 10px; font-weight: 800; letter-spacing: .06em; text-transform: uppercase; }
.context-card { display: grid; gap: 14px; padding: 18px 20px; border: 1px solid #dfe7f2; border-radius: 8px; background: #fff; box-shadow: 0 5px 18px rgb(37 61 99 / 5%); }
.context-card h2 { margin: 4px 0 0; color: #1d335e; font-size: 18px; }
.context-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 1px; overflow: hidden; border: 1px solid #e7edf5; border-radius: 6px; background: #e7edf5; }
.context-grid div { display: grid; gap: 7px; padding: 12px 14px; background: #fbfdff; }
.context-grid span { color: #8190a6; font-size: 11px; }
.context-grid strong { overflow: hidden; color: #2b466d; font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.empty-context { display: grid; justify-items: center; gap: 6px; padding: 26px; border: 1px dashed #d5dfeb; border-radius: 7px; color: #75859e; background: #fbfdff; }
.empty-context strong { color: #355071; font-size: 14px; }
.empty-context span { font-size: 12px; }
@media (max-width: 700px) { .page-heading { align-items: flex-start; flex-direction: column; } .context-grid { grid-template-columns: 1fr; } }
</style>
