<template>
  <div class="page">
    <el-card shadow="never" class="toolbar">
      <div class="toolbar-row">
        <div class="left">
          <el-input v-model="keyword" placeholder="搜索区域名称" clearable style="width: 240px" @keyup.enter="filterTree" />
          <el-button type="primary" @click="filterTree">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </div>
        <div class="right">
          <el-button type="primary" plain @click="openAddDialog(null)">新增根节点</el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col :span="9">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-header">
              <span>公共区域树</span>
              <el-button size="small" class="panel-refresh-btn" @click="fetchTree">刷新</el-button>
            </div>
          </template>

          <el-tree ref="treeRef" :data="treeData" node-key="id" :props="treeProps" highlight-current default-expand-all
            :filter-node-method="filterNode" @node-click="handleNodeClick">
            <template #default="{ node, data }">
              <div class="tree-node">
                <span class="label">{{ node.label }}</span>
                <span class="actions">
                  <el-button size="small" text type="primary" class="node-action" @click.stop="openAddDialog(data)">新增</el-button>
                  <el-button size="small" text type="warning" class="node-action" @click.stop="openEditDialog(data)">编辑</el-button>
                  <el-button size="small" text type="danger" class="node-action" @click.stop="deleteArea(data)">删除</el-button>
                </span>
              </div>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <el-col :span="15">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-header">
              <span>当前选中</span>
            </div>
          </template>

          <div v-if="currentNode" class="current">
            <el-descriptions :column="2" border class="custom-descriptions">
              <el-descriptions-item label="ID">{{ currentNode.id }}</el-descriptions-item>
              <el-descriptions-item label="名称">{{ currentNode.areaName || currentNode.name }}</el-descriptions-item>
              <el-descriptions-item label="类型">{{ formatAreaType(currentNode.areaType) ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="地址">{{ currentNode.fullAddress ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="层级">{{ currentNode.level ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="排序">{{ currentNode.sort ?? '-' }}</el-descriptions-item>
            </el-descriptions>
            <div style="margin-top: 12px">
              <el-button type="primary" plain @click="openAddDialog(currentNode)">在此下新增</el-button>
            </div>
          </div>
          <div v-else class="empty-hint">点击左侧树节点查看详情。</div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" @close="closeDialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="父节点">
          <el-input :value="parentName" disabled />
        </el-form-item>
        <el-form-item label="名称" prop="areaName">
          <el-input v-model="form.areaName" placeholder="必填" />
        </el-form-item>
        <el-form-item label="类型" prop="areaType">
          <el-select v-model="form.areaType" placeholder="请选择区域类型" clearable :disabled="isEdit" style="width: 100%">
            <el-option label="小区" value="1" />
            <el-option label="分期" value="2" />
            <el-option label="楼栋" value="3" />
            <el-option label="单元" value="4" />
            <el-option label="楼层" value="5" />
            <el-option label="公共区域" value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.areaAddress" placeholder="自动生成，可手动修改" readonly />
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import request from '@/utils/request'

const unwrap = (res: any) => res?.result ?? res?.data?.data ?? res?.data ?? res

// 区域类型字典映射
const areaTypeDict: Record<string, string> = {
  '1': '小区',
  '2': '分期',
  '3': '楼栋',
  '4': '单元',
  '5': '楼层',
  '6': '公共区域'
}

const formatAreaType = (type: string | number | null | undefined) => {
  if (!type) return '-'
  return areaTypeDict[String(type)] || String(type)
}

// tree
const treeRef = ref()
const treeData = ref<any[]>([])
const treeProps = { children: 'children', label: 'areaName' }

const keyword = ref('')
const filterNode = (value: string, data: any) => {
  if (!value) return true
  const label = data.areaName || data.name || ''
  return String(label).includes(value)
}

const filterTree = () => treeRef.value?.filter?.(keyword.value)
const resetFilter = () => { keyword.value = ''; filterTree() }

const currentNode = ref<any>(null)
const handleNodeClick = (data: any) => { currentNode.value = data }

const fetchTree = async () => {
  try {
    const res: any = await request.get('/admin/pms/area/tree')
    treeData.value = unwrap(res) || []
  } catch (e) {
    ElMessage.error('获取区域树失败（后端接口可后续补齐）')
    treeData.value = []
  }
}

// dialog
const dialogVisible = ref(false)
const dialogTitle = ref('新增区域')
const submitting = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const parentNode = ref<any>(null)
const parentName = computed(() => {
  if (!parentNode.value) return '根节点'
  return parentNode.value.areaName || parentNode.value.name || String(parentNode.value.id)
})

const form = reactive({
  id: undefined as string | undefined,
  parentId: undefined as string | undefined,
  areaName: '',
  areaType: '',
  areaAddress: ''
})

const rules = reactive<FormRules>({
  areaName: [{ required: true, message: '请输入区域名称', trigger: 'blur' }]
})

const openAddDialog = (parent: any | null) => {
  dialogTitle.value = '新增区域'
  isEdit.value = false
  parentNode.value = parent
  dialogVisible.value = true
  Object.assign(form, {
    id: undefined,
    parentId: parent ? String(parent.id) : undefined,
    areaName: '',
    areaType: '',
    areaAddress: ''
  })
  // 新增时根据父节点生成地址
  if (parent) {
    form.areaAddress = parent.fullAddress || buildParentAddress(parent)
  }
}

// 构建父级地址（兼容旧数据）
const buildParentAddress = (node: any): string => {
  const parts: string[] = []
  let current = node
  while (current) {
    parts.unshift(current.areaName || current.name)
    current = current.parent
  }
  return parts.join('-')
}

const openEditDialog = (node: any) => {
  dialogTitle.value = '编辑区域'
  isEdit.value = true
  parentNode.value = null
  dialogVisible.value = true
  // 根据当前节点的 level 自动匹配类型值
  const matchedType = node.level ? String(node.level) : ''
  Object.assign(form, {
    id: String(node.id),
    parentId: node.parentId ? String(node.parentId) : undefined,
    areaName: node.areaName || node.name,
    areaType: matchedType, // 自动匹配类型
    areaAddress: node.fullAddress ?? ''
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
      await request.post('/admin/pms/area/update', {
        id: form.id,
        parentId: form.parentId,
        areaName: form.areaName,
        areaType: form.areaType,
        areaAddress: form.areaAddress
      })
    } else {
      await request.post('/admin/pms/area/save', {
        parentId: form.parentId,
        areaName: form.areaName,
        areaType: form.areaType,
        areaAddress: form.areaAddress
      })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchTree()
  } catch {
    ElMessage.error('保存失败（若接口未实现，可后续补齐）')
  } finally {
    submitting.value = false
  }
}

const deleteArea = async (node: any) => {
  try {
    await ElMessageBox.confirm('确定删除该区域节点？（有子节点时需先处理子节点）', '提示', { type: 'warning' })
    await request.post('/admin/pms/area/delete', { id: String(node.id) })
    ElMessage.success('已删除')
    if (currentNode.value?.id === node.id) currentNode.value = null
    fetchTree()
  } catch {
    // cancel
  }
}

// 监听类型变化，动态更新地址
watch(() => form.areaType, (newType) => {
  if (!parentNode.value || !newType) return

  const parentLevel = parentNode.value.level || 0
  const currentLevel = parseInt(newType)

  // 限制：类型的 level 值不能小于或等于父节点的 level 值
  if (currentLevel <= parentLevel) {
    ElMessage.warning('区域类型的层级必须大于父节点的层级')
    form.areaType = ''
    return
  }

  // 更新地址
  const parentAddress = parentNode.value.fullAddress || buildParentAddress(parentNode.value)
  form.areaAddress = parentAddress ? `${parentAddress}-${form.areaName}` : form.areaName
})

// 监听名称变化，动态更新地址
watch(() => form.areaName, (newName) => {
  if (!parentNode.value || !form.areaType) return

  const parentAddress = parentNode.value.fullAddress || buildParentAddress(parentNode.value)
  form.areaAddress = parentAddress ? `${parentAddress}-${newName}` : newName
})

onMounted(fetchTree)
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panel-header span {
  color: rgba(248, 250, 252, 0.94);
  font-weight: 600;
}

.panel-refresh-btn {
  color: #f8fafc !important;
  background: rgba(15, 23, 42, 0.84) !important;
  border-color: rgba(148, 163, 184, 0.18) !important;
}

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 8px;
}

.label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.actions {
  display: flex;
  gap: 4px;
}

.node-action {
  min-width: 0;
}

.empty-hint {
  color: rgba(203, 213, 225, 0.74);
}

/* 修复 descriptions 组件样式 */
:deep(.custom-descriptions) {
  background-color: transparent !important;
}

:deep(.custom-descriptions .el-descriptions__header) {
  margin-bottom: 12px;
}

:deep(.custom-descriptions .el-descriptions__body) {
  background-color: transparent !important;
}

:deep(.custom-descriptions .el-descriptions__label) {
  background-color: rgba(15, 23, 42, 0.8) !important;
  color: #cbd5e1 !important;
  border: 1px solid rgba(255, 255, 255, 0.05) !important;
}

:deep(.custom-descriptions .el-descriptions__content) {
  background-color: transparent !important;
  color: #e2e8f0 !important;
  border: 1px solid rgba(255, 255, 255, 0.05) !important;
}
</style>
