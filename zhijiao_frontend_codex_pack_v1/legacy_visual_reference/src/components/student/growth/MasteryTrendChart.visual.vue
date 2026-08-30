<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/growth/MasteryTrendChart.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="growth-panel trend-card" data-test="mastery-trend-chart">
    <header>
      <h2>掌握趋势</h2>
      <select aria-label="趋势范围" value="最近 4 周">
        <option>最近 4 周</option>
      </select>
    </header>
    <div ref="chartElement" class="trend-chart" aria-label="最近 4 周掌握趋势图" />
    <p>{{ trend.summary }}</p>
    <span class="compat-copy">掌握趋势</span>
  </section>
</template>

<style scoped lang="scss">
.growth-panel {
  border: 1px solid #dce6f2;
  border-radius: 9px;
  background: #fff;
}
.trend-card {
  display: grid;
  gap: 8px;
  padding: 14px;
}
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
h2,
p {
  margin: 0;
}
h2 {
  color: #172c58;
  font-size: 16px;
}
select {
  height: 28px;
  border: 1px solid #d8e2ef;
  border-radius: 5px;
  color: #627492;
  background: #fff;
  font-size: 11px;
}
.trend-chart {
  width: 100%;
  height: 155px;
}
p {
  color: #71819a;
  font-size: 11px;
}
.compat-copy {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
}
</style>
