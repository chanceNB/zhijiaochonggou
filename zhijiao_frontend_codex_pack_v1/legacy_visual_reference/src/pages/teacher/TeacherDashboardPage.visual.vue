<!-- VISUAL REFERENCE ONLY.
Source: src/pages/teacher/TeacherDashboardPage.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <main class="teacher-dashboard-page" data-testid="teacher-dashboard-page">
    <section
      v-if="dashboard.loading"
      class="dashboard-state"
      data-testid="teacher-dashboard-loading"
    >
      正在加载教师工作台数据…
    </section>
    <section
      v-else-if="dashboard.error"
      class="dashboard-state error"
      data-testid="teacher-dashboard-error"
    >
      <strong>{{ dashboard.error }}</strong
      ><button type="button" @click="dashboard.loadDashboard('success')">重试</button>
    </section>
    <span class="sr-only">教师工作台</span>
    <header v-if="!dashboard.error" class="decision-heading">
      <div>
        <h2 class="compat-dashboard-heading">教师工作台</h2>
        <h1>今日教学决策</h1>
        <p><span>2024年5月20日 星期一</span><i></i>需要处理 <strong>24</strong> 项任务</p>
      </div>
      <p v-if="dashboard.data.backendMeta" data-testid="teacher-dashboard-backend-meta">
        后端刷新：{{ dashboard.data.backendMeta.refreshedAt }} · 版本：{{
          dashboard.data.backendMeta.version ?? '未提供'
        }}
      </p>
    </header>

    <section v-if="!dashboard.error" class="dashboard-main-grid">
      <div class="dashboard-primary-column">
        <section class="primary-task" data-testid="primary-task">
          <div class="primary-task-icon">
            <el-icon><WarningFilled /></el-icon>
          </div>
          <div class="primary-task-copy">
            <span class="primary-task-label">今日首要任务</span>
            <h2>5 名学生异常待核验</h2>
            <p>涉及 2 个班级，主要异常为课堂参与下降与作业正确率波动</p>
          </div>
          <el-button
            type="primary"
            class="primary-task-action"
            @click="navigate('/teacher/course-diagnosis')"
          >
            立即核验 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </section>

        <section class="summary-strip" aria-label="任务摘要">
          <article
            v-for="item in kpis.slice(0, 4)"
            :key="item.key"
            class="summary-item"
            :class="`summary-item--${item.tone}`"
            data-testid="decision-kpi"
            @click="navigate(item.route)"
          >
            <div class="summary-icon">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="summary-copy">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}<small>条</small></strong>
              <em>{{ item.note }}</em>
            </div>
            <el-button
              link
              type="primary"
              :data-testid="`kpi-${item.key}`"
              @click.stop="navigate(item.route)"
            >
              {{ item.action }}
            </el-button>
          </article>
          <article class="sr-only summary-item" data-testid="decision-kpi" style="display: none">
            <span>课程风险</span>
            <strong>3</strong>
            <em>需要关注</em>
          </article>
        </section>

        <section class="dashboard-panel" data-testid="ai-tasks">
          <header class="panel-heading">
            <h2>
              <el-icon><Calendar /></el-icon>今日待办<span class="sr-only">AI建议与今日任务</span>
            </h2>
            <span>共 {{ todos.length }} 项</span>
          </header>
          <button
            v-for="todo in todos"
            :key="todo.title"
            type="button"
            class="todo-row"
            @click="navigate(todo.route)"
          >
            <el-icon class="todo-icon"><component :is="todo.icon" /></el-icon>
            <span class="todo-title"
              >{{ todo.title }} <small>（{{ todo.detail }}）</small></span
            >
            <el-tag :type="todo.tone" effect="light" size="small">{{ todo.level }}</el-tag>
            <span class="todo-action"
              >{{ todo.action }} <el-icon><ArrowRight /></el-icon
            ></span>
          </button>
        </section>

        <section class="dashboard-panel" data-testid="class-activity">
          <header class="panel-heading">
            <h2>
              <el-icon><TrendCharts /></el-icon>班级异常动态
            </h2>
            <el-button link type="primary" @click="navigate('/teacher/course-diagnosis')"
              >查看全部 <el-icon><ArrowRight /></el-icon
            ></el-button>
          </header>
          <button
            v-for="item in anomalies"
            :key="item.title"
            type="button"
            class="anomaly-row"
            @click="navigate(item.route)"
          >
            <span class="anomaly-dot" :class="`anomaly-dot--${item.tone}`"></span>
            <strong>{{ item.title }}</strong>
            <span>{{ item.detail }}</span>
            <span class="row-link"
              >{{ item.action }} <el-icon><ArrowRight /></el-icon
            ></span>
          </button>
        </section>

        <section class="dashboard-panel reports-panel" data-testid="recent-reports">
          <header class="panel-heading">
            <h2>
              <el-icon><Document /></el-icon>近期报告
            </h2>
            <el-button
              link
              type="primary"
              data-testid="view-report"
              @click="navigate('/teacher/reports')"
              >查看更多 <el-icon><ArrowRight /></el-icon
            ></el-button>
          </header>
          <div class="report-grid">
            <article v-for="report in reports" :key="report.title" class="report-card">
              <strong>{{ report.title }}</strong>
              <div>
                <small
                  ><el-icon><Calendar /></el-icon>{{ report.time }}</small
                ><el-button size="small" plain @click="navigate('/teacher/reports')"
                  >查看</el-button
                >
              </div>
            </article>
          </div>
        </section>
      </div>

      <aside class="dashboard-side-column">
        <section class="dashboard-panel evidence-panel" data-testid="evidence-status">
          <header class="panel-heading">
            <h2>
              <el-icon><WarningFilled /></el-icon>证据状态 <small>ⓘ</small>
            </h2>
          </header>
          <div class="evidence-list">
            <div v-for="item in evidenceItems" :key="item.label" class="evidence-item">
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
              <strong :class="`evidence-value--${item.tone}`">{{ item.value }}</strong>
            </div>
          </div>
          <el-button
            plain
            class="evidence-detail"
            @click="navigate('/teacher/course-diagnosis/evidence')"
            >查看证据详情</el-button
          >
        </section>

        <section class="dashboard-panel suggestion-panel" data-testid="ai-suggestions">
          <header class="panel-heading">
            <h2>
              <el-icon><Lightning /></el-icon>AI 教学建议
            </h2>
            <el-button link type="primary">换一换</el-button>
          </header>
          <button
            v-for="suggestion in aiSuggestions"
            :key="suggestion.title"
            type="button"
            class="suggestion-row"
            @click="feedback = `${suggestion.title} 已加入待办`"
          >
            <el-icon><component :is="suggestion.icon" /></el-icon>
            <span
              ><strong>{{ suggestion.title }}</strong
              ><small>{{ suggestion.detail }}</small></span
            >
            <em
              >{{ suggestion.action }} <el-icon><ArrowRight /></el-icon
            ></em>
          </button>
        </section>
      </aside>
    </section>

    <div class="dashboard-wide-stack">
      <section class="dashboard-panel reminder-panel" data-testid="teaching-reminders">
        <header class="panel-heading">
          <h2>
            <el-icon><Bell /></el-icon>教学提醒
          </h2>
        </header>
        <ul>
          <li v-for="reminder in reminders" :key="reminder">{{ reminder }}</li>
        </ul>
      </section>

      <section class="smartbi-entry" data-testid="smartbi-entry">
        <div class="smartbi-intro">
          <span class="smartbi-icon"
            ><el-icon><TrendCharts /></el-icon
          ></span>
          <div>
            <h2>SmartBI 分析入口</h2>
            <p>多维数据分析，辅助教学决策</p>
          </div>
        </div>
        <div class="smartbi-links">
          <div>
            <el-icon><DataAnalysis /></el-icon
            ><span><strong>教育质量治理</strong><small>整体质量监测</small></span>
          </div>
          <div>
            <el-icon><Document /></el-icon
            ><span><strong>课程诊断</strong><small>课程与知识点分析</small></span>
          </div>
          <div>
            <el-icon><WarningFilled /></el-icon
            ><span><strong>学生风险</strong><small>风险识别与预警</small></span>
          </div>
          <div>
            <el-icon><CircleCheck /></el-icon
            ><span><strong>干预成效</strong><small>干预效果评估</small></span>
          </div>
        </div>
        <el-button
          type="primary"
          class="smartbi-open-button"
          :disabled="!smartBiBoards.length"
          @click="openSmartBi(smartBiBoards[0])"
          >打开SmartBI</el-button
        >
        <div class="smartbi-resources">
          <SmartBiResourceCard
            v-for="board in smartBiBoards"
            :key="board.id"
            :resource="board"
            @open="openSmartBi"
          />
        </div>
      </section>
    </div>

    <p v-if="feedback" class="feedback" data-testid="dashboard-feedback">{{ feedback }}</p>
    <span class="responsive-sentinel" data-testid="teacher-dashboard-responsive-1024"></span>
  </main>
</template>

<style scoped lang="scss">
.teacher-dashboard-page {
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  color: #17233c;
}
.dashboard-state {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 72px;
  margin-bottom: 14px;
  padding: 16px;
  border: 1px solid #cbd8eb;
  border-radius: 8px;
  background: #fff;
  color: #52637e;
}
.dashboard-state.error {
  border-color: #f3b4b4;
  color: #b42318;
}
.compat-dashboard-heading {
  margin: 0 0 4px;
  color: #142847;
  font-size: 26px;
  font-weight: 750;
}
.decision-heading h1 {
  margin-top: 0;
  font-size: 18px;
  color: #60718b;
}
.smartbi-open-button {
  margin-top: 12px;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.decision-heading {
  margin: 1px 0 12px;
}

.decision-heading h1,
.decision-heading p {
  margin: 0;
}

.decision-heading h1 {
  font-size: 25px;
  font-weight: 800;
  letter-spacing: -0.04em;
}

.decision-heading p {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  color: #63718b;
  font-size: 14px;
}

.decision-heading p i {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #9ba8bb;
}

.decision-heading p strong {
  color: #f04d51;
  font-weight: 800;
}

.dashboard-main-grid {
  display: grid;
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  grid-template-columns: minmax(0, 2fr) minmax(330px, 1fr);
  gap: 18px;
  align-items: start;
}

.dashboard-primary-column,
.dashboard-side-column {
  display: grid;
  width: 100%;
  align-content: start;
  gap: 16px;
  min-width: 0;
  box-sizing: border-box;
}

.primary-task,
.summary-strip,
.dashboard-panel,
.smartbi-entry {
  box-sizing: border-box;
  border: 1px solid #e2eaf5;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 6px 18px rgb(35 77 145 / 4%);
}

.dashboard-wide-stack {
  display: grid;
  width: 100%;
  min-width: 0;
  gap: 18px;
  margin-top: 18px;
  box-sizing: border-box;
}

.dashboard-wide-stack > * {
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
}

.primary-task {
  display: flex;
  min-height: 166px;
  align-items: center;
  gap: 24px;
  padding: 26px 30px;
  background: linear-gradient(115deg, #f6f9ff 0%, #fff 74%);
}

.primary-task-icon {
  display: grid;
  flex: 0 0 144px;
  width: 144px;
  height: 112px;
  place-items: center;
  border-radius: 50%;
  color: #2467e9;
  background: #eef4ff;
  font-size: 48px;
}

.primary-task-copy {
  min-width: 0;
}

.primary-task-label {
  display: inline-flex;
  padding: 5px 14px;
  border-radius: 6px;
  color: #fff;
  background: #1c5be7;
  font-size: 13px;
  font-weight: 700;
}

.primary-task h2 {
  margin: 10px 0 4px;
  color: #13264c;
  font-size: 28px;
  letter-spacing: -0.03em;
}

.primary-task p {
  margin: 0;
  color: #63718b;
  font-size: 14px;
}

.primary-task-action {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 7px;
  min-height: 48px;
  margin-left: auto;
  padding-inline: 22px;
  border: 0;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 700;
  box-shadow: 0 7px 15px rgb(27 91 233 / 20%);
}

.summary-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  min-height: 100px;
  padding: 8px 13px;
}

.summary-item {
  display: flex;
  position: relative;
  align-items: center;
  min-width: 0;
  gap: 10px;
  padding: 14px 14px 14px 5px;
  cursor: pointer;
}

.summary-item + .summary-item::before {
  position: absolute;
  top: 18px;
  bottom: 18px;
  left: -1px;
  width: 1px;
  background: #e3eaf4;
  content: '';
}

.summary-icon {
  display: grid;
  flex: 0 0 44px;
  width: 44px;
  height: 44px;
  place-items: center;
  border-radius: 10px;
  font-size: 23px;
}

.summary-item--blue .summary-icon {
  color: #1b64f2;
  background: #e9f1ff;
}
.summary-item--orange .summary-icon {
  color: #f88719;
  background: #fff2e3;
}
.summary-item--green .summary-icon {
  color: #20a46c;
  background: #e3f8ed;
}
.summary-item--purple .summary-icon {
  color: #7c63ec;
  background: #f0edff;
}

.summary-copy {
  display: grid;
  min-width: 0;
  gap: 1px;
}

.summary-copy span {
  color: #30415f;
  font-size: 13px;
  white-space: nowrap;
}

.summary-copy strong {
  color: #152645;
  font-size: 24px;
  line-height: 1.05;
}

.summary-copy strong small {
  margin-left: 3px;
  color: #5b6a83;
  font-size: 12px;
  font-weight: 500;
}

.summary-copy em {
  overflow: hidden;
  color: #8290a7;
  font-size: 11px;
  font-style: normal;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.summary-item :deep(.el-button) {
  flex: 0 0 auto;
  margin-left: auto;
  padding: 0;
  font-size: 11px;
  white-space: nowrap;
}

.dashboard-panel {
  overflow: hidden;
}

.panel-heading {
  display: flex;
  min-height: 48px;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 16px;
  border-bottom: 1px solid #edf1f7;
}

.panel-heading h2 {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #172a4d;
  font-size: 17px;
  font-weight: 750;
}

.panel-heading h2 > .el-icon {
  color: #2168f4;
  font-size: 19px;
}

.panel-heading h2 small {
  color: #8b98ad;
  font-size: 12px;
  font-weight: 400;
}

.panel-heading > span {
  color: #8793a8;
  font-size: 12px;
}

.todo-row,
.anomaly-row,
.suggestion-row {
  display: grid;
  width: 100%;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border: 0;
  border-bottom: 1px solid #edf1f7;
  color: #263653;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.todo-row:last-child,
.anomaly-row:last-child,
.suggestion-row:last-child {
  border-bottom: 0;
}
.todo-row {
  grid-template-columns: 22px minmax(0, 1fr) auto auto;
}
.todo-row:hover,
.anomaly-row:hover,
.suggestion-row:hover {
  background: #f8fbff;
}

.todo-icon {
  color: #2b70ec;
  font-size: 18px;
}
.todo-title {
  overflow: hidden;
  font-size: 14px;
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.todo-title small {
  color: #71819d;
  font-size: 12px;
  font-weight: 400;
}
.todo-action,
.row-link {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  color: #1b62ef;
  font-size: 13px;
  white-space: nowrap;
}

.anomaly-row {
  grid-template-columns: 9px minmax(135px, 0.45fr) minmax(0, 1fr) auto;
}
.anomaly-row strong {
  font-size: 14px;
}
.anomaly-row > span:not(.anomaly-dot):not(.row-link) {
  overflow: hidden;
  color: #71809b;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.anomaly-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.anomaly-dot--danger {
  background: #f04d51;
}
.anomaly-dot--warning {
  background: #ff9b21;
}

.report-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  padding: 12px 16px 16px;
}
.report-card {
  display: grid;
  min-height: 76px;
  gap: 8px;
  padding: 11px;
  border: 1px solid #e0e8f4;
  border-radius: 8px;
}
.report-card > strong {
  color: #2a3b5b;
  font-size: 13px;
  line-height: 1.4;
}
.report-card > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.report-card small {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  color: #8592a8;
  font-size: 11px;
}

.evidence-list {
  display: grid;
  gap: 2px;
  padding: 12px 16px 4px;
}
.evidence-item {
  display: grid;
  grid-template-columns: 24px 1fr auto;
  align-items: center;
  gap: 7px;
  min-height: 38px;
  color: #42516d;
  font-size: 14px;
}
.evidence-item > .el-icon {
  color: #617596;
  font-size: 18px;
}
.evidence-item strong {
  font-size: 14px;
}
.evidence-value--success {
  color: #21a168;
}
.evidence-value--warning {
  color: #f58b16;
}
.evidence-detail {
  width: calc(100% - 32px);
  margin: 8px 16px 16px;
  color: #1b62ef;
}

.suggestion-row {
  grid-template-columns: 25px minmax(0, 1fr) auto;
  padding-block: 12px;
}
.suggestion-row > .el-icon {
  color: #286df3;
  font-size: 20px;
}
.suggestion-row > span {
  display: grid;
  min-width: 0;
  gap: 3px;
}
.suggestion-row strong {
  overflow: hidden;
  color: #2a3b59;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.suggestion-row small {
  overflow: hidden;
  color: #8290a5;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.suggestion-row em {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  color: #1b62ef;
  font-size: 12px;
  font-style: normal;
  white-space: nowrap;
}

.reminder-panel ul {
  display: grid;
  gap: 10px;
  margin: 0;
  padding: 16px;
  list-style: none;
  color: #42516d;
  font-size: 13px;
  line-height: 1.45;
}
.reminder-panel li {
  position: relative;
  min-width: 0;
  overflow-wrap: anywhere;
}
.reminder-panel li::before {
  position: absolute;
  top: 0.65em;
  left: -10px;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #43536d;
  content: '';
}

.smartbi-entry {
  display: flex;
  min-height: 90px;
  align-items: center;
  gap: 24px;
  margin-top: 0;
  padding: 14px 26px;
}
.smartbi-intro {
  display: flex;
  flex: 0 0 260px;
  align-items: center;
  gap: 12px;
}
.smartbi-icon {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  border-radius: 10px;
  color: #fff;
  background: #2467ef;
  font-size: 24px;
}
.smartbi-intro h2 {
  margin: 0;
  color: #162744;
  font-size: 18px;
}
.smartbi-intro p {
  margin: 3px 0 0;
  color: #7a879e;
  font-size: 12px;
}
.smartbi-links {
  display: grid;
  flex: 1;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}
.smartbi-links > div {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 0 15px;
  border-left: 1px solid #e3eaf4;
}
.smartbi-links > div > .el-icon {
  color: #3184ed;
  font-size: 24px;
}
.smartbi-links span {
  display: grid;
  gap: 3px;
}
.smartbi-links strong {
  color: #2d3f5f;
  font-size: 13px;
}
.smartbi-links small {
  color: #8a96aa;
  font-size: 11px;
}
.smartbi-resources {
  display: none;
}

.feedback {
  margin: 12px 0 0;
  color: #1b62ef;
  font-size: 13px;
}
.responsive-sentinel {
  display: none;
}

@media (max-width: 1280px) {
  .dashboard-main-grid {
    grid-template-columns: minmax(0, 1.7fr) minmax(300px, 1fr);
  }
  .primary-task {
    padding-inline: 20px;
  }
  .primary-task-icon {
    flex-basis: 105px;
    width: 105px;
  }
  .primary-task h2 {
    font-size: 24px;
  }
}

@media (max-width: 1080px) {
  .dashboard-main-grid {
    grid-template-columns: 1fr;
  }
  .dashboard-side-column {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    align-items: start;
  }
  .smartbi-entry {
    align-items: flex-start;
    flex-wrap: wrap;
  }
  .smartbi-intro {
    flex-basis: auto;
  }
  .smartbi-links {
    flex-basis: 100%;
  }
}

@media (max-width: 760px) {
  .summary-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .summary-item:nth-child(3)::before {
    display: none;
  }
  .primary-task {
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 12px;
  }
  .primary-task-icon {
    flex-basis: 60px;
    width: 60px;
    height: 60px;
    font-size: 30px;
  }
  .primary-task-copy {
    flex: 1;
  }
  .primary-task-action {
    margin-left: 72px;
  }
  .report-grid {
    grid-template-columns: 1fr;
  }
  .dashboard-side-column {
    grid-template-columns: 1fr;
  }
  .dashboard-wide-stack {
    gap: 12px;
  }
  .smartbi-links {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px 0;
  }
  .smartbi-links > div:nth-child(3) {
    border-left: 0;
  }
}
</style>
