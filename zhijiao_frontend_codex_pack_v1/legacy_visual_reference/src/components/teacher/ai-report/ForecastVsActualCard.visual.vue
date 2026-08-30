<!-- VISUAL REFERENCE ONLY.
Source: src/components/teacher/ai-report/ForecastVsActualCard.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="report-insight-card report-panel" data-testid="prediction-deviation">
    <header class="report-card-heading">
      <h2>预测与实际偏差</h2>
      <button type="button" class="report-card-link" @click="$emit('more')">
        更多指标
        <el-icon aria-hidden="true"><ArrowRight /></el-icon>
      </button>
    </header>
    <div class="forecast-metric-list">
      <article
        v-for="metric in metrics"
        :key="metric.key"
        class="forecast-metric"
        :class="`is-${metric.tone}`"
        :data-testid="metric.key === 'transfer' ? 'v3-report-gap' : undefined"
      >
        <strong class="forecast-metric-name">{{ metric.name }}</strong>
        <div class="forecast-metric-values">
          <span
            >预测 <b>{{ metric.predicted }}</b></span
          >
          <span
            >实际 <b>{{ metric.actual }}</b></span
          >
        </div>
        <p>{{ metric.diffText }}</p>
      </article>
    </div>
  </section>
</template>
