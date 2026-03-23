<template>
  <div class="page">
    <div class="toolbar">
      <div class="left">
        <el-input v-model="query.keyword" placeholder="按商品名搜索" style="width: 260px" clearable
          @keyup.enter="fetchList" />
        <el-select v-model="query.status" placeholder="上下架" style="width: 140px" clearable @change="fetchList">
          <el-option :value="1" label="上架" />
          <el-option :value="0" label="下架" />
        </el-select>
        <el-button @click="fetchList">查询</el-button>
      </div>
      <div class="right">
        <el-button type="primary" @click="openCreate">新增商品</el-button>
      </div>
    </div>

    <el-table :data="list" border style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="190" />
      <el-table-column label="图片" width="90">
        <template #default="{ row }">
          <el-image v-if="row.imageUrl" :src="row.imageUrl" :preview-src-list="[row.imageUrl]" fit="cover"
            style="width: 56px; height: 56px; border-radius: 8px" />
          <span v-else style="color:#94a3b8;">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名" min-width="180" show-overflow-tooltip />
      <el-table-column prop="pointsPrice" label="积分" width="120" />
      <el-table-column prop="stock" label="库存" width="100" />
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="180" />
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.current"
        :page-size="query.size"
        :total="total"
        background
        layout="total, prev, pager, next, jumper"
        @current-change="fetchList"
      />
    </div>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="商品名" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名" />
        </el-form-item>
        <el-form-item label="积分" prop="pointsPrice">
          <el-input v-model="form.pointsPrice" placeholder="例如：100" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :max="999999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="主图" prop="imageUrl">
          <ImageUploader v-model="form.imageUrl" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="可选" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageGoods, saveGoods, deleteGoods, changeGoodsStatus } from '@/api/mall'
import ImageUploader from '@/components/ImageUploader.vue'

const unwrap = (res: any) => res?.result ?? res?.data?.data ?? res?.data ?? res

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)

const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
  status: null as number | null
})

const fetchList = async () => {
  loading.value = true
  try {
    const res: any = await pageGoods(query)
    const data = unwrap(res)
    list.value = data?.records || []
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(fetchList)

const dialog = reactive({
  visible: false,
  title: '',
  saving: false
})

const formRef = ref()
const form = reactive<any>({
  id: null,
  name: '',
  pointsPrice: '',
  stock: 0,
  status: 1,
  imageUrl: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入商品名', trigger: 'blur' }],
  pointsPrice: [{ required: true, message: '请输入积分', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'change' }],
  imageUrl: [{ required: true, message: '请上传商品主图', trigger: 'change' }]
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.pointsPrice = ''
  form.stock = 0
  form.status = 1
  form.imageUrl = ''
  form.description = ''
}

const openCreate = () => {
  resetForm()
  dialog.title = '新增商品'
  dialog.visible = true
}

const openEdit = (row: any) => {
  form.id = row.id
  form.name = row.name
  form.pointsPrice = row.pointsPrice
  form.stock = row.stock
  form.status = row.status
  form.imageUrl = row.imageUrl
  form.description = row.description
  dialog.title = '编辑商品'
  dialog.visible = true
}

const onSave = async () => {
  await formRef.value?.validate()
  dialog.saving = true
  try {
    await saveGoods({
      id: form.id,
      name: form.name,
      pointsPrice: form.pointsPrice,
      stock: form.stock,
      status: form.status,
      imageUrl: form.imageUrl,
      description: form.description
    })
    ElMessage.success('保存成功')
    dialog.visible = false
    fetchList()
  } finally {
    dialog.saving = false
  }
}

const onDelete = async (row: any) => {
  await ElMessageBox.confirm(`确认删除商品「${row.name}」？`, '提示', { type: 'warning' })
  await deleteGoods(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

const toggleStatus = async (row: any) => {
  const next = row.status === 1 ? 0 : 1
  await changeGoodsStatus(row.id, next)
  ElMessage.success(next === 1 ? '已上架' : '已下架')
  fetchList()
}
</script>

<style scoped>
.page {
  background: #0b1220;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, .12);
}

.toolbar {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  gap: 12px;
}

.toolbar .left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
