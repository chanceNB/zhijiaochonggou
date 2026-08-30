<!-- VISUAL REFERENCE ONLY.
Source: src/components/teacher/RiskActivityList.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="surface-card risk-card">
    <div class="card-header">
      <h2 class="card-title">近期风险动态</h2>
      <button type="button" @click="$emit('viewAll')">查看全部 ›</button>
    </div>
    <div class="risk-list">
      <article v-for="risk in risks" :key="risk.id">
        <span class="avatar">{{ risk.avatar }}</span>
        <div>
          <strong>{{ risk.student }}</strong
          ><small>{{ risk.course }} · {{ risk.detail }}</small>
        </div>
        <el-tag :type="risk.level === 'high' ? 'danger' : 'warning'" size="small">{{
          risk.level === 'high' ? '高风险' : '中风险'
        }}</el-tag>
      </article>
    </div>
  </section>
</template>

<style scoped lang="scss">
.card-header button {
  border: 0;
  background: transparent;
  color: #64748b;
  cursor: pointer;
}
.risk-list {
  padding: 7px 14px;
}
.risk-list article {
  display: grid;
  grid-template-columns: 34px 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #edf1f5;
}
.risk-list article:last-child {
  border: 0;
}
.avatar {
  width: 31px;
  height: 31px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: #fee2e2;
  color: #dc2626;
  font-weight: 700;
}
.risk-list div {
  display: grid;
  gap: 2px;
}
.risk-list strong {
  font-size: 13px;
}
.risk-list small {
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
