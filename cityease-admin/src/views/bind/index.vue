<template>
  <div class="page bind-page">
    <section class="page-hero bind-hero">
      <div class="page-hero__content">
        <div class="page-eyebrow">Bind Audit Workspace</div>
        <h2>绑定审核工作台</h2>
        <p>集中核验住户提交的房屋绑定申请，查看材料、补充证明并直接完成审核。</p>
      </div>

      <div class="page-hero__stats">
        <div class="hero-stat">
          <span>待审核</span>
          <strong>{{ pendingCount }}</strong>
        </div>
        <div class="hero-stat">
          <span>已通过</span>
          <strong>{{ approvedCount }}</strong>
        </div>
        <div class="hero-stat">
          <span>已驳回</span>
          <strong>{{ rejectedCount }}</strong>
        </div>
      </div>
    </section>

    <el-card shadow="never" class="toolbar surface-card">
      <div class="surface-card__header">
        <div>
          <h3>筛选申请</h3>
          <p>先筛状态和身份，再进入详情页做材料核验。</p>
        </div>
      </div>

      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.status" clearable placeholder="全部状态" style="width: 180px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="绑定身份">
          <el-select v-model="queryParams.relationType" clearable placeholder="全部身份" style="width: 180px">
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
    </el-card>

    <el-card shadow="never" class="surface-card">
      <div class="surface-card__header">
        <div>
          <h3>申请列表</h3>
          <p>当前只保留管理端审核链，证明材料会同步写入审核快照表和人房绑定表。</p>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="relId" label="申请ID" width="140" />
        <el-table-column prop="userName" label="申请人" min-width="140" />
        <el-table-column prop="userId" label="用户ID" width="130" />
        <el-table-column prop="fullRoomName" label="房屋信息" min-width="260" show-overflow-tooltip />
        <el-table-column prop="relationType" label="身份" width="120" align="center">
          <template #default="{ row }">
            <el-tag effect="plain">{{ relationTypeText(row.relationType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="材料" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.attachmentsList?.length ? 'success' : 'warning'">
              {{ row.attachmentsList?.length ? `${row.attachmentsList.length} 张` : '待补充' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" min-width="180" />
        <el-table-column label="操作" min-width="220">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="viewDetail(row)">详情</el-button>
              <el-button v-if="row.status === 0" size="small" type="success" plain @click="approve(row)">通过</el-button>
              <el-button v-if="row.status === 0" size="small" type="warning" plain @click="reject(row)">驳回</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          background
          layout="total, prev, pager, next, jumper"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="绑定审核详情" width="1100px" top="5vh">
      <div v-loading="detailLoading" class="bind-detail" v-if="detailData">
        <section class="bind-detail__main">
          <div class="detail-panel">
            <div class="detail-panel__header">
              <div>
                <div class="page-eyebrow">Applicant</div>
                <h3>{{ detailData.userName || '未命名申请人' }}</h3>
              </div>
              <el-tag :type="statusTag(detailData.status)">{{ statusText(detailData.status) }}</el-tag>
            </div>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="申请ID">{{ detailData.relId }}</el-descriptions-item>
              <el-descriptions-item label="用户ID">{{ detailData.userId }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ detailData.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="绑定身份">{{ relationTypeText(detailData.relationType) }}</el-descriptions-item>
              <el-descriptions-item label="房屋信息" :span="2">{{ detailData.fullRoomName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="申请时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="审核时间">{{ detailData.auditTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="审核人">{{ detailData.auditorName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="审核备注">{{ detailData.remark || '暂无备注' }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="detail-panel">
            <div class="detail-panel__header detail-panel__header--tight">
              <div>
                <div class="page-eyebrow">Materials</div>
                <h3>证明材料</h3>
              </div>
              <el-button type="primary" :loading="savingAttachments" @click="saveAttachments">保存材料</el-button>
            </div>

            <MultiImageUploader v-model="attachmentDraft" :limit="6" />
          </div>
        </section>

        <aside class="bind-detail__side">
          <div class="detail-side-card">
            <div class="page-eyebrow">Audit Snapshot</div>
            <h3>{{ statusText(detailData.status) }}</h3>
            <p>材料上传到 OSS 后会同时同步到 `pms_bind_audit` 和当前人房绑定记录。</p>
            <div class="detail-side-card__meta">
              <span>材料数量</span>
              <strong>{{ attachmentDraft.length }}</strong>
            </div>
            <div class="detail-side-card__meta">
              <span>审核快照</span>
              <strong>{{ detailData.auditId || '-' }}</strong>
            </div>
          </div>

          <div v-if="detailData.status === 0" class="detail-side-card">
            <div class="page-eyebrow">Actions</div>
            <h3>快速处理</h3>
            <p>确认材料无误后可直接在详情侧边完成审核。</p>
            <div class="detail-side-card__actions">
              <el-button type="success" @click="approve(detailData)">通过申请</el-button>
              <el-button type="warning" plain @click="reject(detailData)">驳回申请</el-button>
            </div>
          </div>
        </aside>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MultiImageUploader from '@/components/MultiImageUploader.vue'
import {
  approveBind,
  getBindDetail,
  pageBindAudit,
  rejectBind,
  updateBindAttachments,
  type BindAuditDetail,
  type BindAuditRecord
} from '@/api/bind'

const unwrap = (res: any) => res?.result ?? res?.data?.data ?? res?.data ?? res

const loading = ref(false)
const detailLoading = ref(false)
const savingAttachments = ref(false)
const total = ref(0)
const tableData = ref<BindAuditRecord[]>([])
const detailVisible = ref(false)
const detailData = ref<BindAuditDetail | null>(null)
const attachmentDraft = ref<string[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  status: undefined as number | undefined,
  relationType: undefined as number | undefined
})

const pendingCount = computed(() => tableData.value.filter(item => item.status === 0).length)
const approvedCount = computed(() => tableData.value.filter(item => item.status === 1).length)
const rejectedCount = computed(() => tableData.value.filter(item => item.status === 2).length)

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
    const res = await pageBindAudit({ ...queryParams })
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

const fetchDetail = async (relId: number) => {
  detailLoading.value = true
  try {
    const detail = await getBindDetail(relId)
    detailData.value = detail
    attachmentDraft.value = [...(detail.attachmentsList || [])]
  } catch {
    ElMessage.error('加载绑定审核详情失败')
  } finally {
    detailLoading.value = false
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

const viewDetail = async (row: BindAuditRecord) => {
  detailVisible.value = true
  await fetchDetail(row.relId)
}

const saveAttachments = async () => {
  if (!detailData.value) return

  savingAttachments.value = true
  try {
    await updateBindAttachments(detailData.value.relId, attachmentDraft.value)
    ElMessage.success('证明材料已保存')
    await fetchDetail(detailData.value.relId)
    await fetchData()
  } catch {
    ElMessage.error('保存证明材料失败')
  } finally {
    savingAttachments.value = false
  }
}

const approve = async (row: BindAuditRecord | BindAuditDetail) => {
  try {
    await ElMessageBox.confirm('确认通过这条绑定申请吗？', '审核确认', { type: 'warning' })
    await approveBind(row.relId)
    ElMessage.success('审核通过')
    if (detailVisible.value && detailData.value?.relId === row.relId) {
      await fetchDetail(row.relId)
    }
    await fetchData()
  } catch {
    // ignore
  }
}

const reject = async (row: BindAuditRecord | BindAuditDetail) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '审核驳回', {
      inputType: 'textarea',
      inputValidator: (input) => !!input?.trim() || '请输入驳回原因',
      confirmButtonText: '提交',
      cancelButtonText: '取消'
    })
    await rejectBind(row.relId, value.trim())
    ElMessage.success('已驳回')
    if (detailVisible.value && detailData.value?.relId === row.relId) {
      await fetchDetail(row.relId)
    }
    await fetchData()
  } catch {
    // ignore
  }
}

fetchData()
</script>

<style scoped>
.bind-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.bind-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(280px, 0.9fr);
  gap: 18px;
  padding: 26px 28px;
  border-radius: 30px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  background:
    radial-gradient(circle at top right, rgba(34, 197, 94, 0.16), transparent 28%),
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
.surface-card__header h3,
.detail-panel__header h3,
.detail-side-card h3 {
  margin: 0;
}

.page-hero__content p,
.surface-card__header p,
.detail-side-card p {
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

.table-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.bind-detail {
  display: grid;
  grid-template-columns: minmax(0, 2fr) 300px;
  gap: 20px;
}

.bind-detail__main {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-panel,
.detail-side-card {
  border-radius: 26px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  background:
    linear-gradient(180deg, rgba(8, 15, 27, 0.92), rgba(11, 20, 34, 0.9)),
    radial-gradient(circle at top right, rgba(56, 189, 248, 0.08), transparent 36%);
  padding: 20px;
}

.detail-panel__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.detail-panel__header--tight {
  align-items: center;
}

.bind-detail__side {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-side-card__meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid rgba(148, 163, 184, 0.1);
  color: rgba(203, 213, 225, 0.78);
}

.detail-side-card__meta strong {
  color: #f8fbff;
}

.detail-side-card__actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 16px;
}

@media (max-width: 1080px) {
  .bind-hero,
  .bind-detail {
    grid-template-columns: 1fr;
  }

  .page-hero__stats {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .page-hero__stats {
    grid-template-columns: 1fr;
  }

  .detail-panel__header,
  .surface-card__header {
    flex-direction: column;
  }
}
</style>
