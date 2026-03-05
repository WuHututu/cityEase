<template>
  <div class="page">
    <el-card shadow="never" class="toolbar">
      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item label="所属区域">
          <el-tree-select
            v-model="queryParams.areaId"
            :data="areaTree"
            node-key="id"
            :props="treeSelectProps"
            clearable
            check-strictly
            placeholder="选择区域（可选）"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="queryParams.keyword" placeholder="房号/地址/业主" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>

        <el-form-item style="margin-left:auto">
          <el-button type="primary" plain @click="openAddDialog">新增房屋</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="120" />
        <el-table-column prop="roomNum" label="房号" min-width="120" />
        <el-table-column prop="fullAddress" label="地址" min-width="240" />
        <el-table-column prop="areaName" label="所属区域" min-width="160" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="deleteRoom(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" @close="closeDialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属区域" prop="areaId">
          <el-tree-select
            v-model="form.areaId"
            :data="areaTree"
            node-key="id"
            :props="treeSelectProps"
            check-strictly
            placeholder="请选择区域（必填）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="房号" prop="roomNum">
          <el-input v-model="form.roomNum" placeholder="如：1101" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="可选" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import request from '@/utils/request'

const unwrap = (res: any) => res?.result ?? res?.data?.data ?? res?.data ?? res

// area tree for selects
const areaTree = ref<any[]>([])
const treeSelectProps = { children: 'children', label: 'areaName', value: 'id' }

const fetchAreaTree = async () => {
  try {
    const res: any = await request.get('/admin/pms/area/tree')
    areaTree.value = unwrap(res) || []
  } catch {
    areaTree.value = []
  }
}

// page
const loading = ref(false)
const total = ref(0)
const tableData = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  areaId: undefined as string | undefined,
  keyword: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    // 约定：/admin/pms/room/page
    const res: any = await request.post('/admin/pms/room/page', { ...queryParams })
    const pageData = unwrap(res)
    tableData.value = pageData?.records || []
    total.value = Number(pageData?.total || 0)
  } catch {
    ElMessage.error('获取房屋列表失败（后端接口可后续补齐）')
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNo = 1
  fetchData()
}
const handleReset = () => {
  queryParams.areaId = undefined
  queryParams.keyword = ''
  handleSearch()
}

// dialog
const dialogVisible = ref(false)
const dialogTitle = ref('新增房屋')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  id: undefined as string | undefined,
  areaId: undefined as string | undefined,
  roomNum: '',
  remark: ''
})

const rules = reactive<FormRules>({
  areaId: [{ required: true, message: '请选择所属区域', trigger: 'change' }],
  roomNum: [{ required: true, message: '请输入房号', trigger: 'blur' }]
})

const openAddDialog = () => {
  dialogTitle.value = '新增房屋'
  isEdit.value = false
  dialogVisible.value = true
  Object.assign(form, { id: undefined, areaId: undefined, roomNum: '', remark: '' })
}
const openEditDialog = (row: any) => {
  dialogTitle.value = '编辑房屋'
  isEdit.value = true
  dialogVisible.value = true
  Object.assign(form, {
    id: String(row.id),
    areaId: row.areaId ? String(row.areaId) : undefined,
    roomNum: row.roomNum,
    remark: row.remark ?? ''
  })
}
const closeDialog = () => formRef.value?.clearValidate?.()

const submitForm = async () => {
  if (!formRef.value) return
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await request.post('/admin/pms/room/update', {
        id: form.id,
        areaId: form.areaId,
        roomNum: form.roomNum,
        remark: form.remark
      })
    } else {
      await request.post('/admin/pms/room/save', {
        areaId: form.areaId,
        roomNum: form.roomNum,
        remark: form.remark
      })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch {
    ElMessage.error('保存失败（若接口未实现，可后续补齐）')
  } finally {
    submitting.value = false
  }
}

const deleteRoom = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除该房屋？', '提示', { type: 'warning' })
    await request.post('/admin/pms/room/delete', { id: String(row.id) })
    ElMessage.success('已删除')
    fetchData()
  } catch {
    // cancel
  }
}

onMounted(async () => {
  await fetchAreaTree()
  await fetchData()
})
</script>

<style scoped>
.page { display:flex; flex-direction:column; gap:12px; }
.pager { display:flex; justify-content:flex-end; padding-top:12px; }
</style>
