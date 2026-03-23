<template>
  <div class="image-uploader">
    <!-- 图片预览区域 -->
    <div v-if="modelValue" class="image-preview" @click="handlePreview">
      <img :src="modelValue" alt="预览图片" class="preview-image" />
      <div class="mask">
        <el-icon><View /></el-icon>
        <span>查看</span>
      </div>
    </div>

    <!-- 上传按钮 -->
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
      </div>
    </el-upload>

    <!-- 操作按钮 -->
    <div v-if="modelValue" class="actions">
      <el-button size="small" type="primary" @click.stop="handleEdit">更换</el-button>
      <el-button size="small" type="danger" @click.stop="handleRemove">删除</el-button>
    </div>

    <!-- 图片预览对话框 -->
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

interface Props {
  modelValue?: string
  maxSize?: number // 最大文件大小 MB
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  maxSize: 5
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

// 上传接口地址
const uploadUrl = '/admin/file/upload'

const previewVisible = ref(false)

// 自定义上传方法
const customUpload = async (options: any) => {
  const { file, onSuccess, onError } = options
  
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const response: string = await request.post(uploadUrl, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }) as string
    // 因为响应拦截器已经处理过，这里直接拿到的是 url
    if (response) {
      emit('update:modelValue', response)
      ElMessage.success('上传成功')
      if (onSuccess) onSuccess(response)
    }
  } catch (error) {
    ElMessage.error('上传失败，请重试')
    if (onError) onError(error)
  }
}

// 上传前校验
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < props.maxSize

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB！`)
    return false
  }
  return true
}

// 预览图片
const handlePreview = () => {
  previewVisible.value = true
}

// 更换图片
const handleEdit = () => {
  // 触发文件选择，需要先移除当前图片
  emit('update:modelValue', '')
}

// 删除图片
const handleRemove = () => {
  emit('update:modelValue', '')
  ElMessage.success('已删除')
}

// 监听外部值变化
watch(() => props.modelValue, (val) => {
  if (val === undefined) {
    emit('update:modelValue', '')
  }
}, { immediate: true })
</script>

<style scoped>
.image-uploader {
  display: inline-block;
}

.image-preview {
  position: relative;
  width: 148px;
  height: 148px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.image-preview:hover {
  border-color: #409EFF;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-preview:hover .mask {
  opacity: 1;
}

.mask i {
  font-size: 20px;
  margin-bottom: 4px;
}

.uploader {
  width: 148px;
  height: 148px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.uploader:hover {
  border-color: #409EFF;
}

.uploader-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #8c939d;
}

.uploader-content i {
  font-size: 28px;
  margin-bottom: 8px;
}

.text {
  font-size: 12px;
}

.actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}
</style>
