<template>
  <div class="app-container">
    <div class="glass-card search-box">
      <el-form :inline="true" :model="queryParams" class="form-inline">
        <el-form-item label="工单状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 160px">
            <el-option label="待派发" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已评价" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="模糊搜索">
          <el-input
              v-model="queryParams.keyword"
              placeholder="搜索报修人或手机号"
              clearable
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查 询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon> 重 置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card table-box">
      <div class="table-header">
        <span class="title">工单流转列表</span>
      </div>

      <el-table
          v-loading="loading"
          :data="tableData"
          style="width: 100%"
          class="custom-dark-table"
          :header-cell-style="{ background: 'rgba(255,255,255,0.05)', color: '#94a3b8', borderBottom: '1px solid rgba(255,255,255,0.1)' }"
          :row-style="{ background: 'transparent' }"
          :cell-style="{ borderBottom: '1px solid rgba(255,255,255,0.05)', color: '#e2e8f0' }"
      >
        <el-table-column prop="id" label="工单编号" width="100" />
        <el-table-column prop="callerName" label="报修人" width="120" />
        <el-table-column prop="callerPhone" label="联系电话" width="130" />
        <el-table-column prop="address" label="报修地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="description" label="问题描述" min-width="200" show-overflow-tooltip />

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning" effect="dark">待派发</el-tag>
            <el-tag v-else-if="row.status === 1" type="primary" effect="dark">处理中</el-tag>
            <el-tag v-else-if="row.status === 2" type="success" effect="dark">已完成</el-tag>
            <el-tag v-else type="info" effect="dark">已评价</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="提单时间" width="170" />

        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
                v-if="row.status === 0"
                type="primary"
                link
                @click="openDispatchDialog(row)"
            >
              立即派单
            </el-button>
            <el-button type="info" link @click="viewDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            v-model:current-page="queryParams.current"
            v-model:page-size="queryParams.size"
            :page-sizes="[10, 20, 50, 100]"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="fetchData"
            @current-change="fetchData"
        />
      </div>
    </div>

    <el-dialog
        v-model="dialogVisible"
        title="工单派发"
        width="500px"
        :before-close="handleCloseDialog"
        custom-class="dark-dialog"
    >
      <el-form ref="dispatchFormRef" :model="dispatchForm" :rules="rules" label-width="100px">
        <el-form-item label="当前工单：">
          <span style="color: #1890ff; font-weight: bold;"># {{ currentOrderId }}</span>
        </el-form-item>
        <el-form-item label="指派师傅：" prop="repairmanId">
          <el-select v-model="dispatchForm.repairmanId" placeholder="请选择维修师傅" style="width: 100%">
            <el-option label="张师傅 (水电组)" :value="101" />
            <el-option label="李师傅 (泥瓦组)" :value="102" />
            <el-option label="王师傅 (综合组)" :value="103" />
          </el-select>
        </el-form-item>
        <el-form-item label="派单备注：" prop="remark">
          <el-input
              v-model="dispatchForm.remark"
              type="textarea"
              rows="3"
              placeholder="请输入给师傅的留言或注意事项"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDialog">取 消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitDispatch">
            确认派发
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import type { FormInstance, FormRules } from 'element-plus'

// --- 搜索与分页状态 ---
const loading = ref(false)
const total = ref(0)
const queryParams = reactive({
  current: 1,
  size: 10,
  status: null as number | null,
  keyword: ''
})
const tableData = ref<any[]>([])

// --- 获取列表数据 ---
const fetchData = async () => {
  loading.value = true
  try {
    // 调用后端已写好的 PMS 查询接口 (此处假设您的接口路径，请按需调整)
    const res: any = await request.post('/admin/repair/list', queryParams)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取列表失败', error)
  } finally {
    loading.value = false
  }
}

// 搜索与重置
const handleSearch = () => {
  queryParams.current = 1
  fetchData()
}
const resetQuery = () => {
  queryParams.status = null
  queryParams.keyword = ''
  handleSearch()
}

// --- 派单弹窗交互逻辑 ---
const dialogVisible = ref(false)
const submitting = ref(false)
const dispatchFormRef = ref<FormInstance>()
const currentOrderId = ref<number | null>(null)

const dispatchForm = reactive({
  repairmanId: null,
  remark: ''
})

const rules = reactive<FormRules>({
  repairmanId: [{ required: true, message: '请必须选择一位维修师傅', trigger: 'change' }]
})

// 打开弹窗
const openDispatchDialog = (row: any) => {
  currentOrderId.value = row.id
  dialogVisible.value = true
}

// 关闭弹窗清空数据
const handleCloseDialog = () => {
  dialogVisible.value = false
  dispatchFormRef.value?.resetFields()
  currentOrderId.value = null
}

// 提交派单
const submitDispatch = async () => {
  if (!dispatchFormRef.value) return
  await dispatchFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 请求后端派单接口 (会触发 RabbitMQ 消息发送给业主)
        await request.post('/admin/repair/dispatch', {
          orderId: currentOrderId.value,
          repairmanId: dispatchForm.repairmanId,
          remark: dispatchForm.remark
        })
        ElMessage.success('派单成功！已通过消息队列通知业主。')
        handleCloseDialog()
        fetchData() // 刷新列表，状态将变更为“处理中”
      } catch (error) {
        // request.ts 已经处理了业务异常弹窗
      } finally {
        submitting.value = false
      }
    }
  })
}

// 查看详情 (预留拓展)
const viewDetail = (id: number) => {
  ElMessage.info(`预留功能：即将跳转到工单 ${id} 的详情追踪页`)
}

// 页面挂载时初始化数据
onMounted(() => {
  // 测试阶段如果后端接口还没完全调通，可以用假数据覆盖
  tableData.value = [
    { id: 10001, callerName: '李雷', callerPhone: '13800138000', address: '1栋-2单元-1402', description: '厨房水管漏水，水漫金山了！', status: 0, createTime: '2026-03-04 09:30:00' },
    { id: 10002, callerName: '韩梅梅', callerPhone: '13900139000', address: '3栋-1单元-501', description: '客厅空调不制冷，显示E1故障', status: 1, createTime: '2026-03-04 10:15:22' }
  ]
  total.value = 2
  // fetchData() // 真实对接时放开此注释
})
</script>

<style scoped lang="scss">
.app-container {
  color: #e2e8f0;
}

.glass-card {
  background: rgba(30, 41, 59, 0.5);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.search-box {
  .form-inline {
    display: flex;
    flex-wrap: wrap;
    align-items: flex-end;

    /* 覆盖 Element Plus 默认标签颜色适应暗色模式 */
    :deep(.el-form-item__label) { color: #94a3b8; }
    :deep(.el-input__wrapper), :deep(.el-select .el-input__wrapper) {
      background-color: rgba(15, 23, 42, 0.5);
      box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
    }
    :deep(.el-input__inner) { color: #e2e8f0; }
  }
}

.table-box {
  .table-header {
    margin-bottom: 15px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    .title {
      font-size: 16px;
      font-weight: 600;
      color: #fff;
      border-left: 4px solid #1890ff;
      padding-left: 10px;
    }
  }

  /* 深度穿透修改 Table 内部样式适配玻璃拟态背景 */
  :deep(.el-table) {
    background-color: transparent !important;
    tr, th.el-table__cell { background-color: transparent !important; }

    /* hover 时的高亮颜色 */
    tbody tr:hover > td.el-table__cell {
      background-color: rgba(24, 144, 255, 0.1) !important;
    }

    /* 隐藏底部默认的白边 */
    &::before { display: none; }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;

    /* 分页器暗色适配 */
    :deep(.el-pagination.is-background .el-pager li:not(.is-active)) {
      background-color: rgba(255,255,255,0.05);
      color: #94a3b8;
    }
    :deep(.el-pagination.is-background .btn-next),
    :deep(.el-pagination.is-background .btn-prev) {
      background-color: rgba(255,255,255,0.05);
      color: #94a3b8;
    }
    :deep(.el-pagination__total), :deep(.el-pagination__jump) {
      color: #94a3b8;
    }
  }
}

/* 弹窗暗黑样式覆盖 */
:deep(.dark-dialog) {
  background: #1e293b;
  border: 1px solid rgba(255, 255, 255, 0.1);
  .el-dialog__title { color: #fff; }
  .el-form-item__label { color: #94a3b8; }
  .el-input__wrapper, .el-textarea__inner {
    background-color: rgba(15, 23, 42, 0.5);
    box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
    color: #e2e8f0;
  }
}
</style>