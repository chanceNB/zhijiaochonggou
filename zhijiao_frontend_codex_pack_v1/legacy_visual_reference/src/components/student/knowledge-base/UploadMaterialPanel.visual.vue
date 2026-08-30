<!-- VISUAL REFERENCE ONLY.
Source: src/components/student/knowledge-base/UploadMaterialPanel.vue
DO NOT COPY OLD BUSINESS TEXT/DATA/API/STORE/ROUTES.
Script intentionally removed. Use only layout/template/style as visual evidence.
-->

<template>
  <section class="upload-panel" data-test="upload-material-panel">
    <header>
      <el-icon><UploadFilled /></el-icon>
      <div>
        <h2>上传资料</h2>
        <p>支持 PDF、Word、Markdown、XMind</p>
      </div>
    </header>
    <div
      class="drop-zone"
      :class="{ dragging: dragActive }"
      data-test="upload-drop-zone"
      @dragenter.prevent="dragActive = true"
      @dragover.prevent="dragActive = true"
      @dragleave.prevent="dragActive = false"
      @drop.prevent="handleDrop"
    >
      <el-icon><UploadFilled /></el-icon><span>将文件拖到此处，或点击按钮选择文件</span
      ><button
        data-test="upload-material"
        class="primary-action"
        type="button"
        :disabled="props.uploading"
        @click="submitUpload"
      >
        {{ props.uploading ? '解析中...' : '选择文件上传' }}
      </button>
      <button
        class="secondary-action"
        data-test="choose-local-file"
        type="button"
        :disabled="props.uploading"
        @click="openFilePicker"
      >
        从本地选择
      </button>
      <input
        ref="fileInput"
        data-test="upload-file-input"
        class="file-input"
        type="file"
        accept=".pdf,.doc,.docx,.md,.xmind"
        @change="handleFileChange"
      />
    </div>
    <div class="upload-types">
      <button
        v-for="option in uploadOptions"
        :key="option.type"
        :data-test="`upload-type-${option.type}`"
        type="button"
        :class="{ active: selectedType === option.type }"
        @click="selectedType = option.type"
      >
        <strong>{{ option.label }}</strong
        ><span>{{ option.description }}</span>
      </button>
    </div>
    <div class="upload-metadata">
      <strong>{{ activeOption.fileName }}</strong
      ><span>{{ activeOption.mimeType }} · {{ Math.round(activeOption.size / 1024) }}KB</span>
    </div>
    <div
      v-if="latestUpload"
      class="upload-status"
      :class="latestUpload.parseStatus"
      data-test="upload-status"
      aria-live="polite"
    >
      <span
        >{{ statusLabel[latestUpload.parseStatus]
        }}<small class="compat-copy">{{ latestUpload.parseStatus }}</small></span
      >
      <div><i :style="{ width: `${latestUpload.progress}%` }" /></div>
      <b>{{ latestUpload.progress }}%</b>
    </div>
    <p class="upload-disclaimer" data-test="upload-disclaimer">
      上传后会自动解析章节与知识点，处理完成后即可作为 AI 回答的引用来源。<span class="compat-copy"
        >不读取正文</span
      >
    </p>
  </section>
</template>

<style scoped lang="scss">
.upload-panel {
  display: grid;
  gap: 12px;
  padding: 16px;
  border: 1px solid #dce6f2;
  border-radius: 9px;
  background: #fff;
}
header {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}
header .el-icon {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  border-radius: 7px;
  color: #2f6de8;
  background: #eef4ff;
  font-size: 18px;
}
h2,
p {
  margin: 0;
}
h2 {
  color: #172d57;
  font-size: 17px;
}
header p {
  margin-top: 3px;
  color: #77859b;
  font-size: 12px;
}
.drop-zone {
  display: grid;
  justify-items: center;
  gap: 8px;
  min-height: 168px;
  padding: 24px;
  border: 1px dashed #b9cff1;
  border-radius: 8px;
  color: #71829d;
  background: #fbfdff;
  transition:
    border-color 0.18s ease,
    background 0.18s ease,
    box-shadow 0.18s ease;
}
.drop-zone.dragging {
  border-color: #2f6be8;
  background: #eef5ff;
  box-shadow: inset 0 0 0 2px rgb(47 107 232 / 10%);
}
.drop-zone .el-icon {
  color: #3b75eb;
  font-size: 34px;
}
.upload-types {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}
button {
  border: 1px solid #d7e2ef;
  border-radius: 7px;
  background: #fff;
  cursor: pointer;
}
.upload-types button {
  display: grid;
  gap: 3px;
  min-height: 62px;
  padding: 9px;
  text-align: left;
}
.upload-types button.active {
  border-color: #4a7ff0;
  background: #eef4ff;
}
.upload-types strong {
  color: #244574;
  font-size: 13px;
}
.upload-types span,
.upload-metadata span {
  color: #7a89a0;
  font-size: 11px;
}
.upload-metadata {
  display: grid;
  gap: 4px;
  padding: 9px 11px;
  border-radius: 7px;
  background: #f7faff;
}
.upload-metadata strong {
  color: #324b71;
  font-size: 12px;
}
.upload-status {
  display: grid;
  grid-template-columns: 50px 1fr 42px;
  gap: 8px;
  align-items: center;
  color: #405878;
  font-size: 12px;
}
.upload-status div {
  height: 7px;
  overflow: hidden;
  border-radius: 999px;
  background: #dce8f8;
}
.upload-status i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #4cae68;
}
.upload-disclaimer {
  padding: 9px;
  border: 1px solid #f2d395;
  border-radius: 7px;
  color: #8a631e;
  background: #fffaf0;
  font-size: 12px;
  line-height: 1.5;
}
.primary-action {
  min-height: 35px;
  padding: 0 27px;
  border: 0;
  color: #fff;
  background: #3d72e8;
  font-weight: 800;
}
.secondary-action {
  min-height: 32px;
  padding: 0 18px;
  border-color: #cddcf0;
  color: #2f6be8;
  background: #fff;
  font-size: 12px;
  font-weight: 700;
}
.secondary-action:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
.file-input {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  opacity: 0;
  pointer-events: none;
}
.primary-action:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.compat-copy {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
}
.upload-status.ready {
  color: #2d8150;
}
.upload-status.parsing,
.upload-status.queued {
  color: #b96f12;
}
.upload-status.failed {
  color: #c33f43;
}
.upload-status.parsing i,
.upload-status.queued i {
  background: #e49a2e;
}
.upload-status.failed i {
  background: #df5e63;
}
@media (max-width: 1024px) {
  .upload-types {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
