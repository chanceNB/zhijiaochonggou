<!-- VISUAL REFERENCE ONLY.
Source: src/pages/student/StudentAiCoachPage.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section data-test="coach-page" class="student-ai-coach-page">
    <div class="coach-page-header">
      <el-alert v-if="coach.error" :title="coach.error" type="error" show-icon />
      <el-alert
        v-if="transferError"
        data-test="transfer-error"
        :title="transferError"
        type="error"
        show-icon
      />
      <div v-if="transferSubmitting" data-test="transfer-loading" class="transfer-state">
        正在转给老师...
      </div>
      <div v-if="transferResult" data-test="transfer-result" class="transfer-state success">
        <span>转交单</span>
        <strong>{{ transferResult.id }}</strong>
        <em>{{ transferResult.status }}</em>
      </div>

      <section v-if="!realCoachMode()" class="v3-coach-brief" data-test="v3-coach-diagnosis">
        <article>
          <span class="brief-icon" aria-hidden="true"
            ><el-icon><DataAnalysis /></el-icon
          ></span>
          <span>主动诊断</span>
          <strong>{{ coach.context.currentKnowledge }}</strong>
          <small>置信度 78% · 证据不足时先诊断再讲解</small>
        </article>
        <article data-test="v3-coach-misconception">
          <span class="brief-icon" aria-hidden="true"
            ><el-icon><WarningFilled /></el-icon
          ></span>
          <span>主要误概念</span>
          <strong>忽略边界条件</strong>
          <small>已关联错题证据与前置知识复习</small>
        </article>
        <article data-test="v3-coach-transfer-practice">
          <span class="brief-icon" aria-hidden="true"
            ><el-icon><MagicStick /></el-icon
          ></span>
          <span>迁移练习</span>
          <strong>生成跨情境验证题</strong>
          <small>验证结果会更新误概念地图</small>
        </article>
      </section>
    </div>
    <div class="coach-shell" :class="contextDrawerOpen ? `drawer-open` : ``">
      <button
        data-test="context-drawer-toggle"
        class="context-toggle"
        type="button"
        @click="toggleContextDrawer"
      >
        <el-icon><Files /></el-icon>
        来源与过程
      </button>

      <div data-test="session-column" class="session-column">
        <CoachSessionList
          :sessions="coach.sessions"
          :active-session-id="coach.activeSessionId"
          :loading="coach.loading"
          @select="selectSession"
          @create="createSession"
        />
      </div>

      <main data-test="conversation-column" class="conversation-column composer-safe">
        <CoachConversation
          :messages="activeMessages"
          :active-answer="coach.activeAnswer"
          :loading="coach.loading"
          :real-mode="realCoachMode()"
          :active-diagnostic-action="coach.activeDiagnosticAction"
          :workflow-status="coach.workflowStatus"
          :diagnostic-submitting="coach.submittingDiagnostic"
          @answer-action="handleAnswerAction"
          @submit-diagnostic="submitDiagnosticAnswer"
        />
        <CoachComposer :sending="coach.sending" @submit="submitQuestion" />
      </main>

      <aside data-test="context-column" class="context-column">
        <AnalysisProcessPanel
          :steps="analysisSteps"
          :expanded="analysisExpanded"
          @toggle="toggleAnalysisPanel"
        />
        <SourceReferencePanel
          :answer="coach.activeAnswer"
          :context="coach.context"
          :real-mode="realCoachMode()"
        />
      </aside>
    </div>

    <TransferTeacherDialog
      v-model="transferDialogOpen"
      :payload="transferPayload"
      :submitting="transferSubmitting"
      @confirm-transfer="confirmTransfer"
    />
  </section>
</template>

<style scoped lang="scss">
.student-ai-coach-page {
  --coach-ink: #20252c;
  --coach-muted: #68707b;
  --coach-blue: #2c5ee9;
  --coach-green: #219653;
  --coach-orange: #ed831c;
  --coach-purple: #7255d8;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 12px;
  height: calc(100dvh - 104px);
  min-height: 720px;
  color: var(--coach-ink);
  font-family: Inter, 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
}

.coach-page-header {
  display: grid;
  gap: 12px;
  min-height: 0;
}

.student-ai-coach-page::before {
  content: '';
  position: fixed;
  inset: 72px 0 0;
  z-index: -1;
  pointer-events: none;
  opacity: 0.22;
  background-image: none;
}

.transfer-state {
  display: inline-flex;
  gap: 10px;
  align-items: center;
  width: fit-content;
  min-height: 32px;
  padding: 0 12px;
  border: 1.5px solid #80a5f1;
  border-radius: 8px;
  color: #1d4eaf;
  background: #f5f8ff;
  font-size: 13px;
}

.transfer-state.success {
  border-color: #7dc795;
  color: #227a45;
  background: #f2fbf4;
}

.transfer-state em {
  font-style: normal;
  font-weight: 700;
}

.v3-coach-brief {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.v3-coach-brief article {
  position: relative;
  display: grid;
  gap: 4px;
  min-width: 0;
  min-height: 112px;
  padding: 14px 38px 13px 68px;
  overflow: hidden;
  border: 1.5px solid #313945;
  border-radius: 8px;
  background: rgb(255 255 252 / 88%);
  box-shadow: 2px 2px 0 rgb(66 68 75 / 8%);
}

.v3-coach-brief article::before {
  position: absolute;
  top: 22px;
  left: 18px;
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border: 2px solid currentcolor;
  border-radius: 50% 45% 55% 42%;
  font-family: Inter, 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  font-size: 21px;
  font-weight: 800;
}

.v3-coach-brief article:nth-child(1)::before {
  content: '♧';
  color: var(--coach-green);
}
.v3-coach-brief article:nth-child(2)::before {
  content: '!';
  color: var(--coach-orange);
  border-radius: 46% 54% 40% 58%;
}
.v3-coach-brief article:nth-child(3)::before {
  content: '☷';
  color: var(--coach-purple);
  border-radius: 52% 42% 56% 44%;
}

.v3-coach-brief span {
  color: var(--coach-green);
  font-size: 16px;
  font-weight: 800;
}

.v3-coach-brief article:nth-child(2) span {
  color: var(--coach-orange);
}
.v3-coach-brief article:nth-child(3) span {
  color: var(--coach-purple);
}

.v3-coach-brief strong {
  overflow: hidden;
  color: #1e2730;
  font-family: inherit;
  font-size: 18px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.v3-coach-brief small {
  color: #5d6570;
  font-size: 13px;
  line-height: 1.35;
}

.coach-shell {
  position: relative;
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr) 390px;
  gap: 14px;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.session-column,
.conversation-column,
.context-column {
  min-width: 0;
  min-height: 0;
  overflow: auto;
}

.session-column :deep(.coach-session-list) {
  display: grid;
  align-content: start;
  gap: 10px;
  height: 100%;
  padding: 13px 11px;
  border: 1.5px solid #313945;
  border-radius: 8px;
  background: rgb(255 255 252 / 86%);
  box-shadow: 2px 2px 0 rgb(66 68 75 / 7%);
}

.session-column :deep(.session-header) {
  padding: 0 5px 5px;
  border-bottom: 1px solid #e1e1dc;
}

.session-column :deep(.session-header h2) {
  color: #1e2731;
  font-family: Inter, 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  font-size: 21px;
}

.session-column :deep(.icon-button) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: auto;
  min-width: 88px;
  height: 36px;
  gap: 6px;
  padding-inline: 10px;
  border: 1.5px solid var(--coach-blue);
  border-radius: 8px;
  color: var(--coach-blue);
  background: #fff;
  font-size: 15px;
  white-space: nowrap;
}

.session-column :deep(.session-card) {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 6px;
  min-height: 76px;
  padding: 10px;
  border: 1px solid #a6adb9;
  border-radius: 8px;
  text-align: left;
  background: #fff;
}

.session-column :deep(.session-card.active) {
  border-color: var(--coach-blue);
  background: #f2f6ff;
  box-shadow: inset 3px 0 0 var(--coach-blue);
}

.session-column :deep(.session-title) {
  color: #1f3045;
  font-size: 15px;
}
.session-column :deep(.session-time),
.session-column :deep(.session-card small) {
  color: #656e7a;
  font-size: 12px;
}
.session-column :deep(.view-all) {
  color: #1e55d0;
  font-size: 14px;
}

.conversation-column {
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
  gap: 8px;
  overflow: hidden;
  border: 1.5px solid #313945;
  border-radius: 8px;
  background: rgb(255 255 252 / 72%);
}

.composer-safe {
  padding-bottom: 0;
}
.conversation-column > :last-child {
  margin: 0 9px 9px;
}

.conversation-column :deep(.coach-conversation) {
  height: 100%;
  display: grid;
  align-content: start;
  gap: 11px;
  min-height: 0;
  padding: 12px 14px;
  overflow: auto;
}

.conversation-column :deep(.coach-conversation) {
  display: flex;
  flex-direction: column;
}

.conversation-column :deep(.message-row.student) {
  margin-top: 1px;
}
.conversation-column :deep(.avatar) {
  border-color: #8e98a6;
  color: #26364f;
  background: #fbfbf8;
}
.conversation-column :deep(.message-row.student .avatar) {
  border-color: #7d94be;
  background: #edf3ff;
}
.conversation-column :deep(.bubble) {
  max-width: 760px;
  border: 1px solid #9aa2ad;
  border-radius: 8px;
  color: #202832;
  background: #fff;
  box-shadow: 1px 1px 0 rgb(66 68 75 / 7%);
}
.conversation-column :deep(.message-row.student .bubble) {
  border-color: #7293ec;
  background: #eef4ff;
}
.conversation-column :deep(time) {
  color: #777f89;
}

.conversation-column :deep(.linked-list-diagram) {
  border-color: #9aa2ad;
  border-radius: 8px;
  background: #fafbf8;
}
.conversation-column :deep(.node) {
  border-color: #7d9be1;
  border-radius: 8px;
  color: #1f4bb9;
  background: #f6f9ff;
}
.conversation-column :deep(.node.target) {
  border-color: #e4a24a;
  color: #9a5e13;
  background: #fff9ed;
}
.conversation-column :deep(.linked-list-diagram i) {
  background: #6f9bea;
}
.conversation-column :deep(.markdown-body) {
  color: #242d37;
  font-size: 14px;
  line-height: 1.42;
}
.conversation-column :deep(.markdown-body pre) {
  max-height: 194px;
  overflow: auto;
  padding: 9px 11px;
  border-color: #a4adb9;
  border-radius: 8px;
  background: #fbfcfa;
  font-size: 12px;
  line-height: 1.3;
}

.conversation-column :deep(.answer-actions) {
  position: sticky;
  bottom: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(128px, 1fr));
  gap: 8px;
  width: 100%;
  padding: 9px 0 3px;
  background: linear-gradient(180deg, transparent, #fdfdfb 32%);
}

.conversation-column :deep(.answer-actions button) {
  min-width: 0;
  justify-content: center;
  min-height: 36px;
  padding: 0 10px;
  border: 1.5px solid var(--coach-blue);
  border-radius: 8px;
  color: var(--coach-blue);
  background: #fff;
  font-family: Inter, 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  font-size: 15px;
}

.conversation-column :deep(.answer-actions button:nth-child(1)) {
  border-color: var(--coach-green);
  color: var(--coach-green);
}
.conversation-column :deep(.answer-actions button:nth-child(3)) {
  color: #fff;
  background: var(--coach-blue);
}

.conversation-column :deep(.coach-composer) {
  grid-template-columns: 42px minmax(0, 1fr) 48px;
  gap: 9px;
  padding: 9px 11px;
  border: 1.5px solid #313945;
  border-radius: 8px;
  background: #fff;
}

.conversation-column :deep(.coach-composer textarea) {
  min-height: 46px;
  font-family: Inter, 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  font-size: 16px;
}
.conversation-column :deep(.tool-button),
.conversation-column :deep(.send-button) {
  border-radius: 8px;
}
.conversation-column :deep(.send-button) {
  border-color: var(--coach-blue);
  background: var(--coach-blue);
}

.context-column {
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow: hidden;
}

.context-column :deep(.analysis-process-panel) {
  flex: 0 0 auto;
}

.context-column :deep(.source-reference-panel) {
  flex: 1 1 auto;
  min-height: 0;
  overflow: auto;
}

.context-column :deep(.reference-details) {
  min-height: 100%;
}

.context-column :deep(.analysis-toggle) {
  min-height: 38px;
  padding: 0 12px;
  border: 1.5px solid var(--coach-blue);
  border-radius: 8px;
  color: var(--coach-blue);
  background: #fff;
  font-family: Inter, 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  font-size: 15px;
}

.context-column :deep(.analysis-detail),
.context-column :deep(.panel-card) {
  padding: 13px;
  border: 1.5px solid #313945;
  border-radius: 8px;
  background: rgb(255 255 252 / 88%);
  box-shadow: 1px 1px 0 rgb(66 68 75 / 7%);
}

.context-column :deep(.panel-card h3) {
  color: #202832;
  font-family: Inter, 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  font-size: 19px;
}
.context-column :deep(.source-card) {
  border: 1.5px solid #83c29b;
  border-radius: 8px;
  background: #f7fff8;
}
.context-column :deep(.source-card:nth-of-type(2)) {
  border-color: #edb56a;
  background: #fffaf1;
}
.context-column :deep(.source-card:nth-of-type(3)) {
  border-color: #a998ef;
  background: #fbf9ff;
}
.context-column :deep(.source-type) {
  padding: 2px 6px;
  border-radius: 6px;
  color: #197749;
  background: transparent;
}
.context-column :deep(.source-card:nth-of-type(2) .source-type) {
  color: #b66a0c;
}
.context-column :deep(.source-card:nth-of-type(3) .source-type) {
  color: #6846c6;
}
.context-column :deep(.source-card b) {
  color: #1e824c;
  background: transparent;
  font-size: 18px;
}
.context-column :deep(.source-card small) {
  color: #515b68;
}
.context-column :deep(.context-list) {
  gap: 10px;
}
.context-column :deep(dt) {
  color: #69737f;
  font-size: 13px;
}
.context-column :deep(dd) {
  color: #273240;
  font-size: 13px;
}
.context-column :deep(.progress) {
  background: #e7e9e6;
}
.context-column :deep(.progress i) {
  background: #68b96e;
}
.context-column :deep(.knowledge-list li) {
  color: #303a46;
}
.context-column :deep(.knowledge-list em) {
  border-color: #ec6c61;
  color: #d94a3e;
  background: #fff;
}
.context-column :deep(.panel-card button) {
  border-color: var(--coach-blue);
  border-radius: 8px;
  color: var(--coach-blue);
  background: #fff;
  font-family: Inter, 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
}

.context-toggle {
  display: none;
  min-height: 36px;
  padding: 0 12px;
  border: 1.5px solid var(--coach-blue);
  border-radius: 8px;
  color: var(--coach-blue);
  background: #fff;
}

@media (max-width: 1320px) {
  .coach-shell {
    grid-template-columns: 280px minmax(0, 1fr) 340px;
    gap: 10px;
  }
}

@media (max-width: 1180px) {
  .student-ai-coach-page {
    height: auto;
    min-height: calc(100dvh - 90px);
  }
  .context-toggle {
    position: absolute;
    top: 8px;
    right: 8px;
    z-index: 7;
    display: inline-grid;
    grid-auto-flow: column;
    gap: 8px;
    align-items: center;
  }
  .coach-shell {
    grid-template-columns: 250px minmax(0, 1fr);
  }
  .context-column {
    position: absolute;
    top: 0;
    right: 0;
    z-index: 6;
    width: min(350px, calc(100vw - 32px));
    max-height: calc(100dvh - 140px);
    overflow: auto;
    transform: translateX(calc(100% + 12px));
    transition: transform 0.2s ease;
  }
  .coach-shell.drawer-open .context-column {
    transform: translateX(0);
  }
}

@media (max-width: 760px) {
  .student-ai-coach-page {
    min-height: calc(100dvh - 80px);
  }
  .coach-shell,
  .v3-coach-brief {
    grid-template-columns: 1fr;
  }
  .session-column {
    max-height: 250px;
  }
  .context-column {
    position: fixed;
    top: 88px;
    right: 12px;
    width: min(390px, calc(100vw - 24px));
  }
  .conversation-column :deep(.answer-actions) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

/* Production UI overrides: preserve the three-column workflow without legacy paper styling. */
.student-ai-coach-page {
  --coach-ink: #182230;
  --coach-muted: #66758b;
  --coach-blue: #2563eb;
  --coach-green: #16835b;
  --coach-orange: #c97913;
  --coach-purple: #6d4bd8;
  font-family: Inter, 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  background: #f4f7fb;
}
.student-ai-coach-page::before {
  display: none;
}
.student-ai-coach-page :deep(*) {
  box-sizing: border-box;
}
.student-ai-coach-page :deep(button),
.student-ai-coach-page :deep(textarea),
.student-ai-coach-page :deep(input) {
  font-family: inherit;
}
.v3-coach-brief article,
.session-column :deep(.coach-session-list),
.conversation-column,
.context-column :deep(.analysis-detail),
.context-column :deep(.panel-card) {
  border: 1px solid #dce4ef;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 4px 14px rgb(25 52 87 / 5%);
}
.v3-coach-brief article {
  min-height: 104px;
  padding: 16px 18px 15px 56px;
}
.v3-coach-brief article::before {
  display: none;
}
.v3-coach-brief article::after {
  position: absolute;
  top: 18px;
  left: 18px;
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  border-radius: 7px;
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  background: var(--coach-green);
  content: 'AI';
}
.v3-coach-brief article:nth-child(2)::after {
  background: #d68a20;
  content: '!';
}
.v3-coach-brief article:nth-child(3)::after {
  background: #7554d8;
  content: '→';
}
.v3-coach-brief span {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
}
.v3-coach-brief strong {
  font-family: inherit;
  font-size: 17px;
}
.v3-coach-brief small {
  color: var(--coach-muted);
}
.coach-shell {
  grid-template-columns: 272px minmax(0, 1fr) 336px;
  gap: 12px;
}
.session-column :deep(.coach-session-list) {
  padding: 12px;
}
.session-column :deep(.session-header h2),
.context-column :deep(.panel-card h3) {
  font-family: inherit;
  font-size: 16px;
  font-weight: 700;
}
.session-column :deep(.icon-button),
.context-column :deep(.analysis-toggle),
.conversation-column :deep(.answer-actions button),
.context-column :deep(.panel-card button) {
  border: 1px solid #cbd8ea;
  border-radius: 7px;
  font-family: inherit;
}
.session-column :deep(.icon-button) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: auto;
  min-width: 88px;
  gap: 6px;
  padding: 0 12px;
  color: #1f5edb;
  white-space: nowrap;
}
.session-column :deep(.session-card) {
  border: 1px solid #e0e7f1;
  border-radius: 7px;
}
.session-column :deep(.session-card.active) {
  border-color: #7da7f4;
  background: #f1f6ff;
  box-shadow: inset 3px 0 #2563eb;
}
.conversation-column {
  overflow: hidden;
}
.conversation-column :deep(.coach-conversation) {
  padding: 16px;
  scrollbar-width: none;
}

.conversation-column :deep(.coach-conversation)::-webkit-scrollbar {
  display: none;
}
.conversation-column :deep(.bubble) {
  border: 1px solid #e0e7f1;
  border-radius: 8px;
  box-shadow: none;
}
.conversation-column :deep(.message-row.student .bubble) {
  border-color: #bcd2fa;
  background: #f1f6ff;
}
.conversation-column :deep(.answer-actions) {
  background: linear-gradient(180deg, transparent, #fff 35%);
}
.conversation-column :deep(.answer-actions button) {
  min-height: 34px;
  padding: 0 11px;
  color: #245fc9;
  background: #fff;
}
.conversation-column :deep(.answer-actions button:nth-child(3)) {
  color: #fff;
  border-color: #2563eb;
  background: #2563eb;
}
.conversation-column :deep(.coach-composer) {
  border: 1px solid #dce4ef;
  border-radius: 8px;
  box-shadow: 0 3px 12px rgb(25 52 87 / 5%);
}
.conversation-column :deep(.coach-composer textarea) {
  min-height: 48px;
  font-family: inherit;
}
.context-column :deep(.source-card) {
  border: 1px solid #dce4ef;
  border-radius: 7px;
  background: #f8fbff;
}
.context-column :deep(.source-card:nth-of-type(2)) {
  border-color: #f0d7aa;
  background: #fffaf2;
}
.context-column :deep(.source-card:nth-of-type(3)) {
  border-color: #d8cdf6;
  background: #fbf9ff;
}
.context-column :deep(.analysis-toggle) {
  color: #245fc9;
  background: #fff;
}
.transfer-state {
  border: 1px solid #bfd2f4;
  border-radius: 7px;
  font-family: inherit;
}
@media (max-width: 1320px) {
  .coach-shell {
    grid-template-columns: 248px minmax(0, 1fr) 316px;
  }
}

/* Keep the coach surface production-grade across every nested legacy panel. */
.v3-coach-brief article,
.session-column :deep(.coach-session-list),
.session-column :deep(.session-card),
.conversation-column,
.conversation-column :deep(.bubble),
.conversation-column :deep(.linked-list-diagram),
.conversation-column :deep(.node),
.conversation-column :deep(.markdown-body pre),
.conversation-column :deep(.answer-actions button),
.conversation-column :deep(.coach-composer),
.context-column :deep(.analysis-toggle),
.context-column :deep(.analysis-detail),
.context-column :deep(.panel-card),
.context-column :deep(.source-card),
.context-column :deep(.source-type),
.context-column :deep(.panel-card button),
.context-toggle,
.transfer-state {
  border-radius: 8px;
}
.v3-coach-brief article {
  padding-left: 18px;
}
.v3-coach-brief article::after,
.v3-coach-brief article::before {
  display: none;
}
.conversation-column :deep(.answer-actions) {
  background: #fff;
}

.conversation-column :deep(.tool-button),
.conversation-column :deep(.send-button) {
  border-radius: 8px;
}

@media (max-width: 760px) {
  .coach-shell,
  .v3-coach-brief {
    grid-template-columns: 1fr;
  }
  .coach-shell {
    min-width: 0;
  }
  .v3-coach-brief article {
    width: auto;
  }
}

/* High-fidelity desktop geometry for the latest coach prototype. */
.student-ai-coach-page {
  height: calc(100dvh - 90px);
  min-height: 0;
  gap: 16px;
  background: #f7f9fc;
}

.v3-coach-brief article {
  min-height: 106px;
  padding: 16px 18px 15px 68px;
  border-color: #e1e7f0;
  box-shadow: 0 2px 8px rgb(35 66 111 / 4%);
}

.v3-coach-brief article::before,
.v3-coach-brief article::after {
  display: none;
}

.brief-icon {
  position: absolute;
  top: 19px;
  left: 17px;
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border: 1px solid #b8ead8;
  border-radius: 50%;
  color: #1aa071;
  background: #f0fbf6;
}

.brief-icon .el-icon {
  font-size: 20px;
}

.v3-coach-brief article:nth-child(2) .brief-icon {
  border-color: #ffd49c;
  color: #df8617;
  background: #fff8ed;
}

.v3-coach-brief article:nth-child(3) .brief-icon {
  border-color: #d9c8ff;
  color: #7453db;
  background: #f8f5ff;
}

.v3-coach-brief span:not(.brief-icon) {
  font-size: 13px;
}

.v3-coach-brief strong {
  font-size: 18px;
}

.coach-shell {
  min-height: 0;
}

.conversation-column :deep(.markdown-body pre) {
  max-height: none;
  min-height: 210px;
}

@media (min-width: 1321px) {
  .v3-coach-brief {
    grid-template-columns: 1.03fr 0.98fr 1.4fr;
  }

  .coach-shell {
    grid-template-columns: 268px minmax(0, 1fr) 358px;
    gap: 12px;
  }

  .context-column :deep(.analysis-toggle) {
    min-height: 54px;
    padding-inline: 14px;
  }

  .context-column {
    overflow: visible;
  }

  .session-column :deep(.icon-button) {
    min-width: 88px;
    justify-content: center;
    gap: 4px;
  }

  .conversation-column :deep(.message-row.student .bubble) {
    width: 412px;
    max-width: 412px;
    font-size: 14px;
    white-space: nowrap;
  }

  .conversation-column :deep(.linked-list-diagram i) {
    position: relative;
  }

  .conversation-column :deep(.linked-list-diagram i)::after {
    position: absolute;
    top: -3px;
    right: 0;
    width: 7px;
    height: 7px;
    border-top: 2px solid currentcolor;
    border-right: 2px solid currentcolor;
    content: '';
    transform: rotate(45deg);
  }
}

@media (max-width: 760px) {
  .student-ai-coach-page {
    width: 100%;
    height: auto;
    min-height: 0;
    gap: 12px;
  }

  .v3-coach-brief {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .v3-coach-brief article {
    min-height: 94px;
    padding: 14px 14px 13px 68px;
  }

  .v3-coach-brief strong {
    font-size: 17px;
  }

  .coach-shell {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: 12px;
    width: 100%;
    min-width: 0;
  }

  .context-toggle {
    position: static;
    grid-row: 1;
    display: inline-flex;
    align-items: center;
    justify-content: flex-start;
    width: 100%;
    min-height: 42px;
    padding-inline: 14px;
  }

  .session-column {
    grid-row: 2;
    max-height: none;
    overflow: visible;
  }

  .session-column :deep(.coach-session-list) {
    height: auto;
    min-height: 220px;
  }

  .conversation-column {
    grid-row: 3;
    min-height: 620px;
    max-height: none;
  }

  .conversation-column :deep(.coach-conversation) {
    min-height: 0;
    padding: 14px 12px;
  }

  .conversation-column :deep(.message-row.student .bubble) {
    max-width: calc(100vw - 92px);
    white-space: normal;
  }

  .conversation-column :deep(.answer-actions) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 6px;
  }

  .conversation-column :deep(.answer-actions button) {
    min-width: 0;
    padding-inline: 7px;
    font-size: 12px;
  }

  .conversation-column :deep(.linked-list-diagram) {
    overflow-x: auto;
  }

  .conversation-column :deep(.coach-composer) {
    grid-template-columns: 40px minmax(0, 1fr) 56px 44px;
    gap: 8px;
    padding: 8px;
  }

  .context-column {
    position: fixed;
    top: 84px;
    right: 12px;
    z-index: 45;
    display: grid;
    width: calc(100vw - 24px);
    max-height: calc(100dvh - 100px);
    overflow: auto;
    transform: translateX(calc(100% + 12px));
  }

  .coach-shell.drawer-open .context-column {
    transform: translateX(0);
  }
}
</style>
