<template>
  <div class="page">
    <el-card shadow="never" class="toolbar">
      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item label="类型">
          <el-select v-model="queryParams.noticeType" placeholder="全部" clearable style="width: 140px">
            <el-option label="通知" :value="1" />
            <el-option label="公告" :value="2" />
            <el-option label="活动" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="queryParams.keyword" placeholder="标题关键字" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="margin-left:auto">
          <el-button type="primary" plain @click="openAddDialog">新增公告</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" border class="custom-table">
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="noticeTitle" label="标题" min-width="240" show-overflow-tooltip />
        <el-table-column prop="noticeType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.noticeType === 1" type="info">通知</el-tag>
            <el-tag v-else-if="row.noticeType === 2" type="success">公告</el-tag>
            <el-tag v-else-if="row.noticeType === 3" type="warning">活动</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isTop" label="置顶" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="danger" size="small">
              置顶
            </el-tag>
            <el-tag v-else type="info" size="small">
              非置顶
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="380" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="editNotice(row)">编辑</el-button>
            <el-button size="small" type="info" plain @click="viewDetail(row)">详情</el-button>
            <el-button size="small" type="success" plain @click="toggleTop(row)">{{ row.isTop === 1 ? '取消置顶' : '置顶'
            }}</el-button>
            <el-button size="small" type="success" plain v-if="row.status === 0" @click="publish(row)">发布</el-button>
            <el-button size="small" type="warning" plain v-else @click="withdraw(row)">撤回</el-button>
            <el-button size="small" type="danger" plain @click="deleteNoticeItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="queryParams.pageNo" :page-size="queryParams.pageSize"
          layout="prev, pager, next, jumper, ->, total" :total="total" @current-change="fetchData" />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="900px" top="5vh" @close="closeDialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="noticeTitle">
          <el-input v-model="form.noticeTitle" placeholder="请输入公告标题" maxlength="150" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="noticeType">
          <el-select v-model="form.noticeType" placeholder="请选择公告类型" style="width: 200px">
            <el-option label="通知" :value="1" />
            <el-option label="公告" :value="2" />
            <el-option label="活动" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否发布">
          <el-switch v-model="form.isPublish" active-text="发布" inactive-text="草稿" />
        </el-form-item>
        <el-form-item label="封面图" prop="coverImage">
          <ImageUploader v-model="form.coverImage" />
        </el-form-item>
        <el-form-item label="内容" prop="noticeContent">
          <el-input v-model="form.noticeContent" type="textarea" :rows="15" placeholder="请输入公告内容（支持 HTML）" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="公告详情" width="900px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="标题" :span="2">{{ detailData.noticeTitle }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag v-if="detailData.noticeType === 1" type="info">通知</el-tag>
          <el-tag v-else-if="detailData.noticeType === 2" type="success">公告</el-tag>
          <el-tag v-else-if="detailData.noticeType === 3" type="warning">活动</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'warning'">
            {{ detailData.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">
          <div v-html="detailData.noticeContent" style="max-height: 500px; overflow-y: auto;"></div>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { pageNotices, getNoticeDetail, saveOrUpdateNotice, deleteNotice, toggleTop as apiToggleTop } from '@/api/notice'
import ImageUploader from '@/components/ImageUploader.vue'

const unwrap = (res: any) => res?.result ?? res?.data?.data ?? res?.data ?? res

// 表格数据
const loading = ref(false)
const total = ref(0)
const tableData = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  noticeType: undefined as number | undefined,
  status: undefined as number | undefined,
  keyword: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await pageNotices({ ...queryParams })
    const pageData = unwrap(res)
    tableData.value = pageData?.records || []
    total.value = Number(pageData?.total || 0)
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryParams.pageNo = 1; fetchData() }
const handleReset = () => {
  queryParams.noticeType = undefined
  queryParams.status = undefined
  queryParams.keyword = ''
  fetchData()
}

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增公告')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  id: undefined as number | undefined,
  noticeTitle: '',
  noticeType: undefined as number | undefined,
  noticeContent: '',
  isPublish: false,
  coverImage: ''
})

const rules = reactive<FormRules>({
  noticeTitle: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  noticeContent: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
})

const openAddDialog = () => {
  dialogTitle.value = '新增公告'
  isEdit.value = false
  dialogVisible.value = true
  Object.assign(form, {
    id: undefined,
    noticeTitle: '',
    noticeType: 1,
    noticeContent: '<p></p>',
    isPublish: false,
    coverImage: ''
  })
}

const editNotice = async (row: any) => {
  dialogTitle.value = '编辑公告'
  isEdit.value = true
  dialogVisible.value = true

  // 加载详情
  try {
    const res: any = await getNoticeDetail(row.id)
    const detail = unwrap(res)
    Object.assign(form, {
      id: detail.id,
      noticeTitle: detail.noticeTitle,
      noticeType: detail.noticeType,
      noticeContent: detail.noticeContent || '<p></p>',
      isPublish: detail.status === 1,
      coverImage: detail.coverImage || ''
    })
  } catch {
    ElMessage.error('加载公告详情失败')
  }
}

const closeDialog = () => {
  formRef.value?.clearValidate?.()
}

const submitForm = async () => {
  if (!formRef.value) return
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return

  submitting.value = true
  try {
    const payload: any = {
      id: form.id,
      noticeTitle: form.noticeTitle,
      noticeType: form.noticeType,
      noticeContent: form.noticeContent,
      isPublish: form.isPublish,
      coverImage: form.coverImage
    }
    await saveOrUpdateNotice(payload)
    ElMessage.success(isEdit.value ? '更新成功' : '保存成功')
    dialogVisible.value = false
    fetchData()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

// 操作
const toggleTop = async (row: any) => {
  try {
    await apiToggleTop(row.id)
    ElMessage.success('操作成功')
    fetchData()
  } catch {
    ElMessage.error('操作失败')
  }
}

const publish = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定发布该公告？', '提示', { type: 'warning' })
    await saveOrUpdateNotice({ id: row.id, isPublish: true })
    ElMessage.success('发布成功')
    fetchData()
  } catch { }
}

const withdraw = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定撤回该公告？', '提示', { type: 'warning' })
    await saveOrUpdateNotice({ id: row.id, isPublish: false })
    ElMessage.success('已撤回')
    fetchData()
  } catch { }
}

const deleteNoticeItem = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' })
    await deleteNotice(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { }
}

// 详情
const detailVisible = ref(false)
const detailData = ref<any>(null)

const viewDetail = async (row: any) => {
  try {
    const res: any = await getNoticeDetail(row.id)
    detailData.value = unwrap(res)
    detailVisible.value = true
  } catch {
    ElMessage.error('加载详情失败')
  }
}

fetchData()
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 12px
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px
}

/* 修复表格背景色，与工单页面保持一致 */
:deep(.custom-table) {
  background-color: transparent;
  --el-table-border-color: rgba(255, 255, 255, 0.05);
  --el-table-row-hover-bg-color: rgba(24, 144, 255, 0.1);

  /* 为固定列添加实色背景 */
  .el-table__fixed-right,
  .el-table__fixed {
    background-color: #1e293b !important;
    height: calc(100% - 12px) !important;
  }

  /* 针对固定列中的单元格 */
  .el-table__cell.is-fixed-right,
  .el-table__cell.is-fixed-left {
    background-color: #1e293b !important;
  }

  /* 修复固定列顶部的表头背景 */
  th.el-table__fixed-right,
  th.el-table__fixed-left {
    background-color: #161e2c !important;
  }

  th.el-table__cell {
    background-color: rgba(15, 23, 42, 0.8) !important;
    color: #cbd5e1;
    font-weight: 600;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  }

  tr {
    background-color: transparent;
  }

  td.el-table__cell {
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    color: #94a3b8;
  }

  /* 悬停效果 */
  tbody tr:hover>td {
    background-color: rgba(24, 144, 255, 0.1) !important;
  }
}
</style>
