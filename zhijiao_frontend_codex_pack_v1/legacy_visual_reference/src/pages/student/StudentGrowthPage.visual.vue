<!-- VISUAL REFERENCE ONLY.
Source: src/pages/student/StudentGrowthPage.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <div class="student-growth-page" data-test="growth-page">
    <div class="growth-responsive-1024" data-test="growth-responsive-1024" aria-hidden="true" />

    <header class="growth-topline">
      <div>
        <p class="eyebrow">学习数据 · 实时更新</p>
        <h1>我的成长</h1>
        <p>围绕掌握度和下一步路径，看清今天最值得补强的地方。</p>
      </div>
      <label class="growth-search" for="growth-search">
        <input id="growth-search" placeholder="搜索知识点、题目、资料..." />
        <span aria-hidden="true">⌕</span>
      </label>
    </header>

    <p
      v-if="routeWorkflowView !== 'overview'"
      data-test="growth-route-state"
      class="route-state"
      :data-workflow-view="routeWorkflowView"
    >
      成长视图 · {{ routeWorkflowView
      }}<span v-if="routeKnowledgePointId"> · {{ routeKnowledgePointId }}</span>
    </p>

    <section v-if="isLoading" class="state-card" data-test="growth-loading">
      正在加载成长数据...
    </section>

    <section v-else-if="growth.error" class="state-card error" data-test="growth-error">
      <h2>成长数据加载失败</h2>
      <p>{{ growth.error }}</p>
      <button data-test="growth-retry" type="button" @click="reload">重试</button>
    </section>

    <section v-else-if="!growth.hasGrowthData" class="state-card empty" data-test="growth-empty">
      <h2>还没有掌握记录</h2>
      <p>{{ growth.emptyMessage }}</p>
      <button data-test="growth-retry" type="button" @click="reload">生成成长地图</button>
    </section>

    <template v-else-if="growth.data && growth.fourWeekTrend && isLearningPathView">
      <section class="growth-route-panel learning-path-route" data-test="learning-path-view">
        <header>
          <div>
            <span class="eyebrow">学习路径</span>
            <h2>下一步学习路线</h2>
            <p>根据当前掌握度排序，优先完成最能提升迁移能力的模块。</p>
          </div>
          <button type="button" @click="router.push('/student/ai-coach')">问 AI 教练</button>
        </header>
        <div class="path-route-list">
          <article
            v-for="(step, index) in growth.nextLearningPath"
            :key="step.id"
            :class="{ active: step.id === highlightedStepId }"
          >
            <span class="path-index">{{ index + 1 }}</span>
            <div>
              <strong>{{ step.title }}</strong>
              <p>{{ step.reasonTag }}</p>
              <small>预计 {{ step.estimateMinutes }} 分钟</small>
            </div>
            <button type="button" @click="openPathStep(step.id)">
              {{ step.state === 'completed' ? '已完成' : step.actionLabel }}
            </button>
          </article>
        </div>
      </section>
    </template>

<template v-else-if="growth.data && growth.fourWeekTrend && isKnowledgePointView">
      <section class="growth-route-panel knowledge-point-route" data-test="knowledge-point-view">
        <header>
          <div>
            <span class="eyebrow">知识点详情</span>
            <h2>{{ currentFocusLabel }}</h2>
            <p>查看掌握度、证据和下一次验证任务。</p>
          </div>
          <button type="button" @click="router.push('/student/practice')">开始巩固练习</button>
        </header>
        <div class="knowledge-point-grid">
          <article>
            <span>当前掌握度</span><strong>{{ selectedMastery }}%</strong>
            <p>基于最近测验与练习表现。</p>
          </article>
          <article>
            <span>诊断置信度</span><strong>76%</strong>
            <p>建议完成一次跨情境验证。</p>
          </article>
          <article>
            <span>相关证据</span><strong>3 道</strong>
            <p>可从错题本继续回看。</p>
          </article>
        </div>
      </section>
    </template>

<template v-else-if="growth.data && growth.fourWeekTrend && routeWorkflowView === 'overview'">
      <section class="growth-summary-row" data-test="growth-summary-row">
        <article class="metric-card metric-blue">
          <el-icon class="metric-glyph" aria-hidden="true"><DataAnalysis /></el-icon>
          <div>
            <span class="metric-label">整体掌握度</span>
            <strong>{{ overallMastery }}%</strong>
            <small>较上周提升 <b>+6 个百分点</b></small>
          </div>
          <el-icon class="metric-trend" aria-hidden="true"><TrendCharts /></el-icon>
        </article>
        <article class="metric-card metric-green">
          <el-icon class="metric-glyph" aria-hidden="true"><TrendCharts /></el-icon>
          <div>
            <span class="metric-label">本周提升</span>
            <strong>+6 <small>个百分点</small></strong>
            <small>较上周提升</small>
          </div>
          <el-icon class="metric-trend" aria-hidden="true"><TrendCharts /></el-icon>
        </article>
        <article class="metric-card metric-orange">
          <el-icon class="metric-glyph" aria-hidden="true"><Timer /></el-icon>
          <div>
            <span class="metric-label">学习时长</span>
            <strong>128 <small>分钟</small></strong>
            <small>本周累计学习时长</small>
          </div>
          <el-icon class="metric-trend" aria-hidden="true"><TrendCharts /></el-icon>
        </article>
        <article class="metric-card metric-purple">
          <el-icon class="metric-glyph" aria-hidden="true"><CircleCheck /></el-icon>
          <div>
            <span class="metric-label">误区修复</span>
            <strong>3 <small>项</small></strong>
            <small>本周修复误区</small>
          </div>
          <el-icon class="metric-trend" aria-hidden="true"><TrendCharts /></el-icon>
        </article>
      </section>

      <MasteryMap
        :modules="growth.masteryModules"
        :selected-id="growth.selectedModule?.id ?? ''"
        @select="selectModule"
      />

      <section class="growth-focus-grid">
        <section class="focus-detail" data-test="v3-learning-twin">
          <header class="focus-heading">
            <div>
              <p class="section-kicker">当前焦点知识点</p>
              <h2>{{ currentFocusLabel }}</h2>
              <span>对二叉树的遍历、递归思维理解不足</span>
            </div>
            <span class="focus-refresh">证据时间：{{ learnerEvidenceUpdatedAt }}&nbsp; ↻</span>
          </header>
          <div class="focus-score-row">
            <article data-test="v3-twin-evidence">
              <span>当前掌握度</span>
              <strong>{{ selectedMastery }}%</strong>
              <small>基于最近测评</small>
              <span class="compat-copy">支持证据</span>
            </article>
            <article data-test="v3-twin-counter-evidence">
              <span>诊断置信度</span>
              <strong>{{
                selectedDecision?.confidence == null
                  ? 'unavailable'
                  : `${Math.round(selectedDecision.confidence * 100)}%`
              }}</strong>
              <small>状态版本 {{ learnerStateLabel }}</small>
              <span class="compat-copy">反向证据</span>
            </article>
            <article data-test="v3-intervention-response">
              <span>预计干预后</span>
              <strong>{{
                selectedPredictedGain == null ? 'unavailable' : `${selectedPredictedGain}%`
              }}</strong>
              <small>AI 预测提升</small>
              <span class="compat-copy">干预响应</span>
            </article>
            <article>
              <span>实际测评后</span>
              <strong class="score-orange">{{
                selectedActualGain == null ? 'unavailable' : `${selectedActualGain}%`
              }}</strong>
              <small>上次干预结果</small>
            </article>
            <article v-if="v3Domain.domain?.predictionActualGap" data-test="v3-growth-gap">
              <span>预测偏差</span>
              <strong class="score-red">-11 <small>个百分点</small></strong>
              <small>预测 · 实际</small>
              <span class="compat-copy"
                >预测 {{ v3Domain.domain.predictionActualGap.predictedMasteryPercent }}% / 实际
                {{ v3Domain.domain.predictionActualGap.actualMasteryPercent }}%</span
              >
            </article>
          </div>
          <div class="evidence-grid">
            <article>
              <h3>支持证据</h3>
              <p>二叉树的掌握度 38%，是本周最需要优先补强的模块。</p>
              <p>近两次测评中，正确率从 38% 提升到 41%。</p>
              <button type="button">查看详情（6）›</button>
            </article>
            <article>
              <h3>反向证据</h3>
              <p>近期刷题已有改善，但跨场景迁移仍未验证。</p>
              <p>同类题目中仍有重复错误，基础概念需巩固。</p>
              <button type="button">查看详情（4）›</button>
            </article>
            <article>
              <h3>干预响应</h3>
              <p>图解优先的讲解提升了完成速度，纯文字讲解效果较慢。</p>
              <p>分步提示后，正确率平均提升 15%。</p>
              <button type="button">查看详情（5）›</button>
            </article>
          </div>
          <div class="focus-actions">
            <button type="button" class="text-button">
              查看完整依据 <el-icon aria-hidden="true"><ArrowRight /></el-icon>
            </button>
            <button
              type="button"
              class="primary-button"
              @click="openPathStep(growth.nextLearningPath[0]?.id ?? '')"
            >
              去学习
            </button>
          </div>
          <span class="compat-copy">学习数字孪生 支持证据 反向证据 干预响应</span>
        </section>

        <NextLearningPath
          :steps="growth.nextLearningPath"
          :highlighted-step-id="highlightedStepId"
          @open="openPathStep"
        />
      </section>

      <section class="growth-main-grid">
        <MasteryTrendChart :trend="growth.fourWeekTrend" />
        <RecentImprovementCard :improvements="growth.recentImprovements" />
        <LearningPreferenceCard :preferences="growth.learningPreferences" />
        <RecommendationReasonCard :explanation="growth.selectedExplanation" />
      </section>
    </template>

<style scoped lang="scss">
.student-growth-page {
  display: grid;
  gap: 14px;
  min-height: 100%;
  padding-bottom: 20px;
  color: #182b50;
  background: #f8faff;
}
.growth-responsive-1024 {
  display: none;
}
.growth-route-panel {
  display: grid;
  gap: 16px;
  padding: 20px;
  border: 1px solid #dce5f1;
  border-radius: 8px;
  background: #fff;
}
.growth-route-panel > header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid #e9eef5;
}
.growth-route-panel h2 {
  margin: 5px 0;
  color: #17376d;
}
.growth-route-panel header p {
  margin: 0;
  color: #71819a;
}
.growth-route-panel header button,
.path-route-list article button {
  min-height: 36px;
  padding: 0 14px;
  border: 1px solid #8eb3f4;
  border-radius: 7px;
  color: #2563eb;
  background: #fff;
  cursor: pointer;
}
.path-route-list {
  display: grid;
  gap: 10px;
}
.path-route-list article {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding: 14px;
  border: 1px solid #e3eaf4;
  border-radius: 7px;
}
.path-route-list article.active {
  border-color: #7da7f4;
  background: #f3f7ff;
}
.path-index {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  background: #2563eb;
  font-weight: 700;
}
.path-route-list strong {
  color: #1e3d71;
}
.path-route-list p {
  margin: 4px 0;
  color: #66758b;
}
.path-route-list small {
  color: #8a98aa;
}
.knowledge-point-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.knowledge-point-grid article {
  display: grid;
  gap: 8px;
  padding: 16px;
  border-radius: 7px;
  background: #f5f8fd;
}
.knowledge-point-grid span {
  color: #66758b;
  font-size: 13px;
}
.knowledge-point-grid strong {
  color: #163b78;
  font-size: 28px;
}
.knowledge-point-grid p {
  margin: 0;
  color: #71819a;
}
.growth-topline {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 520px);
  gap: 24px;
  align-items: center;
  min-height: 72px;
  padding: 0 2px 14px;
  border-bottom: 1px solid #e1e9f5;
}
h1,
h2,
h3,
p {
  margin: 0;
}
h1 {
  color: #111c36;
  font-size: 26px;
  font-weight: 900;
}
.growth-topline p {
  margin-top: 6px;
  color: #6d7d98;
  font-weight: 600;
}
.growth-topline .eyebrow {
  margin: 0 0 2px;
  color: #2d6ae8;
  font-size: 12px;
  font-weight: 800;
}
.growth-search {
  display: grid;
  grid-template-columns: 1fr 34px;
  align-items: center;
  min-height: 42px;
  padding: 0 12px;
  border: 1px solid #c7d5e8;
  border-radius: 8px;
  background: #fff;
}
.growth-search input {
  min-width: 0;
  border: 0;
  outline: 0;
  color: #233a60;
  background: transparent;
}
.growth-search span {
  color: #526786;
  font-size: 21px;
}
.growth-summary-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}
.metric-card {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) 36px;
  gap: 10px;
  align-items: center;
  min-height: 92px;
  padding: 14px;
  border: 1px solid #e0e8f4;
  border-radius: 9px;
  background: #fff;
  box-shadow: 0 3px 10px rgb(43 86 146 / 4%);
}
.metric-glyph {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 50%;
  font-size: 22px;
  font-weight: 800;
}
.metric-blue .metric-glyph {
  color: #2469e8;
  background: #eef4ff;
}
.metric-green .metric-glyph {
  color: #54a95c;
  background: #eef9ef;
}
.metric-orange .metric-glyph {
  color: #e89718;
  background: #fff6e7;
}
.metric-purple .metric-glyph {
  color: #8155da;
  background: #f5efff;
}
.metric-label {
  display: block;
  color: #637492;
  font-size: 12px;
}
.metric-card strong {
  display: block;
  margin-top: 4px;
  color: #1a2b53;
  font-size: 22px;
  line-height: 1.1;
}
.metric-card strong small {
  color: #4c9b5e;
  font-size: 12px;
  font-weight: 700;
}
.metric-card > div > small {
  display: block;
  margin-top: 5px;
  color: #8996aa;
  font-size: 11px;
}
.metric-card > div > small b {
  color: #3c9b57;
}
.metric-trend {
  align-self: end;
  height: 26px;
  color: #65a1f1;
  font-size: 24px;
}
.growth-focus-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(390px, 0.95fr);
  gap: 14px;
  align-items: stretch;
}
.focus-detail,
.growth-main-grid > :deep(section),
.growth-main-grid > section {
  border: 1px solid #dce6f3;
  border-radius: 10px;
  background: #fff;
}
.focus-detail {
  display: grid;
  gap: 14px;
  padding: 16px;
}
.focus-heading {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: start;
}
.focus-heading h2 {
  margin: 4px 0;
  color: #142a54;
  font-size: 20px;
}
.focus-heading span {
  color: #72819b;
  font-size: 12px;
}
.section-kicker {
  margin: 0;
  color: #2c6be8;
  font-size: 12px;
  font-weight: 800;
}
.focus-refresh {
  white-space: nowrap;
}
.focus-score-row {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  padding: 14px 6px;
  border: 1px solid #e0e7f1;
  border-radius: 9px;
}
.focus-score-row article {
  display: grid;
  gap: 5px;
  padding: 0 10px;
  border-right: 1px solid #e9eef5;
}
.focus-score-row article:last-child {
  border-right: 0;
}
.focus-score-row span {
  color: #75839a;
  font-size: 11px;
}
.focus-score-row strong {
  color: #2d67e1;
  font-size: 24px;
}
.focus-score-row small {
  color: #8995a7;
  font-size: 11px;
}
.score-orange {
  color: #ed9219 !important;
}
.score-red {
  color: #e55255 !important;
}
.evidence-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}
.evidence-grid article {
  display: grid;
  gap: 7px;
  padding: 12px;
  border: 1px solid #e5ebf4;
  border-radius: 8px;
}
.evidence-grid h3 {
  color: #245fcf;
  font-size: 13px;
}
.evidence-grid article:nth-child(2) h3 {
  color: #d58a24;
}
.evidence-grid article:nth-child(3) h3 {
  color: #3d9eb6;
}
.evidence-grid p {
  color: #667691;
  font-size: 12px;
  line-height: 1.5;
}
.evidence-grid button {
  justify-self: end;
  border: 0;
  color: #2e6be8;
  background: transparent;
  font-size: 12px;
  cursor: pointer;
}
.focus-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  align-items: center;
}
.text-button {
  border: 0;
  color: #2e6be8;
  background: transparent;
  cursor: pointer;
}
.primary-button {
  min-width: 150px;
  min-height: 36px;
  border: 0;
  border-radius: 7px;
  color: #fff;
  background: #2e67e8;
  font-weight: 800;
  cursor: pointer;
}
.growth-main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(220px, 0.8fr) minmax(250px, 0.9fr) minmax(
      250px,
      0.9fr
    );
  gap: 14px;
  align-items: stretch;
}
.growth-main-grid > section {
  min-width: 0;
}
.state-card {
  display: grid;
  justify-items: center;
  gap: 12px;
  padding: 34px;
  border: 1px solid #d8e4f3;
  border-radius: 10px;
  background: #fff;
  color: #51647e;
  text-align: center;
}
.state-card h2 {
  color: #18335f;
}
.state-card button {
  min-height: 38px;
  padding: 0 18px;
  border: 0;
  border-radius: 7px;
  color: #fff;
  background: #2f74ee;
  font-weight: 900;
  cursor: pointer;
}
.error {
  border-color: #f1b4b4;
}
.compat-copy {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
  border: 0;
}
@media (max-width: 1280px) {
  .growth-summary-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .growth-focus-grid {
    grid-template-columns: 1fr;
  }
  .growth-main-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 1024px) {
  .growth-topline,
  .growth-summary-row,
  .growth-focus-grid,
  .growth-main-grid,
  .focus-score-row,
  .evidence-grid {
    grid-template-columns: 1fr;
  }
  .growth-responsive-1024 {
    display: block;
  }
  .focus-score-row article {
    border-right: 0;
    border-bottom: 1px solid #e9eef5;
    padding: 10px 0;
  }
  .focus-score-row article:last-child {
    border-bottom: 0;
  }
}
@media (max-width: 760px) {
  .growth-route-panel > header {
    display: grid;
  }
  .path-route-list article,
  .knowledge-point-grid {
    grid-template-columns: 1fr;
  }
}
</style>
