<template>
  <div class="image-uploader">
    <div v-if="modelValue" class="image-preview" @click="handlePreview">
      <img :src="modelValue" alt="预览图片" class="preview-image" />
      <div class="mask">
        <el-icon><View /></el-icon>
        <span>查看</span>
      </div>
    </div>

    <el-upload
      v-else
      class="uploader"
      :http-request="customUpload"
      :show-file-list="false"
      :before-upload="beforeUpload"
      accept="image/*"
    >
      <div class="uploader-content">
        <el-icon><Plus /></el-icon>
        <span class="text">点击上传</span>
        <span v-if="showHint" class="hint">单张不超过 {{ maxText }}</span>
      </div>
    </el-upload>

    <div v-if="modelValue" class="actions">
      <el-button size="small" type="primary" @click.stop="handleEdit">更换</el-button>
      <el-button size="small" type="danger" @click.stop="handleRemove">删除</el-button>
    </div>

    <el-dialog v-model="previewVisible" title="图片预览" width="800px">
      <img :src="modelValue" alt="预览" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, View } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { MAX_UPLOAD_BYTES, MAX_UPLOAD_TEXT, validateImageUpload } from '@/utils/upload'

interface Props {
  modelValue?: string
  maxBytes?: number
  showHint?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  maxBytes: MAX_UPLOAD_BYTES,
  showHint: true
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const previewVisible = ref(false)
const maxText = MAX_UPLOAD_TEXT

const customUpload = async (options: any) => {
  const { file, onSuccess, onError } = options
  const formData = new FormData()
  formData.append('file', file)

  try {
    const response: string = await request.post('/admin/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }) as string

    emit('update:modelValue', response)
    ElMessage.success('上传成功')
    onSuccess?.(response)
  } catch (error) {
    ElMessage.error('上传失败，请重试')
    onError?.(error)
  }
}

const beforeUpload = (file: File) => validateImageUpload(file, props.maxBytes)

const handlePreview = () => {
  previewVisible.value = true
}

const handleEdit = () => {
  emit('update:modelValue', '')
}

const handleRemove = () => {
  emit('update:modelValue', '')
  ElMessage.success('已删除')
}

watch(() => props.modelValue, (val) => {
  if (val === undefined) {
    emit('update:modelValue', '')
  }
}, { immediate: true })
</script>

<style scoped>
.image-uploader {
  display: inline-flex;
  flex-direction: column;
  gap: 12px;
}

.image-preview,
.uploader {
  position: relative;
  width: 156px;
  height: 156px;
  overflow: hidden;
  border-radius: 24px;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.image-preview {
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(7, 14, 26, 0.72);
}

.uploader {
  border: 1px dashed rgba(56, 189, 248, 0.48);
  background:
    linear-gradient(180deg, rgba(8, 15, 27, 0.9), rgba(12, 22, 37, 0.86)),
    radial-gradient(circle at top right, rgba(56, 189, 248, 0.12), transparent 36%);
}

.image-preview:hover,
.uploader:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 34px rgba(3, 8, 18, 0.26);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  background: rgba(3, 8, 18, 0.54);
  opacity: 0;
  transition: opacity 0.18s ease;
}

.image-preview:hover .mask {
  opacity: 1;
}

.uploader-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 10px;
  color: rgba(203, 213, 225, 0.92);
}

.uploader-content i {
  font-size: 28px;
}

.text {
  font-size: 13px;
  font-weight: 600;
}

.hint {
  font-size: 11px;
  color: rgba(148, 163, 184, 0.82);
}

.actions {
  display: flex;
  gap: 8px;
}
</style>
