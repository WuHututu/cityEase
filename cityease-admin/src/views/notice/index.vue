<template>
  <div class="page notice-page">
    <section class="page-hero notice-hero">
      <div class="page-hero__content">
        <div class="page-eyebrow">Notice Studio</div>
        <h2>公告内容中心</h2>
        <p>把通知、公告与活动内容统一维护在一个面板里，支持富文本、封面图和发布节奏控制。</p>
      </div>

      <div class="page-hero__stats">
        <div class="hero-stat">
          <span>当前草稿</span>
          <strong>{{ draftCount }}</strong>
        </div>
        <div class="hero-stat">
          <span>已发布</span>
          <strong>{{ publishedCount }}</strong>
        </div>
        <div class="hero-stat">
          <span>置顶内容</span>
          <strong>{{ topCount }}</strong>
        </div>
      </div>
    </section>

    <el-card shadow="never" class="toolbar surface-card">
      <div class="surface-card__header">
        <div>
          <h3>筛选与管理</h3>
          <p>快速定位要处理的公告，并直接进入编辑或详情预览。</p>
        </div>
        <el-button type="primary" @click="openAddDialog">新增公告</el-button>
      </div>

      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item label="公告类型">
          <el-select v-model="queryParams.noticeType" clearable placeholder="全部类型" style="width: 180px">
            <el-option v-for="item in noticeTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布状态">
          <el-select v-model="queryParams.status" clearable placeholder="全部状态" style="width: 180px">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题关键词">
          <el-input v-model="queryParams.keyword" clearable placeholder="搜索标题内容" style="width: 260px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="surface-card">
      <div class="surface-card__header">
        <div>
          <h3>内容列表</h3>
          <p>操作列改成主操作加更多菜单，避免固定列叠压影响阅读。</p>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" border style="width: 100%">
        <el-table-column label="封面" width="96" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="row.coverImage"
              :preview-src-list="[row.coverImage]"
              fit="cover"
              class="cover-thumb"
              preview-teleported
            />
            <span v-else class="muted">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" width="120" />
        <el-table-column prop="noticeTitle" label="标题" min-width="260" show-overflow-tooltip />
        <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="noticeTypeTag(row.noticeType)">{{ noticeTypeText(row.noticeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="108" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isTop === 1 ? 'danger' : 'info'">{{ row.isTop === 1 ? '置顶中' : '普通' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" min-width="230">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="editNotice(row)">编辑</el-button>
              <el-button size="small" type="info" plain @click="viewDetail(row)">详情</el-button>
              <el-dropdown trigger="click">
                <el-button size="small" plain>更多</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="toggleTop(row)">
                      {{ row.isTop === 1 ? '取消置顶' : '设为置顶' }}
                    </el-dropdown-item>
                    <el-dropdown-item v-if="row.status === 0" @click="publish(row)">发布</el-dropdown-item>
                    <el-dropdown-item v-else @click="withdraw(row)">撤回</el-dropdown-item>
                    <el-dropdown-item divided @click="deleteNoticeItem(row)">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          :page-size="queryParams.pageSize"
          :total="total"
          background
          layout="total, prev, pager, next, jumper"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="1120px" top="4vh" @close="closeDialog">
      <div class="notice-dialog">
        <div class="notice-dialog__form">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
            <div class="notice-grid">
              <el-form-item label="标题" prop="noticeTitle">
                <el-input v-model="form.noticeTitle" maxlength="150" show-word-limit placeholder="请输入公告标题" />
              </el-form-item>
              <el-form-item label="公告类型" prop="noticeType">
                <el-select v-model="form.noticeType" placeholder="请选择公告类型" style="width: 100%">
                  <el-option v-for="item in noticeTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </div>

            <div class="notice-grid">
              <el-form-item label="发布状态">
                <el-switch
                  v-model="form.isPublish"
                  inline-prompt
                  active-text="发布"
                  inactive-text="草稿"
                />
              </el-form-item>
              <el-form-item label="封面图" prop="coverImage">
                <ImageUploader v-model="form.coverImage" :show-hint="false" />
              </el-form-item>
            </div>

            <el-form-item label="正文内容" prop="noticeContent" class="editor-form-item">
              <RichTextEditor v-model="form.noticeContent" placeholder="请输入公告正文，支持富文本排版和图片上传" />
            </el-form-item>
          </el-form>
        </div>

        <aside class="notice-dialog__preview">
          <div class="preview-card">
            <div class="page-eyebrow">Preview</div>
            <div class="preview-card__cover">
              <el-image v-if="form.coverImage" :src="form.coverImage" fit="cover" preview-teleported />
              <div v-else class="preview-card__empty">未设置封面</div>
            </div>
            <div class="preview-card__type">{{ noticeTypeText(form.noticeType) }}</div>
            <div class="preview-card__headline">{{ form.noticeTitle || '这里会显示公告标题' }}</div>
            <div class="preview-card__status">
              <el-tag :type="form.isPublish ? 'success' : 'warning'">{{ form.isPublish ? '保存后将直接发布' : '当前保存为草稿' }}</el-tag>
            </div>
          </div>
        </aside>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">{{ isEdit ? '保存修改' : '创建公告' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="公告详情" width="1000px">
      <template v-if="detailData">
        <div class="notice-detail">
          <div class="notice-detail__cover" v-if="detailData.coverImage">
            <el-image :src="detailData.coverImage" fit="cover" :preview-src-list="[detailData.coverImage]" preview-teleported />
          </div>
          <div class="notice-detail__header">
            <div>
              <div class="page-eyebrow">{{ noticeTypeText(detailData.noticeType) }}</div>
              <h2>{{ detailData.noticeTitle }}</h2>
            </div>
            <el-tag :type="detailData.status === 1 ? 'success' : 'warning'">
              {{ detailData.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </div>
          <div class="notice-detail__meta">
            <span>创建时间：{{ detailData.createTime || '-' }}</span>
            <span>更新时间：{{ detailData.updateTime || '-' }}</span>
          </div>
          <div class="notice-detail__content" v-html="detailData.noticeContent"></div>
        </div>
      </template>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { deleteNotice, getNoticeDetail, pageNotices, saveOrUpdateNotice, toggleTop as apiToggleTop } from '@/api/notice'
import ImageUploader from '@/components/ImageUploader.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'

interface NoticeRecord {
  id: number
  noticeTitle: string
  noticeType: number
  status: number
  isTop?: number
  coverImage?: string
  noticeContent?: string
  createTime?: string
  updateTime?: string
}

const noticeTypeOptions = [
  { value: 1, label: '通知' },
  { value: 2, label: '公告' },
  { value: 3, label: '活动' }
]

const loading = ref(false)
const total = ref(0)
const tableData = ref<NoticeRecord[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  noticeType: undefined as number | undefined,
  status: undefined as number | undefined,
  keyword: ''
})

const draftCount = computed(() => tableData.value.filter(item => item.status === 0).length)
const publishedCount = computed(() => tableData.value.filter(item => item.status === 1).length)
const topCount = computed(() => tableData.value.filter(item => item.isTop === 1).length)

const formRef = ref<FormInstance>()
const dialogVisible = ref(false)
const detailVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const detailData = ref<NoticeRecord | null>(null)

const form = reactive({
  id: undefined as number | undefined,
  noticeTitle: '',
  noticeType: 1,
  noticeContent: '',
  isPublish: false,
  coverImage: ''
})

const noticeTypeText = (value?: number) =>
  noticeTypeOptions.find((item) => item.value === value)?.label || '未分类'

const noticeTypeTag = (value?: number) => {
  if (value === 1) return 'info'
  if (value === 2) return 'success'
  if (value === 3) return 'warning'
  return 'info'
}

const isRichTextEmpty = (html?: string) => {
  const plain = (html || '')
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/gi, '')
    .replace(/\s+/g, '')
    .trim()
  return plain.length === 0
}

const rules = reactive<FormRules>({
  noticeTitle: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  noticeContent: [{
    validator: (_rule, value, callback) => {
      if (isRichTextEmpty(value)) {
        callback(new Error('请输入公告正文'))
        return
      }
      callback()
    },
    trigger: 'change'
  }]
})

const dialogTitle = ref('新增公告')

const fetchData = async () => {
  loading.value = true
  try {
    const pageData = await pageNotices({ ...queryParams }) as any
    tableData.value = pageData?.records || []
    total.value = Number(pageData?.total || 0)
  } catch {
    tableData.value = []
    total.value = 0
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: undefined,
    noticeTitle: '',
    noticeType: 1,
    noticeContent: '',
    isPublish: false,
    coverImage: ''
  })
  formRef.value?.clearValidate()
}

const handleSearch = () => {
  queryParams.pageNo = 1
  fetchData()
}

const handleReset = () => {
  queryParams.pageNo = 1
  queryParams.noticeType = undefined
  queryParams.status = undefined
  queryParams.keyword = ''
  fetchData()
}

const openAddDialog = () => {
  dialogTitle.value = '新增公告'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const editNotice = async (row: NoticeRecord) => {
  dialogTitle.value = '编辑公告'
  isEdit.value = true
  dialogVisible.value = true
  try {
    const detail = await getNoticeDetail(row.id) as any
    Object.assign(form, {
      id: detail.id,
      noticeTitle: detail.noticeTitle || '',
      noticeType: detail.noticeType || 1,
      noticeContent: detail.noticeContent || '',
      isPublish: detail.status === 1,
      coverImage: detail.coverImage || ''
    })
  } catch {
    ElMessage.error('加载公告详情失败')
  }
}

const closeDialog = () => {
  formRef.value?.clearValidate()
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await saveOrUpdateNotice({
      id: form.id,
      noticeTitle: form.noticeTitle,
      noticeType: form.noticeType,
      noticeContent: form.noticeContent,
      isPublish: form.isPublish,
      coverImage: form.coverImage
    })
    ElMessage.success(isEdit.value ? '公告已更新' : '公告已创建')
    dialogVisible.value = false
    fetchData()
  } catch {
    ElMessage.error('保存公告失败')
  } finally {
    submitting.value = false
  }
}

const toggleTop = async (row: NoticeRecord) => {
  try {
    await apiToggleTop(row.id)
    ElMessage.success(row.isTop === 1 ? '已取消置顶' : '已设为置顶')
    fetchData()
  } catch {
    ElMessage.error('置顶操作失败')
  }
}

const publish = async (row: NoticeRecord) => {
  try {
    await ElMessageBox.confirm('确定要发布这条公告吗？发布后住户端即可查看。', '发布公告', { type: 'warning' })
    await saveOrUpdateNotice({ id: row.id, isPublish: true })
    ElMessage.success('公告已发布')
    fetchData()
  } catch {
    // ignore
  }
}

const withdraw = async (row: NoticeRecord) => {
  try {
    await ElMessageBox.confirm('确定要撤回这条公告吗？撤回后会恢复为草稿。', '撤回公告', { type: 'warning' })
    await saveOrUpdateNotice({ id: row.id, isPublish: false })
    ElMessage.success('公告已撤回')
    fetchData()
  } catch {
    // ignore
  }
}

const deleteNoticeItem = async (row: NoticeRecord) => {
  try {
    await ElMessageBox.confirm('删除后无法恢复，确定继续吗？', '删除公告', { type: 'warning' })
    await deleteNotice(row.id)
    ElMessage.success('公告已删除')
    fetchData()
  } catch {
    // ignore
  }
}

const viewDetail = async (row: NoticeRecord) => {
  try {
    detailData.value = await getNoticeDetail(row.id) as NoticeRecord
    detailVisible.value = true
  } catch {
    ElMessage.error('加载公告详情失败')
  }
}

fetchData()
</script>

<style scoped>
.notice-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.notice-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(280px, 0.9fr);
  gap: 18px;
  padding: 26px 28px;
  border-radius: 30px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  background:
    radial-gradient(circle at top right, rgba(245, 158, 11, 0.14), transparent 26%),
    radial-gradient(circle at top left, rgba(56, 189, 248, 0.16), transparent 34%),
    linear-gradient(135deg, rgba(9, 18, 32, 0.98), rgba(12, 22, 37, 0.94));
  box-shadow: 0 24px 80px rgba(2, 8, 18, 0.34);
}

.page-eyebrow {
  margin-bottom: 10px;
  color: #7dd3fc;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.page-hero__content h2,
.surface-card__header h3 {
  margin: 0;
  color: #f8fafc;
}

.page-hero__content p,
.surface-card__header p {
  margin: 10px 0 0;
  color: rgba(203, 213, 225, 0.76);
}

.page-hero__stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.hero-stat {
  padding: 18px;
  border-radius: 22px;
  border: 1px solid rgba(148, 163, 184, 0.12);
  background: rgba(6, 12, 22, 0.48);
}

.hero-stat span {
  display: block;
  color: rgba(148, 163, 184, 0.8);
  font-size: 13px;
}

.hero-stat strong {
  display: block;
  margin-top: 10px;
  font-size: 30px;
  line-height: 1;
}

.surface-card__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.cover-thumb {
  width: 60px;
  height: 60px;
  border-radius: 18px;
  overflow: hidden;
}

.muted {
  color: rgba(148, 163, 184, 0.72);
}

.table-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.notice-dialog {
  display: grid;
  grid-template-columns: minmax(0, 2fr) 320px;
  gap: 20px;
}

.notice-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.editor-form-item :deep(.el-form-item__content) {
  display: block;
}

.preview-card {
  padding: 18px;
  border-radius: 24px;
  background:
    linear-gradient(180deg, rgba(8, 15, 27, 0.94), rgba(13, 22, 37, 0.9)),
    radial-gradient(circle at top right, rgba(245, 158, 11, 0.12), transparent 36%);
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.preview-card__cover {
  height: 190px;
  border-radius: 18px;
  overflow: hidden;
  background: rgba(15, 23, 42, 0.8);
}

.preview-card__cover :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.preview-card__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: rgba(148, 163, 184, 0.72);
}

.preview-card__type {
  margin-top: 18px;
  color: #fde68a;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.preview-card__headline {
  margin-top: 10px;
  font-size: 24px;
  line-height: 1.45;
  font-weight: 700;
}

.preview-card__status {
  margin-top: 16px;
}

.notice-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notice-detail__cover {
  border-radius: 22px;
  overflow: hidden;
  max-height: 320px;
}

.notice-detail__cover :deep(.el-image) {
  width: 100%;
  display: block;
}

.notice-detail__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.notice-detail__header h2 {
  margin: 0;
  font-size: 28px;
  line-height: 1.35;
}

.notice-detail__meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  color: rgba(148, 163, 184, 0.82);
  font-size: 13px;
}

.notice-detail__content {
  padding: 24px 26px;
  border-radius: 24px;
  background: rgba(8, 15, 27, 0.56);
  border: 1px solid rgba(148, 163, 184, 0.12);
  line-height: 1.85;
}

.notice-detail__content :deep(img) {
  max-width: 100%;
  border-radius: 18px;
}

@media (max-width: 1080px) {
  .notice-hero,
  .notice-dialog {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-hero__stats {
    grid-template-columns: 1fr;
  }

  .notice-grid,
  .surface-card__header {
    grid-template-columns: 1fr;
    flex-direction: column;
  }
}
</style>
