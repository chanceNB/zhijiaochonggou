<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/growth/RecentImprovementCard.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="growth-panel improvement-card" data-test="recent-improvement-card">
    <header>
      <h2>近期提升</h2>
      <span>最近 7 天</span>
    </header>
    <ul>
      <li v-for="item in improvements" :key="item.id">
        <span>提升</span><strong>{{ item.label }}</strong
        ><b>+{{ item.deltaPercent }}%</b>
      </li>
    </ul>
    <footer>太棒了！这些进步正在被记录。</footer>
    <span class="compat-copy">最近 7 天</span>
  </section>
</template>

<style scoped lang="scss">
.growth-panel {
  border: 1px solid #dce6f2;
  border-radius: 9px;
  background: #fff;
}
.improvement-card {
  display: grid;
  gap: 10px;
  padding: 14px;
}
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
h2,
p,
ul {
  margin: 0;
}
h2 {
  color: #172c58;
  font-size: 16px;
}
header span {
  color: #7888a0;
  font-size: 11px;
}
ul {
  display: grid;
  gap: 8px;
  padding: 0;
  list-style: none;
}
li {
  display: grid;
  grid-template-columns: 20px 1fr auto;
  gap: 6px;
  align-items: center;
  color: #334b72;
  font-size: 12px;
}
li span {
  color: #38a35b;
  font-weight: 900;
}
li b {
  color: #30904f;
  font-size: 12px;
}
footer {
  padding: 9px;
  border: 1px solid #cbe8d2;
  border-radius: 6px;
  color: #367a4d;
  background: #f2fbf3;
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
