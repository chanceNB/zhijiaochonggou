<!-- VISUAL REFERENCE ONLY.
Source: src/pages/teacher/TeacherStudentProfilePage.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <div class="teacher-page profile-page" data-testid="teacher-student-profile-page">
    <header class="page-header">
      <div class="heading-copy">
        <div class="heading-icon" aria-hidden="true">
          <el-icon><User /></el-icon>
        </div>
        <div>
          <h1>学生画像</h1>
          <p>班级与学生学习数字孪生</p>
        </div>
      </div>
      <div class="header-controls">
        <el-select v-model="selectedClass" data-testid="profile-class-select">
          <el-option label="初二（3）班" value="初二（3）班" />
        </el-select>
        <el-input
          v-model="studentSearch"
          class="search-input"
          data-testid="profile-toolbar-search"
          placeholder="搜索学生姓名"
          clearable
        />
        <el-button
          data-testid="student-profile-open-smartbi"
          type="primary"
          plain
          @click="openSmartBiFromToolbar"
          >打开 SmartBI</el-button
        >
      </div>
    </header>
    <section v-if="pageState === 'loading'" class="state-panel" data-testid="profile-loading">
      正在加载学生画像…
    </section>
    <section v-else-if="pageState === 'empty'" class="state-panel" data-testid="profile-empty">
      <strong>暂无学生画像</strong><span>当前筛选范围内没有可展示的学习数字孪生。</span>
    </section>
    <section
      v-else-if="pageState === 'error'"
      class="state-panel error-state"
      data-testid="profile-error"
    >
      <strong>学生画像数据加载失败</strong
      ><el-button data-testid="profile-retry" type="primary" @click="retryProfile">重试</el-button>
    </section>
    <section
      v-else-if="realProfileMode && realProfile"
      class="panel"
      data-testid="real-student-profile"
    >
      <h2>{{ realProfile.studentId ?? '未指定学生' }}</h2>
      <p>caseId：{{ realProfile.caseId }} · version={{ realProfile.version }}</p>
      <p>掌握度：{{ Math.round(realProfile.mastery * 100) }}%</p>
      <p>完成任务：{{ realProfile.completedTasks }}</p>
      <p>修复误概念：{{ realProfile.repairedMisconceptions }}</p>
      <div>
        <strong>薄弱点</strong>
        <span v-for="point in realProfile.weakPoints" :key="point">{{ point }}</span>
      </div>
      <div>
        <strong>证据</strong>
        <span v-for="reference in realProfile.evidenceRefs" :key="reference">{{ reference }}</span>
      </div>
      <div class="profile-actions">
        <el-button
          type="primary"
          data-testid="real-profile-intervention"
          @click="openRealProfileIntervention"
        >
          创建同 caseId 干预
        </el-button>
        <el-button data-testid="real-profile-report" @click="openRealProfileReport">
          查看同 caseId 报告
        </el-button>
      </div>
      <SmartBiEmbedPanel
        v-if="realProfile"
        dashboard-key="student-twin"
        :filters="{
          ...(demoRunId ? { demoRunId } : {}),
          courseId: realProfile.courseId,
          ...(realProfile.classId ? { classId: realProfile.classId } : {}),
          ...(realProfile.studentId ? { studentId: realProfile.studentId } : {}),
        }"
      />
    </section>
    <template v-else>
      <section class="smartbi-hidden" data-testid="student-profile-smartbi">
        <SmartBiResourceCard
          v-if="smartBiResource"
          :resource="smartBiResource"
          @open="openSmartBi"
        />
      </section>
      <section class="overview-grid" data-testid="profile-class-metrics">
        <article v-for="metric in classMetrics" :key="metric.id" class="panel overview-card">
          <span class="overview-icon" :class="metric.tone" aria-hidden="true">
            <el-icon><component :is="metricIcon(metric.id)" /></el-icon>
          </span>
          <div>
            <span>{{ metric.label }}</span
            ><strong>{{ metric.value }}</strong
            ><em :class="{ 'is-positive': metric.tone !== 'red' }">{{ metric.change }}</em>
          </div>
        </article>
      </section>
      <section class="profile-layout">
        <aside class="student-panel panel">
          <div class="student-list-head">
            <h2>学生列表</h2>
            <div class="filter-bar">
              <el-button
                data-testid="filter-risk-all"
                :type="riskFilter === '全部' ? 'primary' : 'default'"
                @click="clearFilters"
                >全部</el-button
              ><el-button
                data-testid="filter-risk-high"
                :type="riskFilter === '高风险' ? 'danger' : 'default'"
                @click="filterRisk('高风险')"
                >高风险</el-button
              ><el-button
                :type="riskFilter === '中风险' ? 'warning' : 'default'"
                @click="filterRisk('中风险')"
                >中风险</el-button
              ><el-button
                data-testid="filter-risk-low"
                :type="riskFilter === '低风险' ? 'success' : 'default'"
                @click="filterRisk('低风险')"
                >低风险</el-button
              >
            </div>
            <el-input
              v-model="studentSearch"
              data-testid="profile-student-search"
              class="student-list-search"
              placeholder="搜索学生姓名..."
              clearable
            >
              <template #prefix
                ><el-icon><Search /></el-icon
              ></template>

<style scoped lang="scss">
.profile-page {
  color: #17233c;
  max-width: 1600px;
  margin: 0 auto;
  background: #f6f8fc;
}
.page-header,
.heading-copy,
.header-controls,
.student-row,
.action-stack,
.section-title,
.hero-metrics {
  display: flex;
  align-items: center;
}
.page-header {
  justify-content: space-between;
  gap: 16px;
  margin: 8px 0 18px;
}
.heading-copy {
  gap: 14px;
}
.heading-icon {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 11px;
  background: #eef3ff;
  color: #2f66eb;
  font-size: 20px;
}
.heading-copy h1 {
  margin: 0;
  font-size: 28px;
}
.heading-copy p {
  margin: 5px 0 0;
  color: #728099;
  font-size: 13px;
}
.header-controls {
  gap: 12px;
}
.header-controls .el-select {
  width: 160px;
}
.search-input {
  width: 240px;
}
.smartbi-hidden {
  display: none;
}
.panel {
  border: 1px solid #e3eaf4;
  border-radius: 12px;
  background: #fff;
}
h2,
h3,
p {
  margin: 0;
}
.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 14px;
}
.overview-card {
  display: flex;
  align-items: center;
  gap: 13px;
  min-height: 108px;
  padding: 16px 20px;
}
.overview-card > div {
  display: grid;
  gap: 5px;
}
.overview-card > div > span {
  color: #68778e;
  font-size: 12px;
}
.overview-card strong {
  font-size: 25px;
}
.overview-card em {
  font-style: normal;
  color: #738198;
  font-size: 11px;
}
.overview-card em.is-positive,
.overview-card em b {
  color: #22a166;
}
.overview-icon {
  display: grid;
  flex: 0 0 46px;
  width: 46px;
  height: 46px;
  place-items: center;
  border-radius: 50%;
  font-size: 18px;
}
.overview-icon.blue {
  background: #eaf1ff;
  color: #2f6ce9;
}
.overview-icon.red {
  background: #fff0ef;
  color: #ed504a;
}
.overview-icon.orange {
  background: #fff5e6;
  color: #e99918;
}
.overview-icon.green {
  background: #eaf8ef;
  color: #2da35f;
}
.profile-layout {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}
.student-panel {
  overflow: hidden;
}
.student-list-head {
  display: grid;
  gap: 12px;
  padding: 17px 18px 12px;
}
.student-list-head h2 {
  font-size: 15px;
}
.filter-bar {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}
.filter-bar .el-button {
  margin: 0;
  padding: 6px 11px;
}
.student-list-search {
  width: 100%;
}
.student-head,
.student-row {
  display: grid;
  grid-template-columns: minmax(112px, 1.3fr) 58px 58px 70px minmax(62px, 0.82fr);
  gap: 6px;
  align-items: center;
}
.student-head > *,
.student-row > * {
  min-width: 0;
}
.student-head {
  padding: 10px 14px;
  background: #f8faff;
  color: #738098;
  font-size: 11px;
}
.student-row {
  min-height: 50px;
  padding: 0 14px;
  border-bottom: 1px solid #edf0f5;
  color: #4e5f77;
  font-size: 12px;
  cursor: pointer;
}
.student-row.active {
  background: #f0f5ff;
  box-shadow: inset 3px 0 #2f6cf0;
}
.student-name-cell {
  display: inline-flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
}
.student-avatar {
  display: inline-grid;
  flex: 0 0 26px;
  width: 26px;
  height: 26px;
  place-items: center;
  border: 2px solid #fff;
  border-radius: 50%;
  color: #fff;
  background: #9bb8ed;
  box-shadow: 0 1px 4px rgb(30 64 120 / 10%);
  font-size: 13px;
}
.student-avatar--blue {
  background: linear-gradient(145deg, #8ab5ff, #3c78ed);
}
.student-avatar--violet {
  background: linear-gradient(145deg, #b9a7f5, #7258d6);
}
.student-avatar--teal {
  background: linear-gradient(145deg, #82d8cb, #2c9d9a);
}
.student-avatar--orange {
  background: linear-gradient(145deg, #ffd293, #e87c28);
}
.student-avatar--rose {
  background: linear-gradient(145deg, #f8a6ad, #d8566d);
}
.student-avatar--indigo {
  background: linear-gradient(145deg, #94a7e8, #4d5db3);
}
.student-row strong {
  overflow: hidden;
  color: #263750;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.student-empty {
  display: grid;
  min-height: 240px;
  place-items: center;
  align-content: center;
  gap: 6px;
  padding: 20px;
  color: #708099;
  font-size: 12px;
  text-align: center;
}
.student-empty strong {
  color: #263750;
  font-size: 14px;
}
.student-row .el-tag {
  justify-self: start;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.trend-cell {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  min-width: 0;
  font-size: 12px;
  line-height: 1;
  white-space: nowrap;
}
.trend-cell .el-icon {
  flex: 0 0 auto;
  font-size: 15px;
}
.trend-up {
  color: #23a167;
}
.trend-down {
  color: #ee504d;
}
.list-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 5px;
  padding: 10px 12px;
  color: #7a879a;
  font-size: 11px;
}
.list-footer span {
  margin-right: auto;
}
.profile-main {
  display: grid;
  gap: 12px;
  min-width: 0;
}
.student-hero {
  display: grid;
  grid-template-columns: 58px 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 16px 18px;
}
.avatar-large {
  display: grid;
  width: 58px;
  height: 58px;
  place-items: center;
  border-radius: 50%;
  background: #9bb8ed;
  color: #fff;
  font-size: 23px;
  font-weight: 700;
}
.avatar-large.student-avatar--blue {
  background: linear-gradient(145deg, #8ab5ff, #3c78ed);
}
.avatar-large.student-avatar--violet {
  background: linear-gradient(145deg, #b9a7f5, #7258d6);
}
.avatar-large.student-avatar--teal {
  background: linear-gradient(145deg, #82d8cb, #2c9d9a);
}
.avatar-large.student-avatar--orange {
  background: linear-gradient(145deg, #ffd293, #e87c28);
}
.avatar-large.student-avatar--rose {
  background: linear-gradient(145deg, #f8a6ad, #d8566d);
}
.avatar-large.student-avatar--indigo {
  background: linear-gradient(145deg, #94a7e8, #4d5db3);
}
.student-identity h2 {
  font-size: 24px;
}
.student-identity p {
  margin-top: 6px;
  color: #6e7d95;
  font-size: 12px;
}
.hero-metrics {
  grid-column: 1/-1;
  gap: 0;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #edf0f5;
}
.hero-metrics > div {
  display: grid;
  gap: 5px;
  flex: 1;
  padding: 0 14px;
  border-right: 1px solid #edf0f5;
}
.hero-metrics > div:first-child {
  padding-left: 0;
}
.hero-metrics > div:last-child {
  border: 0;
}
.hero-metrics span {
  color: #78869b;
  font-size: 11px;
}
.hero-metrics strong {
  font-size: 20px;
}
.hero-metrics em {
  font-style: normal;
  color: #26a16a;
  font-size: 11px;
}
.detail-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  align-items: start;
}
.detail-column {
  display: grid;
  align-content: start;
  gap: 12px;
  min-width: 0;
}
.detail-column > .panel {
  min-height: 0;
  overflow: hidden;
  align-self: start;
}
.section-title {
  justify-content: space-between;
  min-height: 44px;
  padding: 11px 14px;
  border-bottom: 1px solid #edf0f5;
}
.section-title h3 {
  font-size: 14px;
}
.section-title .section-kicker {
  margin-left: auto;
  color: #8a98ac;
  font-size: 11px;
}
.section-title .section-icon {
  margin-left: auto;
  color: #2f6be5;
  font-size: 16px;
}
.section-title a,
.section-link {
  color: #2f6be5;
  font-size: 11px;
}
.section-link {
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  font-family: inherit;
}
.ability-bars {
  display: grid;
  gap: 8px;
  padding: 12px 14px 14px;
}
.ability-bars > div {
  display: grid;
  grid-template-columns: 95px 1fr 38px;
  align-items: center;
  gap: 8px;
  color: #61718a;
  font-size: 11px;
}
.ability-bars b {
  height: 6px;
  border-radius: 5px;
  background: #edf1f9;
  overflow: hidden;
}
.ability-bars i {
  display: block;
  height: 100%;
  border-radius: 5px;
  background: #8da7f5;
}
.ability-bars strong {
  color: #1e2f4b;
  font-size: 12px;
}
.evidence-list,
.timeline-list,
.rhythm-summary,
.insight-copy,
.advice-copy {
  display: grid;
  gap: 8px;
  padding: 12px 14px 14px;
}
.evidence-item {
  display: grid;
  grid-template-columns: 20px minmax(0, 1fr);
  gap: 8px;
  align-items: start;
  padding: 8px 9px;
  border: 1px solid #edf1f7;
  border-radius: 7px;
  background: #f8faff;
}
.evidence-marker {
  display: grid;
  width: 20px;
  height: 20px;
  place-items: center;
  border-radius: 50%;
  background: #e8f6ee;
  color: #239360;
  font-size: 13px;
}
.evidence-list--counter .evidence-marker {
  background: #fff1ef;
  color: #e45b55;
}
.evidence-item p,
.timeline-item p,
.rhythm-summary p,
.insight-copy p,
.advice-copy p {
  color: #63738c;
  font-size: 12px;
  line-height: 1.55;
}
.timeline-item {
  display: grid;
  grid-template-columns: 8px minmax(0, 1fr);
  gap: 9px;
  align-items: start;
}
.timeline-dot {
  width: 8px;
  height: 8px;
  margin-top: 5px;
  border-radius: 50%;
  background: #7e9df0;
  box-shadow: 0 0 0 3px #edf2ff;
}
.timeline-item--success .timeline-dot {
  background: #2aa169;
  box-shadow: 0 0 0 3px #e8f6ee;
}
.rhythm-summary p:first-child {
  padding-bottom: 8px;
  border-bottom: 1px solid #edf1f7;
  color: #315da0;
  font-weight: 700;
}
.advice-copy {
  background: #f7faff;
}
.advice-copy small {
  display: block;
  color: #8c99ad;
  font-size: 10px;
  line-height: 1.5;
}
blockquote {
  margin: 0;
  padding: 12px 14px 14px;
  color: #63738c;
  font-size: 12px;
  line-height: 1.65;
}
.action-stack {
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px 0 0;
  border-top: 1px solid #e7edf5;
}
.action-stack .el-button {
  margin: 0;
}
.profile-action-status {
  color: #738198;
  font-size: 11px;
  text-align: right;
}
.profile-action-status::before {
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-right: 6px;
  border-radius: 50%;
  background: #22a166;
  content: '';
}
.profile-empty-state {
  display: grid;
  min-height: 320px;
  place-items: center;
  align-content: center;
  gap: 10px;
  padding: 24px;
  border: 1px dashed #cbd8eb;
  border-radius: 12px;
  color: #71809a;
  text-align: center;
}
.profile-empty-state strong {
  color: #263750;
  font-size: 18px;
}
.drawer-items {
  display: grid;
  gap: 10px;
}
.drawer-item {
  display: grid;
  gap: 7px;
  padding: 13px;
  border: 1px solid #e3eaf4;
  border-radius: 10px;
  background: #fbfcff;
}
.drawer-item-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: #76849a;
  font-size: 11px;
}
.drawer-item-head > div {
  display: grid;
  gap: 3px;
}
.drawer-item-head > div:last-child {
  text-align: right;
}
.drawer-item-head span,
.drawer-item p > span,
.drawer-item small > span {
  display: block;
  margin-bottom: 2px;
  color: #8a97aa;
  font-size: 10px;
}
.drawer-item-head strong {
  color: #2b4063;
  font-size: 13px;
}
.drawer-item p {
  color: #53657f;
  font-size: 12px;
  line-height: 1.55;
}
.drawer-item small {
  color: #278b5b;
  font-size: 10px;
}
.drawer-record-link {
  justify-self: start;
  padding: 0;
  border: 0;
  color: #2f66eb;
  background: transparent;
  cursor: pointer;
  font-size: 11px;
}
.plan-panel {
  display: grid;
  gap: 12px;
  padding: 15px 17px;
  border: 1px solid #cbd9f7;
  border-radius: 9px;
  background: #f4f8ff;
}
.plan-panel span {
  color: #315da0;
  font-weight: 700;
  font-size: 12px;
}
.plan-panel p,
.plan-panel ol {
  margin-top: 5px;
  color: #566982;
  font-size: 12px;
  line-height: 1.6;
}
.state-panel {
  display: grid;
  min-height: 190px;
  place-items: center;
  gap: 10px;
  border: 1px dashed #ced9ec;
  border-radius: 9px;
  background: #fbfcff;
  color: #66758c;
}
.error-state {
  color: #c43f3f;
}
@media (max-width: 1439px) {
  .profile-layout {
    grid-template-columns: 320px minmax(0, 1fr);
  }
}
@media (max-width: 1180px) {
  .detail-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .detail-column:last-child {
    grid-column: 1/-1;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    align-items: start;
  }
}
@media (max-width: 820px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }
  .header-controls {
    width: 100%;
    flex-wrap: wrap;
  }
  .overview-grid,
  .profile-layout {
    grid-template-columns: 1fr;
  }
  .student-panel {
    order: 1;
  }
  .profile-main {
    order: 2;
  }
  .student-hero {
    grid-template-columns: 52px 1fr;
  }
  .student-hero > .el-tag {
    grid-column: 2;
  }
  .hero-metrics {
    grid-column: 1/-1;
    flex-wrap: wrap;
    gap: 12px;
  }
  .hero-metrics > div {
    min-width: 42%;
    padding: 0;
    border: 0;
  }
}
@media (max-width: 767px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
  .detail-column:last-child {
    grid-column: auto;
    grid-template-columns: 1fr;
  }
}
@media (max-width: 560px) {
  .header-controls {
    align-items: stretch;
    gap: 8px;
  }
  .header-controls .el-select,
  .search-input,
  .header-controls .el-button {
    width: 100%;
  }
  .overview-grid {
    display: flex;
    grid-template-columns: none;
    overflow-x: auto;
    gap: 10px;
    padding-bottom: 4px;
  }
  .overview-card {
    flex: 0 0 min(260px, 78vw);
    min-height: 82px;
    gap: 9px;
    padding: 11px 12px;
  }
  .overview-icon {
    flex-basis: 34px;
    width: 34px;
    height: 34px;
    font-size: 15px;
  }
  .overview-card strong {
    font-size: 21px;
  }
  .overview-card em {
    font-size: 10px;
  }
  .filter-bar {
    overflow-x: auto;
    padding-bottom: 2px;
  }
  .filter-bar .el-button {
    flex: 0 0 auto;
  }
  .student-head,
  .student-row {
    grid-template-columns: minmax(82px, 1fr) 42px 50px 66px 58px;
    gap: 4px;
    padding-right: 10px;
    padding-left: 10px;
  }
  .student-head {
    font-size: 10px;
  }
  .student-row {
    font-size: 11px;
  }
  .student-row .el-tag {
    font-size: 10px;
  }
  .student-hero {
    padding: 14px;
  }
  .student-identity h2 {
    font-size: 21px;
  }
  .hero-metrics {
    row-gap: 10px;
  }
  .hero-metrics > div {
    min-width: calc(50% - 6px);
  }
  .action-stack {
    justify-content: stretch;
  }
  .action-stack .el-button {
    flex: 1 1 calc(50% - 8px);
  }
}
@media (max-width: 430px) {
  .overview-grid {
    display: flex;
    grid-template-columns: none;
  }
  .student-head,
  .student-row {
    grid-template-columns: minmax(78px, 1fr) 40px 48px 62px 54px;
  }
  .trend-cell span {
    display: none;
  }
  .trend-cell {
    justify-content: center;
  }
  .action-stack .el-button {
    flex-basis: 100%;
  }
}
</style>
