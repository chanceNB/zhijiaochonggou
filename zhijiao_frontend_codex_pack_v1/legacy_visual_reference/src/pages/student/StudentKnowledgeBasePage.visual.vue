<!-- VISUAL REFERENCE ONLY.
Source: src/pages/student/StudentKnowledgeBasePage.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="student-knowledge-base-page" data-test="knowledge-base-page">
    <div
      data-test="responsive-sentinel"
      class="knowledge-base-responsive-1024"
      aria-hidden="true"
    />

    <div v-if="store.loading" data-test="loading-state" class="state-card">正在加载知识库...</div>
    <div v-else-if="store.error" data-test="error-state" class="state-card error">
      <strong>{{ store.error }}</strong>
      <button data-test="retry-load" type="button" @click="store.loadKnowledgeBase()">重试</button>
    </div>
    <div v-else-if="store.isEmpty" data-test="empty-state" class="state-card empty">
      <strong>还没有学习资料</strong>
      <span>{{ store.emptyMessage }}</span>
      <UploadMaterialPanel
        v-if="!store.readOnly"
        :uploading="store.uploading"
        :latest-upload="latestUpload"
        @upload="uploadMaterial"
      />
    </div>

    <div v-else data-test="success-state" class="knowledge-layout">
      <header class="knowledge-header">
        <div class="header-copy">
          <p class="eyebrow">AI 教练的专属资料空间</p>
          <div class="title-line">
            <h1>知识库</h1>
            <span class="header-status"
              ><i />{{ store.summary.readyCount }} 份资料可供 AI 引用</span
            >
          </div>
          <p>
            {{
              store.readOnly
                ? '查看教师或管理员授权的课程资料，让 AI 教练使用可靠来源。'
                : '上传并管理你的学习资料，让 AI 教练更懂你的课程内容。'
            }}
          </p>
        </div>
        <label class="knowledge-search" for="knowledge-search">
          <el-icon aria-hidden="true"><Search /></el-icon>
          <input
            id="knowledge-search"
            v-model="searchTerm"
            placeholder="搜索资料、章节或知识点..."
          />
          <button v-if="searchTerm" type="button" aria-label="清空搜索" @click="clearSearch">
            <el-icon><Close /></el-icon>
          </button>
        </label>
      </header>

      <KnowledgeSummary :summary="store.summary" />

      <section class="source-quality" data-test="v3-source-quality">
        <header>
          <p>资料质量</p>
          <h2>保证每一份资料都能被准确引用</h2>
        </header>
        <article data-test="v3-coverage">
          <span>知识覆盖</span
          ><strong>{{ store.summary.readyCount }} 份可用资料覆盖课程核心内容。</strong>
        </article>
        <article data-test="v3-citation-hit">
          <span>引用命中</span
          ><strong>{{ store.summary.citeableCount }} 份资料允许 AI 教练引用。</strong>
        </article>
        <article data-test="v3-source-trust">
          <span>可信状态</span><strong>解析失败或处理中的资料不会进入高置信度回答。</strong>
        </article>
      </section>

      <div class="toolbar" data-test="knowledge-filters">
        <label class="filter-search" for="material-search">
          <el-icon aria-hidden="true"><Search /></el-icon>
          <input
            id="material-search"
            v-model="searchTerm"
            placeholder="搜索资料、章节或知识点..."
          />
          <button v-if="searchTerm" type="button" aria-label="清空筛选搜索" @click="clearSearch">
            <el-icon><Close /></el-icon>
          </button>
        </label>
        <label class="filter-select" for="material-type-filter">
          <span>资料类型</span>
          <select id="material-type-filter" v-model="typeFilter" data-test="material-type-filter">
            <option value="all">全部类型</option>
            <option v-for="option in typeOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
          <el-icon aria-hidden="true"><ArrowDown /></el-icon>
        </label>
        <label class="filter-select" for="material-status-filter">
          <span>资料状态</span>
          <select
            id="material-status-filter"
            v-model="statusFilter"
            data-test="material-status-filter"
          >
            <option value="all">全部状态</option>
            <option v-for="option in statusOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
          <el-icon aria-hidden="true"><ArrowDown /></el-icon>
        </label>
        <button
          class="reset-button"
          type="button"
          :disabled="!hasActiveFilters"
          @click="resetFilters"
        >
          <el-icon><Refresh /></el-icon>重置
        </button>
        <p class="filter-summary" data-test="filter-summary">{{ filterSummary }}</p>
      </div>

      <div class="main-grid">
        <div class="left-stack">
          <UploadMaterialPanel
            v-if="!store.readOnly"
            :uploading="store.uploading"
            :latest-upload="latestUpload"
            @upload="uploadMaterial"
          />
          <MaterialTable
            :materials="filteredMaterials"
            :total-count="store.materials.length"
            :read-only="store.readOnly"
            :selected-material-id="store.selectedMaterialId"
            @select="store.selectMaterial"
            @toggle-citation="toggleCitation"
            @preview="handleMaterialAction('preview', $event)"
            @chapter="handleMaterialAction('chapter', $event)"
          />
          <section class="health-card" data-test="knowledge-health">
            <header>
              <div>
                <p class="health-eyebrow">资料健康度</p>
                <h2>让每一次 AI 引用都有可靠来源</h2>
                <span>根据当前解析状态，优先处理异常资料即可提升引用覆盖。</span>
              </div>
              <strong class="health-score">{{ citationCoverage }}<small>%</small></strong>
            </header>
            <div class="health-metrics">
              <div>
                <span>可引用资料</span>
                <strong>{{ store.summary.citeableCount }}</strong>
              </div>
              <div>
                <span>处理中</span>
                <strong>{{ store.summary.parsingCount }}</strong>
              </div>
              <div>
                <span>解析失败</span>
                <strong class="risk">{{ store.summary.failedCount }}</strong>
              </div>
            </div>
            <footer>
              <span v-if="failedMaterial">建议先处理「{{ failedMaterial.name }}」</span>
              <span v-else>当前资料状态良好，可以继续学习。</span>
              <button
                v-if="failedMaterial"
                data-test="locate-failed-material"
                type="button"
                @click="locateFailedMaterial"
              >
                定位资料
              </button>
            </footer>
          </section>
        </div>
        <aside class="right-stack">
          <div ref="previewPanelRef" class="preview-panel-anchor">
            <MaterialPreviewTree
              :nodes="store.activePreviewTree"
              :material="store.selectedMaterial"
            />
          </div>
          <KnowledgeBaseSettings
            v-if="!store.readOnly"
            :settings="store.settings"
            :saving="store.savingSettings"
            :saved="store.settingsSaved"
            @save="saveSettings"
          />
          <div v-else class="state-card" data-test="student-knowledge-read-only">
            学生端仅可查看教师或管理员授权的真实资料。
          </div>
          <section v-if="!store.readOnly" class="citation-card" data-test="citation-history">
            <header>
              <h2>引用记录</h2>
              <button type="button">查看全部</button>
            </header>
            <p>AI 教练在“一次函数斜率怎么理解？”中引用了本资料。</p>
            <time>2025-05-22 18:35</time>
            <p>定向刷题生成中使用了本章内容。</p>
            <time>2025-05-20 16:12</time>
          </section>
          <section v-else class="citation-card" data-test="citation-history-restricted">
            <h2>引用记录</h2>
            <p>引用日志仅对教师和管理员开放，学生端不伪造引用历史。</p>
          </section>
          <section data-test="fallback-answer" class="fallback-card">
            <strong>{{ store.fallbackAnswer.citationLabel }}</strong>
            <span>{{ store.fallbackAnswer.message }}</span>
          </section>
          <p v-if="store.actionFeedback" data-test="action-feedback" class="action-feedback">
            {{ store.actionFeedback }}
          </p>
        </aside>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.student-knowledge-base-page {
  min-height: 100%;
  padding: 16px 20px 34px;
  color: #17233f;
  background: #f8faff;
}
.knowledge-base-responsive-1024 {
  display: none;
}
.knowledge-layout {
  display: grid;
  gap: 14px;
  padding-bottom: 20px;
}
.knowledge-header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 460px);
  align-items: center;
  gap: 22px;
  padding-bottom: 13px;
  border-bottom: 1px solid #e1e8f3;
}
.header-copy {
  min-width: 0;
}
.title-line {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}
.knowledge-header h1 {
  margin: 3px 0 5px;
  color: #13234b;
  font-size: 28px;
}
.header-status {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  padding: 5px 9px;
  border: 1px solid #cfe5d7;
  border-radius: 999px;
  color: #2d8150;
  background: #f1fbf4;
  font-size: 11px;
  font-weight: 700;
}
.header-status i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
}
.knowledge-header p {
  margin: 0;
  color: #667691;
  line-height: 1.55;
}
.knowledge-header .eyebrow {
  color: #2e6be8;
  font-size: 12px;
  font-weight: 800;
}
.knowledge-search,
.filter-search {
  display: grid;
  grid-template-columns: 22px minmax(0, 1fr) 28px;
  align-items: center;
  min-height: 40px;
  padding: 0 11px;
  border: 1px solid #cdd9e9;
  border-radius: 8px;
  background: #fff;
}
.knowledge-search > .el-icon,
.filter-search > .el-icon {
  color: #58729b;
  font-size: 16px;
}
.knowledge-search input,
.filter-search input {
  min-width: 0;
  border: 0;
  outline: 0;
  background: transparent;
  color: #283e64;
}
.knowledge-search span,
.filter-search span {
  color: #536b8f;
  font-size: 20px;
}
.knowledge-search button,
.filter-search button {
  display: grid;
  width: 28px;
  height: 28px;
  place-items: center;
  border: 0;
  border-radius: 6px;
  color: #6f82a1;
  background: transparent;
  cursor: pointer;
}
.knowledge-search button:hover,
.filter-search button:hover {
  background: #eef4ff;
  color: #2f6be9;
}
.source-quality {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) repeat(3, minmax(0, 1fr));
  gap: 10px;
  padding: 13px;
  border: 1px solid #dbe5f2;
  border-radius: 9px;
  background: #fff;
}
.source-quality header,
.source-quality article {
  display: grid;
  gap: 5px;
  min-width: 0;
}
.source-quality p {
  margin: 0;
  color: #2d6be8;
  font-size: 12px;
  font-weight: 800;
}
.source-quality h2 {
  margin: 0;
  color: #162b53;
  font-size: 16px;
}
.source-quality article {
  padding-left: 12px;
  border-left: 1px solid #e7edf5;
}
.source-quality article span {
  color: #75839a;
  font-size: 12px;
}
.source-quality article strong {
  color: #2f4468;
  font-size: 12px;
  line-height: 1.5;
}
.toolbar {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) 170px 170px auto;
  gap: 10px;
  align-items: center;
  padding: 10px;
  border: 1px solid #dbe5f2;
  border-radius: 9px;
  background: #fff;
}
.toolbar > .filter-select,
.toolbar > button {
  min-height: 38px;
  border: 1px solid #d7e1ef;
  border-radius: 7px;
  color: #314a71;
  background: #fff;
  cursor: pointer;
}
.filter-select {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 22px;
  align-items: center;
  padding: 0 9px 0 11px;
}
.filter-select span {
  overflow: hidden;
  color: #314a71;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.filter-select select {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  padding: 0 32px 0 11px;
  border: 0;
  outline: 0;
  opacity: 0;
  cursor: pointer;
}
.filter-select .el-icon {
  grid-column: 2;
  color: #6f82a1;
  pointer-events: none;
}
.toolbar > button {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  justify-content: center;
  padding: 0 12px;
  font-size: 12px;
}
.toolbar .reset-button {
  border-color: transparent;
  color: #2e6be8;
}
.toolbar .reset-button:disabled {
  color: #a9b5c6;
  cursor: not-allowed;
}
.filter-summary {
  grid-column: 1 / -1;
  margin: -2px 2px 0;
  color: #7b899f;
  font-size: 11px;
}
.main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.65fr) minmax(300px, 0.75fr);
  gap: 20px;
  align-items: start;
}
.left-stack,
.right-stack {
  display: grid;
  gap: 18px;
  min-width: 0;
}
.left-stack > *,
.right-stack > * {
  width: 100%;
  min-width: 0;
}
.preview-panel-anchor {
  min-width: 0;
}
.main-grid :deep(.upload-panel),
.main-grid :deep(.material-table),
.main-grid :deep(.preview-tree),
.main-grid :deep(.settings-panel),
.main-grid :deep(.citation-card),
.main-grid :deep(.fallback-card),
.health-card {
  border-radius: 12px;
  box-shadow: 0 8px 24px rgb(37 61 99 / 6%);
}
.health-card {
  display: grid;
  gap: 16px;
  padding: 18px;
  border: 1px solid #dbe5f2;
  background: #fff;
}
.health-card header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}
.health-card header > div {
  min-width: 0;
}
.health-eyebrow {
  margin: 0 0 4px;
  color: #2f6be8;
  font-size: 12px;
  font-weight: 800;
}
.health-card h2 {
  margin: 0;
  color: #172e59;
  font-size: 17px;
}
.health-card header span:not(.health-eyebrow) {
  display: block;
  margin-top: 5px;
  color: #71829c;
  font-size: 12px;
  line-height: 1.5;
}
.health-score {
  flex: 0 0 auto;
  color: #2f6be8;
  font-size: 32px;
  line-height: 1;
}
.health-score small {
  margin-left: 2px;
  font-size: 14px;
}
.health-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}
.health-metrics div {
  display: grid;
  gap: 5px;
  padding: 11px 12px;
  border: 1px solid #e4ebf4;
  border-radius: 8px;
  background: #f9fbfe;
}
.health-metrics span {
  color: #7a8aa2;
  font-size: 11px;
}
.health-metrics strong {
  color: #213b68;
  font-size: 22px;
}
.health-metrics strong.risk {
  color: #d25357;
}
.health-card footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #edf1f6;
  color: #657894;
  font-size: 12px;
}
.health-card footer button {
  min-height: 32px;
  padding: 0 12px;
  border: 1px solid #bed1f2;
  border-radius: 7px;
  color: #2f6be8;
  background: #f3f7ff;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.citation-card {
  display: grid;
  gap: 8px;
  padding: 15px;
  border: 1px solid #dbe5f2;
  border-radius: 9px;
  background: #fff;
}
.citation-card header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.citation-card h2 {
  margin: 0;
  color: #1e3158;
  font-size: 16px;
}
.citation-card button {
  border: 0;
  color: #2f6be8;
  background: transparent;
  cursor: pointer;
}
.citation-card p {
  margin: 0;
  color: #536987;
  font-size: 12px;
  line-height: 1.5;
}
.citation-card time {
  color: #9aa7b9;
  font-size: 11px;
}
.fallback-card,
.action-feedback {
  display: grid;
  gap: 7px;
  padding: 13px;
  border: 1px solid #f2d28e;
  border-radius: 8px;
  color: #895e17;
  background: #fffaf0;
}
.fallback-card strong {
  color: #aa6e08;
}
.fallback-card span {
  color: #7f6b46;
  line-height: 1.5;
}
.action-feedback {
  border-color: #abdcb9;
  color: #20764a;
  background: #f1fbf4;
}
.state-card {
  display: grid;
  gap: 12px;
  min-height: 220px;
  place-items: center;
  padding: 30px;
  border: 1px solid #d7e4f2;
  border-radius: 10px;
  background: #fff;
  color: #5e6f89;
  text-align: center;
}
.state-card button {
  min-height: 38px;
  padding: 0 16px;
  border: 0;
  border-radius: 7px;
  color: #fff;
  background: #2f74ee;
  cursor: pointer;
}
.state-card.error {
  color: #b42318;
}
.state-card.empty {
  place-items: stretch;
}
@media (max-width: 1120px) {
  .main-grid {
    grid-template-columns: 1fr;
  }
  .right-stack {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 1024px) {
  .student-knowledge-base-page {
    padding: 12px 14px 28px;
  }
  .knowledge-base-responsive-1024 {
    display: block;
  }
  .knowledge-header,
  .source-quality,
  .right-stack {
    grid-template-columns: 1fr;
  }
  .toolbar {
    grid-template-columns: minmax(0, 1fr) 150px 150px auto;
  }
}
@media (max-width: 700px) {
  .knowledge-header {
    align-items: stretch;
  }
  .knowledge-search {
    width: 100%;
  }
  .source-quality {
    grid-template-columns: 1fr 1fr;
  }
  .source-quality header {
    grid-column: 1 / -1;
  }
  .source-quality article {
    padding: 0;
    border-top: 1px solid #e7edf5;
    border-left: 0;
    padding-top: 10px;
  }
  .toolbar {
    grid-template-columns: 1fr 1fr;
  }
  .filter-search,
  .filter-summary {
    grid-column: 1 / -1;
  }
  .toolbar .reset-button {
    min-width: 0;
  }
  .health-card header,
  .health-card footer {
    align-items: flex-start;
    flex-direction: column;
  }
  .health-score {
    align-self: flex-end;
    margin-top: -42px;
  }
  .health-card footer button {
    width: 100%;
  }
}
</style>
