<!-- VISUAL REFERENCE ONLY.
Source: src/pages/teacher/TeacherCourseKnowledgeBasePage.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <main class="teacher-knowledge-base-page" data-testid="teacher-knowledge-base-page">
    <header class="page-heading">
      <div class="heading-copy">
        <div class="heading-icon" aria-hidden="true">KB</div>
        <div>
          <h1>课程知识库</h1>
          <p>治理资源质量、知识点覆盖、AI 引用表现与版本影响</p>
        </div>
      </div>
    </header>
    <section v-if="loading" class="state-panel" data-testid="knowledge-loading">
      正在加载课程知识库…
    </section>
    <section v-else-if="error" class="state-panel error-state" data-testid="knowledge-error">
      <strong>{{ error }}</strong
      ><el-button data-testid="knowledge-retry" @click="knowledgeStore.loadWorkspace()"
        >重试</el-button
      >
    </section>
    <section v-else-if="isEmpty" class="state-panel" data-testid="knowledge-empty">
      {{ workspace?.emptyMessage }}
    </section>
    <div v-else-if="workspace" data-testid="knowledge-success" class="knowledge-content">
      <section class="filter-bar panel">
        <label class="filter-field"
          ><span>课程选择</span><el-input v-model="courseFilter" data-testid="course-filter"
        /></label>
        <label class="filter-field"
          ><span>文件类型</span
          ><el-select model-value="全部类型"
            ><el-option label="全部类型" value="全部类型" /></el-select
        ></label>
        <label class="filter-field"
          ><span>质量等级</span
          ><el-select model-value="全部"><el-option label="全部" value="全部" /></el-select
        ></label>
        <label class="filter-field filter-search"
          ><span>资源搜索</span><el-input placeholder="搜索文件名称、关键词"
        /></label>
        <el-button class="filter-advanced" plain>高级筛选</el-button>
        <span class="active-filter" data-testid="active-filter">当前筛选：{{ courseFilter }}</span>
      </section>
      <section class="kpi-grid" aria-label="知识库指标">
        <article
          v-for="(kpi, index) in kpis"
          :key="kpi.label"
          class="panel kpi-card"
          :class="{ 'kpi-extra': index > 2 }"
          data-testid="resource-kpi"
        >
          <div class="kpi-label">{{ kpi.label }}</div>
          <strong>{{ kpi.value }}</strong
          ><span>{{ kpi.meta }}</span
          ><i class="kpi-mark" aria-hidden="true">{{
            index === 2 ? 'AI' : index === 1 ? '质量' : '资源'
          }}</i>
        </article>
      </section>
      <aside class="directory-compat" aria-hidden="true">
        <button
          v-for="directory in directories"
          :key="directory.id"
          type="button"
          data-testid="directory-node"
          :data-directory-id="directory.id"
          :class="{ active: selectedDirectoryId === directory.id }"
          @click="knowledgeStore.selectDirectory(directory.id)"
        >
          {{ directory.title }} <strong>{{ directory.count }}</strong>
        </button>
      </aside>
      <section class="workspace-grid">
        <div class="main-stack">
          <section class="toolbar panel">
            <el-button type="primary" data-testid="upload-resource" @click="uploadResource"
              >上传资料</el-button
            ><el-button plain>新建文件夹</el-button><el-button plain>批量操作⌄</el-button
            ><el-button plain>标签管理</el-button><el-button plain>刷新</el-button>
            <input
              ref="uploadInput"
              data-testid="teacher-knowledge-file"
              type="file"
              accept=".txt,.md,.pdf,.docx,text/plain,text/markdown,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
              hidden
              @change="uploadResourceFile"
            />
          </section>
          <section class="resource-table panel" data-testid="resource-table">
            <table>
              <thead>
                <tr>
                  <th>文件名称</th>
                  <th>类型</th>
                  <th>质量分</th>
                  <th>知识点覆盖</th>
                  <th>AI 引用命中率</th>
                  <th>最新版本</th>
                  <th>新鲜度</th>
                  <th>可见范围</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="resource in visibleResources"
                  :key="resource.id"
                  data-testid="resource-row"
                  :data-resource-id="resource.id"
                  :class="{ selected: selectedResource?.id === resource.id }"
                  @click="knowledgeStore.selectResource(resource.id)"
                >
                  <td class="resource-name">
                    <span class="file-icon">PDF</span><strong>{{ resource.name }}</strong>
                  </td>
                  <td>{{ resource.type }}</td>
                  <td>{{ resource.score ?? '未提供' }}</td>
                  <td>{{ resource.coverage }}</td>
                  <td>{{ resource.hitRate }}</td>
                  <td>{{ resource.version }}</td>
                  <td>{{ resource.freshness }}</td>
                  <td>{{ resource.visibility }}</td>
                  <td>
                    <el-tag :type="resource.status === '已发布' ? 'success' : 'info'">{{
                      resource.status
                    }}</el-tag>
                  </td>
                </tr>
              </tbody>
            </table>
            <footer class="table-footer">
              <span>共 {{ visibleResources.length }} 条</span>
              <div>
                <el-button circle>‹</el-button><el-button type="primary" circle>1</el-button
                ><el-button circle>›</el-button
                ><el-select model-value="20 条/页" size="small"
                  ><el-option label="20 条/页" value="20 条/页"
                /></el-select>
              </div>
            </footer>
          </section>
          <section class="panel governance-panel" data-testid="resource-governance-overview">
            <div class="section-title governance-heading">
              <div>
                <h2>资源治理概览</h2>
                <p>把质量问题转成下一步可执行的资料任务</p>
              </div>
              <span>当前课程</span>
            </div>
            <div class="governance-metrics">
              <article
                v-for="metric in governanceMetrics"
                :key="metric.label"
                class="governance-metric"
                :data-tone="metric.tone"
              >
                <div class="governance-metric-label">{{ metric.label }}</div>
                <strong>{{ metric.value }}</strong>
                <div class="governance-meter" aria-hidden="true">
                  <span :style="{ width: metric.progress }"></span>
                </div>
                <small>{{ metric.meta }}</small>
              </article>
            </div>
            <div class="governance-actions">
              <div class="governance-actions-heading">
                <h3>待处理事项</h3>
                <span>{{ governanceTasks.length }} 项</span>
              </div>
              <ul v-if="governanceTasks.length" class="governance-task-list">
                <li
                  v-for="task in governanceTasks"
                  :key="task.key"
                  class="governance-task"
                  :data-testid="`governance-task-${task.key}`"
                >
                  <span
                    class="governance-task-dot"
                    :data-tone="task.tone"
                    aria-hidden="true"
                  ></span>
                  <div class="governance-task-copy">
                    <strong>{{ task.title }}</strong>
                    <span>{{ task.description }}</span>
                  </div>
                  <el-button
                    link
                    type="primary"
                    :data-testid="`governance-task-action-${task.key}`"
                    @click="focusGovernanceResource(task.resourceId, task.feedback)"
                  >
                    查看资料
                  </el-button>
                </li>
              </ul>
              <p v-else class="governance-empty">当前课程没有待处理的资料任务。</p>
            </div>
          </section>
        </div>
        <aside class="side-stack">
          <section v-if="selectedResource" class="panel selected-resource">
            <div class="resource-head">
              <div class="resource-title">
                <span class="file-icon large">PDF</span>
                <div>
                  <h2>{{ selectedResource.name }}</h2>
                  <small
                    >{{ selectedResource.type }} · {{ selectedResource.version }} · 更新于
                    {{ selectedResource.freshness }}</small
                  ><small class="resource-id" data-testid="selected-resource-id"
                    >ID: {{ selectedResource.id }}</small
                  >
                </div>
              </div>
              <el-tag type="success">{{ selectedResource.status }}</el-tag>
            </div>
            <nav class="detail-tabs" data-testid="detail-tabs">
              <button
                v-for="tab in Object.keys(tabLabels) as TeacherKnowledgeDetailTab[]"
                :key="tab"
                type="button"
                :data-testid="'detail-tab-' + tab"
                :class="{ active: activeTab === tab }"
                @click="knowledgeStore.selectTab(tab)"
              >
                {{ tabLabels[tab] }}
              </button>
            </nav>
            <strong class="active-tab-label" data-testid="active-detail-tab">{{
              tabLabels[activeTab]
            }}</strong>
            <div v-if="activeTab === 'details'" class="detail-content">
              <p>{{ selectedResource.preview }}</p>
              <p>
                质量分：{{ selectedResource.score ?? '未提供' }} · 版本：{{
                  selectedResource.version
                }}
              </p>
              <el-button
                v-if="realKnowledgeMode"
                type="danger"
                plain
                data-testid="delete-real-resource"
                :loading="saving"
                @click="deleteResource"
                >删除资料</el-button
              >
            </div>
            <div v-else-if="activeTab === 'citations'" class="detail-content">
              <p v-for="log in selectedResource.citationLogs" :key="log">{{ log }}</p>
              <p v-if="!selectedResource.citationLogs.length">暂无引用日志</p>
            </div>
            <div v-else-if="activeTab === 'versions'" class="detail-content">
              <p v-for="impact in selectedResource.versionImpact" :key="impact">{{ impact }}</p>
            </div>
            <div v-else class="detail-content"><p>可查看资源的引用权限与学生可见范围。</p></div>
          </section>
          <section v-if="selectedResource" class="panel detail-panel" data-testid="version-impact">
            <div class="section-title"><h2>版本影响分析</h2></div>
            <div v-if="!realKnowledgeMode" class="impact-grid">
              <article><strong>128</strong><span>引用次数</span></article>
              <article><strong>42</strong><span>答案引用</span></article>
              <article><strong>28</strong><span>诊断规则</span></article>
              <article><strong>16</strong><span>报告模板</span></article>
            </div>
            <div v-else class="detail-content">
              <p v-for="impact in selectedResource.versionImpact" :key="impact">{{ impact }}</p>
            </div>
            <p class="panel-note">
              {{
                realKnowledgeMode
                  ? '仅展示服务端返回的版本、解析任务与结果引用。'
                  : '版本更新可能影响 AI 回答、诊断结果与报告生成。'
              }}
            </p>
          </section>
          <section v-if="selectedResource" class="panel detail-panel">
            <div class="section-title">
              <h2>质量评估（{{ selectedResource.version }}）</h2>
              <strong class="score"
                >{{ selectedResource.score ?? '未提供'
                }}<template v-if="selectedResource.score != null"> 分</template>

<style scoped lang="scss">
.teacher-knowledge-base-page {
  min-height: calc(100vh - 110px);
  color: #16233c;
}
.page-heading {
  display: flex;
  align-items: center;
  margin: 8px 0 20px;
}
.heading-copy {
  display: flex;
  align-items: center;
  gap: 14px;
}
.heading-icon {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 11px;
  background: #edf3ff;
  color: #2f66eb;
  font-size: 23px;
}
h1,
h2,
h3,
p {
  margin: 0;
}
h1 {
  font-size: 27px;
  letter-spacing: -0.02em;
}
h2 {
  font-size: 16px;
}
.heading-copy p {
  margin-top: 5px;
  color: #7a879d;
  font-size: 13px;
}
.panel {
  border: 1px solid #e5eaf2;
  border-radius: 9px;
  background: #fff;
  box-shadow: 0 8px 24px rgb(30 64 120 / 4%);
}
.filter-bar {
  display: grid;
  grid-template-columns: 1.1fr 1fr 0.9fr 1.2fr auto;
  gap: 14px;
  align-items: end;
  padding: 16px;
}
.filter-field {
  display: grid;
  gap: 6px;
  color: #69758a;
  font-size: 12px;
}
.filter-field .el-input,
.filter-field .el-select {
  width: 100%;
}
.filter-advanced {
  align-self: end;
  height: 36px;
}
.active-filter {
  grid-column: 1/-1;
  color: #8b98ac;
  font-size: 12px;
}
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin: 14px 0;
}
.kpi-card {
  position: relative;
  display: grid;
  min-height: 107px;
  gap: 7px;
  padding: 18px 20px;
  overflow: hidden;
}
.kpi-label {
  color: #6f7c91;
  font-size: 13px;
}
.kpi-card strong {
  color: #16233c;
  font-size: 29px;
  line-height: 1;
}
.kpi-card span {
  color: #6f7c91;
  font-size: 12px;
}
.kpi-mark {
  position: absolute;
  right: 19px;
  top: 42px;
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 10px;
  background: #eef4ff;
  color: #3568e8;
  font-style: normal;
  font-size: 13px;
  font-weight: 800;
}
.kpi-extra {
  display: none;
}
.directory-compat {
  display: none;
}
.workspace-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(335px, 0.75fr);
  gap: 16px;
  align-items: start;
}
.main-stack,
.side-stack {
  display: grid;
  gap: 14px;
  min-width: 0;
}
.toolbar {
  display: flex;
  gap: 9px;
  padding: 11px 13px;
}
.resource-table {
  overflow: hidden;
}
.resource-table table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}
.resource-table th,
.resource-table td {
  padding: 13px 11px;
  border-bottom: 1px solid #edf0f5;
  white-space: nowrap;
  text-align: left;
}
.resource-table th {
  background: #f8faff;
  color: #66748a;
  font-weight: 700;
}
.resource-table td {
  color: #45546c;
}
.resource-table tbody tr {
  cursor: pointer;
  transition: background 0.15s ease;
}
.resource-table tbody tr:hover,
.resource-table tbody tr.selected {
  background: #f1f6ff;
}
.resource-name {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1f2e46 !important;
}
.file-icon {
  display: inline-grid;
  width: 23px;
  height: 27px;
  place-items: center;
  border-radius: 4px;
  background: #f15c56;
  color: #fff;
  font-size: 8px;
  font-weight: 800;
}
.file-icon.large {
  width: 30px;
  height: 34px;
  font-size: 9px;
}
.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 11px 14px;
  color: #6d7c93;
  font-size: 12px;
}
.table-footer > div {
  display: flex;
  align-items: center;
  gap: 6px;
}
.governance-panel {
  overflow: hidden;
}
.governance-heading {
  align-items: flex-start;
}
.governance-heading h2 {
  color: #253552;
}
.governance-heading p {
  margin-top: 4px;
  color: #8290a4;
  font-size: 11px;
  font-weight: 400;
}
.governance-heading > span {
  color: #8a97a9;
  font-size: 11px;
}
.governance-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  padding: 15px 17px 0;
}
.governance-metric {
  display: grid;
  min-width: 0;
  gap: 6px;
  padding: 12px;
  border: 1px solid #e9eef6;
  border-radius: 8px;
  background: #fbfcff;
}
.governance-metric-label {
  overflow: hidden;
  color: #69778d;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.governance-metric strong {
  color: #253552;
  font-size: 22px;
  line-height: 1;
}
.governance-metric[data-tone='ai'] strong {
  color: #168e68;
}
.governance-metric[data-tone='warning'] strong {
  color: #c47b17;
}
.governance-metric small {
  overflow: hidden;
  color: #8a97a9;
  font-size: 10px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.governance-meter {
  height: 4px;
  overflow: hidden;
  border-radius: 999px;
  background: #e8eef8;
}
.governance-meter span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #3568e8;
}
.governance-metric[data-tone='ai'] .governance-meter span {
  background: #25a36b;
}
.governance-metric[data-tone='warning'] .governance-meter span {
  background: #e3a13b;
}
.governance-actions {
  margin: 15px 17px 17px;
  padding-top: 13px;
  border-top: 1px solid #edf0f5;
}
.governance-actions-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.governance-actions-heading h3 {
  color: #3e4d65;
  font-size: 13px;
}
.governance-actions-heading span {
  color: #8a97a9;
  font-size: 11px;
}
.governance-task-list {
  padding: 0;
  margin: 6px 0 0;
  list-style: none;
}
.governance-task {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 9px;
  align-items: center;
  padding: 9px 0;
  border-top: 1px solid #f0f3f7;
}
.governance-task:first-child {
  border-top: 0;
}
.governance-task-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #3568e8;
}
.governance-task-dot[data-tone='ai'] {
  background: #25a36b;
}
.governance-task-dot[data-tone='warning'] {
  background: #e3a13b;
}
.governance-task-copy {
  display: grid;
  min-width: 0;
  gap: 3px;
}
.governance-task-copy strong,
.governance-task-copy span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.governance-task-copy strong {
  color: #46546b;
  font-size: 12px;
}
.governance-task-copy span {
  color: #8a97a9;
  font-size: 10px;
}
.governance-task .el-button {
  min-height: 30px;
  padding: 4px 0 4px 8px;
  font-size: 11px;
}
.governance-empty {
  padding-top: 9px;
  color: #8a97a9;
  font-size: 11px;
}
.resource-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 17px;
}
.resource-title {
  display: flex;
  gap: 10px;
  align-items: center;
  min-width: 0;
}
.resource-title h2 {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.resource-id {
  display: none;
}
.resource-title small {
  display: block;
  margin-top: 5px;
  color: #8a95a8;
}
.detail-tabs {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  border-top: 1px solid #edf0f5;
  border-bottom: 1px solid #edf0f5;
}
.detail-tabs button {
  position: relative;
  padding: 12px 4px;
  border: 0;
  background: transparent;
  color: #79859a;
  cursor: pointer;
  font-size: 12px;
}
.detail-tabs button.active {
  color: #2f66eb;
  font-weight: 700;
}
.detail-tabs button.active::after {
  position: absolute;
  right: 12px;
  bottom: -1px;
  left: 12px;
  height: 2px;
  background: #2f66eb;
  content: '';
}
.active-tab-label {
  display: block;
  padding: 12px 17px 0;
  color: #283952;
  font-size: 13px;
}
.detail-content {
  min-height: 60px;
  padding: 8px 17px 17px;
  color: #6d7b91;
  line-height: 1.7;
  font-size: 12px;
}
.detail-content p + p {
  margin-top: 7px;
}
.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 17px;
  border-bottom: 1px solid #edf0f5;
}
.impact-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  padding: 15px 17px 11px;
}
.impact-grid article {
  display: grid;
  gap: 5px;
  padding: 11px 8px;
  border-radius: 7px;
  background: #f4f7fc;
  text-align: center;
}
.impact-grid strong {
  font-size: 19px;
  color: #253552;
}
.impact-grid span,
.panel-note {
  color: #7a879c;
  font-size: 11px;
}
.panel-note {
  padding: 0 17px 16px;
  line-height: 1.6;
}
.score {
  color: #25a36b;
}
.quality-lines {
  padding: 7px 17px 15px;
}
.quality-lines p {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 5px;
  padding: 6px 0;
  border-bottom: 1px solid #f0f2f6;
  color: #68758a;
  font-size: 12px;
}
.quality-lines p:last-child {
  border: 0;
}
.quality-lines b {
  color: #485a74;
  font-weight: 500;
}
.quality-lines b.positive {
  color: #21a167;
}
.quality-lines i {
  grid-column: 1/-1;
  display: block;
  height: 3px;
  width: 90%;
  border-radius: 3px;
  background: #2db16e;
}
.detail-panel > p {
  padding: 9px 17px 0;
  color: #69778e;
  font-size: 12px;
}
.permission-switches {
  display: grid;
  gap: 9px;
  padding: 11px 17px 16px;
}
.permission-switches label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #56657b;
  font-size: 12px;
}
.feedback {
  margin-top: 12px;
  color: #2f66eb;
  font-size: 13px;
}
.state-panel {
  display: grid;
  min-height: 180px;
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
@media (max-width: 1260px) {
  .filter-bar {
    grid-template-columns: repeat(3, 1fr);
  }
  .filter-search {
    grid-column: span 2;
  }
  .filter-advanced {
    grid-column: span 1;
  }
  .workspace-grid {
    grid-template-columns: 1fr;
  }
  .side-stack {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .selected-resource {
    grid-column: 1/-1;
  }
}
@media (max-width: 860px) {
  .filter-bar,
  .kpi-grid,
  .side-stack {
    grid-template-columns: 1fr;
  }
  .filter-search,
  .filter-advanced {
    grid-column: auto;
  }
  .workspace-grid,
  .main-stack,
  .side-stack {
    min-width: 0;
    grid-template-columns: minmax(0, 1fr);
  }
  .main-stack > *,
  .side-stack > * {
    width: 100%;
    min-width: 0;
    max-width: 100%;
    box-sizing: border-box;
  }
  .toolbar {
    flex-wrap: wrap;
  }
  .governance-metrics {
    grid-template-columns: 1fr;
  }
  .governance-task {
    grid-template-columns: auto minmax(0, 1fr);
  }
  .governance-task .el-button {
    grid-column: 2;
    justify-self: start;
    padding-left: 0;
  }
  .resource-table {
    width: 100%;
    min-width: 0;
    max-width: 100%;
    overflow: hidden;
  }
  .resource-table table {
    width: 100%;
    min-width: 0;
    table-layout: fixed;
  }
  .resource-table th,
  .resource-table td {
    overflow: hidden;
    padding: 10px 6px;
    text-overflow: ellipsis;
    white-space: normal;
  }
  .resource-table th:nth-child(n + 4):nth-child(-n + 8),
  .resource-table td:nth-child(n + 4):nth-child(-n + 8) {
    display: none;
  }
}
</style>
