import { ElMessage } from 'element-plus'

export const MAX_UPLOAD_BYTES = 1048576
export const MAX_UPLOAD_TEXT = '1048576 bytes (1MB)'

export const validateImageUpload = (file: File, maxBytes = MAX_UPLOAD_BYTES) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }

  if (file.size > maxBytes) {
    ElMessage.error(`图片大小不能超过 ${MAX_UPLOAD_TEXT}`)
    return false
  }

  return true
}
