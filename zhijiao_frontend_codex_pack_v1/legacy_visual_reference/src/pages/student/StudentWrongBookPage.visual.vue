<!-- VISUAL REFERENCE ONLY.
Source: src/pages/student/StudentWrongBookPage.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <div class="student-wrong-book-page" data-test="wrong-book-page">
    <div class="wrong-book-responsive-1024" data-test="responsive-sentinel" aria-hidden="true" />

    <header class="page-header">
      <div class="page-header__brand">
        <div class="page-header__icon" aria-hidden="true"><Box /></div>
        <div>
          <h1>错题本</h1>
          <p>你的专属薄弱点库，针对性复盘，精准提升。</p>
        </div>
      </div>
      <label class="page-search" for="wrong-book-search">
        <input
          id="wrong-book-search"
          v-model="searchTerm"
          type="search"
          placeholder="搜索题目、知识点或错因..."
        />
        <span class="search-icon" aria-hidden="true" />
      </label>
    </header>

    <section v-if="isLoading" class="state-card state-card--loading" data-test="wrong-book-loading">
      <span class="state-card__sr-only">正在加载错题本...</span>
      <div class="state-skeleton" aria-hidden="true">
        <span />
        <span />
        <span />
      </div>
    </section>
    <section
      v-else-if="practice.wrongBookState === 'error'"
      class="state-card"
      data-test="wrong-book-error"
    >
      <h2>错题本数据加载失败</h2>
      <p>{{ practice.wrongBookError }}</p>
      <button data-test="wrong-book-reload" type="button" @click="reload">重试</button>
    </section>
    <section v-else-if="practice.isWrongBookEmpty" class="state-card" data-test="wrong-book-empty">
      <h2>还没有错题</h2>
      <p>{{ practice.wrongBookData?.emptyMessage }}</p>
      <button data-test="wrong-book-reload" type="button" @click="reload">重新加载</button>
    </section>

    <template v-else>
      <section class="filter-bar" data-test="wrong-book-filters" aria-label="错题筛选">
        <span class="filter-label">学科</span>
        <div class="filter-control">
          <button
            type="button"
            :aria-expanded="openFilter === 'subject'"
            aria-haspopup="listbox"
            data-test="wrong-book-filter-subject"
            @click="toggleFilter('subject')"
          >
            <span>{{ filterValues.subject }}</span
            ><i class="select-chevron" aria-hidden="true" />
          </button>
          <div v-if="openFilter === 'subject'" class="filter-menu" role="listbox">
            <button
              v-for="option in filterOptions.subject"
              :key="option"
              type="button"
              role="option"
              :aria-selected="filterValues.subject === option"
              @click="setFilter('subject', option)"
            >
              {{ option }}
            </button>
          </div>
        </div>

        <span class="filter-label">知识点</span>
        <div class="filter-control">
          <button
            type="button"
            :aria-expanded="openFilter === 'knowledge'"
            aria-haspopup="listbox"
            data-test="wrong-book-filter-knowledge"
            @click="toggleFilter('knowledge')"
          >
            <span>{{ filterValues.knowledge }}</span
            ><i class="select-chevron" aria-hidden="true" />
          </button>
          <div v-if="openFilter === 'knowledge'" class="filter-menu" role="listbox">
            <button
              v-for="option in filterOptions.knowledge"
              :key="option"
              type="button"
              role="option"
              :aria-selected="filterValues.knowledge === option"
              @click="setFilter('knowledge', option)"
            >
              {{ option }}
            </button>
          </div>
        </div>

        <span class="filter-label">错因</span>
        <div class="filter-control">
          <button
            type="button"
            :aria-expanded="openFilter === 'cause'"
            aria-haspopup="listbox"
            data-test="wrong-book-filter-cause"
            @click="toggleFilter('cause')"
          >
            <span>{{ filterValues.cause }}</span
            ><i class="select-chevron" aria-hidden="true" />
          </button>
          <div v-if="openFilter === 'cause'" class="filter-menu" role="listbox">
            <button
              v-for="option in filterOptions.cause"
              :key="option"
              type="button"
              role="option"
              :aria-selected="filterValues.cause === option"
              @click="setFilter('cause', option)"
            >
              {{ option }}
            </button>
          </div>
        </div>

        <span class="filter-label">状态</span>
        <div class="filter-control">
          <button
            type="button"
            :aria-expanded="openFilter === 'status'"
            aria-haspopup="listbox"
            data-test="wrong-book-filter-status"
            @click="toggleFilter('status')"
          >
            <span>{{ filterValues.status }}</span
            ><i class="select-chevron" aria-hidden="true" />
          </button>
          <div v-if="openFilter === 'status'" class="filter-menu" role="listbox">
            <button
              v-for="option in filterOptions.status"
              :key="option"
              type="button"
              role="option"
              :aria-selected="filterValues.status === option"
              @click="setFilter('status', option)"
            >
              {{ option }}
            </button>
          </div>
        </div>

        <button
          class="filter-reset"
          data-test="wrong-book-reset"
          type="button"
          @click="resetFilters"
        >
          <RefreshRight class="refresh-icon" aria-hidden="true" />重置
        </button>
      </section>

      <section class="diagnosis-summary" data-test="v3-misconception-map">
        <article class="summary-focus" data-test="v3-misconception-node">
          <p class="summary-eyebrow">课程全地图&nbsp;·&nbsp;当前主题误区</p>
          <h2>边界条件遗漏 <span>主要错因</span></h2>
          <p>近 3 次错题均涉及头节点/尾节点的特殊情况处理</p>
          <span class="compat-copy">误概念地图 · 置信度 82%</span>
        </article>
        <article class="summary-segment" data-test="v3-transfer-validation">
          <span class="summary-icon summary-icon--success" aria-hidden="true"
            ><CircleCheckFilled
          /></span>
          <div>
            <h3>迁移验证 <em>未验证</em></h3>
            <p>在其他相似场景中尚未验证通过，建议尽快验证。</p>
            <button type="button" @click="verificationRecordsOpen = true">
              查看验证记录 <b>›</b>
            </button>
          </div>
        </article>
        <article class="summary-segment">
          <span class="summary-icon summary-icon--primary" aria-hidden="true"><PieChart /></span>
          <div>
            <h3>掌握状态</h3>
            <p>完成纠正练习后，跨场景验证即可解锁。</p>
          </div>
        </article>
      </section>

      <button
        class="mobile-knowledge-trigger"
        data-test="mobile-knowledge-trigger"
        type="button"
        @click="knowledgeDrawerOpen = true"
      >
        打开知识点分类
      </button>
      <button class="mobile-reason-trigger" type="button" @click="reasonDrawerOpen = true">
        查看错误原因
      </button>

      <section
        class="wrong-book-main"
        :class="{
          'is-sidebar-collapsed': sidebarCollapsed,
          'is-mobile-detail-open': mobileDetailOpen,
        }"
        data-test="wrong-book-main"
      >
        <aside class="sidebar-column">
          <KnowledgeTree
            :nodes="practice.wrongBookData?.knowledgeTree ?? []"
            :selected-id="practice.selectedKnowledgeId"
            :collapsed="sidebarCollapsed"
            @select="selectKnowledge"
            @update:collapsed="sidebarCollapsed = $event"
          />
          <section v-if="!sidebarCollapsed" class="summary-card">
            <h2>本知识点统计</h2>
            <dl>
              <div>
                <dt>错题数</dt>
                <dd>{{ practice.wrongBookData?.summary.totalWrong ?? 0 }}</dd>
              </div>
              <div>
                <dt>知识点</dt>
                <dd>{{ practice.wrongBookData?.summary.knowledgeCount ?? 0 }}</dd>
              </div>
              <div>
                <dt>待验证</dt>
                <dd>{{ practice.wrongBookData?.summary.needsReview ?? 0 }}</dd>
              </div>
            </dl>
          </section>
        </aside>

        <main class="workspace-column">
          <div class="question-list-view">
            <WrongQuestionList
              :items="visibleWrongBookItems"
              :selected-id="practice.selectedWrongBookItemId"
              :loading="loadingFlags.filterLoading || loadingFlags.questionListLoading"
              @select="selectQuestion"
            />
            <section
              v-if="!selectedItem"
              class="filtered-empty"
              data-test="wrong-book-filter-empty"
            >
              <strong>没有匹配的错题</strong>
              <span>请调整搜索关键词或筛选条件。</span>
            </section>
          </div>
          <section v-if="selectedItem" class="question-detail" data-test="wrong-question-detail">
            <button
              class="mobile-detail-back"
              data-test="mobile-detail-back"
              type="button"
              @click="mobileDetailOpen = false"
            >
              <span aria-hidden="true">‹</span>返回错题列表
            </button>
            <header class="question-detail__header">
              <div>
                <span class="question-index"
                  >错题 {{ selectedIndex + 1 }}/{{ visibleWrongBookItems.length }}</span
                >
                <h2>{{ selectedItem.title }}</h2>
              </div>
              <span
                >来源：{{ selectedItem.questionCode }} · 错误次数：{{
                  selectedItem.errorCount
                }}
                次</span
              >
            </header>
            <p class="question-description">{{ selectedQuestionDetail?.description }}</p>
            <section class="answer-comparison">
              <div>
                <h3>你的答案 <em>（错误）</em></h3>
                <p>{{ selectedQuestionDetail?.studentAnswer }}</p>
                <code>{{ selectedQuestionDetail?.studentResult }}</code>
              </div>
              <div>
                <h3>正确答案</h3>
                <p>{{ selectedQuestionDetail?.correctAnswer }}</p>
                <code>{{ selectedQuestionDetail?.correctResult }}</code>
              </div>
            </section>
            <div class="tag-row">
              <span v-for="tag in selectedQuestionDetail?.tags" :key="tag">{{ tag }}</span>
            </div>
            <section class="correction-steps">
              <h3>纠错解析（步骤）</h3>
              <ol>
                <li
                  v-for="(step, index) in selectedQuestionDetail?.analysisSteps"
                  :key="step.title"
                >
                  <b>{{ index + 1 }}</b>
                  <strong>{{ step.title }}</strong>
                  <span>{{ step.content }}</span>
                </li>
              </ol>
            </section>
          </section>
        </main>

        <aside class="reason-column">
          <WrongBookReasonWorkspace
            :item="selectedItem"
            :loading="loadingFlags.reasonLoading"
            :action-feedback="practice.wrongBookActionFeedback"
            :action-error="actionError"
            :similar-preview="practice.wrongBookSimilarPreview"
            :busy-action="activeAction"
            @open-review="openReviewSuggestion"
            @retry="handleRetry"
            @similar="handleSimilar"
            @knowledge-only="handleKnowledgeOnly"
            @mark-mastered="handleMarkMastered"
            @open-verification-records="verificationRecordsOpen = true"
            @open-verification="verificationOpen = true"
          />
        </aside>
      </section>
    </template>

<style scoped lang="scss">
.student-wrong-book-page {
  --page-bg: #f6f8fc;
  --card-bg: #fff;
  --primary: #2563eb;
  --text: #14213d;
  --muted: #64748b;
  --border: #dfe7f2;
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 14px;
  width: 100%;
  min-width: 0;
  margin: 0;
  padding: 16px 20px 28px;
  color: var(--text);
  background: var(--page-bg);
}

.wrong-book-responsive-1024 {
  display: none;
}

.page-header {
  display: flex;
  min-height: 54px;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.page-header__brand {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 14px;
}

.page-header__icon {
  display: grid;
  width: 50px;
  height: 50px;
  flex: 0 0 50px;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  background: linear-gradient(145deg, #2f74f4, #1454dc);
  box-shadow: 0 7px 16px rgb(37 99 235 / 18%);
}

.page-header__icon svg {
  width: 25px;
  height: 25px;
}

.page-header h1 {
  margin: 0;
  color: var(--text);
  font-size: 24px;
  line-height: 1.25;
}

.page-header p {
  margin: 5px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.page-search {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 22px;
  width: min(324px, 36vw);
  min-height: 42px;
  align-items: center;
  gap: 8px;
  padding: 0 12px 0 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
}

.page-search input {
  min-width: 0;
  border: 0;
  outline: 0;
  color: var(--text);
  background: transparent;
  font-size: 13px;
}

.page-search input::placeholder {
  color: #94a3b8;
}

.search-icon {
  position: relative;
  width: 14px;
  height: 14px;
  border: 1.7px solid #1f3a70;
  border-radius: 50%;
}

.search-icon::after {
  position: absolute;
  right: -5px;
  bottom: -2px;
  width: 6px;
  border-top: 1.7px solid #1f3a70;
  content: '';
  transform: rotate(45deg);
}

.filter-bar {
  display: grid;
  grid-template-columns:
    auto minmax(145px, 170px) auto minmax(160px, 190px) auto minmax(175px, 215px)
    auto minmax(145px, 170px) 1fr;
  gap: 10px;
  align-items: center;
  padding: 13px 14px;
  border: 1px solid var(--border);
  border-radius: 9px;
  background: #fff;
}

.filter-label {
  color: #475569;
  font-size: 13px;
  font-weight: 700;
}

.filter-control {
  position: relative;
  min-width: 0;
}

.filter-control > button {
  display: flex;
  min-height: 36px;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  padding: 0 11px;
  border: 1px solid #d5dfec;
  border-radius: 7px;
  color: #334d75;
  background: #fff;
  font-size: 13px;
  text-align: left;
  cursor: pointer;
}

.filter-control > button:hover,
.filter-control > button:focus-visible {
  border-color: #8fb3ee;
  background: #f9fbff;
}

.filter-control > button:focus-visible,
.filter-reset:focus-visible,
.modal-card button:focus-visible {
  outline: 3px solid rgb(37 99 235 / 18%);
  outline-offset: 2px;
}

.select-chevron {
  width: 7px;
  height: 7px;
  border-right: 1.5px solid currentColor;
  border-bottom: 1.5px solid currentColor;
  transform: rotate(45deg) translateY(-2px);
}

.filter-menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  left: 0;
  z-index: 20;
  display: grid;
  gap: 2px;
  max-height: 260px;
  overflow-y: auto;
  padding: 6px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 26px rgb(30 60 110 / 12%);
}

.filter-menu button {
  min-height: 32px;
  padding: 0 8px;
  border: 1px solid transparent;
  border-radius: 6px;
  color: #496486;
  background: #fff;
  font-size: 12px;
  text-align: left;
  cursor: pointer;
}

.filter-menu button[aria-selected='true'],
.filter-menu button:hover {
  border-color: #d5e3ff;
  color: #265fc8;
  background: #eef4ff;
}

.filter-reset {
  justify-self: end;
  min-height: 36px;
  padding: 0 8px;
  border: 0;
  color: var(--primary);
  background: transparent;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.refresh-icon {
  display: inline-block;
  width: 17px;
  height: 17px;
  margin-right: 4px;
  vertical-align: -4px;
}

.diagnosis-summary {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr;
  min-height: 102px;
  align-items: stretch;
  padding: 14px 0;
  border: 1px solid var(--border);
  border-radius: 9px;
  background: #fff;
}

.diagnosis-summary article {
  min-width: 0;
  padding: 0 16px;
}

.diagnosis-summary article + article {
  border-left: 1px solid #e7edf5;
}

.summary-focus {
  display: grid;
  align-content: center;
  gap: 4px;
}

.summary-eyebrow {
  color: var(--primary);
  font-size: 12px;
  font-weight: 800;
}

.summary-focus h2 {
  margin: 2px 0 0;
  color: var(--text);
  font-size: 18px;
}

.summary-focus h2 span {
  margin-left: 7px;
  padding: 3px 7px;
  border-radius: 6px;
  color: #7c3aed;
  background: #f2eafe;
  font-size: 10px;
  font-weight: 700;
  vertical-align: middle;
}

.summary-focus > p:last-of-type,
.summary-segment p {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.55;
}

.summary-segment {
  display: flex;
  align-items: center;
  gap: 10px;
}

.summary-icon {
  display: grid;
  width: 32px;
  height: 32px;
  flex: 0 0 32px;
  place-items: center;
  border-radius: 8px;
  font-weight: 800;
}

.summary-icon--success {
  color: #16a34a;
  background: #eaf8ef;
}

.summary-icon--primary {
  color: #2563eb;
  background: #edf4ff;
}

.summary-icon svg {
  width: 17px;
  height: 17px;
}

.summary-segment h3 {
  margin: 0 0 5px;
  color: var(--text);
  font-size: 14px;
}

.summary-segment h3 em {
  margin-left: 6px;
  padding: 3px 7px;
  border-radius: 6px;
  color: #16a34a;
  background: #eaf8ef;
  font-size: 10px;
  font-style: normal;
  font-weight: 700;
}

.summary-segment button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0;
  border: 0;
  color: var(--primary);
  background: transparent;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.summary-segment button b {
  font-size: 16px;
  font-weight: 400;
}

.wrong-book-main {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr) 360px;
  gap: 12px;
  align-items: stretch;
}

.wrong-book-main.is-sidebar-collapsed {
  grid-template-columns: 56px minmax(0, 1fr) 360px;
}

.sidebar-column,
.workspace-column,
.reason-column {
  display: grid;
  align-content: start;
  gap: 12px;
  min-width: 0;
}

.sidebar-column {
  grid-template-rows: minmax(0, 1fr) auto;
}

.workspace-column {
  grid-template-rows: auto minmax(0, 1fr);
}

.question-list-view {
  display: grid;
  gap: 12px;
  min-width: 0;
}

.summary-card,
.question-detail,
.state-card,
.filtered-empty {
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
}

.summary-card h2 {
  margin: 0;
  color: var(--text);
  font-size: 16px;
}

dl {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin: 13px 0 0;
}

dl div {
  display: grid;
  gap: 4px;
  padding: 6px 3px;
  border-right: 1px solid #e4ebf4;
  text-align: center;
}

dl div:last-child {
  border-right: 0;
}

dt {
  color: #7e8ba2;
  font-size: 11px;
}

dd {
  margin: 0;
  color: var(--primary);
  font-size: 22px;
  font-weight: 800;
}

.question-detail {
  display: grid;
  gap: 13px;
  padding: 16px;
  min-height: 0;
}

.mobile-detail-back {
  display: none;
  min-height: 38px;
  align-items: center;
  justify-self: start;
  gap: 6px;
  padding: 0 10px;
  border: 1px solid #c7d8f3;
  border-radius: 7px;
  color: var(--primary);
  background: #fff;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.mobile-detail-back span {
  font-size: 20px;
  line-height: 1;
}

.question-detail__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.question-detail__header > div {
  min-width: 0;
}

.question-detail__header > span {
  flex: 0 0 auto;
  color: var(--muted);
  font-size: 11px;
  white-space: nowrap;
}

.question-index {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 5px;
  color: #fff;
  background: var(--primary);
  font-size: 11px;
  font-weight: 700;
}

.question-detail h2 {
  margin: 7px 0 0;
  color: var(--text);
  font-size: 17px;
  line-height: 1.45;
}

.question-description {
  margin: 0;
  color: #475569;
  font-size: 13px;
  line-height: 1.65;
}

.answer-comparison {
  display: grid;
  grid-template-columns: 1fr 1fr;
  border: 1px solid var(--border);
  border-radius: 7px;
}

.answer-comparison > div {
  display: grid;
  align-content: start;
  gap: 7px;
  min-width: 0;
  padding: 13px;
}

.answer-comparison > div + div {
  border-left: 1px solid var(--border);
}

.answer-comparison h3 {
  margin: 0;
  color: #1e3a66;
  font-size: 14px;
}

.answer-comparison h3 em {
  color: #ef4444;
  font-style: normal;
}

.answer-comparison p,
.answer-comparison code {
  margin: 0;
  color: var(--muted);
  font-family: inherit;
  font-size: 12px;
  line-height: 1.55;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-row span {
  padding: 4px 8px;
  border-radius: 6px;
  color: #2563eb;
  background: #edf4ff;
  font-size: 11px;
}

.correction-steps {
  padding-top: 10px;
  border-top: 1px solid #edf1f6;
}

.correction-steps h3 {
  margin: 0 0 9px;
  color: var(--text);
  font-size: 15px;
}

.correction-steps ol {
  display: grid;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.correction-steps li {
  display: grid;
  grid-template-columns: 24px 72px minmax(0, 1fr);
  gap: 8px;
  align-items: start;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.55;
}

.correction-steps b {
  display: grid;
  width: 22px;
  height: 22px;
  place-items: center;
  border-radius: 50%;
  color: var(--primary);
  background: #edf4ff;
}

.correction-steps strong {
  color: #334d75;
}

.filtered-empty {
  display: grid;
  gap: 6px;
  color: var(--muted);
  text-align: center;
}

.filtered-empty strong {
  color: var(--text);
}

.modal-primary:hover {
  background: #1d4ed8;
}

.state-card {
  display: grid;
  min-height: 220px;
  place-items: center;
  gap: 10px;
  color: var(--muted);
  text-align: center;
}

.state-card h2 {
  margin: 0;
  color: var(--text);
}

.state-card p {
  margin: 0;
}

.state-card button {
  min-height: 36px;
  padding: 0 17px;
  border: 0;
  border-radius: 6px;
  color: #fff;
  background: var(--primary);
  cursor: pointer;
}

.state-skeleton {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr) 360px;
  gap: 12px;
  width: 100%;
}

.state-skeleton span {
  height: 160px;
  border: 1px solid #e5ebf4;
  border-radius: 8px;
  background: linear-gradient(90deg, #f6f8fc, #fff, #f6f8fc);
  background-size: 220% 100%;
  animation: skeleton-shimmer 1.2s ease-in-out infinite;
}

.state-card__sr-only,
.compat-copy {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
}

.mobile-knowledge-trigger,
.mobile-reason-trigger {
  display: none;
  min-height: 38px;
  padding: 0 12px;
  border: 1px solid #c7d8f3;
  border-radius: 7px;
  color: var(--primary);
  background: #fff;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.drawer-backdrop,
.dialog-backdrop {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  justify-content: flex-end;
  background: rgb(15 23 42 / 28%);
}

.dialog-backdrop {
  align-items: center;
  justify-content: center;
}

.side-drawer,
.records-drawer {
  display: grid;
  align-content: start;
  gap: 14px;
  width: min(420px, 92vw);
  height: 100%;
  overflow-y: auto;
  padding: 18px;
  background: #f6f8fc;
  box-shadow: -12px 0 30px rgb(15 23 42 / 12%);
}

.side-drawer > header,
.records-drawer > header,
.modal-card > header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.side-drawer h2,
.records-drawer h2,
.modal-card h2 {
  margin: 0;
  color: var(--text);
  font-size: 18px;
}

.side-drawer > header button,
.records-drawer > header button,
.modal-card > header button {
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: 6px;
  color: #64748b;
  background: transparent;
  font-size: 22px;
  cursor: pointer;
}

.modal-card {
  display: grid;
  gap: 16px;
  width: min(440px, calc(100vw - 32px));
  padding: 22px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 20px 48px rgb(15 23 42 / 18%);
}

.modal-card > p {
  margin: 0;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.65;
}

.modal-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.modal-primary,
.modal-secondary,
.modal-link {
  min-height: 38px;
  border-radius: 7px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.modal-primary {
  border: 1px solid var(--primary);
  color: #fff;
  background: var(--primary);
}

.modal-secondary {
  border: 1px solid #cbd8eb;
  color: #475569;
  background: #fff;
}

.modal-link {
  border: 0;
  color: var(--primary);
  background: transparent;
}

.settings-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin: 0;
}

.settings-list div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 11px;
  border: 1px solid #e5ebf4;
  border-radius: 7px;
}

.settings-list dd {
  color: var(--text);
  font-size: 13px;
}

.records-table {
  display: grid;
  border: 1px solid var(--border);
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.records-head,
.records-row {
  display: grid;
  grid-template-columns: 1.5fr 0.55fr 0.75fr 0.75fr;
  gap: 8px;
  padding: 10px;
  font-size: 11px;
}

.records-head {
  color: #475569;
  background: #f3f7fd;
  font-weight: 700;
}

.records-row {
  border-top: 1px solid #edf1f6;
  color: var(--muted);
}

.records-note {
  margin: 0;
  padding: 10px;
  border-radius: 7px;
  color: #64748b;
  background: #fff;
  font-size: 12px;
}

@keyframes skeleton-shimmer {
  to {
    background-position: -120% 0;
  }
}

@media (max-width: 1439.98px) {
  .wrong-book-main {
    grid-template-columns: 250px minmax(0, 1fr) 330px;
  }

  .wrong-book-main.is-sidebar-collapsed {
    grid-template-columns: 56px minmax(0, 1fr) 330px;
  }

  .state-skeleton {
    grid-template-columns: 250px minmax(0, 1fr) 330px;
  }
}

@media (max-width: 1199.98px) {
  .wrong-book-main {
    grid-template-columns: 250px minmax(0, 1fr);
  }

  .wrong-book-main.is-sidebar-collapsed {
    grid-template-columns: 56px minmax(0, 1fr);
  }

  .reason-column {
    display: none;
  }

  .mobile-reason-trigger {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    justify-self: end;
  }

  .state-skeleton {
    grid-template-columns: 250px minmax(0, 1fr);
  }

  .state-skeleton span:last-child {
    display: none;
  }
}

@media (max-width: 991.98px) {
  .filter-bar {
    grid-template-columns: auto minmax(0, 1fr) auto minmax(0, 1fr);
  }

  .filter-reset {
    grid-column: 4;
  }

  .wrong-book-main,
  .wrong-book-main.is-sidebar-collapsed {
    grid-template-columns: minmax(0, 1fr);
  }

  .sidebar-column {
    display: none;
  }

  .mobile-knowledge-trigger {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    justify-self: start;
  }

  .diagnosis-summary {
    grid-template-columns: 1fr;
    gap: 0;
    padding: 0;
  }

  .diagnosis-summary article {
    min-height: 92px;
    padding: 14px 16px;
  }

  .diagnosis-summary article + article {
    border-top: 1px solid #e7edf5;
    border-left: 0;
  }

  .state-skeleton {
    grid-template-columns: minmax(0, 1fr);
  }

  .state-skeleton span:not(:first-child) {
    display: none;
  }
}

@media (max-width: 767.98px) {
  .student-wrong-book-page {
    gap: 12px;
    padding: 12px 12px 20px;
  }

  .page-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  .page-search {
    width: 100%;
  }

  .filter-bar {
    grid-template-columns: auto minmax(0, 1fr);
    gap: 8px;
  }

  .filter-reset {
    grid-column: 1 / -1;
    justify-self: start;
  }

  .wrong-book-main:not(.is-mobile-detail-open) .question-detail {
    display: none;
  }

  .wrong-book-main.is-mobile-detail-open .question-list-view {
    display: none;
  }

  .mobile-detail-back {
    display: inline-flex;
  }

  .question-detail__header {
    flex-direction: column;
  }

  .question-detail__header > span {
    white-space: normal;
  }

  .answer-comparison {
    grid-template-columns: 1fr;
  }

  .answer-comparison > div + div {
    border-top: 1px solid var(--border);
    border-left: 0;
  }

  .correction-steps li {
    grid-template-columns: 24px minmax(0, 1fr);
  }

  .correction-steps li > span {
    grid-column: 2;
  }

  .settings-list,
  .modal-actions {
    grid-template-columns: 1fr;
  }

  .records-head,
  .records-row {
    grid-template-columns: 1.3fr 0.6fr 0.75fr 0.7fr;
    padding-inline: 8px;
    font-size: 10px;
  }
}
</style>
