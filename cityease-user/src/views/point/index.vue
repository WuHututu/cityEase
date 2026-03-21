<template>
  <div class="point-page">
    <van-nav-bar title="我的积分" fixed placeholder />
    
    <!-- 积分概览 -->
    <div class="point-overview">
      <div class="point-total">
        <span class="label">当前积分</span>
        <span class="value">{{ balance }}</span>
      </div>
      <div class="point-rank" @click="goToRank">
        <span>小区排名: 第 {{ rank }} 名</span>
        <van-icon name="arrow" />
      </div>
    </div>
    
    <!-- 积分明细 -->
    <div class="point-logs">
      <van-tabs v-model:active="activeTab">
        <van-tab title="全部">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="onLoad"
          >
            <van-cell
              v-for="log in logs"
              :key="log.id"
              :title="log.reason"
              :value="formatValue(log)"
              :label="log.createTime"
              :class="log.changeType === 1 ? 'income' : 'expense'"
            />
          </van-list>
        </van-tab>
        <van-tab title="收入">
          <van-cell
            v-for="log in incomeLogs"
            :key="log.id"
            :title="log.reason"
            :value="'+' + log.changeAmount"
            :label="log.createTime"
            class="income"
          />
        </van-tab>
        <van-tab title="支出">
          <van-cell
            v-for="log in expenseLogs"
            :key="log.id"
            :title="log.reason"
            :value="'-' + log.changeAmount"
            :label="log.createTime"
            class="expense"
          />
        </van-tab>
      </van-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyPointBalance, getMyRanking, getMyPointLogs } from '@/api/point'

const router = useRouter()
const activeTab = ref(0)
const loading = ref(false)
const finished = ref(false)

const balance = ref(0)
const rank = ref(0)

const logs = ref<any[]>([])

const incomeLogs = computed(() => logs.value.filter(l => l.changeType === 1))
const expenseLogs = computed(() => logs.value.filter(l => l.changeType === 2))

const formatValue = (log: any) => {
  return (log.changeType === 1 ? '+' : '-') + log.changeAmount
}

const onLoad = async () => {
  if (activeTab.value === 0) {
    try {
      const result = await getMyPointLogs({ current: 1, size: 20 })
      logs.value = result.records || []
    } catch (error) {
      console.error('获取积分明细失败:', error)
    }
  }
  finished.value = true
}

const goToRank = () => {
  router.push('/point/rank')
}

// 加载积分信息
const loadPointInfo = async () => {
  try {
    const [balanceRes, rankRes] = await Promise.all([
      getMyPointBalance(),
      getMyRanking()
    ])
    balance.value = balanceRes || 0
    rank.value = rankRes?.ranking || 0
  } catch (error) {
    console.error('获取积分信息失败:', error)
  }
}

onMounted(() => {
  loadPointInfo()
  onLoad()
})
</script>

<style scoped lang="scss">
.point-page {
  background: #f5f5f5;
  min-height: 100vh;
}

.point-overview {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px 20px;
  color: #fff;
  text-align: center;
}

.point-total {
  margin-bottom: 15px;
  
  .label {
    display: block;
    font-size: 14px;
    opacity: 0.9;
    margin-bottom: 10px;
  }
  
  .value {
    display: block;
    font-size: 48px;
    font-weight: bold;
  }
}

.point-rank {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  font-size: 14px;
  opacity: 0.9;
}

.point-logs {
  margin-top: 10px;
}

.income {
  :deep(.van-cell__value) {
    color: #07c160;
  }
}

.expense {
  :deep(.van-cell__value) {
    color: #ee0a24;
  }
}
</style>
