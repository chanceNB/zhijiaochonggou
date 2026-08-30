<!-- VISUAL REFERENCE ONLY.
Source: src/pages/student/StudentPracticePage.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <div class="student-practice-page" data-test="practice-page">
    <div class="practice-responsive-1024" data-test="responsive-sentinel" aria-hidden="true" />
    <section class="practice-hero">
      <div>
        <p class="eyebrow">精准定位薄弱点，针对性练习，高效提升</p>
        <h1>定向刷题</h1>
        <p>精准定位薄弱点，针对性练习，让每一次练习都更有效。</p>
      </div>
      <form class="practice-search" @submit.prevent="submitPracticeSearch">
        <input
          id="practice-search"
          v-model="searchQuery"
          aria-label="搜索知识点、题目、资料..."
          placeholder="搜索知识点、题目、资料..."
        />
        <button type="submit" aria-label="提交定向刷题搜索">
          <el-icon><Search /></el-icon>
        </button>
      </form>
    </section>
    <p
      v-if="routeWorkflowView !== 'workspace'"
      data-test="practice-route-state"
      class="route-state"
      :data-workflow-view="routeWorkflowView"
    >
      练习状态 · {{ routeWorkflowView }}<span v-if="routePracticeId"> · {{ routePracticeId }}</span>
    </p>
    <div v-if="isInitialLoading" class="state-card" data-test="practice-loading">
      正在准备你的练习空间...
    </div>
    <section v-else-if="practice.error" class="state-card error" data-test="practice-error">
      <h2>加载失败</h2>
      <p>{{ practice.error }}</p>
      <button type="button" @click="practice.loadPractice('success')">重试</button>
    </section>
    <section
      v-if="isHistoryView"
      class="practice-state-view practice-history-view"
      data-test="practice-history-view"
    >
      <header class="state-view-header">
        <div>
          <span class="eyebrow">练习记录</span>
          <h2>练习历史</h2>
          <p>查看最近完成的定向练习和掌握变化。</p>
        </div>
        <button type="button" class="state-view-action" @click="router.push('/student/practice')">
          创建练习
        </button>
      </header>
      <div class="history-list">
        <article
          v-for="item in practice.data?.preview?.questions.slice(0, 3) ?? []"
          :key="item.id"
          class="history-item"
        >
          <div>
            <strong>{{ item.order }}. {{ item.targetAbility }}</strong>
            <p>{{ item.stem }}</p>
          </div>
          <span class="status-chip success">已完成</span>
          <button
            type="button"
            @click="
              router.push({
                name: 'student-practice-result',
                params: { practiceId: routePracticeId || 'history' },
              })
            "
          >
            查看结果
          </button>
        </article>
      </div>
      <el-empty v-if="!practice.data?.preview?.questions.length" description="暂无练习记录" />
    </section>
    <section
      v-else-if="isResultView"
      class="practice-state-view practice-result-view"
      data-test="practice-result-view"
    >
      <header class="state-view-header">
        <div>
          <span class="eyebrow">练习结果</span>
          <h2>本次练习已完成</h2>
          <p>根据答题结果更新错题本与掌握度。</p>
        </div>
        <button type="button" class="state-view-action" @click="router.push('/student/practice')">
          再练一次
        </button>
      </header>
      <div class="result-summary-grid">
        <article>
          <span>完成题数</span><strong>{{ practice.preview?.summary.totalCount ?? 4 }}</strong
          ><small>题</small>
        </article>
        <article>
          <span>预计用时</span
          ><strong>{{ practice.preview?.summary.estimatedMinutes ?? 18 }}</strong
          ><small>分钟</small>
        </article>
        <article>
          <span>掌握变化</span><strong class="success-text">+8%</strong><small>较上次</small>
        </article>
      </div>
      <section class="result-next-step">
        <h3>下一步建议</h3>
        <p>继续完成相似题，验证知识点能否迁移到新情境。</p>
        <button type="button" @click="router.push('/student/wrong-book')">查看错题本</button>
      </section>
    </section>
    <p
      v-if="isResultView && practice.actionFeedback"
      class="action-feedback"
      data-test="practice-action-feedback"
    >
      {{ practice.actionFeedback }}
    </p>
    <template v-else-if="!isHistoryView && !isResultView">
      <div
        v-if="isPreviewView || isAnswerView"
        class="workflow-banner"
        data-test="practice-workflow-banner"
      >
        <strong>{{ isPreviewView ? '练习预览' : '开始作答' }}</strong>
        <span>{{ routePracticeId || '当前练习' }}</span>
      </div>
      <section v-if="practice.isEmpty" class="state-card empty" data-test="practice-empty">
        <h2>还没有练习组</h2>
        <p>{{ practice.data?.emptyMessage }}</p>
      </section>
      <section class="v3-practice-states" aria-label="练习流程">
        <article data-test="v3-practice-diagnosis">
          <span>01</span><strong>薄弱点诊断</strong>
          <p>定位薄弱知识点</p>
          <small>完成</small><i class="compat-copy">诊断</i>
        </article>
        <article data-test="v3-practice-correction">
          <span>02</span><strong>误概念纠正</strong>
          <p>针对误概念强化练习</p>
          <small>进行中</small><i class="compat-copy">纠正</i>
        </article>
        <article data-test="v3-practice-transfer">
          <span>03</span><strong>迁移验证</strong>
          <p>在新情境中验证掌握情况</p>
          <small>{{
            v3Domain.domain?.transferValidation.status === 'passed' ? '已通过' : '待解锁'
          }}</small
          ><i class="compat-copy">迁移验证</i>
        </article>
      </section>
      <section class="practice-flow">
        <PracticeInputPanel
          :quick-knowledge-points="practice.data?.quickKnowledgePoints ?? []"
          :loading="practice.recognitionState === 'loading'"
          :upload-progress="practice.uploadProgress"
          @submit="submitInput"
        /><RecognitionResultCard
          :recognition="practice.recognition"
          :state="practice.recognitionState"
        /><GenerationSettings
          v-model="localSettings"
          :loading="practice.generationState === 'loading'"
          @generate="generate"
          @add-today="addToTodayTask"
        /><PracticePreview
          :preview="practice.preview"
          :loading="practice.generationState === 'loading'"
        />
      </section>
      <p
        v-if="practice.actionFeedback"
        class="action-feedback"
        data-test="practice-action-feedback"
      >
        {{ practice.actionFeedback }}
      </p>
      <AnswerWorkspace
        :preview="practice.preview"
        :active-index="practice.activeQuestionIndex"
        :feedback="practice.answerFeedback"
        :submitting="practice.answerState === 'loading'"
        @submit="submitAnswer"
        @next="practice.nextQuestion"
        @generate-similar="generateSimilarQuestion"
        @complete="completePractice"
      />
    </template>

<style scoped lang="scss">
.student-practice-page {
  display: grid;
  gap: 21px;
  width: min(1350px, calc(100% - 48px));
  min-height: 100%;
  margin: 0 auto;
  padding: 20px 0 22px;
  color: #1d3154;
  background: transparent;
}
.practice-responsive-1024 {
  display: none;
}
.practice-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 470px);
  gap: 22px;
  align-items: center;
  padding: 0 6px 15px;
}
.eyebrow {
  margin: 0 0 2px;
  color: #2f6de9;
  font-size: 12px;
  font-weight: 800;
}
h1 {
  margin: 2px 0 4px;
  color: #14264f;
  font-size: 28px;
}
.practice-hero > div > p:last-child {
  margin: 0;
  color: #697a95;
}
.practice-search {
  display: grid;
  grid-template-columns: 1fr 28px;
  align-items: center;
  min-height: 42px;
  padding: 0 10px 0 14px;
  border: 1px solid #ccd8e8;
  border-radius: 9px;
  background: #fff;
  box-shadow: 0 5px 18px rgb(50 79 125 / 6%);
}
.practice-search input {
  min-width: 0;
  border: 0;
  outline: 0;
  background: transparent;
  color: #2c4166;
}
.practice-search button {
  display: grid;
  width: 28px;
  height: 32px;
  place-items: center;
  border: 0;
  color: #587090;
  background: transparent;
  cursor: pointer;
}

.practice-search .el-icon {
  color: #5d7597;
  font-size: 18px;
}
.v3-practice-states {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 46px;
}
.v3-practice-states article {
  position: relative;
  display: grid;
  grid-template-columns: 28px 1fr auto;
  gap: 5px 9px;
  align-items: center;
  min-height: 76px;
  padding: 12px 16px;
  border: 1px solid #dce5f1;
  border-radius: 9px;
  background: #fff;
}
.v3-practice-states article:not(:last-child)::after {
  content: '›';
  position: absolute;
  top: 50%;
  right: -31px;
  color: #a9bbd4;
  font-size: 32px;
  font-weight: 300;
  line-height: 1;
  transform: translateY(-52%);
}
.v3-practice-states article > span {
  display: grid;
  width: 25px;
  height: 25px;
  place-items: center;
  grid-row: span 2;
  border-radius: 50%;
  color: #fff;
  background: #48a965;
  font-size: 11px;
  font-weight: 800;
}
.v3-practice-states article:nth-child(2) > span {
  background: #e8991e;
}
.v3-practice-states article:nth-child(3) > span {
  background: #8255d9;
}
.v3-practice-states strong {
  color: #1d335e;
  font-size: 16px;
}
.v3-practice-states p {
  margin: 0;
  color: #72819a;
  font-size: 11px;
}
.v3-practice-states small {
  justify-self: end;
  padding: 3px 7px;
  border: 1px solid #b8ddc0;
  border-radius: 8px;
  color: #3b9455;
  background: #f1fbf3;
  font-size: 10px;
}
.practice-flow {
  display: grid;
  grid-template-columns: minmax(0, 0.98fr) minmax(0, 1.08fr) minmax(0, 1.05fr) minmax(0, 1.42fr);
  gap: 12px;
  align-items: stretch;
  grid-auto-rows: minmax(470px, auto);
}

.student-practice-page :deep(.practice-flow + .answer-card) {
  margin-top: -6px;
}
.practice-flow > * {
  min-width: 0;
}
.action-feedback {
  margin: 0;
  padding: 10px 12px;
  border: 1px solid #b8dfc5;
  border-radius: 7px;
  color: #23754a;
  background: #f1fbf4;
  font-size: 12px;
  font-weight: 800;
}
.state-card {
  display: grid;
  justify-items: center;
  gap: 10px;
  min-height: 220px;
  place-items: center;
  padding: 30px;
  border: 1px solid #dbe5f1;
  border-radius: 9px;
  background: #fff;
  color: #62738d;
  text-align: center;
}
.state-card h2,
.state-card p {
  margin: 0;
}
.state-card h2 {
  color: #1a315e;
}
.state-card button {
  min-height: 36px;
  padding: 0 17px;
  border: 0;
  border-radius: 6px;
  color: #fff;
  background: #2f6de9;
  cursor: pointer;
}
.state-card.error {
  color: #b42318;
}
.compat-copy {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
}
.practice-state-view {
  display: grid;
  gap: 16px;
  padding: 20px;
  border: 1px solid #dce5f1;
  border-radius: 8px;
  background: #fff;
}
.state-view-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid #e9eef5;
}
.state-view-header h2 {
  margin: 5px 0 4px;
  color: #142b58;
  font-size: 22px;
}
.state-view-header p {
  margin: 0;
  color: #71819a;
}
.state-view-action,
.history-item button,
.result-next-step button {
  min-height: 36px;
  padding: 0 14px;
  border: 1px solid #8eb3f4;
  border-radius: 7px;
  color: #2563eb;
  background: #fff;
  cursor: pointer;
}
.history-list {
  display: grid;
  gap: 8px;
}
.history-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 14px;
  align-items: center;
  padding: 14px;
  border: 1px solid #e4ebf4;
  border-radius: 7px;
}
.history-item strong {
  color: #1d3c70;
}
.history-item p {
  margin: 5px 0 0;
  overflow: hidden;
  color: #71819a;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.status-chip {
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 12px;
}
.status-chip.success {
  color: #16835b;
  background: #e8f8ef;
}
.result-summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.result-summary-grid article {
  display: grid;
  gap: 6px;
  padding: 16px;
  border-radius: 7px;
  background: #f5f8fd;
}
.result-summary-grid span {
  color: #66758b;
  font-size: 13px;
}
.result-summary-grid strong {
  color: #163b78;
  font-size: 30px;
}
.result-summary-grid small {
  color: #66758b;
}
.success-text {
  color: #16835b !important;
}
.result-next-step {
  padding: 16px;
  border: 1px solid #cfe0fb;
  border-radius: 7px;
  background: #f4f8ff;
}
.result-next-step h3 {
  margin: 0 0 5px;
  color: #173b77;
}
.result-next-step p {
  margin: 0 0 12px;
  color: #60718b;
}
.workflow-banner {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 10px 14px;
  border: 1px solid #cfe0fb;
  border-radius: 7px;
  color: #2563eb;
  background: #f4f8ff;
}
@media (max-width: 1199px) {
  .practice-flow {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .practice-flow > :last-child {
    grid-column: 1 / -1;
  }
}
@media (max-width: 959px) {
  .practice-responsive-1024 {
    display: block;
  }
  .practice-flow {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .practice-flow > :last-child {
    grid-column: 1 / -1;
  }
}
@media (max-width: 720px) {
  .student-practice-page {
    width: calc(100% - 24px);
    padding-top: 14px;
  }
  .practice-hero,
  .v3-practice-states,
  .practice-flow {
    grid-template-columns: 1fr;
  }
  .v3-practice-states {
    gap: 8px;
  }
  .v3-practice-states article:not(:last-child)::after {
    display: none;
  }
  .practice-flow > :last-child {
    grid-column: auto;
  }
  .state-view-header,
  .history-item {
    grid-template-columns: 1fr;
    display: grid;
  }
  .state-view-action {
    justify-self: start;
  }
  .result-summary-grid {
    grid-template-columns: 1fr;
  }
  .history-item p {
    white-space: normal;
  }
}
</style>
