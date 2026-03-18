<template>
    <div class="dict-container">
        <el-row :gutter="20">
            <!-- 左侧：字典类型列表 -->
            <el-col :span="8">
                <div class="type-box">
                    <div class="toolbar">
                        <el-button type="primary" size="small" @click="openTypeDialog()">新增类型</el-button>
                    </div>
                    <el-table :data="typeList" border highlight-current-row style="width: 100%" v-loading="typeLoading"
                        @current-change="handleTypeChange" class="custom-table">
                        <el-table-column prop="dictName" label="字典名称" show-overflow-tooltip min-width="120" />
                        <el-table-column prop="dictType" label="字典类型" show-overflow-tooltip min-width="120" />
                        <el-table-column prop="remark" label="备注" show-overflow-tooltip min-width="150" />
                        <el-table-column label="操作" width="120" fixed="right">
                            <template #default="{ row }">
                                <el-button size="small" @click="openTypeDialog(row)">编辑</el-button>
                                <el-button size="small" type="danger" @click="handleDeleteType(row)">删除</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-col>

            <!-- 右侧：字典数据列表 -->
            <el-col :span="16">
                <div class="data-box" v-if="currentType">
                    <div class="header">
                        <h3>{{ currentType.dictName }} - 字典数据</h3>
                        <el-button type="primary" size="small" @click="openDataDialog()">新增数据</el-button>
                    </div>

                    <div class="toolbar">
                        <el-input v-model="queryForm.label" placeholder="搜索字典标签" style="width: 200px" clearable
                            @keyup.enter="fetchDataList" />
                        <el-button @click="fetchDataList">查询</el-button>
                    </div>

                    <el-table :data="dataList" border style="width: 100%" v-loading="dataLoading" class="custom-table">
                        <el-table-column prop="dictLabel" label="字典标签" width="150" />
                        <el-table-column prop="dictValue" label="字典值" width="150" />
                        <el-table-column prop="dictSort" label="排序" width="80" />
                        <el-table-column prop="status" label="状态" width="80">
                            <template #default="{ row }">
                                <el-tag :type="row.status === 1 ? 'success' : 'info'">
                                    {{ row.status === 1 ? '正常' : '停用' }}
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column prop="remark" label="备注" show-overflow-tooltip min-width="150" />
                        <el-table-column label="操作" width="180" fixed="right">
                            <template #default="{ row }">
                                <el-button size="small" @click="openDataDialog(row)">编辑</el-button>
                                <el-button size="small" type="danger" @click="handleDeleteData(row)">删除</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
                <div class="empty-tip" v-else>
                    <el-empty description="请选择一个字典类型" />
                </div>
            </el-col>
        </el-row>

        <!-- 字典类型弹窗 -->
        <el-dialog v-model="typeDialog.visible" :title="typeDialog.title" width="500px">
            <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="100px">
                <el-form-item label="字典名称" prop="dictName">
                    <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
                </el-form-item>
                <el-form-item label="字典类型" prop="dictType">
                    <el-input v-model="typeForm.dictType" placeholder="请输入字典类型（如：repair_type）" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="typeForm.status">
                        <el-radio :label="1">正常</el-radio>
                        <el-radio :label="0">停用</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="typeForm.remark" type="textarea" :rows="3" placeholder="可选" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="typeDialog.visible = false">取消</el-button>
                <el-button type="primary" :loading="typeDialog.saving" @click="handleSaveType">保存</el-button>
            </template>
        </el-dialog>

        <!-- 字典数据弹窗 -->
        <el-dialog v-model="dataDialog.visible" :title="dataDialog.title" width="600px">
            <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px">
                <el-form-item label="字典标签" prop="dictLabel">
                    <el-input v-model="dataForm.dictLabel" placeholder="请输入字典标签" />
                </el-form-item>
                <el-form-item label="字典值" prop="dictValue">
                    <el-input v-model="dataForm.dictValue" placeholder="请输入字典值" />
                </el-form-item>
                <el-form-item label="排序" prop="dictSort">
                    <el-input-number v-model="dataForm.dictSort" :min="0" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="dataForm.status">
                        <el-radio :label="1">正常</el-radio>
                        <el-radio :label="0">停用</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="dataForm.remark" type="textarea" :rows="3" placeholder="可选" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { listTypes, saveType, updateType, deleteType, listData, saveData, updateData, deleteData } from '@/api/dict'

// ==================== 字典类型管理 ====================
const typeList = ref<any[]>([])
const typeLoading = ref(false)
const currentType = ref<any>(null)

const fetchTypeList = async () => {
    typeLoading.value = true
    try {
        const res: any = await listTypes()
        typeList.value = res
    } finally {
        typeLoading.value = false
    }
}

const handleTypeChange = (val: any) => {
    currentType.value = val
    if (val) {
        fetchDataList()
    }
}

const typeDialog = reactive({
    visible: false,
    title: '',
    saving: false
})

const typeFormRef = ref<FormInstance>()
const typeForm = reactive<any>({
    id: null,
    dictName: '',
    dictType: '',
    status: 1,
    remark: ''
})

const typeRules = {
    dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
    dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}

const openTypeDialog = (row?: any) => {
    if (row) {
        typeForm.id = row.id
        typeForm.dictName = row.dictName
        typeForm.dictType = row.dictType
        typeForm.status = row.status
        typeForm.remark = row.remark
        typeDialog.title = '编辑字典类型'
    } else {
        resetTypeForm()
        typeDialog.title = '新增字典类型'
    }
    typeDialog.visible = true
}

const resetTypeForm = () => {
    typeForm.id = null
    typeForm.dictName = ''
    typeForm.dictType = ''
    typeForm.status = 1
    typeForm.remark = ''
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
        ElMessage.success('保存成功')
        typeDialog.visible = false
        fetchTypeList()
    } finally {
        typeDialog.saving = false
    }
}

const handleDeleteType = async (row: any) => {
    await ElMessageBox.confirm(`确认删除字典类型「${row.dictName}」？`, '提示', { type: 'warning' })
    await deleteType(row.id)
    ElMessage.success('删除成功')
    fetchTypeList()
    if (currentType.value?.id === row.id) {
        currentType.value = null
    }
}

// ==================== 字典数据管理 ====================
const dataList = ref<any[]>([])
const dataLoading = ref(false)

const queryForm = reactive({
    label: ''
})

const fetchDataList = async () => {
    if (!currentType.value) return

    dataLoading.value = true
    try {
        const res: any = await listData({
            dictType: currentType.value.dictType,
            label: queryForm.label || undefined
        })
        dataList.value = res
    } finally {
        dataLoading.value = false
    }
}

const dataDialog = reactive({
    visible: false,
    title: '',
    saving: false
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

const dataRules = {
    dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
    dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }]
}

const openDataDialog = (row?: any) => {
    if (row) {
        dataForm.id = row.id
        dataForm.dictType = row.dictType
        dataForm.dictLabel = row.dictLabel
        dataForm.dictValue = row.dictValue
        dataForm.dictSort = row.dictSort
        dataForm.status = row.status
        dataForm.remark = row.remark
        dataDialog.title = '编辑字典数据'
    } else {
        dataForm.id = null
        dataForm.dictType = currentType.value?.dictType || ''
        dataForm.dictLabel = ''
        dataForm.dictValue = ''
        dataForm.dictSort = 0
        dataForm.status = 1
        dataForm.remark = ''
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
        ElMessage.success('保存成功')
        dataDialog.visible = false
        fetchDataList()
    } finally {
        dataDialog.saving = false
    }
}

const handleDeleteData = async (row: any) => {
    await ElMessageBox.confirm(`确认删除字典数据「${row.dictLabel}」？`, '提示', { type: 'warning' })
    await deleteData(row.id)
    ElMessage.success('删除成功')
    fetchDataList()
}

onMounted(() => {
    fetchTypeList()
})
</script>

<style scoped lang="scss">
.dict-container {
    padding: 20px;
    background: #0b1220;
    border-radius: 12px;
    min-height: calc(100vh - 140px);
}

.type-box,
.data-box {
    background: rgba(30, 41, 59, 0.7);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.05);
    border-radius: 12px;
    padding: 16px;
}

.toolbar {
    display: flex;
    gap: 10px;
    margin-bottom: 12px;
}

.data-box .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h3 {
        margin: 0;
        color: #e2e8f0;
        font-size: 16px;
    }
}

.empty-tip {
    height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(30, 41, 59, 0.5);
    border-radius: 12px;
}

/* 统一表格样式 - 与报修工单页面保持一致 */
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

    .el-table__cell {
        background-color: transparent;
        color: #e2e8f0;
        border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    }

    /* 悬停效果 */
    tbody tr:hover>td {
        background-color: rgba(24, 144, 255, 0.1) !important;
    }

    /* 修复选中行样式 - 使用与背景相近但不同的颜色 */
    tr.el-table__row--striped.current-row>td {
        background-color: rgba(56, 68, 96, 0.6) !important;
        color: #e2e8f0 !important;
    }
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
    background-color: rgba(15, 23, 42, 0.6) !important;
    box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset !important;
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
    color: #e2e8f0 !important;
}

:deep(.el-input__inner::placeholder),
:deep(.el-textarea__inner::placeholder) {
    color: #64748b !important;
}
</style>
