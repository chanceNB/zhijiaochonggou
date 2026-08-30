<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/practice/AnswerWorkspace.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="answer-card" data-test="answer-workspace">
    <header>
      <div>
        <h2>开始答题</h2>
        <p v-if="preview">第 {{ activeIndex + 1 }} / {{ preview.questions.length }} 题</p>
      </div>
      <span class="answer-tools">标记收藏 · 下载本题</span>
    </header>
    <div v-if="currentQuestion" class="workspace-body">
      <button class="start-button" data-test="start-answering" type="button">开始答题</button
      ><strong class="question-stem">{{ currentQuestion.stem }}</strong>
      <div v-if="currentQuestion.options?.length" class="options">
        <button
          v-for="option in currentQuestion.options"
          :key="option"
          :data-test="`answer-option-${option.slice(0, 1)}`"
          type="button"
          :class="{ active: selectedAnswer === option.slice(0, 1) }"
          @click="selectedAnswer = option.slice(0, 1)"
        >
          {{ option }}
        </button>
      </div>
      <textarea
        v-else
        v-model="selectedAnswer"
        data-test="free-answer"
        placeholder="在此输入你的解答过程..."
      />
      <div class="answer-actions">
        <button
          data-test="submit-answer"
          type="button"
          :disabled="submitting"
          @click="emit('submit', selectedAnswer)"
        >
          {{ submitting ? '批改中...' : '提交答案' }}</button
        ><button type="button" @click="emit('next')">下一题</button
        ><button data-test="complete-practice" type="button" @click="emit('complete')">
          完成练习
        </button>
      </div>
      <div v-if="currentFeedback" class="feedback" data-test="answer-feedback">
        <strong>{{ currentFeedback.correct ? '回答正确' : '这题先看提示' }}</strong>
        <ol>
          <li v-for="stage in currentFeedback.feedbackStages" :key="stage.stage">
            <span>{{ stage.title }}</span>
            <p>{{ stage.content }}</p>
          </li>
        </ol>
        <button
          v-if="!currentFeedback.correct && currentQuestion"
          data-test="generate-similar-question"
          type="button"
          @click="emit('generateSimilar', currentQuestion.id)"
        >
          生成同类题
        </button>
      </div>
    </div>
    <div v-else class="empty-answer">生成练习组后可以开始答题。</div>
    <span class="compat-copy">开始答题</span>
  </section>
</template>

<style scoped lang="scss">
.answer-card {
  display: grid;
  gap: 12px;
  padding: 15px;
  border: 1px solid #dbe5f1;
  border-radius: 9px;
  background: #fff;
}
header,
.answer-actions {
  display: flex;
  gap: 9px;
  align-items: center;
  justify-content: space-between;
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
.answer-tools,
.empty-answer {
  color: #78879e;
  font-size: 11px;
}
.workspace-body {
  display: grid;
  gap: 11px;
}
.workspace-body > strong {
  color: #273f65;
  line-height: 1.6;
}
.start-button {
  justify-self: start;
  min-width: 92px;
  border: 0;
  color: #fff;
  background: #3d72e9;
}
.options {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 7px;
}
button {
  min-height: 35px;
  border: 1px solid #d3dfed;
  border-radius: 6px;
  color: #385171;
  background: #fff;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.options button.active,
.answer-actions button:first-child {
  border-color: #3d72e9;
  color: #fff;
  background: #3d72e9;
}
textarea {
  min-height: 90px;
  padding: 10px;
  border: 1px solid #cedbea;
  border-radius: 7px;
  resize: vertical;
}
.answer-actions {
  justify-content: flex-end;
}
.answer-actions button:first-child {
  min-width: 100px;
}
.answer-actions button:last-child {
  border-color: #5eb47b;
  color: #2f8a50;
}
.feedback {
  display: grid;
  gap: 8px;
  padding: 12px;
  border: 1px solid #b8dfc5;
  border-radius: 7px;
  background: #f1fbf4;
}
.feedback strong {
  color: #26784b;
}
ol {
  display: grid;
  gap: 6px;
  margin: 0;
  padding-left: 18px;
}
li span {
  color: #28784b;
  font-size: 11px;
  font-weight: 800;
}
li p {
  color: #5d708a;
  font-size: 11px;
  line-height: 1.45;
}
.feedback button {
  justify-self: start;
  border-color: #b4cff7;
  color: #2f6de8;
}
.compat-copy {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
}
@media (max-width: 1024px) {
  header,
  .answer-actions,
  .options {
    display: grid;
    grid-template-columns: 1fr;
    justify-items: start;
  }
}

.answer-card {
  box-shadow: 0 6px 20px rgb(38 64 118 / 5%);
}

.answer-card button {
  transition:
    border-color 160ms ease,
    background-color 160ms ease,
    box-shadow 160ms ease,
    transform 160ms ease;
}

.answer-card button:hover:not(:disabled) {
  border-color: #9fbef2;
  box-shadow: 0 3px 10px rgb(47 94 171 / 8%);
}

.answer-card button:focus-visible,
.answer-card textarea:focus-visible {
  outline: 3px solid rgb(53 104 244 / 18%);
  outline-offset: 2px;
}

.answer-card .answer-tools {
  color: #6b7c96;
}
</style>
