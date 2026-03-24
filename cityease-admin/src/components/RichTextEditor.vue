<template>
  <div class="rich-editor">
    <div ref="toolbarRef" class="rich-editor__toolbar"></div>
    <div ref="editorRef" class="rich-editor__content"></div>
    <div class="rich-editor__footer">
      <span>支持标题、列表、链接、引用、代码块和图片上传，单张图片不超过 1048576 bytes</span>
      <span>{{ textLength }} 字</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { createEditor, createToolbar, type IDomEditor, type IEditorConfig, type IToolbarConfig } from '@wangeditor/editor'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { validateImageUpload } from '@/utils/upload'
import '@wangeditor/editor/dist/css/style.css'

const props = withDefaults(defineProps<{
  modelValue?: string
  placeholder?: string
}>(), {
  modelValue: '',
  placeholder: '请输入内容'
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const toolbarRef = ref<HTMLElement | null>(null)
const editorRef = ref<HTMLElement | null>(null)
const textLength = ref(0)

let editor: IDomEditor | null = null
let toolbar: any = null
let isSyncingFromOutside = false

const normalizeHtml = (html?: string) => {
  const value = html?.trim()
  return value ? value : '<p><br></p>'
}

const updateTextLength = () => {
  textLength.value = editor ? editor.getText().replace(/\s+/g, '').length : 0
}

const uploadImage = async (file: File, insertFn: (url: string, alt?: string, href?: string) => void) => {
  if (!validateImageUpload(file)) {
    return
  }

  const formData = new FormData()
  formData.append('file', file)

  try {
    const url = await request.post('/admin/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }) as string

    insertFn(url, file.name, url)
  } catch {
    ElMessage.error('图片上传失败，请稍后重试')
  }
}

const initEditor = async () => {
  await nextTick()
  if (!toolbarRef.value || !editorRef.value) return

  const editorConfig: Partial<IEditorConfig> = {
    placeholder: props.placeholder,
    autoFocus: false,
    onChange(currentEditor) {
      updateTextLength()
      if (isSyncingFromOutside) return
      emit('update:modelValue', currentEditor.getHtml())
    },
    MENU_CONF: {
      uploadImage: {
        async customUpload(file: File, insertFn: (url: string, alt?: string, href?: string) => void) {
          await uploadImage(file, insertFn)
        }
      }
    }
  }

  const toolbarConfig: Partial<IToolbarConfig> = {
    toolbarKeys: [
      'headerSelect',
      'bold',
      'italic',
      'underline',
      'through',
      'color',
      'bgColor',
      '|',
      'bulletedList',
      'numberedList',
      'todo',
      '|',
      'justifyLeft',
      'justifyCenter',
      'justifyRight',
      '|',
      'blockquote',
      'codeBlock',
      'insertLink',
      'uploadImage',
      'insertTable',
      'divider',
      '|',
      'undo',
      'redo'
    ]
  }

  editor = createEditor({
    selector: editorRef.value,
    mode: 'default',
    html: normalizeHtml(props.modelValue),
    config: editorConfig
  })

  toolbar = createToolbar({
    editor,
    selector: toolbarRef.value,
    mode: 'default',
    config: toolbarConfig
  })

  updateTextLength()
}

watch(
  () => props.modelValue,
  (value) => {
    if (!editor) return
    const nextHtml = normalizeHtml(value)
    if (editor.getHtml() === nextHtml) return

    isSyncingFromOutside = true
    editor.setHtml(nextHtml)
    isSyncingFromOutside = false
    updateTextLength()
  }
)

onMounted(initEditor)

onBeforeUnmount(() => {
  toolbar?.destroy?.()
  editor?.destroy()
  toolbar = null
  editor = null
})
</script>

<style scoped>
.rich-editor {
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 22px;
  overflow: hidden;
  background:
    linear-gradient(180deg, rgba(8, 15, 27, 0.98), rgba(10, 18, 31, 0.94)),
    radial-gradient(circle at top right, rgba(56, 189, 248, 0.12), transparent 35%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.rich-editor__footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 16px 14px;
  color: rgba(148, 163, 184, 0.86);
  font-size: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.12);
}

:deep(.w-e-bar) {
  background: rgba(15, 23, 42, 0.94);
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
}

:deep(.w-e-toolbar) {
  background: transparent;
  border: 0;
}

:deep(.w-e-bar-divider) {
  background-color: rgba(148, 163, 184, 0.18);
}

:deep(.w-e-bar-item button),
:deep(.w-e-menu-tooltip-v5) {
  color: #d9e7f5;
}

:deep(.w-e-bar-item button:hover),
:deep(.w-e-bar-item button.active) {
  background: rgba(56, 189, 248, 0.14);
}

:deep(.w-e-text-container) {
  background: transparent;
  color: #e6edf7;
  min-height: 340px;
}

:deep(.w-e-text-placeholder) {
  color: rgba(148, 163, 184, 0.56);
  font-style: normal;
}

:deep(.w-e-text) {
  padding: 18px 20px 24px;
  line-height: 1.8;
  font-size: 15px;
}

:deep(.w-e-text p),
:deep(.w-e-text li),
:deep(.w-e-text h1),
:deep(.w-e-text h2),
:deep(.w-e-text h3),
:deep(.w-e-text blockquote) {
  color: #e6edf7;
}

:deep(.w-e-text blockquote) {
  margin: 0;
  padding: 12px 16px;
  border-left: 4px solid rgba(56, 189, 248, 0.58);
  background: rgba(15, 23, 42, 0.64);
  border-radius: 0 14px 14px 0;
}

:deep(.w-e-panel-container),
:deep(.w-e-drop-panel),
:deep(.w-e-modal) {
  background: #132035;
  border: 1px solid rgba(148, 163, 184, 0.16);
  box-shadow: 0 18px 48px rgba(3, 8, 18, 0.55);
}

:deep(.w-e-panel-tab-title),
:deep(.w-e-select-list),
:deep(.w-e-modal input),
:deep(.w-e-modal textarea) {
  color: #e6edf7;
}

:deep(.w-e-modal input),
:deep(.w-e-modal textarea) {
  background: rgba(8, 15, 27, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.18);
}

:deep(.w-e-text-container [data-slate-editor]) {
  min-height: 340px;
}

@media (max-width: 768px) {
  .rich-editor__footer {
    flex-direction: column;
    align-items: flex-start;
  }

  :deep(.w-e-text) {
    padding: 14px 14px 18px;
  }
}
</style>
