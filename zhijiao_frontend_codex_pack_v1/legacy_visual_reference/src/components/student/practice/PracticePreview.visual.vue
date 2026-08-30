<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/practice/PracticePreview.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="preview-card" data-test="practice-preview">
    <header>
      <div>
        <span class="step-badge">4</span>
        <h2>练习组预览</h2>
      </div>
      <p v-if="preview">
        共 {{ preview.summary.totalCount }} 题，预计用时 {{ preview.summary.estimatedMinutes }} 分钟
      </p>
    </header>
    <p data-test="generation-status" class="status">{{ loading ? '生成中...' : '预览已就绪' }}</p>
    <div v-if="preview && preview.questions.length" class="question-grid">
      <article
        v-for="question in preview.questions"
        :key="question.id"
        data-test="preview-question"
      >
        <div class="question-top">
          <span>{{ question.order }}</span
          ><strong>{{ typeLabels[question.type] }}</strong
          ><small
            >难度：{{ question.difficulty === 'medium' ? '中等' : question.difficulty }}</small
          >
        </div>
        <p>{{ question.stem }}</p>
        <footer>
          <span>来源：{{ sourceLabels[question.source] }}</span
          ><span>{{ question.targetAbility }}</span>
        </footer>
      </article>
    </div>
    <div v-else class="empty-preview">生成后会在这里看到每道题的来源、难度、题型和能力点。</div>
    <span class="compat-copy">练习组预览</span>
  </section>
</template>

<style scoped lang="scss">
.preview-card {
  display: grid;
  gap: 10px;
  padding: 15px;
  border: 1px solid #dbe5f1;
  border-radius: 9px;
  background: #fff;
}
header,
header div {
  display: flex;
  gap: 9px;
  align-items: center;
  justify-content: space-between;
}
.step-badge {
  display: grid;
  width: 25px;
  height: 25px;
  place-items: center;
  border-radius: 5px;
  color: #fff;
  background: #3d72e9;
  font-weight: 900;
}
h2,
p {
  margin: 0;
}
h2 {
  color: #1b315c;
  font-size: 16px;
}
header p,
.status,
.empty-preview {
  color: #74849c;
  font-size: 11px;
}
.question-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}
article {
  display: grid;
  gap: 8px;
  min-height: 135px;
  padding: 10px;
  border: 1px solid #d9e3f0;
  border-radius: 7px;
  background: #fbfdff;
}
.question-top,
footer {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}
.question-top span {
  display: grid;
  width: 22px;
  height: 22px;
  place-items: center;
  border-radius: 4px;
  color: #fff;
  background: #3d72e9;
  font-size: 11px;
  font-weight: 800;
}
.question-top strong {
  color: #3d567b;
  font-size: 11px;
}
small,
footer span {
  color: #71819a;
  font-size: 10px;
}
article p {
  color: #314868;
  font-size: 11px;
  line-height: 1.5;
}
footer {
  align-self: end;
  justify-content: space-between;
}
.compat-copy {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
}
@media (max-width: 1280px) {
  .question-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 1024px) {
  .question-grid {
    grid-template-columns: 1fr;
  }
  header {
    align-items: start;
    flex-direction: column;
  }
}

@media (min-width: 721px) {
  .preview-card {
    min-height: 470px;
    box-sizing: border-box;
    align-content: start;
    grid-template-rows: auto auto minmax(0, 1fr);
    box-shadow: 0 6px 20px rgb(38 64 118 / 5%);
  }

  .preview-card .question-grid {
    margin-top: 8px;
  }
}

.preview-card article {
  min-height: 190px;
  transition:
    border-color 160ms ease,
    box-shadow 160ms ease,
    background-color 160ms ease;
}

.preview-card article:hover {
  border-color: #b9cff4;
  background: #f8fbff;
  box-shadow: 0 5px 14px rgb(47 94 171 / 8%);
}

@media (max-width: 720px) {
  .preview-card {
    min-height: 0;
  }
}
</style>
