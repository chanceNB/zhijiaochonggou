<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/knowledge-base/KnowledgeSummary.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="knowledge-summary" data-test="knowledge-summary">
    <div class="summary-copy">
      <p>我的资料 · {{ summary.sourceTypeCount }} 类来源</p>
      <h2>资料越丰富，AI 回答越精准</h2>
      <span>上传课本、讲义、XMind 和自定义资料后，AI 会优先引用可用课程来源。</span>
      <small>状态会随上传与解析进度实时更新</small>
    </div>
    <div class="summary-grid">
      <article data-test="summary-total">
        <el-icon><Collection /></el-icon><span>资料总数</span
        ><strong>{{ summary.totalCount }}</strong
        ><small>已连接资料</small>
      </article>
      <article data-test="summary-ready">
        <el-icon><Finished /></el-icon><span>AI 可使用</span
        ><strong>{{ summary.readyCount }}</strong
        ><small>可检索资料</small>
      </article>
      <article data-test="summary-processing">
        <el-icon><Link /></el-icon><span>处理状态</span><strong>{{ summary.parsingCount }}</strong
        ><small>处理中</small>
      </article>
      <article data-test="summary-failed">
        <el-icon><MagicStick /></el-icon><span>需要处理</span
        ><strong>{{ summary.failedCount }}</strong
        ><small>解析失败</small>
      </article>
    </div>
  </section>
</template>

<style scoped lang="scss">
.knowledge-summary {
  display: grid;
  grid-template-columns: minmax(260px, 0.8fr) minmax(420px, 1.6fr);
  gap: 12px;
}
.summary-copy,
.summary-grid article {
  border: 1px solid #dce6f2;
  border-radius: 9px;
  background: #fff;
}
.summary-copy {
  display: grid;
  align-content: center;
  gap: 7px;
  padding: 18px;
  background: linear-gradient(135deg, #eef5ff, #fff 70%);
}
.summary-copy p,
.summary-copy h2,
.summary-copy span {
  margin: 0;
}
.summary-copy p {
  color: #2f6de9;
  font-size: 12px;
  font-weight: 800;
}
.summary-copy h2 {
  color: #142b59;
  font-size: 22px;
}
.summary-copy span {
  color: #637491;
  line-height: 1.55;
}
.summary-copy small {
  color: #8391a6;
  font-size: 11px;
}
.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}
.summary-grid article {
  display: grid;
  align-content: start;
  gap: 6px;
  min-height: 142px;
  padding: 15px;
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}
.summary-grid article:hover {
  border-color: #b7cdef;
  box-shadow: 0 7px 18px rgb(45 86 145 / 8%);
}
.summary-grid .el-icon {
  color: #2f70ef;
  font-size: 22px;
}
.summary-grid article:nth-child(2) .el-icon {
  color: #44ad68;
}
.summary-grid article:nth-child(3) .el-icon {
  color: #ed9b1c;
}
.summary-grid article:nth-child(4) .el-icon {
  color: #e75d62;
}
.summary-grid span,
.summary-grid small {
  color: #73839b;
  font-size: 12px;
}
.summary-grid strong {
  color: #172b56;
  font-size: 25px;
  line-height: 1;
}
@media (max-width: 1024px) {
  .knowledge-summary,
  .summary-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
