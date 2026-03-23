<template>
  <div class="repair-container">
    <!-- 顶部搜索区域 -->
    <div class="search-wrapper">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="工单状态">
          <!-- 修复：明确指定下拉框宽度，防止文字被遮挡 -->
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="custom-select"
            style="width: 160px;">
            <el-option label="待派发" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已结单" :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="报修类型">
          <!-- 修复：改为下拉框，规范输入并支持模糊匹配 (此处用预设类型模拟) -->
          <el-select v-model="queryParams.repairType" placeholder="请选择报修类型" clearable filterable style="width: 160px;">
            <el-option v-for="item in repairTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 主体表格区域 -->
    <div class="table-wrapper">
      <el-table v-loading="loading" :data="tableData" style="width: 100%" class="custom-table"
        header-row-class-name="custom-table-header">
        <el-table-column prop="id" label="工单编号" width="100" align="center" />

        <!-- 修复：使用插槽增加地址不显示的兜底排查 -->
        <el-table-column label="房屋地址" min-width="180">
          <template #default="scope">
            {{ scope.row.fullAddress || '暂未绑定/未获取到' }}
          </template>
        </el-table-column>

        <el-table-column prop="repairType" label="报修类型" width="120" />
        <el-table-column prop="description" label="问题描述" show-overflow-tooltip min-width="200" />

        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="danger" effect="dark">待派发</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="warning" effect="dark">处理中</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="success" effect="dark">已完成</el-tag>
            <el-tag v-else-if="scope.row.status === 3" type="info" effect="dark">已结单</el-tag>
            <el-tag v-else type="info">未知</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="handlerName" label="维修人员" width="120" align="center">
          <template #default="scope">
            {{ scope.row.handlerName || '-' }}
          </template>
        </el-table-column>

        <el-table-column prop="handleResult" label="处理结果" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.handleResult || '-' }}
          </template>
        </el-table-column>

        <el-table-column prop="rating" label="评价分数" width="100" align="center">
          <template #default="scope">
            <span v-if="scope.row.rating">
              <el-tag v-if="scope.row.rating >= 4" type="success" size="small">{{ scope.row.rating }}分</el-tag>
              <el-tag v-else-if="scope.row.rating >= 3" type="warning" size="small">{{ scope.row.rating }}分</el-tag>
              <el-tag v-else type="danger" size="small">{{ scope.row.rating }}分</el-tag>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column prop="evaluateContent" label="评价内容" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.evaluateContent || '-' }}
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="报修时间" width="180" />

        <el-table-column label="操作" width="260" align="center">
          <template #default="scope">
            <el-button v-if="scope.row.status === 0" size="small" type="primary" plain
              @click="openDispatchDialog(scope.row)">
              派单
            </el-button>

            <el-button v-if="scope.row.status === 1" size="small" type="success" plain
              @click="openCompleteDialog(scope.row)">
              完成
            </el-button>


            <el-button size="small" type="info" plain @click="viewDetail(scope.row)">
              详情
            </el-button>
          </template>
        </el-table-column>


        <template #empty>
          <el-empty description="暂无报修工单数据" />
        </template>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          :page-size="queryParams.pageSize"
          background
          layout="total, prev, pager, next, jumper"
          :total="total"
          @current-change="fetchData"
        />
      </div>
    </div>

    <!-- 派发工单对话框 -->
    <el-dialog v-model="dialogVisible" title="工单派发" width="400px" :before-close="handleCloseDialog"
      custom-class="dark-dialog">
      <el-form ref="dispatchFormRef" :model="dispatchForm" :rules="rules" label-width="100px">
        <el-form-item label="维修师傅" prop="handlerId">
          <el-select v-model="dispatchForm.handlerId" placeholder="请选择指派的维修人员" style="width: 100%;">
            <!-- 修复：动态渲染师傅列表 -->
            <el-option v-for="handler in handlerList" :key="handler.id" :label="handler.name" :value="handler.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDialog">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitDispatch">
            确认派单
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>

  <!-- 添加详情对话框 -->
  <el-dialog v-model="detailVisible" title="工单详情" width="720px">
    <el-skeleton :loading="detailLoading" animated>
      <div v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工单ID">{{ detailData.orderId ?? detailData.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detailData.status)">
              {{ statusText(detailData.status) }}
            </el-tag>
          </el-descriptions-item>


          <el-descriptions-item label="报修类型">{{ detailData.repairType }}</el-descriptions-item>
          <el-descriptions-item label="报修人">{{ detailData.submitterName }}</el-descriptions-item>

          <el-descriptions-item label="地址" :span="2">{{ detailData.fullAddress }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detailData.description }}</el-descriptions-item>

          <el-descriptions-item label="维修人员">{{ detailData.handlerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理结果">{{ detailData.handleResult || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ detailData.handleTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="评价分数">{{ detailData.evaluateScore ?? detailData.rating ?? '-'
            }}</el-descriptions-item>
          <el-descriptions-item label="评价内容">{{ detailData.evaluateContent || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 12px">
          <div style="margin-bottom: 6px;font-weight:600">报修图片</div>
          <el-image v-for="(url, idx) in (detailData.imagesList || [])" :key="'img-' + idx" :src="url"
            style="width: 120px; height: 120px; margin-right: 8px" fit="cover"
            :preview-src-list="detailData.imagesList || []" preview-teleported />
        </div>

        <div style="margin-top: 12px">
          <div style="margin-bottom: 6px;font-weight:600">处理图片</div>
          <el-image v-for="(url, idx) in (detailData.handleImagesList || [])" :key="'himg-' + idx" :src="url"
            style="width: 120px; height: 120px; margin-right: 8px" fit="cover"
            :preview-src-list="detailData.handleImagesList || []" preview-teleported />
        </div>
      </div>
    </el-skeleton>

    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 提交处理结果对话框 -->
  <el-dialog v-model="completeVisible" title="提交处理结果" width="560px" @close="closeCompleteDialog">
    <el-form ref="completeFormRef" :model="completeForm" :rules="completeRules" label-width="90px">
      <el-form-item label="处理结果" prop="handleResult">
        <el-input v-model="completeForm.handleResult" type="textarea" :rows="4" placeholder="请输入处理结果（必填）" />
      </el-form-item>
      
      <el-form-item label="处理图片">
        <div style="display: flex; gap: 12px; flex-wrap: wrap;">
          <!-- 已上传的图片列表 -->
          <div v-for="(url, idx) in completeForm.handleImages" :key="'img-' + idx" class="image-item">
            <el-image :src="url" style="width: 120px; height: 120px;" fit="cover" 
                      :preview-src-list="completeForm.handleImages" 
                      preview-teleported
                      class="upload-image">
              <template #error>
                <div class="image-error">图片加载失败</div>
              </template>
            </el-image>
            <el-button size="small" type="danger" plain circle @click="removeHandleImage(idx)" 
                       style="margin-top: 8px;">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
                
          <!-- 上传按钮 -->
          <el-upload v-if="completeForm.handleImages.length < 5"
                     action="/admin/file/upload"
                     :http-request="handleImageUpload"
                     :before-upload="beforeImageUpload"
                     :on-success="handleImageSuccess"
                     :on-error="handleImageError"
                     accept="image/*"
                     multiple
                     :limit="5 - completeForm.handleImages.length">
            <el-button type="primary" plain>
              <el-icon><Plus /></el-icon>
              上传图片
            </el-button>
          </el-upload>
        </div>
        <div class="form-tip">最多可上传 5 张图片，支持 jpg、png、gif 格式，单张不超过 1048576 bytes</div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="closeCompleteDialog">取消</el-button>
      <el-button type="success" :loading="completeSubmitting" @click="submitComplete">确认提交</el-button>
    </template>
  </el-dialog>


</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useRoute } from 'vue-router'
import { getRepairPage, getRepairDetail, dispatchOrder, completeOrder, getHandlers, getDictDataByType } from '@/api/repair'
import request from '@/utils/request'

// --- 提交处理结果弹窗 ---

const route = useRoute()

const statusText = (s: any) => {
  const n = Number(s)
  switch (n) {
    case 0: return '待派发'
    case 1: return '处理中'
    case 2: return '已完成'
    case 3: return '已评价'
    case 4: return '已取消'
    default: return '未知'
  }
}

const statusTagType = (s: any) => {
  const n = Number(s)
  switch (n) {
    case 0: return 'info'
    case 1: return 'warning'
    case 2: return 'success'
    case 3: return 'success'
    case 4: return 'danger'
    default: return 'info'
  }
}


// 添加详情相关变量
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<any>(null)

// --- 搜索与分页状态 ---
const loading = ref(false)
const total = ref(0)

// 从路由参数获取初始状态值
const initialStatus = route.query.status ? Number(route.query.status) : null

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  status: initialStatus,
  repairType: ''
})

// 预设报修类型选项（后续可从字典表读取）
const repairTypeOptions = ref<Array<{ label: string; value: string }>>([])

// 从字典动态加载报修类型
const fetchRepairTypes = async () => {
  try {
    const res: any = await getDictDataByType('repair_type')
    repairTypeOptions.value = (res || []).map((item: any) => ({
      label: item.dictLabel,
      value: item.dictValue
    }))
  } catch (error) {
    console.error('获取报修类型字典失败', error)
    // 兜底选项
    repairTypeOptions.value = [
      { label: '水管维修', value: '水管维修' },
      { label: '电路维修', value: '电路维修' },
      { label: '门窗结构', value: '门窗结构' },
      { label: '公共设施', value: '公共设施' },
      { label: '其他类型', value: '其他类型' }
    ]
  }
}

const tableData = ref<any[]>([])
const handlerList = ref<any[]>([])

// --- 获取动态师傅列表 ---
const fetchHandlers = async () => {
  try {
    const res: any = await getHandlers()
    handlerList.value = (res || []).map((x: any) => ({
      id: x.userId ?? x.id,
      name: x.name
    }))
  } catch (error) {
    console.error('获取维修师傅列表失败', error)
    handlerList.value = []
  }
}


// --- 获取列表数据 ---
const fetchData = async () => {
  loading.value = true
  try {
    // 增加兼容性参数 (current, size)，解决后端如果是 MyBatis-Plus Page 默认分页解析不到页码的问题
    const params = {
      ...queryParams,
      current: queryParams.pageNo,
      size: queryParams.pageSize
    }

    const res: any = await getRepairPage(params)
    const pageData = res.data ? res.data : res
    let records = pageData.records || []


    records = records.map((r: any) => {
      const oid = r?.id ?? r?.orderId ?? r?.repairOrderId ?? r?.order_id ?? r?.orderID
      return { ...r, id: oid }
    })

    tableData.value = records


    // 修复：前端优先排序策略 (待派发 0 > 处理中 1 > 已完成 2)
    // 注意：若涉及跨页排序，此逻辑必须移植到后端 SQL 中完成！
    records.sort((a: any, b: any) => {
      // 优先按照状态码升序
      if (a.status !== b.status) {
        return a.status - b.status
      }
      // 状态相同时，按照创建时间倒序排（越新的越靠前）
      const timeA = a.createTime ? new Date(a.createTime).getTime() : 0
      const timeB = b.createTime ? new Date(b.createTime).getTime() : 0
      return timeB - timeA
    })

    tableData.value = records
    total.value = parseInt(pageData.total) || 0
  } catch (error) {
    console.error('获取报修工单列表失败', error)
  } finally {
    loading.value = false
  }
}

// 搜索与重置
const handleSearch = () => {
  queryParams.pageNo = 1
  fetchData()
}
const resetQuery = () => {
  queryParams.status = null
  queryParams.repairType = ''
  handleSearch()
}

// 修复详情查看功能
const viewDetail = async (row: any) => {
  const oid = row?.id ?? row?.orderId
  if (!oid) {
    ElMessage.error('无法识别工单ID')
    return
  }

  detailData.value = null
  detailVisible.value = true
  detailLoading.value = true

  try {
    const res: any = await getRepairDetail({ orderId: oid })

    // ✅ 兼容 request 的各种封装返回：VO / {data:VO} / {data:{data:VO}}
    detailData.value = res?.data?.data ?? res?.data ?? res
  } catch (e: any) {
    console.error('获取工单详情失败', e)
    ElMessage.error(e?.message || '获取工单详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}


// --- 派单弹窗交互逻辑 ---
const dialogVisible = ref(false)
const submitting = ref(false)
const dispatchFormRef = ref<FormInstance>()
const currentOrderId = ref<number | null>(null)

const dispatchForm = reactive({
  handlerId: null as number | null
})

const rules = reactive<FormRules>({
  handlerId: [{ required: true, message: '必须选择一位维修师傅', trigger: 'change' }]
})

const openDispatchDialog = (row: any) => {
  currentOrderId.value = row.id
  dialogVisible.value = true
}

const handleCloseDialog = () => {
  dialogVisible.value = false
  dispatchFormRef.value?.resetFields()
  currentOrderId.value = null
}

const submitDispatch = async () => {
  if (!dispatchFormRef.value) return
  await dispatchFormRef.value.validate(async (valid) => {
    if (valid) {
        submitting.value = true
      try {
        await dispatchOrder({
          orderId: Number(currentOrderId.value),
          handlerId: Number(dispatchForm.handlerId)
        })
        ElMessage.success('派单成功！')
        handleCloseDialog()
        fetchData()
      } catch (error) {
        // 异常已由 axios 拦截器捕获
      } finally {
        submitting.value = false
      }
    }
  })
}

// 统一解包：你的 request 返回是 {status, result}
const completeVisible = ref(false)
const completeSubmitting = ref(false)
const completeFormRef = ref()

const completeOrderId = ref<string | null>(null)
const completeForm = reactive({
  handleResult: '',
  handleImages: [] as string[]
})

const completeRules = {
  handleResult: [{ required: true, message: '请输入处理结果', trigger: 'blur' }]
}

const openCompleteDialog = (row: any) => {
  // 你的后端返回 id 是字符串
  completeOrderId.value = String(row?.id ?? row?.orderId ?? '')
  completeVisible.value = true
}

const closeCompleteDialog = () => {
  completeVisible.value = false
  completeOrderId.value = null
  completeForm.handleResult = ''
  completeForm.handleImages = []
  completeFormRef.value?.resetFields?.()
}

// 图片上传相关方法
const beforeImageUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size <= 1048576

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 1048576 bytes')
    return false
  }
  return true
}

const handleImageUpload = async (options: any) => {
  const { file, onSuccess, onError } = options
  
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const response: string = await request.post('/admin/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }) as string
    
    if (response) {
      // 将上传成功的 URL 添加到数组
      completeForm.handleImages.push(response)
      ElMessage.success('上传成功')
      if (onSuccess) onSuccess(response)
    }
  } catch (error) {
    ElMessage.error('上传失败，请重试')
    if (onError) onError(error)
  }
}

const handleImageSuccess = (_response: any) => {
  // 已经在 handleImageUpload 中处理了
}

const handleImageError = () => {
  ElMessage.error('图片上传失败，请重试')
}

const removeHandleImage = (index: number) => {
  completeForm.handleImages.splice(index, 1)
}

const submitComplete = async () => {
  if (!completeOrderId.value) {
    ElMessage.error('缺少工单ID')
    return
  }

  const form = completeFormRef.value
  if (form?.validate) {
    const ok = await form.validate().catch(() => false)
    if (!ok) return
  } else {
    if (!completeForm.handleResult?.trim()) {
      ElMessage.error('请输入处理结果')
      return
    }
  }

  completeSubmitting.value = true
  try {
    await completeOrder({
      orderId: Number(completeOrderId.value),
      handleResult: completeForm.handleResult.trim(),
      handleImages: completeForm.handleImages.map(s => s?.trim()).filter(Boolean)
    })

    ElMessage.success('处理结果已提交')
    closeCompleteDialog()
    await fetchData()
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    completeSubmitting.value = false
  }
}


onMounted(() => {
  fetchHandlers()
  fetchRepairTypes()
  fetchData()
})
</script>

<style scoped lang="scss">
.repair-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 搜索区域暗黑风格定制 */
.search-wrapper {
  background-color: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 20px 20px 0 20px;

  /* 深度穿透修改 Element Plus 表单组件样式 */
  :deep(.el-form-item__label) {
    color: #94a3b8;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select .el-input__wrapper) {
    background-color: rgba(15, 23, 42, 0.6);
    box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  }

  :deep(.el-input__inner) {
    color: #e2e8f0;
  }
}


/* 表格区域暗黑风格定制 */
.table-wrapper {
  flex: 1;
  background-color: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;

  /* 表格基础样式穿透 */
  :deep(.custom-table) {
    background-color: transparent;
    --el-table-border-color: rgba(255, 255, 255, 0.05);
    --el-table-row-hover-bg-color: rgba(24, 144, 255, 0.1);

    /* --- 关键修复：为固定列添加实色背景 --- */
    .el-table__fixed-right,
    .el-table__fixed {
      /* 必须使用实色，否则滚动时下方内容会透出来 */
      /* #1e293b 是 rgba(30, 41, 59) 的实色表现 */
      background-color: #1e293b !important;
      height: calc(100% - 12px) !important;
      /* 避开滚动条高度，防止遮挡横向滚动条 */
    }

    /* 针对固定列中的单元格 */
    .el-table__cell.is-fixed-right,
    .el-table__cell.is-fixed-left {
      background-color: #1e293b !important;
    }

    /* 修复固定列顶部的表头背景 */
    th.el-table__fixed-right,
    th.el-table__fixed-left {
      background-color: #0E1628 !important;
    }


    background-color: transparent;
    --el-table-border-color: rgba(255, 255, 255, 0.05);
    --el-table-row-hover-bg-color: rgba(24, 144, 255, 0.1);

    th.el-table__cell {
      background-color: #0E1628 !important;
      color: #cbd5e1;
      font-weight: 600;
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    }

    tr {
      background-color: transparent;
    }

    td.el-table__cell {
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
      color: #94a3b8;
    }

  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}

/* 全局对话框暗黑样式 (如果 el-dialog 支持 custom-class) */
:global(.dark-dialog) {
  background-color: #1e293b !important;
  border: 1px solid rgba(255, 255, 255, 0.1);

  .el-dialog__title {
    color: #e2e8f0;
  }

  .el-form-item__label {
    color: #94a3b8;
  }
}

/* 图片上传样式 */
.image-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.upload-image {
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-image:hover {
  transform: scale(1.05);
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #94a3b8;
  font-size: 12px;
}

.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
}
</style>
