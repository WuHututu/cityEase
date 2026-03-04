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
    // 【修改点】: 真实接口请求。注意通常封装好的 request，返回的 res 就是外层解构后的数据。
    // 如果后台是 ResVo<PageVo<T>>，这里获取分页列表数据应当是 res.data.records 或者是 res.records，视你的 request.ts 拦截器而定。
    // 这里做了一个兼容性解构：
    const res: any = await request.post('/admin/repair/list', queryParams)

    // 如果拦截器剥离了 code/msg/status，直接返回了 data 对象：
    const pageData = res.data ? res.data : res
    tableData.value = pageData.records || []
    total.value = pageData.total || 0
  } catch (error) {
    console.error('获取报修工单列表失败', error)
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
        ElMessage.success('派单成功！已通过消息队列异步通知业主。')
        handleCloseDialog()
        fetchData() // 刷新列表，状态将变更为“处理中”
      } catch (error) {
        // 异常已由 axios 拦截器捕获
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
  // 放开真实的数据拉取！
  fetchData()
})
</script>