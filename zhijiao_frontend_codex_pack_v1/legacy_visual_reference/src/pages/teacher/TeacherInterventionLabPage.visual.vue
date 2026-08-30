<!-- VISUAL REFERENCE ONLY.
Source: src/pages/teacher/TeacherInterventionLabPage.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <main class="intervention-lab-page" data-testid="intervention-lab-page">
    <section v-if="realInterventionMode" class="panel" data-testid="real-intervention-state">
      <strong>真实干预闭环 · caseId={{ realContext.caseId || '缺失' }}</strong>
      <template v-if="realIntervention.intervention">
        <p>
          {{ realIntervention.intervention.status }} · version={{
            realIntervention.intervention.version
          }}
          · interventionId={{ realIntervention.intervention.interventionId }}
        </p>
        <section
          v-if="realIntervention.intervention.effect"
          class="intervention-effect-observation"
          data-testid="intervention-effect-observation"
        >
          <strong>迁移正确率的观察性关联估计</strong>
          <p>该估计不代表因果、最优性或保证的改进。</p>
          <dl>
            <dt>历史基线</dt>
            <dd>{{ realIntervention.intervention.effect.baseline.value.toFixed(2) }}</dd>
            <dt>预测区间</dt>
            <dd>
              {{ realIntervention.intervention.effect.prediction.lowerBound.toFixed(2) }} –
              {{ realIntervention.intervention.effect.prediction.upperBound.toFixed(2) }}
            </dd>
            <dt>目标差距</dt>
            <dd>{{ realIntervention.intervention.effect.prediction.targetGap.toFixed(2) }}</dd>
          </dl>
          <p
            v-if="realIntervention.intervention.effect.actual"
            data-testid="intervention-effect-actual"
          >
            实际迁移正确率：{{
              realIntervention.intervention.effect.actual.value.toFixed(2)
            }}；预测偏差：
            {{ realIntervention.intervention.effect.predictionDeviation?.toFixed(2) ?? '待记录' }}
          </p>
          <p v-if="realIntervention.intervention.effect.matchedReference">
            可选历史参照：{{
              realIntervention.intervention.effect.matchedReference.transferAccuracy.toFixed(2)
            }}
          </p>
        </section>
        <el-input
          v-model="outcomeTransferValidationId"
          data-testid="outcome-transfer-validation-id"
          placeholder="填写已验证的迁移验证 ID"
        />
        <el-input
          v-model="outcomeSummary"
          data-testid="outcome-summary"
          placeholder="填写结果总结"
        />
        <el-button data-testid="submit-outcome" @click="submitOutcome">记录实际结果</el-button>
        <el-button data-testid="real-intervention-report" @click="openRealReport">
          查看同 caseId 报告
        </el-button>
      </template>

<template v-else>
      <section class="problem-strip panel">
        <article>
          <span>诊断问题</span><strong>BFS/DFS 遍历过程与复杂度核算不稳定</strong
          ><el-tag type="danger">高风险</el-tag>
        </article>
        <article>
          <span>目标人群</span><strong>2024级计算机2班 · 12 人</strong>
          <p>风险占比 66.7%</p>
        </article>
        <article>
          <span>关键知识点</span><strong>图遍历 / BFS / DFS / 时间复杂度</strong>
          <p>关联错题 23 道</p>
        </article>
        <article>
          <span>已验证假设</span><strong>概念理解不清 + 题型迁移能力弱</strong
          ><ConfidenceBadge :value="0.82" />
        </article>
        <article>
          <span>证据摘要</span>
          <ul>
            <li>近3次作业正确率 &lt; 50%</li>
            <li>课堂互动频率下降</li>
            <li>错题集中于图像识别</li>
          </ul>
        </article>
      </section>
      <section
        v-if="v3Domain.domain"
        class="shared-domain-strip"
        data-testid="v3-intervention-context"
      >
        <strong>{{ v3Domain.domain.learner.name }} · {{ v3Domain.domain.learner.topic }}</strong
        ><span>{{ v3Domain.domain.diagnosticHypothesis.statement }}</span
        ><small
          >当前置信度
          {{ Math.round(v3Domain.domain.diagnosticHypothesis.confidence * 100) }}%</small
        >
        <small v-if="v3Domain.domain.evidence.supports.length">
          支持证据 {{ v3Domain.domain.evidence.supports.length }} 条
        </small>
        <small v-else data-testid="v3-support-evidence-empty">暂无支持证据</small>
        <small v-if="v3Domain.domain.evidence.counterEvidence.length">
          反向证据 {{ v3Domain.domain.evidence.counterEvidence.length }} 条
        </small>
        <small v-else data-testid="v3-counter-evidence-empty">暂无反向证据</small>
      </section>
      <section
        v-else-if="v3Domain.isEmpty"
        class="shared-domain-strip"
        data-testid="v3-domain-empty"
      >
        {{ v3Domain.emptyMessage }}
      </section>
      <section class="main-grid">
        <section class="panel plan-section">
          <div class="section-title">
            <h2>干预方案对比（系统推荐）</h2>
            <span>仅可选择一个方案用于正式执行</span
            ><el-button data-testid="regenerate-plan" link type="primary" @click="regeneratePlan"
              >生成新方案</el-button
            >
          </div>
          <div class="plan-grid">
            <article
              v-for="plan in plans"
              :key="plan.id"
              class="plan-card"
              :class="{ active: plan.id === selectedPlanId }"
              data-testid="plan-card"
              @click="selectedPlanId = plan.id"
            >
              <div class="plan-radio"></div>
              <h3>方案 {{ plan.id }}</h3>
              <el-tag size="small">{{ plan.title }}</el-tag>
              <p>{{ plan.tag }}</p>
              <strong>{{ plan.strategy }}</strong>
              <dl>
                <dt>预计效果（目标达成率）</dt>
                <dd>{{ plan.expected }}%</dd>
                <dt>覆盖人数</dt>
                <dd>{{ plan.coverage }}</dd>
                <dt>教师工时</dt>
                <dd>{{ plan.teacherHours }}</dd>
                <dt>学生用时</dt>
                <dd>{{ plan.studentHours }}</dd>
                <dt>风险</dt>
                <dd>{{ plan.risk }}</dd>
                <dt>置信度</dt>
                <dd><ConfidenceBadge :value="plan.confidence" /></dd>
              </dl>
              <el-button
                type="primary"
                :plain="plan.id !== selectedPlanId"
                :aria-pressed="plan.id === selectedPlanId"
                :data-testid="'select-plan-' + plan.id"
                @click.stop="selectedPlanId = plan.id"
                >{{ plan.id === selectedPlanId ? '已选择方案' : '选择方案' }}</el-button
              >
            </article>
          </div>
          <div class="plan-hint">
            ⓘ 选择后将锁定为本次实验的唯一执行方案，执行期间不可同时运行其他方案。
          </div>
        </section>
        <aside class="panel settings" data-testid="experiment-settings">
          <div class="section-title"><h2>实验设置</h2></div>
          <label>观察周期<el-input v-model="period" /></label
          ><label>成功阈值<el-input v-model="successThreshold" /></label>
          <p>成功阈值（需同时满足以下全部条件）</p>
          <div class="check-list">
            <el-checkbox checked>知识点掌握度（提升值）<b>≥ 20%</b></el-checkbox
            ><el-checkbox checked>题型正确率（提升值）<b>≥ 15%</b></el-checkbox
            ><el-checkbox checked>课堂参与度（提升值）<b>≥ 12%</b></el-checkbox>
          </div>
          <p>基线参考：2024级计算机2班历史基线（近4周平均）</p>
          <label
            >抽样范围<el-radio-group model-value="all"
              ><el-radio value="all">全部 12 人（100%）</el-radio
              ><el-radio value="sample">按条件抽样</el-radio></el-radio-group
            ></label
          ><small>实验将在观察周期结束后自动评估成效。</small>
        </aside>
      </section>
      <section class="decision-panel panel">
        <h2>教师决策确认</h2>
        <p>选择本次执行方案的理由（必填）</p>
        <el-input
          v-model="reason"
          data-testid="confirm-reason"
          placeholder="请说明选择该方案的关键理由与预期收益（至少 10 个字）"
          type="textarea"
          :rows="2"
        />
        <div class="decision-footer">
          <span
            >已选方案：<strong>方案 {{ selectedPlanId }}</strong></span
          ><span
            >覆盖人数：<strong>{{ selectedPlan?.coverage }}</strong></span
          ><span
            >观察周期：<strong>{{ period }}</strong></span
          ><el-button plain>保存草稿</el-button
          ><el-button type="primary" data-testid="confirm-plan" @click="confirmPlan"
            >确认执行方案</el-button
          >
        </div>
        <p data-testid="teacher-ledger">{{ ledger }}</p>
        <el-button
          link
          type="primary"
          data-testid="open-intervention-ledger"
          @click="ledgerOpen = true"
          >查看完整证据账本 ↗</el-button
        >
        <p v-if="confirmationOpen" data-testid="human-confirmation-state">
          等待张老师最终确认，当前不会执行任何正式教学安排。
        </p>
      </section>
      <section class="execution-grid">
        <section class="panel" data-testid="execution-status">
          <div class="section-title">
            <h2>执行进度</h2>
            <el-tag type="primary">进行中</el-tag>
          </div>
          <div class="progress-steps">
            <p>
              <b>1</b><strong>方案启动与学生分组</strong><span>已完成</span><small>05-20</small>
            </p>
            <p>
              <b>2</b><strong>第 1 周干预执行</strong><span>已完成</span
              ><small>05-20 - 05-26</small>
            </p>
            <p>
              <b>3</b><strong>第 2 周干预执行</strong><span>进行中</span
              ><small>05-27 - 06-02</small>
            </p>
            <p>
              <b>4</b><strong>效果评估与复盘</strong><span>待开始</span><small>06-03 开始</small>
            </p>
          </div>
        </section>
        <section class="panel" data-testid="effect-verification">
          <div class="section-title">
            <h2>效果验证（中期数据）</h2>
            <span>数据更新：2024-05-30 10:30</span>
          </div>
          <table>
            <thead>
              <tr>
                <th>指标</th>
                <th>基线</th>
                <th>目标</th>
                <th>预测值</th>
                <th>实际值</th>
                <th>偏差</th>
                <th>达标情况</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>知识点掌握度</td>
                <td>58%</td>
                <td>≥78%</td>
                <td>78%</td>
                <td>76%</td>
                <td>-2.0%</td>
                <td><el-tag type="warning">接近达标</el-tag></td>
              </tr>
              <tr>
                <td>相关题型正确率</td>
                <td>46%</td>
                <td>≥66%</td>
                <td>66%</td>
                <td>70%</td>
                <td>+4.0%</td>
                <td><el-tag type="success">已达标</el-tag></td>
              </tr>
              <tr>
                <td>课堂参与度</td>
                <td>2.8 / 5</td>
                <td>≥4.0 / 5</td>
                <td>4.0 / 5</td>
                <td>4.1 / 5</td>
                <td>+0.1</td>
                <td><el-tag type="success">已达标</el-tag></td>
              </tr>
            </tbody>
          </table>
        </section>
        <section class="panel deviation-panel">
          <div class="section-title">
            <h2>预测与实际偏差</h2>
            <a>更多指标 ›</a>
          </div>
          <p>知识点掌握度 <em>-2.0%</em></p>
          <p>相关题型正确率 <b>+4.0%</b></p>
          <p>课堂参与度 <b>+0.1</b></p>
          <p>高阶思维题正确率 <em>-4.0%</em></p>
        </section>
      </section>
      <section class="risk-and-next-grid">
        <section class="panel risk-panel">
          <div class="section-title">
            <h2>异常学生与风险提醒</h2>
            <a>查看全部 ›</a>
          </div>
          <div class="risk-student-grid">
            <article class="risk-student-item">
              <div class="risk-student-item__head">
                <span>王浩（S003）</span><em>负向</em><a>一对一辅导</a>
              </div>
              <p>高阶思维题正确率下降</p>
            </article>
            <article class="risk-student-item">
              <div class="risk-student-item__head">
                <span>赵晨（S007）</span><em class="orange">无明显变化</em><a>关注互动</a>
              </div>
              <p>课堂参与度偏低</p>
            </article>
            <article class="risk-student-item">
              <div class="risk-student-item__head">
                <span>刘雨桐（S009）</span><em class="orange">无明显变化</em><a>针对性练习</a>
              </div>
              <p>相关题型进步缓慢</p>
            </article>
          </div>
        </section>
        <section class="panel next-round" data-testid="next-round">
          <div class="next-round__header">
            <div class="next-round__title">
              <span class="next-round__eyebrow">下一步行动</span>
              <div class="section-title">
                <h2>下一轮建议</h2>
                <el-tag type="warning" effect="light">待教师确认</el-tag>
              </div>
              <p class="next-round__lead">延续问题探究与变式训练，增加高阶思维可视化工具。</p>
            </div>
            <div class="next-round__confidence">
              <span>建议置信度</span>
              <strong>82%</strong>
              <small>基于本轮 12 名学生数据</small>
            </div>
          </div>
          <div class="next-round__grid">
            <article class="next-round__item">
              <span>建议动作</span>
              <strong>小组互助 + 个别指导</strong>
              <p>对高阶思维题薄弱学生开展分组练习，并安排个别指导。</p>
            </article>
            <article class="next-round__item">
              <span>观察周期</span>
              <strong>2 周</strong>
              <small>2024-06-03 ～ 2024-06-16</small>
            </article>
            <article class="next-round__item">
              <span>重点指标</span>
              <strong>高阶思维题正确率</strong>
              <small>目标提升 ≥ 10%</small>
            </article>
          </div>
          <div class="next-round__footer">
            <p
              v-if="nextRoundFeedback"
              class="next-round__feedback"
              data-testid="next-round-feedback"
              aria-live="polite"
            >
              {{ nextRoundFeedback }}
            </p>
            <p v-else class="next-round__hint">
              生成后将进入待确认状态，不会自动改变正式教学安排。
            </p>
            <div class="next-round__actions">
              <el-button plain data-testid="view-next-round-evidence" @click="ledgerOpen = true"
                >查看依据</el-button
              >
              <el-button type="primary" data-testid="regenerate-next-round" @click="regeneratePlan"
                >生成下一轮方案</el-button
              >
            </div>
          </div>
        </section>
      </section>
      <section class="panel effect-distribution" data-testid="effect-distribution">
        <div class="section-title"><h2>效果分布（12 人）</h2></div>
        <div class="distribution-bar"><i></i><i></i><i></i><i></i></div>
        <p>显著有效 4 人（33%） 有效 5 人（42%） 无明显变化 2 人（17%） 负向 1 人（8%）</p>
      </section>
      <section v-if="smartBiResource" class="smartbi-hidden" data-testid="intervention-smartbi">
        <SmartBiResourceCard :resource="smartBiResource" @open="openSmartBi" />
      </section>
      <section class="legacy-risk" aria-hidden="true">
        <section class="risk-workflow panel">
          <div class="risk-toolbar">
            <div>
              <h2>风险学生干预</h2>
              <span>证据驱动，所有动作写入时间线</span>
            </div>
            <div class="risk-filters">
              <el-button @click="filterRisk('全部')">全部</el-button
              ><el-button
                data-testid="filter-risk-high"
                type="danger"
                plain
                @click="filterRisk('高风险')"
                >高风险</el-button
              ><el-button type="warning" plain @click="filterRisk('中风险')">中风险</el-button>
            </div>
          </div>
          <div class="risk-layout">
            <div class="risk-list">
              <article
                v-for="student in filteredRiskStudents"
                :key="student.id"
                data-testid="risk-student-row"
                class="risk-row"
              >
                <div>
                  <strong>{{ student.name }}</strong
                  ><span>{{ student.className }}</span>
                </div>
                <el-tag>{{ student.level }}</el-tag
                ><span>{{ student.status }}</span>
                <p>{{ student.reason }}</p>
                <el-button
                  :data-testid="'open-risk-' + student.id"
                  text
                  type="primary"
                  @click="openRisk(student.id)"
                  >查看并干预</el-button
                >
              </article>
            </div>
            <aside v-if="selectedRisk" class="risk-drawer" data-testid="risk-detail-drawer">
              <h3>{{ selectedRisk.name }}</h3>
              <el-tag>{{ selectedRisk.status }}</el-tag>
              <p>风险证据</p>
              <p v-for="item in selectedRisk.evidence" :key="item">{{ item }}</p>
              <p>干预建议</p>
              <p v-for="item in selectedRisk.recommendations" :key="item">{{ item }}</p>
              <el-button
                data-testid="record-risk-interview"
                type="primary"
                @click="recordRiskInterview"
                >记录教师面谈</el-button
              >
              <section data-testid="risk-timeline">
                <h4>干预时间线</h4>
                <p v-for="item in selectedRisk.timeline" :key="item">{{ item }}</p>
              </section>
            </aside>
          </div>
        </section>
      </section>
      <HumanConfirmationDialog
        v-model="confirmationOpen"
        :title="'确认执行方案 ' + selectedPlanId"
        confirmer="张老师"
        action-label="确认执行"
        :initial-reason="reason"
        @confirm="applyConfirmation"
      /><EvidenceLedgerDrawer
        v-model="ledgerOpen"
        title="教学干预实验"
        :ledger="interventionLedger"
      />
    </template>

<style scoped lang="scss">
.intervention-lab-page {
  min-height: calc(100vh - 110px);
  min-width: 0;
  color: #17233c;
}
.page-heading,
.heading-copy,
.heading-actions,
.section-title,
.problem-strip,
.shared-domain-strip,
.decision-footer {
  display: flex;
  align-items: center;
}
.page-heading {
  justify-content: space-between;
  gap: 18px;
  margin: 0 0 10px;
}
.heading-copy {
  gap: 14px;
}
.heading-icon {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 9px;
  background: #eef3ff;
  color: #2f66eb;
  font-size: 18px;
}
.heading-copy h1 {
  margin: 0;
  font-size: 24px;
}
.heading-copy p {
  margin: 3px 0 0;
  color: #728099;
  font-size: 12px;
}
.heading-actions {
  gap: 10px;
}
.panel {
  border: 1px solid #e4eaf3;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 5px 16px rgb(30 64 120 / 4%);
}
h2,
h3,
p {
  margin: 0;
}
.problem-strip {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 0;
  padding: 10px 12px;
  margin-bottom: 9px;
}
.problem-strip article {
  position: relative;
  display: grid;
  gap: 4px;
  min-height: 72px;
  padding: 0 12px;
  border-right: 1px solid #edf0f5;
}
.problem-strip article:last-child {
  border: 0;
}
.problem-strip span {
  color: #78869b;
  font-size: 12px;
}
.problem-strip strong {
  font-size: 12px;
  line-height: 1.45;
}
.problem-strip p,
.problem-strip li {
  color: #64758d;
  font-size: 11px;
  line-height: 1.5;
}
.problem-strip ul {
  margin: 0;
  padding-left: 15px;
}
.shared-domain-strip {
  gap: 14px;
  padding: 7px 12px;
  margin-bottom: 9px;
  border: 1px solid #cbdafa;
  border-radius: 8px;
  background: #f4f7ff;
  color: #55719c;
  font-size: 11px;
}
.shared-domain-strip strong {
  color: #2d65d6;
}
.shared-domain-strip span {
  flex: 1;
}
.shared-domain-strip small {
  color: #2d65d6;
}
.main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 10px;
  align-items: start;
}
.plan-section {
  padding-bottom: 12px;
}
.section-title {
  justify-content: space-between;
  gap: 8px;
  padding: 10px 12px;
  border-bottom: 1px solid #edf0f5;
}
.section-title h2 {
  font-size: 14px;
}
.section-title > span {
  margin-left: auto;
  color: #8290a5;
  font-size: 10px;
}
.plan-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  padding: 10px;
}
.plan-card {
  position: relative;
  display: grid;
  min-width: 0;
  min-height: 250px;
  grid-template-rows: auto auto minmax(32px, auto) auto 1fr auto;
  gap: 5px;
  padding: 10px 11px;
  border: 1px solid #e5eaf2;
  border-radius: 8px;
  cursor: pointer;
}
.plan-card.active {
  border-color: #79a2ff;
  box-shadow: 0 0 0 1px #79a2ff;
  background: #fbfdff;
}
.plan-radio {
  position: absolute;
  top: 13px;
  left: 11px;
  width: 14px;
  height: 14px;
  border: 2px solid #cbd4e3;
  border-radius: 50%;
}
.plan-card.active .plan-radio {
  border-color: #3974ef;
  box-shadow: inset 0 0 0 3px #fff;
  background: #3974ef;
}
.plan-card h3 {
  margin-left: 22px;
  font-size: 14px;
}
.plan-card p {
  color: #62728b;
  font-size: 11px;
  line-height: 1.55;
}
.plan-card > strong {
  font-size: 11px;
  color: #354965;
}
.plan-card dl {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 4px;
  margin: 2px 0;
  color: #6a7890;
  font-size: 10px;
}
.plan-card dt,
.plan-card dd {
  margin: 0;
}
.plan-card dd {
  color: #283a55;
  font-weight: 700;
}
.plan-card .el-button {
  margin-top: auto;
  width: 100%;
}
.plan-hint {
  padding: 7px 10px;
  margin: 0 10px;
  border-top: 1px solid #edf0f5;
  background: #f8faff;
  color: #6f809a;
  font-size: 10px;
}
.settings {
  display: grid;
  gap: 9px;
  padding-bottom: 10px;
}
.settings label {
  display: grid;
  gap: 4px;
  padding: 0 12px;
  color: #53647c;
  font-size: 11px;
}
.settings > p {
  padding: 0 12px;
  color: #79879c;
  font-size: 10px;
}
.settings .el-checkbox {
  display: flex;
  min-height: 24px;
  padding: 0 12px;
  font-size: 11px;
}
.settings .el-checkbox b {
  margin-left: auto;
  color: #516581;
}
.settings .el-radio-group {
  display: grid;
  gap: 7px;
}
.settings small {
  padding: 0 12px;
  color: #8794a8;
  font-size: 10px;
}
.settings :deep(.el-input__wrapper) {
  min-height: 28px;
  padding: 0 8px;
}
.settings :deep(.el-radio) {
  min-height: 22px;
  margin-right: 0;
}
.decision-panel {
  margin-top: 10px;
  padding: 12px;
}
.decision-panel > h2 {
  font-size: 14px;
}
.decision-panel > p {
  margin: 4px 0 7px;
  color: #718198;
  font-size: 11px;
}
.decision-panel > .el-input {
  display: block;
  margin-bottom: 8px;
}
.decision-panel :deep(.el-textarea__inner) {
  min-height: 42px !important;
  padding: 7px 9px;
  font-size: 12px;
}
.decision-footer {
  gap: 11px;
  padding: 8px 0;
  border-top: 1px solid #edf0f5;
}
.decision-footer span {
  color: #718198;
  font-size: 11px;
}
.decision-footer span:nth-child(2) {
  padding-left: 11px;
  border-left: 1px solid #edf0f5;
}
.decision-footer span:nth-child(3) {
  padding-left: 11px;
  border-left: 1px solid #edf0f5;
}
.decision-footer .el-button:first-of-type {
  margin-left: auto;
}
.decision-panel [data-testid='teacher-ledger'] {
  margin-top: 6px;
  color: #567094;
  font-size: 11px;
}
.decision-panel [data-testid='human-confirmation-state'] {
  color: #b36a14;
}
.execution-grid {
  display: grid;
  grid-template-columns: minmax(320px, 0.9fr) minmax(0, 1.4fr) minmax(320px, 0.9fr);
  gap: 10px;
  min-width: 0;
  margin-top: 10px;
}
.execution-grid > .panel {
  min-width: 0;
  overflow: hidden;
}
.progress-steps {
  padding: 3px 12px;
}
.progress-steps p {
  display: grid;
  grid-template-columns: 24px 1fr 48px 86px;
  gap: 6px;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px solid #edf0f5;
  font-size: 10px;
}
.progress-steps p:last-child {
  border: 0;
}
.progress-steps b {
  display: grid;
  width: 22px;
  height: 22px;
  place-items: center;
  border-radius: 50%;
  background: #2e6be8;
  color: #fff;
}
.progress-steps p:nth-child(4) b {
  background: #a8b4c6;
}
.progress-steps span {
  color: #2aa366;
}
.progress-steps small {
  color: #75849a;
}
.execution-grid table {
  width: 100%;
  border-collapse: collapse;
  font-size: 10px;
}
.execution-grid th,
.execution-grid td {
  padding: 7px 6px;
  border-bottom: 1px solid #edf0f5;
  text-align: left;
  white-space: nowrap;
}
.execution-grid th {
  background: #f8faff;
  color: #718096;
}
.execution-grid td {
  color: #53647f;
}
.deviation-panel > p {
  display: grid;
  grid-template-columns: 1.2fr auto;
  gap: 4px;
  padding: 7px 12px;
  border-bottom: 1px solid #edf0f5;
  color: #62738c;
  font-size: 10px;
}
.deviation-panel p em {
  grid-column: 2;
  color: #ef4f4c;
  font-style: normal;
}
.deviation-panel p b {
  grid-column: 2;
  color: #21a065;
}
.risk-and-next-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(300px, 1fr);
  gap: 10px;
  margin-top: 10px;
  align-items: stretch;
}
.risk-and-next-grid > .panel {
  min-width: 0;
  overflow: hidden;
}
.risk-student-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  padding: 10px 12px 12px;
}
.risk-student-item {
  display: grid;
  min-width: 0;
  align-content: start;
  gap: 6px;
  padding: 9px 10px;
  border: 1px solid #e5eaf2;
  border-radius: 7px;
  background: #fbfcff;
}
.risk-student-item__head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  grid-template-rows: auto auto;
  gap: 3px 6px;
  align-items: center;
}
.risk-student-item__head span {
  min-width: 0;
  overflow: hidden;
  color: #2d4261;
  font-size: 11px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.risk-student-item__head em {
  color: #ef4f4c;
  font-size: 10px;
  font-style: normal;
}
.risk-student-item__head em.orange {
  color: #eb9715;
}
.risk-student-item__head a {
  grid-column: 2;
  grid-row: 1 / 3;
  color: #2f6be8;
  font-size: 10px;
  white-space: nowrap;
}
.risk-student-item > p {
  display: block;
  padding: 0;
  border: 0;
  color: #78879c;
  font-size: 10px;
  line-height: 1.5;
}
.next-round {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.next-round__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px 9px;
  border-bottom: 1px solid #edf0f5;
}
.next-round__title {
  min-width: 0;
}
.next-round__eyebrow {
  display: block;
  margin-bottom: 3px;
  color: #6f7f98;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.04em;
}
.next-round .section-title {
  justify-content: flex-start;
  padding: 0;
  border-bottom: 0;
}
.next-round .section-title h2 {
  font-size: 16px;
}
.next-round .section-title .el-tag {
  margin-left: 7px;
}
.next-round__lead {
  margin-top: 4px;
  color: #62738d;
  font-size: 11px;
  line-height: 1.45;
}
.next-round__confidence {
  display: grid;
  min-width: 108px;
  justify-items: end;
  gap: 2px;
  padding-left: 10px;
  border-left: 1px solid #edf0f5;
  color: #718198;
  font-size: 10px;
}
.next-round__confidence strong {
  color: #2467ef;
  font-size: 21px;
  line-height: 1.1;
}
.next-round__confidence small {
  color: #8a97aa;
  font-size: 10px;
  white-space: normal;
  text-align: right;
}
.next-round__grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0;
  padding: 5px 12px;
  background: #f8faff;
}
.next-round__item {
  display: grid;
  grid-template-columns: 54px minmax(0, 1fr);
  grid-template-rows: auto auto;
  min-width: 0;
  align-content: start;
  gap: 2px 7px;
  padding: 6px 0;
  border: 0;
  border-bottom: 1px solid #e5eaf2;
  background: transparent;
}
.next-round__item:last-child {
  border-bottom: 0;
}
.next-round__item span {
  grid-row: 1 / 3;
  align-self: start;
  color: #7b899e;
  font-size: 10px;
}
.next-round__item strong {
  min-width: 0;
  color: #2b3f5c;
  font-size: 11px;
  line-height: 1.4;
}
.next-round__item p,
.next-round__item small {
  min-width: 0;
  color: #687991;
  font-size: 10px;
  line-height: 1.4;
}
.next-round__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 7px;
  padding: 8px 12px 10px;
}
.next-round__hint,
.next-round__feedback {
  min-width: 0;
  color: #728199;
  width: 100%;
  font-size: 10px;
  line-height: 1.4;
}
.next-round__feedback {
  color: #2a8c5a;
}
.next-round__actions {
  display: flex;
  width: 100%;
  justify-content: flex-end;
  gap: 7px;
}
.next-round__actions .el-button {
  min-height: 28px;
  padding: 5px 10px;
}
.effect-distribution {
  margin-top: 10px;
}
.distribution-bar {
  display: flex;
  height: 12px;
  margin: 10px 12px 6px;
  gap: 2px;
}
.distribution-bar i {
  height: 100%;
  background: #22a461;
}
.distribution-bar i:nth-child(2) {
  width: 42%;
  background: #5790ed;
}
.distribution-bar i:nth-child(3) {
  width: 17%;
  background: #efa617;
}
.distribution-bar i:nth-child(4) {
  width: 8%;
  background: #ed504c;
}
.effect-distribution p {
  padding: 0 12px 10px;
  color: #63738d;
  font-size: 10px;
}
.smartbi-hidden,
.legacy-risk {
  display: none;
}
.export-feedback {
  margin: -8px 0 10px;
  color: #2e6be8;
  font-size: 12px;
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
@media (max-width: 1200px) {
  .problem-strip {
    grid-template-columns: repeat(3, 1fr);
    row-gap: 12px;
  }
  .problem-strip article:nth-child(3) {
    border: 0;
  }
  .plan-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .main-grid {
    grid-template-columns: 1fr;
  }
  .execution-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .risk-and-next-grid {
    grid-template-columns: 1fr;
  }
  .risk-student-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
@media (max-width: 800px) {
  .page-heading {
    align-items: flex-start;
    flex-direction: column;
  }
  .problem-strip,
  .plan-grid,
  .execution-grid,
  .risk-and-next-grid,
  .risk-student-grid {
    grid-template-columns: 1fr;
  }
  .problem-strip article {
    border: 0;
    border-bottom: 1px solid #edf0f5;
    padding: 10px 0;
  }
  .decision-footer {
    align-items: flex-start;
    flex-wrap: wrap;
  }
  .decision-footer .el-button:first-of-type {
    margin-left: 0;
  }
  .next-round__header,
  .next-round__footer {
    align-items: flex-start;
    flex-direction: column;
  }
  .next-round__confidence {
    width: 100%;
    justify-items: start;
    padding-top: 12px;
    padding-left: 0;
    border-top: 1px solid #edf0f5;
    border-left: 0;
  }
  .next-round__confidence small {
    white-space: normal;
  }
  .next-round__grid {
    grid-template-columns: 1fr;
  }
  .next-round__actions {
    width: 100%;
  }
  .next-round__actions .el-button {
    flex: 1;
  }
}
</style>
