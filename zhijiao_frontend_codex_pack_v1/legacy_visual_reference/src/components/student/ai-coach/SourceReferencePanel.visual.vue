<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/ai-coach/SourceReferencePanel.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <aside class="source-reference-panel">
    <section v-if="!props.realMode" class="panel-card coach-performance-card">
      <header class="panel-header">
        <h3>
          <el-icon><ReadingLamp /></el-icon>学习表现
        </h3>
      </header>
      <div class="metric-grid">
        <article class="metric-card metric-card--green">
          <span class="metric-label">知识掌握度</span>
          <strong>62%</strong>
          <i class="sparkline" aria-hidden="true">
            <b></b><b></b><b></b><b></b><b></b><b></b><b></b>
          </i>
        </article>
        <article class="metric-card metric-card--orange">
          <span class="metric-label">预测掌握度变化</span>
          <strong>+23%</strong>
          <i class="sparkline" aria-hidden="true">
            <b></b><b></b><b></b><b></b><b></b><b></b><b></b>
          </i>
        </article>
        <article class="metric-card metric-card--blue">
          <span class="metric-label">达到 80%+ 概率</span>
          <strong>67%</strong>
          <i class="sparkline" aria-hidden="true">
            <b></b><b></b><b></b><b></b><b></b><b></b><b></b>
          </i>
        </article>
      </div>
    </section>

    <section v-if="!props.realMode" class="panel-card coach-progress-card">
      <header class="panel-header">
        <h3>
          <el-icon><List /></el-icon>任务进度
        </h3>
      </header>
      <div class="progress-meta">
        <span>全部进度 1 / 3</span><i><b></b></i>
      </div>
      <ol class="task-list">
        <li class="is-active">
          <span>1</span>
          <div><strong>学习中</strong><small>单链表的删除操作</small></div>
        </li>
        <li>
          <span>2</span>
          <div><strong>待学习</strong><small>链表的插入操作</small></div>
        </li>
        <li>
          <span>3</span>
          <div><strong>未开始</strong><small>递归时间复杂度</small></div>
        </li>
      </ol>
    </section>

    <section v-if="!props.realMode" class="panel-card coach-material-card">
      <header class="panel-header">
        <h3>
          <el-icon><CollectionTag /></el-icon>学习资料
        </h3>
      </header>
      <ul class="material-list">
        <li><span>《数据结构》教材</span><em class="tag tag--pdf">PDF</em></li>
        <li><span>单链表操作视频讲解</span><em class="tag tag--video">视频</em></li>
        <li><span>章节参考树（思维导图）</span><em class="tag tag--mind">XMind</em></li>
      </ul>
      <a class="material-link" href="#upload-material"
        >上传课本 / 参考资料 <el-icon><ArrowRight /></el-icon
      ></a>
    </section>

    <section v-if="!props.realMode" class="panel-card coach-wrong-card">
      <header class="panel-header">
        <h3>
          <el-icon><WarningFilled /></el-icon>错题提醒
        </h3>
        <span class="wrong-badge">!</span>
      </header>
      <p>你有 <strong>5</strong> 道相关错题需要巩固</p>
      <a href="/student/wrong-book"
        >去错题本巩固 <el-icon><ArrowRight /></el-icon
      ></a>
      <span class="wrong-cta-icon" aria-hidden="true"><ArrowRight /></span>
    </section>

    <details class="reference-details">
      <summary>引用来源与学习上下文</summary>
      <section class="panel-card">
        <header>
          <h3>
            <el-icon><Link /></el-icon>引用来源
          </h3>
        </header>
        <p v-if="answer?.sourceMode === 'general_knowledge'" class="general-note">未引用课程资料</p>
        <a
          v-for="source in answer?.sources ?? []"
          :key="source.id"
          :href="source.jumpUrl"
          data-test="source-card"
          class="source-card"
        >
          <span class="source-type">{{ source.sourceType }}</span>
          <strong>{{ source.title }}</strong>
          <b>{{
            source.confidence == null ? '后端未提供' : `${Math.round(source.confidence * 100)}%`
          }}</b>
          <small>{{ source.snippet }}</small>
        </a>
        <p v-if="answer" data-test="coach-citation-status" class="citation-state">
          引用状态：{{ citationStatusLabel }}
          <span v-if="fallbackReasonLabel"> · {{ fallbackReasonLabel }}</span>
        </p>
      </section>

      <section class="panel-card">
        <h3>
          <el-icon><Reading /></el-icon>学习上下文
        </h3>
        <dl class="context-list">
          <div>
            <dt>当前课程</dt>
            <dd>{{ context.currentCourse || '暂无课程' }}</dd>
          </div>
          <div>
            <dt>当前知识点</dt>
            <dd>{{ context.currentKnowledge || '暂无知识点' }}</dd>
          </div>
          <div>
            <dt>掌握程度</dt>
            <dd>
              {{ context.masteryLabel || '待评估' }}
              <span class="progress"><i :style="{ width: `${context.masteryPercent}%` }"></i></span>
              {{ context.masteryPercent }}%
            </dd>
          </div>
          <div>
            <dt>相关错题</dt>
            <dd>{{ context.relatedWrongCount }} 道</dd>
          </div>
        </dl>
      </section>

      <section class="panel-card">
        <h3>
          <el-icon><Upload /></el-icon>知识库来源
        </h3>
        <ul class="knowledge-list">
          <li v-for="source in context.knowledgeSources" :key="source.id">
            <span>{{ source.title }}</span>
            <em>{{ source.fileType }}</em>
          </li>
        </ul>
        <button type="button">上传课本/参考树</button>
        <p class="footnote">
          本次回答使用的知识来源：<strong>{{ sourceModeLabel }}</strong>
        </p>
      </section>
    </details>
  </aside>
</template>

<style scoped lang="scss">
.source-reference-panel {
  display: grid;
  align-content: start;
  gap: 12px;
}
.panel-card {
  padding: 14px;
  border: 1px solid #d9e5f7;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.9);
}
h3 {
  display: flex;
  gap: 8px;
  align-items: center;
  margin: 0 0 12px;
  color: #10224f;
  font-size: 17px;
}
.source-card {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 8px;
  align-items: start;
  padding: 10px;
  border: 1px solid #dfe7f3;
  border-radius: 8px;
  color: #344054;
  text-decoration: none;
  background: #ffffff;
}
.source-card + .source-card {
  margin-top: 10px;
}
.source-card small {
  grid-column: 1 / -1;
  color: #667085;
  line-height: 1.5;
}
.source-card b {
  padding: 2px 8px;
  border-radius: 8px;
  color: #2f7a31;
  background: #e7f8e6;
}
.source-type {
  padding: 2px 8px;
  border-radius: 6px;
  color: #ffffff;
  font-weight: 700;
  background: #4f8ef7;
}
.general-note {
  padding: 10px;
  border: 1px dashed #f97066;
  border-radius: 8px;
  color: #b42318;
  background: #fff4f2;
}
.citation-state {
  color: #667085;
  font-size: 12px;
}
.context-list {
  display: grid;
  gap: 8px;
  margin: 0;
}
.context-list div,
.knowledge-list li {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
dt {
  color: #667085;
}
dd {
  margin: 0;
  color: #1d2939;
  text-align: right;
}
.progress {
  display: inline-block;
  width: 70px;
  height: 9px;
  margin: 0 6px;
  overflow: hidden;
  border-radius: 999px;
  background: #e4e7ec;
}
.progress i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #58c76f;
}
.knowledge-list {
  display: grid;
  gap: 8px;
  padding: 0;
  margin: 0 0 12px;
  list-style: none;
}
.knowledge-list em {
  padding: 1px 6px;
  border: 1px solid #fda29b;
  border-radius: 5px;
  color: #d92d20;
  font-style: normal;
}
button {
  width: 100%;
  min-height: 38px;
  border: 1px solid #73a7ff;
  border-radius: 8px;
  color: #235fd7;
  background: #ffffff;
}
.footnote {
  margin: 10px 0 0;
  color: #667085;
  line-height: 1.5;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-header h3 {
  margin-bottom: 0;
  color: #1b2b45;
  font-size: 17px;
  font-weight: 750;
}

.panel-header h3 .el-icon {
  color: #2563eb;
}

.coach-progress-card .panel-header h3 .el-icon {
  color: #2aa27a;
}

.coach-material-card .panel-header h3 .el-icon {
  color: #e58a25;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-top: 14px;
}

.metric-card {
  display: grid;
  gap: 6px;
  min-width: 0;
  min-height: 104px;
  padding: 10px 8px 8px;
  border: 1px solid #e4ebf2;
  border-radius: 8px;
  background: #fbfdff;
}

.metric-label {
  overflow: hidden;
  color: #63748c;
  font-size: 11px;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.metric-card strong {
  font-size: 21px;
  line-height: 1;
}

.metric-card--green strong {
  color: #299b4f;
}

.metric-card--orange strong {
  color: #e47d1a;
}

.metric-card--blue strong {
  color: #2e65d9;
}

.metric-card--green .metric-label {
  color: #2c8a58;
}

.metric-card--orange .metric-label {
  color: #d17a18;
}

.metric-card--blue .metric-label {
  color: #3567c9;
}

.sparkline {
  display: flex;
  align-items: end;
  gap: 2px;
  height: 18px;
  opacity: 0.85;
}

.sparkline b {
  display: block;
  width: 7px;
  border-radius: 999px 999px 2px 2px;
  background: currentcolor;
}

.sparkline b:nth-child(1) {
  height: 8px;
}
.sparkline b:nth-child(2) {
  height: 13px;
}
.sparkline b:nth-child(3) {
  height: 10px;
}
.sparkline b:nth-child(4) {
  height: 15px;
}
.sparkline b:nth-child(5) {
  height: 9px;
}
.sparkline b:nth-child(6) {
  height: 12px;
}
.sparkline b:nth-child(7) {
  height: 16px;
}

.metric-card--green .sparkline {
  color: #4ebc73;
}
.metric-card--orange .sparkline {
  color: #f2a247;
}
.metric-card--blue .sparkline {
  color: #6593f3;
}

.progress-meta {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  margin-top: 12px;
  color: #65738a;
  font-size: 12px;
}

.progress-meta > i {
  display: block;
  height: 6px;
  overflow: hidden;
  border-radius: 999px;
  background: #e8eef8;
}

.progress-meta > i b {
  display: block;
  width: 33%;
  height: 100%;
  border-radius: inherit;
  background: #2c66d8;
}

.task-list {
  display: grid;
  gap: 8px;
  padding: 0;
  margin: 12px 0 0;
  list-style: none;
}

.task-list li {
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr);
  gap: 8px;
  align-items: center;
  min-height: 38px;
  padding: 5px 7px;
  border-radius: 8px;
  color: #26364f;
}

.task-list li.is-active {
  background: #f0f5ff;
}

.task-list li > span {
  display: grid;
  width: 22px;
  height: 22px;
  place-items: center;
  border: 1px solid #d4dfef;
  border-radius: 6px;
  color: #334a72;
  background: #fff;
  font-size: 12px;
  font-weight: 700;
}

.task-list li.is-active > span {
  border-color: #a7c3ff;
  color: #2d67d8;
  background: #edf3ff;
}

.task-list strong,
.task-list small {
  display: block;
  line-height: 1.35;
}

.task-list strong {
  color: #2c3c56;
  font-size: 12px;
}

.task-list small {
  color: #708099;
  font-size: 11px;
}

.material-list {
  display: grid;
  gap: 10px;
  padding: 0;
  margin: 14px 0 12px;
  list-style: none;
}

.material-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  color: #30415d;
  font-size: 13px;
}

.tag {
  flex: 0 0 auto;
  padding: 2px 7px;
  border: 1px solid currentcolor;
  border-radius: 5px;
  font-size: 11px;
  font-style: normal;
  font-weight: 700;
}

.tag--pdf {
  color: #ef6e62;
}
.tag--video {
  color: #5585e8;
}
.tag--mind {
  color: #4ab58b;
}

.material-link,
.coach-wrong-card > a {
  display: inline-flex;
  gap: 5px;
  align-items: center;
  color: #2461d2;
  font-size: 13px;
  text-decoration: none;
}

.coach-wrong-card {
  position: relative;
}

.coach-wrong-card .panel-header h3 .el-icon {
  color: #e9483f;
}

.wrong-badge {
  display: grid;
  width: 22px;
  height: 22px;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  background: #e9483f;
  font-size: 13px;
  font-weight: 800;
}

.coach-wrong-card p {
  margin: 14px 0 10px;
  color: #56657c;
  font-size: 13px;
}

.coach-wrong-card p strong {
  color: #e9483f;
  font-size: 16px;
}

.wrong-cta-icon {
  position: absolute;
  right: 14px;
  bottom: 14px;
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border-radius: 8px;
  color: #e9483f;
  background: #fff3f1;
}

.wrong-cta-icon .el-icon {
  font-size: 17px;
}

.reference-details {
  border: 1px solid #e1e8f2;
  border-radius: 8px;
  background: #fff;
}

.reference-details summary {
  padding: 10px 12px;
  color: #34527f;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
}

.reference-details > .panel-card {
  margin: 0 10px 10px;
}

@media (min-width: 1321px) {
  .coach-progress-card {
    padding: 13px;
  }

  .coach-progress-card .task-list {
    gap: 6px;
    margin-top: 10px;
  }

  .coach-progress-card .task-list li {
    min-height: 35px;
    padding-block: 3px;
  }

  .coach-material-card {
    padding: 13px;
  }

  .coach-material-card .material-list {
    gap: 9px;
    margin-block: 12px 10px;
  }

  .coach-wrong-card {
    padding: 13px;
  }

  .coach-wrong-card p {
    margin-block: 13px 9px;
  }
}
</style>
