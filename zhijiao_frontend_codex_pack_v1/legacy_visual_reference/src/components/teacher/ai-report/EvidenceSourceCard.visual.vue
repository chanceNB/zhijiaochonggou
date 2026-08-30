<!-- VISUAL REFERENCE ONLY.
Source: src/components/teacher/ai-report/EvidenceSourceCard.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section
    class="report-insight-card report-panel report-evidence-card"
    data-testid="evidence-trace"
  >
    <header class="report-card-heading">
      <h2>证据溯源</h2>
      <button type="button" class="report-card-link" @click="$emit('open')">
        查看全部溯源
        <el-icon aria-hidden="true"><ArrowRight /></el-icon>
      </button>
    </header>
    <button
      v-for="item in items"
      :key="item.key"
      type="button"
      class="report-evidence-item"
      data-testid="evidence-source-item"
      @click="$emit('select', item)"
    >
      <span>{{ item.title }}</span>
      <small v-if="item.subtitle">{{ item.subtitle }}</small>
    </button>
    <button
      type="button"
      class="report-evidence-more"
      data-testid="open-report-ledger"
      @click="$emit('open')"
    >
      查看全部溯源
      <el-icon aria-hidden="true"><ArrowRight /></el-icon>
    </button>
  </section>
</template>
