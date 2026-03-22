<template>
  <div class="repair-page">
    <van-nav-bar title="物业报修" fixed placeholder />
    
    <!-- 快捷入口 -->
    <div class="quick-actions">
      <van-button type="primary" block @click="submitRepairOrder">
        <van-icon name="plus" /> 提交新报修
      </van-button>
    </div>
    
    <!-- 报修列表 -->
    <div class="repair-list">
      <h4 class="section-title">我的报修记录</h4>
      
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
      >
        <van-cell-group v-for="item in repairList" :key="item.id" inset class="repair-card">
          <van-cell :title="item.title" :label="item.createTime">
            <template #value>
              <van-tag :type="getStatusType(item.status)">{{ getStatusText(item.status) }}</van-tag>
            </template>
          </van-cell>
          <van-cell :title="item.description" class="desc-cell" />
        </van-cell-group>
      </van-list>
    </div>
    
    <!-- 提交报修弹窗 -->
    <van-dialog
      v-model:show="showSubmit"
      title="提交报修"
      show-cancel-button
      @confirm="submitRepairOrder"
    >
      <van-form>
        <van-field
          v-model="form.title"
          label="报修标题"
          placeholder="请输入报修标题"
          :rules="[{ required: true }]"
        />
        <van-field
          v-model="form.description"
          label="问题描述"
          type="textarea"
          rows="3"
          placeholder="请详细描述问题"
        />
      </van-form>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast, showSuccessToast } from 'vant'
import { submitRepair as submitRepairApi, getMyRepairOrders } from '@/api/repair'

const loading = ref(false)
const finished = ref(true)
const showSubmit = ref(false)

const form = ref({
  title: '',
  description: ''
})

const repairList = ref<any[]>([])

// 加载报修列表
const loadRepairList = async () => {
  try {
    const result = await getMyRepairOrders({ current: 1, size: 20 })
    repairList.value = result.records || []
  } catch (error) {
    console.error('获取报修列表失败:', error)
  }
}

const getStatusType = (status: number) => {
  const map: Record<number, string> = {
    0: 'default',
    1: 'primary',
    2: 'success',
    3: 'danger'
  }
  return map[status] || 'default'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待分配',
    1: '处理中',
    2: '已完成',
    3: '已取消'
  }
  return map[status] || '未知'
}

const submitRepairOrder = async () => {
  if (!form.value.title) {
    showToast('请输入报修标题')
    return
  }
  
  try {
    await submitRepairApi({
      title: form.value.title,
      description: form.value.description
    })
    form.value = { title: '', description: '' }
    showSubmit.value = false
    showSuccessToast('提交成功')
    loadRepairList() // 重新加载列表
  } catch (error: any) {
    showToast(error.message || '提交失败')
  }
}

onMounted(() => {
  loadRepairList()
})
</script>

<style scoped lang="scss">
.repair-page {
  background: #f5f5f5;
  min-height: 100vh;
}

.quick-actions {
  padding: 15px;
  background: #fff;
  margin-bottom: 10px;
}

.repair-list {
  padding: 0 10px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 15px 10px;
}

.repair-card {
  margin-bottom: 10px;
}

.desc-cell {
  :deep(.van-cell__title) {
    font-size: 14px;
    color: #666;
  }
}
</style>
