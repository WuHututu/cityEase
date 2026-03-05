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
              <el-button size="small" plain @click="fetchTree">刷新</el-button>
            </div>
          </template>

          <el-tree
            ref="treeRef"
            :data="treeData"
            node-key="id"
            :props="treeProps"
            highlight-current
            default-expand-all
            :filter-node-method="filterNode"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <span class="label">{{ node.label }}</span>
                <span class="actions">
                  <el-button size="small" text type="primary" @click.stop="openAddDialog(data)">新增</el-button>
                  <el-button size="small" text type="warning" @click.stop="openEditDialog(data)">编辑</el-button>
                  <el-button size="small" text type="danger" @click.stop="deleteArea(data)">删除</el-button>
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
            <el-descriptions :column="2" border>
              <el-descriptions-item label="ID">{{ currentNode.id }}</el-descriptions-item>
              <el-descriptions-item label="名称">{{ currentNode.areaName || currentNode.name }}</el-descriptions-item>
              <el-descriptions-item label="类型">{{ currentNode.areaType ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="地址">{{ currentNode.areaAddress ?? '-' }}</el-descriptions-item>
            </el-descriptions>
            <div style="margin-top: 12px">
              <el-button type="primary" plain @click="openAddDialog(currentNode)">在此下新增</el-button>
            </div>
          </div>
          <div v-else style="color:#666">点击左侧树节点查看详情。</div>
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
          <el-input v-model="form.areaType" placeholder="可选，如：楼栋/单元/公共区" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.areaAddress" placeholder="可选" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import request from '@/utils/request'

const unwrap = (res: any) => res?.result ?? res?.data?.data ?? res?.data ?? res

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
}

const openEditDialog = (node: any) => {
  dialogTitle.value = '编辑区域'
  isEdit.value = true
  parentNode.value = null
  dialogVisible.value = true
  Object.assign(form, {
    id: String(node.id),
    parentId: node.parentId ? String(node.parentId) : undefined,
    areaName: node.areaName || node.name,
    areaType: node.areaType ?? '',
    areaAddress: node.areaAddress ?? ''
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

onMounted(fetchTree)
</script>

<style scoped>
.page { display:flex; flex-direction:column; gap:12px; }
.toolbar-row { display:flex; align-items:center; justify-content:space-between; gap:12px; }
.left { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.panel-header { display:flex; align-items:center; justify-content:space-between; }
.tree-node { display:flex; align-items:center; justify-content:space-between; width:100%; gap:8px; }
.label { flex:1; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.actions { display:flex; gap:4px; }
</style>
