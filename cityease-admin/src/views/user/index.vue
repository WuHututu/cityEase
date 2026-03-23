<template>
  <div class="page">
    <el-card shadow="never" class="toolbar">
      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="用户名/姓名/手机号" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryParams.userRole" placeholder="全部" clearable style="width: 160px">
            <el-option label="管理员" :value="1" />
            <el-option label="物业人员" :value="2" />
            <el-option label="业主" :value="3" />
            <el-option label="维修工" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="margin-left: auto">
          <el-button type="primary" plain @click="openAddDialog">新增用户</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" border style="width: 100%">
        <el-table-column prop="userId" label="ID" width="120" />
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="userRole" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.userRole)">{{ roleText(row.userRole) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.isDisabled === 0 ? 'success' : 'danger'">{{ row.isDisabled === 0 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="warning" plain @click="resetPwd(row)">重置密码</el-button>
            <el-button size="small" :type="row.isDisabled === 0 ? 'danger' : 'success'" plain @click="toggleStatus(row)">
              {{ row.isDisabled === 0 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination v-model:current-page="queryParams.pageNo" v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]" background layout="total, sizes, prev, pager, next, jumper" :total="total"
          @size-change="handleSearch" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" @close="closeDialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="必填" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="可选" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="可选" />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <ImageUploader v-model="form.avatar" />
        </el-form-item>
        <el-form-item label="角色" prop="userRole">
          <el-select v-model="form.userRole" placeholder="请选择" style="width: 100%">
            <el-option label="管理员" :value="1" />
            <el-option label="物业人员" :value="2" />
            <el-option label="业主" :value="3" />
            <el-option label="维修工" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input v-model="form.password" placeholder="必填" show-password />
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
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import request from '@/utils/request'
import ImageUploader from '@/components/ImageUploader.vue'

const unwrap = (res: any) => res?.result ?? res?.data?.data ?? res?.data ?? res

const loading = ref(false)
const total = ref(0)
const tableData = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: '',
  userRole: undefined as number | undefined
})

const roleText = (r: any) => {
  switch (Number(r)) {
    case 1: return '管理员'
    case 2: return '物业'
    case 3: return '业主'
    case 4: return '维修工'
    default: return '未知'
  }
}
const roleTagType = (r: any) => {
  switch (Number(r)) {
    case 1: return 'danger'
    case 2: return 'warning'
    case 3: return 'success'
    case 4: return 'info'
    default: return 'info'
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    // 约定：/admin/system/user/page
    const res: any = await request.post('/admin/system/user/page', { ...queryParams })
    const pageData = unwrap(res)
    tableData.value = pageData?.records || []
    total.value = Number(pageData?.total || 0)
  } catch (e) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNo = 1
  fetchData()
}
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.userRole = undefined
  handleSearch()
}

// dialog
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  userId: undefined as string | undefined,
  username: '',
  realName: '',
  phone: '',
  avatar: '',
  userRole: undefined as number | undefined,
  password: ''
})

const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  userRole: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
})

const openAddDialog = () => {
  dialogTitle.value = '新增用户'
  isEdit.value = false
  dialogVisible.value = true
  Object.assign(form, { userId: undefined, username: '', realName: '', phone: '', avatar: '', userRole: undefined, password: '' })
}
const openEditDialog = (row: any) => {
  dialogTitle.value = '编辑用户'
  isEdit.value = true
  dialogVisible.value = true
  Object.assign(form, {
    userId: row.userId ?? row.id,
    username: row.username,
    realName: row.realName,
    phone: row.phone,
    avatar: row.avatar || '',
    userRole: row.userRole,
    password: ''
  })
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
    if (isEdit.value) {
      await request.post('/admin/system/user/save', {
        userId: form.userId,
        username: form.username,
        realName: form.realName,
        phone: form.phone,
        avatar: form.avatar,
        userRole: form.userRole
      })
    } else {
      await request.post('/admin/system/user/save', {
        username: form.username,
        realName: form.realName,
        phone: form.phone,
        avatar: form.avatar,
        userRole: form.userRole,
        password: form.password
      })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row: any) => {
  const enable = row.isDisabled !== 0  // 如果当前是禁用状态（1），则启用
  try {
    await ElMessageBox.confirm(
      enable ? '确定启用该用户？' : '确定禁用该用户？',
      '提示',
      { type: 'warning' }
    )
    await request.post('/admin/system/user/disable', { userId: row.userId ?? row.id, disable: enable ? 0 : 1 })
    ElMessage.success('操作成功')
    fetchData()
  } catch {
    // cancel
  }
}

const resetPwd = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定重置该用户密码？', '提示', { type: 'warning' })
    await request.post('/admin/system/user/resetPwd', { userId: row.userId ?? row.id })
    ElMessage.success('已重置密码')
  } catch {
    // cancel
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

.toolbar {
  margin-bottom: 0;
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}
</style>
