<!-- VISUAL REFERENCE ONLY.
Source: src/components/teacher/ai-report/TeacherDecisionCard.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="report-insight-card report-panel report-decision-card">
    <header class="report-card-heading">
      <h2>教师决策</h2>
    </header>
    <p>基于以上分析结果，确认是否采纳本轮干预方案及下一轮建议。</p>
    <div class="report-decision-actions" role="group" aria-label="教师决策">
      <el-button
        type="primary"
        :plain="decision !== 'adopt'"
        data-testid="teacher-decision-adopt"
        @click="$emit('decide', 'adopt')"
      >
        采纳
      </el-button>
      <el-button
        :type="decision === 'edit' ? 'primary' : 'default'"
        :plain="decision !== 'edit'"
        data-testid="teacher-decision-edit"
        @click="$emit('decide', 'edit')"
      >
        修改后采纳
      </el-button>
      <el-button
        :type="decision === 'defer' ? 'primary' : 'default'"
        :plain="decision !== 'defer'"
        data-testid="teacher-decision-defer"
        @click="$emit('decide', 'defer')"
      >
        暂不采纳
      </el-button>
    </div>
  </section>
</template>
