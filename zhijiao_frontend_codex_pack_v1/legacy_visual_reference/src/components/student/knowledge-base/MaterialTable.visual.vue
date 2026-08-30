<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/knowledge-base/MaterialTable.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="material-table" data-test="material-table" aria-label="资料列表">
    <header>
      <div>
        <h2>资料列表</h2>
        <p>{{ props.readOnly ? '查看资料状态与 AI 引用状态' : '管理资料状态与 AI 引用权限' }}</p>
      </div>
      <span data-test="material-count">
        {{
          materials.length === totalCount
            ? `共 ${totalCount}`
            : `显示 ${materials.length} / ${totalCount}`
        }}
        条
      </span>
    </header>
    <div class="table-head">
      <span>资料名称</span><span>类型</span><span>当前状态</span><span>最近更新</span
      ><span>AI 引用状态</span><span>操作</span>
    </div>
    <div v-if="materials.length === 0" class="no-results" data-test="material-empty-filter">
      <strong>没有匹配的资料</strong>
      <span>试试清空搜索或调整筛选条件。</span>
    </div>
    <article
      v-for="material in materials"
      :key="material.id"
      class="table-row"
      :class="{ active: material.id === selectedMaterialId }"
      data-test="material-row"
      :data-material-id="material.id"
      role="button"
      tabindex="0"
      @click="emit('select', material.id)"
      @keydown.enter="emit('select', material.id)"
    >
      <div class="file-name">
        <span class="file-icon" :class="material.type">{{
          material.type === 'textbook_pdf'
            ? 'PDF'
            : material.type === 'lecture_pdf_word'
              ? 'W'
              : material.type === 'reference_tree_xmind'
                ? 'X'
                : 'M'
        }}</span
        ><strong>{{ material.name }}</strong
        ><span v-if="material.type === 'reference_tree_xmind'" class="compat-copy">XMind</span>
      </div>
      <span>{{ typeLabel[material.type] }}</span>
      <em :class="material.parseStatus"
        ><i />{{ statusLabel[material.parseStatus]
        }}<span class="compat-copy">{{ material.parseStatus }}</span></em
      >
      <span>{{ material.updatedAt }}</span>
      <span v-if="props.readOnly" class="citation-status">
        {{ material.canCite ? '可引用' : '不可引用' }}
      </span>
      <label v-else data-test="citation-toggle" class="switch" @click.stop
        ><input
          type="checkbox"
          :checked="material.canCite"
          :disabled="material.parseStatus !== 'ready'"
          @change="
            emit('toggle-citation', material.id, ($event.target as HTMLInputElement).checked)
          " /><i
      /></label>
      <div class="row-actions">
        <button
          type="button"
          :data-test="`preview-material-${material.id}`"
          :aria-label="`预览 ${material.name}`"
          @click.stop="emit('preview', material.id)"
        >
          预览</button
        ><button
          type="button"
          :data-test="`chapter-material-${material.id}`"
          :aria-label="`查看 ${material.name} 章节`"
          @click.stop="emit('chapter', material.id)"
        >
          章节
        </button>
      </div>
      <small v-if="material.errorMessage">{{ material.errorMessage }}</small>
    </article>
    <footer>
      <span>
        {{
          materials.length === totalCount
            ? `共 ${totalCount}`
            : `当前显示 ${materials.length} / ${totalCount}`
        }}
        条
      </span>
    </footer>
  </section>
</template>

<style scoped lang="scss">
.material-table {
  display: grid;
  gap: 9px;
  width: 100%;
  min-width: 0;
  overflow-x: auto;
  padding: 16px;
  border: 1px solid #dce6f2;
  border-radius: 9px;
  background: #fff;
}
header,
footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
h2 {
  margin: 0;
  color: #142b55;
  font-size: 17px;
}
header p {
  margin: 4px 0 0;
  color: #77869e;
  font-size: 12px;
}
header > span,
footer > span {
  color: #75839a;
  font-size: 12px;
}
.table-head,
.table-row {
  display: grid;
  grid-template-columns: minmax(180px, 1.5fr) 75px 90px 105px 130px 94px;
  gap: 10px;
  align-items: center;
  min-width: 724px;
}
.table-head {
  padding: 0 10px;
  color: #7b899f;
  font-size: 11px;
  font-weight: 800;
}
.table-row {
  min-height: 62px;
  padding: 8px 10px;
  border: 1px solid transparent;
  border-bottom-color: #edf1f6;
  color: #637491;
  font-size: 12px;
  cursor: pointer;
}
.table-row:focus-visible,
.row-actions button:focus-visible,
.switch input:focus-visible + i {
  outline: 2px solid #4d83ef;
  outline-offset: 2px;
}
.table-row.active {
  border-color: #5c8ff1;
  border-radius: 7px;
  background: #f0f5ff;
}
.file-name {
  display: flex;
  gap: 8px;
  align-items: center;
  min-width: 0;
}
.file-name strong {
  overflow: hidden;
  color: #263e65;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.file-icon {
  display: grid;
  width: 27px;
  height: 32px;
  place-items: center;
  flex: 0 0 auto;
  border-radius: 4px;
  color: #fff;
  font-size: 9px;
  font-weight: 800;
}
.file-icon.textbook_pdf {
  background: #ed4c4c;
}
.file-icon.lecture_pdf_word {
  background: #3e76dd;
}
.file-icon.reference_tree_xmind {
  background: #4cab76;
}
.file-icon.custom_pdf_word_md {
  background: #657899;
}
.table-row em {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  width: fit-content;
  font-style: normal;
  font-weight: 700;
}
.table-row em i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
}
.ready {
  color: #3c9b5d;
}
.queued,
.parsing {
  color: #d68a1a;
}
.failed {
  color: #e04e51;
}
.switch input {
  position: absolute;
  opacity: 0;
}
.switch i {
  display: block;
  width: 38px;
  height: 21px;
  border-radius: 999px;
  background: #dce3ef;
}
.switch i::after {
  display: block;
  width: 15px;
  height: 15px;
  margin: 3px;
  border-radius: 50%;
  background: #fff;
  content: '';
  transition: transform 0.18s ease;
}
.switch input:checked + i {
  background: #3976eb;
}
.switch input:checked + i::after {
  transform: translateX(17px);
}
.switch input:disabled + i {
  opacity: 0.55;
}
.citation-status {
  color: #3c9b5d;
  font-weight: 700;
}
.row-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
.row-actions button {
  padding: 0;
  border: 0;
  color: #2d6be8;
  background: transparent;
  font-size: 12px;
  cursor: pointer;
}
.table-row small {
  grid-column: 1 / -1;
  color: #bd3e3e;
}
.no-results {
  display: grid;
  justify-items: center;
  gap: 5px;
  padding: 28px 16px;
  border: 1px dashed #cbd8ea;
  border-radius: 8px;
  color: #7889a4;
  background: #fbfdff;
  text-align: center;
}
.no-results strong {
  color: #35527d;
}
footer {
  padding-top: 7px;
  border-top: 1px solid #edf1f6;
}
.compat-copy {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
}
@media (max-width: 1440px) {
  .table-head,
  .table-row {
    grid-template-columns: minmax(150px, 1.5fr) 64px 76px 86px 110px 72px;
    gap: 8px;
    min-width: 598px;
  }
}
@media (max-width: 1024px) {
  .table-head {
    display: none;
  }
  .table-row {
    grid-template-columns: 1fr auto;
    gap: 7px;
    min-width: 0;
  }
  .table-row > :nth-child(n + 2):not(.row-actions):not(.switch) {
    display: none;
  }
  .row-actions {
    justify-self: end;
  }
}
</style>
