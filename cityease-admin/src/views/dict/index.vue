<template>
  <div class="dict-page">
    <el-card shadow="never" class="toolbar">
      <div class="dict-hero">
        <div>
          <h2>字典管理</h2>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="dict-section">
      <div class="dict-section__header">
        <div>
          <div class="section-eyebrow">Step 1</div>
          <h3>字典类型</h3>
        </div>
        <div class="dict-section__actions">
          <el-input v-model="typeKeyword" clearable placeholder="搜索字典名称或类型" style="width: 260px" />
          <el-button type="primary" @click="openTypeDialog()">新增类型</el-button>
        </div>
      </div>

      <el-table
        :data="filteredTypeList"
        border
        highlight-current-row
        v-loading="typeLoading"
        row-key="id"
        @current-change="handleTypeChange"
      >
        <el-table-column prop="dictName" label="字典名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="dictType" label="字典类型" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="openTypeDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="handleDeleteType(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="dict-section">
      <div v-if="currentType" class="dict-section__header">
        <div>
          <div class="section-eyebrow">Step 2</div>
          <h3>{{ currentType.dictName }}</h3>
          <p>当前类型：{{ currentType.dictType }}</p>
        </div>
        <div class="dict-section__actions">
          <el-input
            v-model="queryForm.label"
            clearable
            placeholder="搜索字典标签"
            style="width: 240px"
            @keyup.enter="fetchDataList"
          />
          <el-button @click="fetchDataList">查询</el-button>
          <el-button type="primary" @click="openDataDialog()">新增数据</el-button>
        </div>
      </div>

      <template v-if="currentType">
        <div class="type-meta">
          <el-tag type="info">共 {{ dataList.length }} 条数据</el-tag>
          <el-tag :type="currentType.status === 1 ? 'success' : 'warning'">
            {{ currentType.status === 1 ? '类型已启用' : '类型已停用' }}
          </el-tag>
        </div>

        <el-table :data="dataList" border v-loading="dataLoading" row-key="id">
          <el-table-column prop="dictLabel" label="字典标签" min-width="180" />
          <el-table-column prop="dictValue" label="字典值" min-width="180" />
          <el-table-column prop="dictSort" label="排序" width="100" align="center" />
          <el-table-column label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="240" show-overflow-tooltip />
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button size="small" type="primary" plain @click="openDataDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="handleDeleteData(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <div v-else class="empty-panel">
        <el-empty description="先从上方选择一个字典类型，再查看和维护它的数据。" :image-size="88" />
      </div>
    </el-card>

    <el-dialog v-model="typeDialog.visible" :title="typeDialog.title" width="560px">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="100px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="typeForm.dictType" placeholder="例如 repair_type" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="typeForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="typeForm.remark" type="textarea" :rows="3" placeholder="可选说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="typeDialog.saving" @click="handleSaveType">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dataDialog.visible" :title="dataDialog.title" width="620px">
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px">
        <el-form-item label="所属类型">
          <el-input :model-value="dataForm.dictType" disabled />
        </el-form-item>
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="请输入展示标签" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="请输入提交值" />
        </el-form-item>
        <el-form-item label="排序" prop="dictSort">
          <el-input-number v-model="dataForm.dictSort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dataForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dataForm.remark" type="textarea" :rows="3" placeholder="可选说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dataDialog.saving" @click="handleSaveData">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { deleteData, deleteType, listData, listTypes, saveData, saveType, updateData, updateType } from '@/api/dict'

const typeLoading = ref(false)
const dataLoading = ref(false)

const typeList = ref<any[]>([])
const dataList = ref<any[]>([])
const currentType = ref<any>(null)
const typeKeyword = ref('')

const filteredTypeList = computed(() => {
  const keyword = typeKeyword.value.trim().toLowerCase()
  if (!keyword) return typeList.value
  return typeList.value.filter((item) =>
    String(item.dictName || '').toLowerCase().includes(keyword) ||
    String(item.dictType || '').toLowerCase().includes(keyword)
  )
})

const typeDialog = reactive({
  visible: false,
  title: '',
  saving: false
})

const dataDialog = reactive({
  visible: false,
  title: '',
  saving: false
})

const queryForm = reactive({
  label: ''
})

const typeFormRef = ref<FormInstance>()
const typeForm = reactive<any>({
  id: null,
  dictName: '',
  dictType: '',
  status: 1,
  remark: ''
})

const dataFormRef = ref<FormInstance>()
const dataForm = reactive<any>({
  id: null,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  dictSort: 0,
  status: 1,
  remark: ''
})

const typeRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}

const dataRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }]
}

const fetchTypeList = async () => {
  typeLoading.value = true
  try {
    const res: any = await listTypes()
    typeList.value = res || []

    if (!typeList.value.length) {
      currentType.value = null
      dataList.value = []
      return
    }

    if (currentType.value?.id) {
      currentType.value = typeList.value.find((item) => item.id === currentType.value.id) || typeList.value[0]
    } else {
      currentType.value = typeList.value[0]
    }

    await fetchDataList()
  } finally {
    typeLoading.value = false
  }
}

const fetchDataList = async () => {
  if (!currentType.value) return

  dataLoading.value = true
  try {
    const res: any = await listData({
      dictType: currentType.value.dictType,
      label: queryForm.label || undefined
    })
    dataList.value = res || []
  } finally {
    dataLoading.value = false
  }
}

const handleTypeChange = (row: any) => {
  currentType.value = row
  queryForm.label = ''
  fetchDataList()
}

const openTypeDialog = (row?: any) => {
  if (row) {
    Object.assign(typeForm, {
      id: row.id,
      dictName: row.dictName,
      dictType: row.dictType,
      status: row.status,
      remark: row.remark
    })
    typeDialog.title = '编辑字典类型'
  } else {
    Object.assign(typeForm, {
      id: null,
      dictName: '',
      dictType: '',
      status: 1,
      remark: ''
    })
    typeDialog.title = '新增字典类型'
  }
  typeDialog.visible = true
}

const handleSaveType = async () => {
  await typeFormRef.value?.validate()
  typeDialog.saving = true
  try {
    if (typeForm.id) {
      await updateType(typeForm)
    } else {
      await saveType(typeForm)
    }
    ElMessage.success('字典类型已保存')
    typeDialog.visible = false
    await fetchTypeList()
  } finally {
    typeDialog.saving = false
  }
}

const handleDeleteType = async (row: any) => {
  await ElMessageBox.confirm(`确定删除字典类型“${row.dictName}”吗？`, '删除字典类型', { type: 'warning' })
  await deleteType(row.id)
  ElMessage.success('字典类型已删除')
  if (currentType.value?.id === row.id) {
    currentType.value = null
    dataList.value = []
  }
  await fetchTypeList()
}

const openDataDialog = (row?: any) => {
  if (!currentType.value && !row) {
    ElMessage.warning('请先选择一个字典类型')
    return
  }

  if (row) {
    Object.assign(dataForm, {
      id: row.id,
      dictType: row.dictType,
      dictLabel: row.dictLabel,
      dictValue: row.dictValue,
      dictSort: row.dictSort,
      status: row.status,
      remark: row.remark
    })
    dataDialog.title = '编辑字典数据'
  } else {
    Object.assign(dataForm, {
      id: null,
      dictType: currentType.value?.dictType || '',
      dictLabel: '',
      dictValue: '',
      dictSort: 0,
      status: 1,
      remark: ''
    })
    dataDialog.title = '新增字典数据'
  }

  dataDialog.visible = true
}

const handleSaveData = async () => {
  await dataFormRef.value?.validate()
  dataDialog.saving = true
  try {
    if (dataForm.id) {
      await updateData(dataForm)
    } else {
      await saveData(dataForm)
    }
    ElMessage.success('字典数据已保存')
    dataDialog.visible = false
    await fetchDataList()
  } finally {
    dataDialog.saving = false
  }
}

const handleDeleteData = async (row: any) => {
  await ElMessageBox.confirm(`确定删除字典数据“${row.dictLabel}”吗？`, '删除字典数据', { type: 'warning' })
  await deleteData(row.id)
  ElMessage.success('字典数据已删除')
  await fetchDataList()
}

onMounted(fetchTypeList)
</script>

<style scoped>
.dict-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dict-hero h2 {
  margin: 0 0 8px;
  font-size: 24px;
}

.dict-hero p {
  margin: 0;
  color: rgba(148, 163, 184, 0.86);
}

.dict-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dict-section__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 4px;
}

.dict-section__header h3 {
  margin: 4px 0 8px;
  font-size: 22px;
}

.dict-section__header p {
  margin: 0;
  color: rgba(148, 163, 184, 0.8);
}

.dict-section__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.section-eyebrow {
  color: #7dd3fc;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.type-meta {
  margin-top: 10px;
  margin-bottom: 10px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.empty-panel {
  padding: 42px 0 28px;
}

@media (max-width: 960px) {
  .dict-section__header {
    flex-direction: column;
  }

  .dict-section__actions {
    width: 100%;
  }
}
</style>
