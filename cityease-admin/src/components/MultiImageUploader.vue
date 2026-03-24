<template>
  <div class="multi-image-uploader">
    <div class="multi-image-uploader__grid">
      <div
        v-for="(url, index) in innerValue"
        :key="`${url}-${index}`"
        class="multi-image-uploader__item"
      >
        <el-image
          :src="url"
          fit="cover"
          :preview-src-list="innerValue"
          :initial-index="index"
          preview-teleported
          class="multi-image-uploader__image"
        />
        <div class="multi-image-uploader__mask">
          <el-button size="small" type="primary" plain @click.stop="previewIndex = index; previewVisible = true">预览</el-button>
          <el-button size="small" type="danger" plain @click.stop="removeAt(index)">移除</el-button>
        </div>
      </div>

      <el-upload
        v-if="innerValue.length < limit"
        class="multi-image-uploader__trigger"
        :show-file-list="false"
        :http-request="customUpload"
        :before-upload="beforeUpload"
        accept="image/*"
      >
        <div class="multi-image-uploader__trigger-inner">
          <el-icon><Plus /></el-icon>
          <span>上传材料</span>
        </div>
      </el-upload>
    </div>

    <div class="multi-image-uploader__footer">
      <span>支持 JPG / PNG / GIF / WEBP</span>
      <span>单张不超过 {{ maxText }}，最多 {{ limit }} 张</span>
    </div>

    <el-dialog v-model="previewVisible" title="材料预览" width="880px">
      <el-image
        v-if="innerValue[previewIndex]"
        :src="innerValue[previewIndex]"
        fit="contain"
        class="multi-image-uploader__preview"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { MAX_UPLOAD_BYTES, MAX_UPLOAD_TEXT, validateImageUpload } from '@/utils/upload'

interface Props {
  modelValue?: string[]
  limit?: number
  maxBytes?: number
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [],
  limit: 6,
  maxBytes: MAX_UPLOAD_BYTES
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string[]): void
}>()

const innerValue = ref<string[]>([])
const previewVisible = ref(false)
const previewIndex = ref(0)
const maxText = MAX_UPLOAD_TEXT

const syncValue = (value: string[]) => {
  innerValue.value = Array.isArray(value) ? [...value] : []
}

watch(
  () => props.modelValue,
  (value) => syncValue(value || []),
  { immediate: true, deep: true }
)

const updateValue = (value: string[]) => {
  innerValue.value = value
  emit('update:modelValue', [...value])
}

const beforeUpload = (file: File) => validateImageUpload(file, props.maxBytes)

const customUpload = async (options: any) => {
  const { file, onSuccess, onError } = options
  const formData = new FormData()
  formData.append('file', file)

  try {
    const response = await request.post('/admin/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }) as string

    updateValue([...innerValue.value, response])
    ElMessage.success('材料上传成功')
    onSuccess?.(response)
  } catch (error) {
    ElMessage.error('材料上传失败，请稍后重试')
    onError?.(error)
  }
}

const removeAt = (index: number) => {
  const next = [...innerValue.value]
  next.splice(index, 1)
  updateValue(next)
}
</script>

<style scoped>
.multi-image-uploader {
  border: 1px solid rgba(148, 163, 184, 0.16);
  border-radius: 24px;
  padding: 18px;
  background:
    linear-gradient(180deg, rgba(8, 15, 27, 0.92), rgba(12, 20, 34, 0.9)),
    radial-gradient(circle at top right, rgba(56, 189, 248, 0.08), transparent 36%);
}

.multi-image-uploader__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 14px;
}

.multi-image-uploader__item,
.multi-image-uploader__trigger {
  position: relative;
  height: 150px;
  border-radius: 20px;
  overflow: hidden;
}

.multi-image-uploader__item {
  border: 1px solid rgba(148, 163, 184, 0.14);
  background: rgba(8, 15, 27, 0.72);
}

.multi-image-uploader__image {
  width: 100%;
  height: 100%;
}

.multi-image-uploader__mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px;
  background: linear-gradient(180deg, transparent, rgba(2, 6, 12, 0.8));
  opacity: 0;
  transition: opacity 0.18s ease;
}

.multi-image-uploader__item:hover .multi-image-uploader__mask {
  opacity: 1;
}

.multi-image-uploader__trigger {
  border: 1px dashed rgba(56, 189, 248, 0.44);
  background: rgba(5, 11, 21, 0.82);
}

.multi-image-uploader__trigger-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 10px;
  color: rgba(203, 213, 225, 0.9);
}

.multi-image-uploader__trigger-inner :deep(svg) {
  font-size: 28px;
}

.multi-image-uploader__footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 14px;
  color: rgba(148, 163, 184, 0.82);
  font-size: 12px;
}

.multi-image-uploader__preview {
  width: 100%;
  max-height: 72vh;
}

@media (max-width: 768px) {
  .multi-image-uploader__footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
