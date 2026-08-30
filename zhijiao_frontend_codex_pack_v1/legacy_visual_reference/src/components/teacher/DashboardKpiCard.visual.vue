<!-- VISUAL REFERENCE ONLY.
Source: src/components/teacher/DashboardKpiCard.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <article
    :class="['kpi-card', `tone-${item.tone}`]"
    :data-testid="`kpi-${item.key}`"
    @click="emit('navigate', item.target)"
  >
    <div class="kpi-icon">
      <el-icon><component :is="icons[props.item.key]" /></el-icon>
    </div>
    <div class="kpi-copy">
      <span>{{ item.label }}</span
      ><strong>{{ item.value }}</strong>
    </div>
    <button data-testid="kpi-card" type="button" @click.stop="emit('navigate', item.target)">
      {{ item.actionLabel }} <b>›</b>
    </button>
  </article>
</template>

<style scoped lang="scss">
.kpi-card {
  min-height: 112px;
  padding: 20px;
  display: grid;
  grid-template-columns: 58px 1fr auto;
  align-items: center;
  gap: 16px;
  border-radius: 12px;
  border: 1px solid;
  cursor: pointer;
  transition: 0.2s ease;
  background: #fff;
}
.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgb(15 23 42 / 9%);
}
.kpi-icon {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 30px;
}
.kpi-copy {
  display: grid;
  gap: 4px;
}
.kpi-copy span {
  font-weight: 700;
  color: #334155;
}
.kpi-copy strong {
  font-size: 34px;
  line-height: 1;
}
.kpi-card button {
  border: 0;
  background: transparent;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}
.tone-blue {
  border-color: #bfdbfe;
  background: linear-gradient(135deg, #fff, #eff6ff);
}
.tone-blue .kpi-icon {
  background: linear-gradient(135deg, #60a5fa, #2563eb);
}
.tone-blue button {
  color: #2563eb;
}
.tone-red {
  border-color: #fecaca;
  background: linear-gradient(135deg, #fff, #fff1f2);
}
.tone-red .kpi-icon {
  background: linear-gradient(135deg, #fb7185, #ef4444);
}
.tone-red button {
  color: #ef4444;
}
.tone-green {
  border-color: #bbf7d0;
  background: linear-gradient(135deg, #fff, #f0fdf4);
}
.tone-green .kpi-icon {
  background: linear-gradient(135deg, #34d399, #059669);
}
.tone-green button {
  color: #059669;
}
.tone-orange {
  border-color: #fed7aa;
  background: linear-gradient(135deg, #fff, #fff7ed);
}
.tone-orange .kpi-icon {
  background: linear-gradient(135deg, #fb923c, #f59e0b);
}
.tone-orange button {
  color: #ea580c;
}
@media (max-width: 1100px) {
  .kpi-card {
    grid-template-columns: 52px 1fr;
  }
  .kpi-card button {
    grid-column: 2;
    justify-self: start;
  }
}
</style>
