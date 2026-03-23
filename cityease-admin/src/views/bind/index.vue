<template>
  <div class="page">
    <el-card shadow="never" class="toolbar">
      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.status" clearable placeholder="全部状态" style="width: 160px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="绑定身份">
          <el-select v-model="queryParams.relationType" clearable placeholder="全部身份" style="width: 160px">
            <el-option label="业主" :value="1" />
            <el-option label="家属" :value="2" />
            <el-option label="租客" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="toolbar-tip">当前页面已切到真实的人房绑定审核接口，审批结果会直接影响绑定关系。</div>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="relId" label="申请ID" width="120" />
        <el-table-column prop="userName" label="申请人" min-width="140" />
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="fullRoomName" label="房屋信息" min-width="260" />
        <el-table-column prop="relationType" label="身份" width="120">
          <template #default="{ row }">
            <el-tag effect="plain">{{ relationTypeText(row.relationType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" min-width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" size="small" type="success" plain @click="approve(row)">通过</el-button>
            <el-button v-if="row.status === 0" size="small" type="warning" plain @click="reject(row)">驳回</el-button>
            <el-button size="small" type="info" plain @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="绑定申请详情" width="760px">
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请ID">{{ detailData.relId }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ detailData.userId }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detailData.userName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="绑定身份">{{ relationTypeText(detailData.relationType) }}</el-descriptions-item>
          <el-descriptions-item label="房屋信息" :span="2">{{ detailData.fullRoomName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="statusTag(detailData.status)">{{ statusText(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="attachments">
          <div class="attachments-title">证明材料</div>
          <div v-if="detailData.attachmentsList?.length" class="attachments-grid">
            <el-image
              v-for="url in detailData.attachmentsList"
              :key="url"
              :src="url"
              :preview-src-list="detailData.attachmentsList"
              fit="cover"
              class="attachment-image"
              preview-teleported
            />
          </div>
          <el-empty v-else description="暂无证明材料" :image-size="80" />
        </div>
      </template>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { approveBind, pageBindAudit, rejectBind, type BindAuditRecord } from '@/api/bind'

const unwrap = (res: any) => res?.result ?? res?.data?.data ?? res?.data ?? res

const loading = ref(false)
const total = ref(0)
const tableData = ref<BindAuditRecord[]>([])
const detailVisible = ref(false)
const detailData = ref<BindAuditRecord | null>(null)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  status: undefined as number | undefined,
  relationType: undefined as number | undefined
})

const statusText = (status?: number) => {
  if (status === 0) return '待审核'
  if (status === 1) return '已通过'
  if (status === 2) return '已驳回'
  return '未知'
}

const statusTag = (status?: number) => {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
}

const relationTypeText = (relationType?: number) => {
  if (relationType === 1) return '业主'
  if (relationType === 2) return '家属'
  if (relationType === 3) return '租客'
  return '-'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await pageBindAudit({ ...queryParams })
    const pageData = unwrap(res)
    tableData.value = pageData?.records || []
    total.value = Number(pageData?.total || 0)
  } catch {
    tableData.value = []
    total.value = 0
    ElMessage.error('获取绑定审核列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNo = 1
  fetchData()
}

const handleReset = () => {
  queryParams.pageNo = 1
  queryParams.status = undefined
  queryParams.relationType = undefined
  fetchData()
}

const viewDetail = (row: BindAuditRecord) => {
  detailData.value = row
  detailVisible.value = true
}

const approve = async (row: BindAuditRecord) => {
  try {
    await ElMessageBox.confirm('确认通过这条绑定申请吗？', '提示', { type: 'warning' })
    await approveBind(row.relId)
    ElMessage.success('审核通过')
    fetchData()
  } catch {
    // ignore
  }
}

const reject = async (row: BindAuditRecord) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '提示', {
      inputType: 'textarea',
      inputValidator: (input) => !!input?.trim() || '请输入驳回原因',
      confirmButtonText: '提交',
      cancelButtonText: '取消'
    })
    await rejectBind(row.relId, value.trim())
    ElMessage.success('已驳回')
    fetchData()
  } catch {
    // ignore
  }
}

fetchData()
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.toolbar-tip {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}

.attachments {
  margin-top: 16px;
}

.attachments-title {
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}

.attachments-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.attachment-image {
  width: 100%;
  height: 120px;
  border-radius: 10px;
  overflow: hidden;
  background: #f8fafc;
}
</style>
