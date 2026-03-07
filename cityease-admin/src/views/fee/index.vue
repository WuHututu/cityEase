<template>
  <div class="page">
    <el-card shadow="never" class="toolbar">
      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item label="月份">
          <el-input v-model="queryParams.feeMonth" placeholder="yyyy-MM，如 2026-03" clearable style="width: 190px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待缴费" :value="0" />
            <el-option label="已缴费" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="房号">
          <el-input v-model="queryParams.roomKeyword" placeholder="房号关键字" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>

        <el-form-item style="margin-left:auto">
          <el-button type="primary" plain @click="openGenerateDialog">批量生成</el-button>
          <el-button type="primary" plain @click="openAddDialog">新增账单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="120" />
        <el-table-column prop="feeMonth" label="月份" width="110" />
        <el-table-column prop="amount" label="金额" width="110" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status===1 ? 'success' : 'warning'">
              {{ row.status===1 ? '已缴费' : '待缴费' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="roomNum" label="房号" width="120" />
        <el-table-column prop="fullAddress" label="地址" min-width="220" show-overflow-tooltip />

        <el-table-column prop="payerId" label="支付人ID" width="140" />
        <el-table-column prop="payTime" label="支付时间" min-width="160" />
        <el-table-column prop="createTime" label="生成时间" min-width="160" />

        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="openEditDialog(row)">编辑</el-button>

            <el-button
              v-if="row.status===0"
              size="small"
              type="success"
              plain
              @click="markPaid(row)"
            >标记缴费</el-button>

            <el-button
              v-else
              size="small"
              type="warning"
              plain
              @click="markUnpaid(row)"
            >取消缴费</el-button>

            <el-button size="small" type="danger" plain @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          :page-size="queryParams.pageSize"
          layout="prev, pager, next, jumper, ->, total"
          :total="total"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" @close="closeDialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="房屋ID" prop="roomId">
          <el-input v-model="form.roomId" placeholder="请输入房屋ID（MVP）" />
        </el-form-item>
        <el-form-item label="月份" prop="feeMonth">
          <el-input v-model="form.feeMonth" placeholder="yyyy-MM，如 2026-03" />
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input v-model="form.amount" placeholder="如 120.00" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="默认待缴费" clearable style="width: 180px">
            <el-option label="待缴费" :value="0" />
            <el-option label="已缴费" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量生成 -->
    <el-dialog v-model="genVisible" title="批量生成账单" width="520px">
      <el-form :model="genForm" label-width="90px">
        <el-form-item label="月份">
          <el-input v-model="genForm.feeMonth" placeholder="yyyy-MM，如 2026-03" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input v-model="genForm.amount" placeholder="如 120.00" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="genVisible=false">取消</el-button>
        <el-button type="primary" :loading="genLoading" @click="doGenerate">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import request from '@/utils/request'

const unwrap = (res:any)=>res?.result ?? res?.data?.data ?? res?.data ?? res

const loading = ref(false)
const total = ref(0)
const tableData = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  feeMonth: '',
  status: undefined as number | undefined,
  roomKeyword: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const res:any = await request.post('/admin/pms/fee/page', { ...queryParams })
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
const handleReset = () => { queryParams.feeMonth=''; queryParams.status=undefined; queryParams.roomKeyword=''; handleSearch() }

// add/edit dialog
const dialogVisible = ref(false)
const dialogTitle = ref('新增账单')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  id: undefined as string | undefined,
  roomId: '',
  feeMonth: '',
  amount: '',
  status: undefined as number | undefined
})

const rules = reactive<FormRules>({
  roomId: [{ required: true, message: '请输入房屋ID', trigger: 'blur' }],
  feeMonth: [{ required: true, message: '请输入月份', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
})

const openAddDialog = () => {
  dialogTitle.value = '新增账单'
  isEdit.value = false
  dialogVisible.value = true
  Object.assign(form, { id: undefined, roomId: '', feeMonth: '', amount: '', status: undefined })
}
const openEditDialog = (row:any) => {
  dialogTitle.value = '编辑账单'
  isEdit.value = true
  dialogVisible.value = true
  Object.assign(form, {
    id: String(row.id),
    roomId: String(row.roomId ?? ''),
    feeMonth: row.feeMonth ?? '',
    amount: String(row.amount ?? ''),
    status: row.status
  })
}
const closeDialog = () => formRef.value?.clearValidate?.()

const submitForm = async () => {
  if (!formRef.value) return
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return

  submitting.value = true
  try {
    const payload:any = {
      id: form.id ? Number(form.id) : undefined,
      roomId: Number(form.roomId),
      feeMonth: form.feeMonth,
      amount: Number(form.amount),
      status: form.status
    }
    if (isEdit.value) {
      const res:any = await request.post('/admin/pms/fee/update', payload)
      ElMessage.success(unwrap(res) || '更新成功')
    } else {
      const res:any = await request.post('/admin/pms/fee/save', payload)
      ElMessage.success(unwrap(res) || '保存成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

// operations
const markPaid = async (row:any) => {
  try {
    await ElMessageBox.confirm('确定标记为已缴费？', '提示', { type: 'warning' })
    const res:any = await request.post('/admin/pms/fee/markPaid', { id: row.id })
    ElMessage.success(unwrap(res) || '已标记缴费')
    fetchData()
  } catch {}
}
const markUnpaid = async (row:any) => {
  try {
    await ElMessageBox.confirm('确定取消缴费？', '提示', { type: 'warning' })
    const res:any = await request.post('/admin/pms/fee/markUnpaid', { id: row.id })
    ElMessage.success(unwrap(res) || '已取消缴费')
    fetchData()
  } catch {}
}
const remove = async (row:any) => {
  try {
    await ElMessageBox.confirm('确定删除该账单？', '提示', { type: 'warning' })
    const res:any = await request.post('/admin/pms/fee/delete', { id: row.id })
    ElMessage.success(unwrap(res) || '删除成功')
    fetchData()
  } catch {}
}

// generate dialog
const genVisible = ref(false)
const genLoading = ref(false)
const genForm = reactive({ feeMonth: '', amount: '' })

const openGenerateDialog = () => {
  genVisible.value = true
  Object.assign(genForm, { feeMonth: '', amount: '' })
}

const doGenerate = async () => {
  if (!genForm.feeMonth || !genForm.amount) {
    ElMessage.warning('请输入月份和金额')
    return
  }
  genLoading.value = true
  try {
    const res:any = await request.post('/admin/pms/fee/generate', { feeMonth: genForm.feeMonth, amount: Number(genForm.amount) })
    ElMessage.success(unwrap(res) || '生成成功')
    genVisible.value = false
    fetchData()
  } catch {
    ElMessage.error('生成失败')
  } finally {
    genLoading.value = false
  }
}

fetchData()
</script>

<style scoped>
.page { display:flex; flex-direction:column; gap:12px; }
.toolbar { margin-bottom:0; }
.pager { display:flex; justify-content:flex-end; padding-top:12px; }
</style>
