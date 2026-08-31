<template>
  <section class="embed-panel" :class="`embed-panel--${viewState.toLowerCase()}`" data-testid="smartbi-embed-panel">
    <div v-if="viewState === 'LOADING'" class="embed-state" data-testid="smartbi-state-loading">
      <span class="spinner" aria-hidden="true"></span>
      <strong>正在连接分析平台</strong>
      <p>正在获取当前 SmartBI 资产状态。</p>
    </div>

    <div v-else-if="viewState === 'READY_IFRAME'" class="embed-ready" data-testid="smartbi-state-iframe">
      <div class="embed-toolbar">
        <div><span class="status-kicker">已连接</span><strong>{{ asset?.displayName }}</strong></div>
        <a class="external-link" :href="asset?.resourceUrl ?? undefined" target="_blank" rel="noreferrer">在 SmartBI 中打开 <span aria-hidden="true">↗</span></a>
      </div>
      <iframe :src="asset?.resourceUrl ?? undefined" :title="asset?.displayName ?? 'SmartBI 分析'" loading="lazy"></iframe>
    </div>

    <div v-else-if="viewState === 'READY_NEW_TAB'" class="embed-state embed-state--ready" data-testid="smartbi-state-new-tab">
      <span class="state-icon" aria-hidden="true">↗</span>
      <strong>{{ asset?.displayName }}</strong>
      <p>分析平台已配置，点击打开真实 SmartBI 资源。</p>
      <a class="primary-link" :href="asset?.resourceUrl ?? undefined" target="_blank" rel="noreferrer">在 SmartBI 中打开 <span aria-hidden="true">↗</span></a>
    </div>

    <div v-else-if="viewState === 'UNVERIFIED'" class="embed-state" data-testid="smartbi-state-unverified">
      <span class="state-icon state-icon--soft" aria-hidden="true">▦</span>
      <strong>{{ asset?.displayName ?? '分析平台入口' }}</strong>
      <p>{{ pendingMessage }}</p>
      <span class="state-note">平台资源地址待验证，当前不加载模拟图表。</span>
    </div>

    <div v-else-if="viewState === 'FORBIDDEN'" class="embed-state embed-state--warning" data-testid="smartbi-state-forbidden">
      <span class="state-icon" aria-hidden="true">!</span>
      <strong>当前账号暂无访问权限</strong>
      <p>请联系管理员开通 SmartBI 资产权限。</p>
    </div>

    <div v-else-if="viewState === 'DEGRADED'" class="embed-state embed-state--warning" data-testid="smartbi-state-degraded">
      <span class="state-icon" aria-hidden="true">!</span>
      <strong>分析平台暂时不可用</strong>
      <p>本地教学数据仍然保留，稍后可以重试连接。</p>
    </div>

    <div v-else class="embed-state embed-state--warning" data-testid="smartbi-state-error">
      <span class="state-icon" aria-hidden="true">!</span>
      <strong>分析资产暂时无法加载</strong>
      <p>请稍后重试，当前不会显示未经验证的分析结果。</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { SmartBiAssetVm } from '@/adapters/teacher/smartbi'

const props = withDefaults(defineProps<{
  asset: SmartBiAssetVm | null
  loading?: boolean
  errorState?: 'FORBIDDEN' | 'DEGRADED' | 'ERROR' | null
  pendingMessage?: string
}>(), {
  loading: false,
  errorState: null,
  pendingMessage: '分析平台入口尚未完成配置。',
})

const viewState = computed(() => {
  if (props.loading) return 'LOADING'
  if (props.errorState) return props.errorState
  if (!props.asset) return 'ERROR'
  if (props.asset.status === 'FORBIDDEN') return 'FORBIDDEN'
  if (props.asset.status === 'DEGRADED') return 'DEGRADED'
  if (props.asset.status === 'VERIFIED' && props.asset.launchMode === 'IFRAME' && props.asset.resourceUrl) return 'READY_IFRAME'
  if (props.asset.status === 'VERIFIED' && props.asset.launchMode === 'NEW_TAB' && props.asset.resourceUrl) return 'READY_NEW_TAB'
  return 'UNVERIFIED'
})
</script>

<style scoped>
.embed-panel { min-width: 0; overflow: hidden; border: 1px solid #dfe7f2; border-radius: 8px; background: #fff; box-shadow: 0 5px 18px rgb(37 61 99 / 5%); }
.embed-ready { min-width: 0; }
.embed-toolbar { display: flex; min-height: 58px; align-items: center; justify-content: space-between; gap: 14px; padding: 0 18px; border-bottom: 1px solid #edf1f6; }
.embed-toolbar > div { display: grid; gap: 4px; min-width: 0; }
.embed-toolbar strong { overflow: hidden; color: #1d335e; font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.status-kicker { color: #198457; font-size: 11px; font-weight: 800; }
.external-link, .primary-link { color: #2563eb; font-size: 12px; font-weight: 700; text-decoration: none; white-space: nowrap; }
.external-link:hover, .primary-link:hover { text-decoration: underline; }
.embed-ready iframe { display: block; width: 100%; min-height: 600px; border: 0; background: #f8fbff; }
.embed-state { display: grid; min-height: 290px; place-items: center; align-content: center; gap: 9px; padding: 28px; color: #425675; text-align: center; }
.embed-state strong { color: #1d335e; font-size: 17px; }
.embed-state p { max-width: 430px; margin: 0; color: #71819a; font-size: 13px; line-height: 1.7; }
.embed-state--ready { min-height: 310px; }
.embed-state--warning { background: #fffdfb; }
.state-icon { display: grid; width: 40px; height: 40px; place-items: center; border-radius: 50%; color: #2563eb; background: #eaf2ff; font-size: 20px; font-weight: 800; }
.state-icon--soft { color: #5578b4; background: #edf4ff; }
.embed-state--warning .state-icon { color: #b26b1a; background: #fff2d8; }
.state-note { color: #99a6b8; font-size: 11px; }
.spinner { width: 22px; height: 22px; border: 2px solid #dbe7fb; border-top-color: #3d79ec; border-radius: 50%; animation: spin 800ms linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
@media (max-width: 640px) { .embed-toolbar { align-items: flex-start; flex-direction: column; padding: 14px; } .embed-ready iframe { min-height: 480px; } }
</style>
