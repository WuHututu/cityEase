<template>
  <div class="page">
    <div class="overview-header">
      <h2>积分系统总览</h2>
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon size="24"><Trophy /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">总积分发放</div>
            <div class="stat-value">{{ overview.totalPointsIssued || 0 }}</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon size="24"><ShoppingCart /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">总积分消耗</div>
            <div class="stat-value">{{ overview.totalPointsConsumed || 0 }}</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon size="24"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">参与用户</div>
            <div class="stat-value">{{ overview.activeUsers || 0 }}</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
            <el-icon size="24"><DataLine /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">今日积分变动</div>
            <div class="stat-value">{{ overview.todayPointChanges || 0 }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="content-section">
      <div class="section-left">
        <!-- 快捷操作 -->
        <el-card class="quick-actions" header="快捷操作">
          <div class="action-grid">
            <div class="action-item" @click="goToRanking">
              <el-icon size="32"><Trophy /></el-icon>
              <span>积分排行榜</span>
            </div>
            <div class="action-item" @click="goToRules">
              <el-icon size="32"><Setting /></el-icon>
              <span>规则管理</span>
            </div>
            <div class="action-item" @click="goToMall">
              <el-icon size="32"><ShoppingBag /></el-icon>
              <span>积分商城</span>
            </div>
            <div class="action-item" @click="refreshCache">
              <el-icon size="32"><Refresh /></el-icon>
              <span>刷新缓存</span>
            </div>
          </div>
        </el-card>

        <!-- 缓存状态 -->
        <el-card class="cache-status" header="缓存状态">
          <div class="cache-info">
            <div class="cache-item">
              <span class="cache-label">状态:</span>
              <el-tag :type="cacheStatus.type">{{ cacheStatus.text }}</el-tag>
            </div>
            <div class="cache-item">
              <span class="cache-label">最后更新:</span>
              <span>{{ cacheStatus.lastUpdate }}</span>
            </div>
            <div class="cache-item">
              <el-button size="small" @click="refreshCache" :loading="refreshing">
                立即刷新
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <div class="section-right">
        <!-- 积分趋势图 -->
        <el-card class="chart-card" header="积分趋势（最近7天）">
          <div ref="chartRef" class="chart-container"></div>
        </el-card>

        <!-- 热门商品 -->
        <el-card class="hot-items" header="热门兑换商品">
          <div class="hot-list">
            <div v-for="item in hotItems" :key="item.id" class="hot-item">
              <el-image 
                :src="item.imageUrl" 
                fit="cover"
                style="width: 40px; height: 40px; border-radius: 4px;"
              />
              <div class="hot-info">
                <div class="hot-name">{{ item.name }}</div>
                <div class="hot-stats">兑换 {{ item.exchangeCount }} 次</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 最新积分动态 -->
    <el-card class="recent-activities" header="最新积分动态">
      <el-timeline>
        <el-timeline-item 
          v-for="activity in recentActivities" 
          :key="activity.id"
          :timestamp="activity.createTime"
          :type="getActivityType(activity.changeType)"
        >
          <div class="activity-content">
            <div class="activity-main">
              <span class="activity-user">{{ activity.roomNum }}</span>
              <span :class="getActivityClass(activity.changeType)">
                {{ getActivityText(activity.changeType) }} {{ Math.abs(activity.changeAmount) }} 积分
              </span>
            </div>
            <div class="activity-reason">{{ activity.reason }}</div>
            <div class="activity-balance">余额: {{ activity.afterBalance }} 积分</div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Trophy, 
  ShoppingCart, 
  User, 
  DataLine, 
  Setting, 
  ShoppingBag, 
  Refresh 
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import * as pointApi from '@/api/point'
import { useRouter } from 'vue-router'

const router = useRouter()

// 响应式数据
const refreshing = ref(false)
const chartRef = ref()
const overview = reactive({
  totalPointsIssued: 0,
  totalPointsConsumed: 0,
  activeUsers: 0,
  todayPointChanges: 0
})

const cacheStatus = reactive({
  type: 'success',
  text: '正常',
  lastUpdate: '-'
})

const hotItems = ref([])
const recentActivities = ref([])

// 获取总览数据
const fetchOverview = async () => {
  try {
    // 这里应该有专门的统计接口，暂时用模拟数据
    overview.totalPointsIssued = 15420
    overview.totalPointsConsumed = 8960
    overview.activeUsers = 156
    overview.todayPointChanges = 238
  } catch (error) {
    ElMessage.error('获取总览数据失败: ' + error.message)
  }
}

// 获取缓存状态
const fetchCacheStatus = async () => {
  try {
    const response = await pointApi.getCacheStatus()
    // response直接就是缓存状态数据
    const status = response?.status || 'unknown'
    if (status.includes('success')) {
      cacheStatus.type = 'success'
      cacheStatus.text = '正常'
    } else if (status.includes('error')) {
      cacheStatus.type = 'danger'
      cacheStatus.text = '异常'
    } else {
      cacheStatus.type = 'warning'
      cacheStatus.text = '未知'
    }
    cacheStatus.lastUpdate = new Date().toLocaleString()
  } catch (error) {
    cacheStatus.type = 'danger'
    cacheStatus.text = '获取失败'
  }
}

// 获取热门商品
const fetchHotItems = async () => {
  try {
    // 这里应该有专门的统计接口，暂时用模拟数据
    hotItems.value = [
      {
        id: 1,
        name: '5L食用油',
        imageUrl: 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png',
        exchangeCount: 45
      },
      {
        id: 2,
        name: '洗衣液',
        imageUrl: 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png',
        exchangeCount: 32
      },
      {
        id: 3,
        name: '纸巾套装',
        imageUrl: 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0065.png',
        exchangeCount: 28
      }
    ]
  } catch (error) {
    ElMessage.error('获取热门商品失败: ' + error.message)
  }
}

// 获取最新动态
const fetchRecentActivities = async () => {
  try {
    const response = await pointApi.getPointLogPage({ current: 1, size: 10 })
    // response直接就是Page对象
    recentActivities.value = response?.records || []
  } catch (error) {
    ElMessage.error('获取最新动态失败: ' + error.message)
  }
}

// 刷新缓存
const refreshCache = async () => {
  try {
    refreshing.value = true
    await pointApi.refreshAllCache()
    ElMessage.success('缓存刷新成功')
    fetchCacheStatus()
  } catch (error) {
    ElMessage.error('缓存刷新失败: ' + error.message)
  } finally {
    refreshing.value = false
  }
}

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return

  const chart = echarts.init(chartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      },
      position: function (point, params, dom, rect, size) {
        // 自定义tooltip位置，避免与横轴重合
        const obj = {top: 60}
        obj[['left', 'right'][+(point[0] < size.viewSize[0] / 2)]] = 5
        return obj
      }
    },
    legend: {
      data: ['积分发放', '积分消耗'],
      textStyle: {
        color: '#CBD5E1'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['7天前', '6天前', '5天前', '4天前', '3天前', '2天前', '昨天', '今天'],
      axisLabel: {
        color: '#CBD5E1'
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: '#CBD5E1'
      }
    },
    series: [
      {
        name: '积分发放',
        type: 'line',
        smooth: true,
        itemStyle: {
          color: '#67c23a'
        },
        data: [120, 132, 101, 134, 90, 230, 210, 238]
      },
      {
        name: '积分消耗',
        type: 'line',
        smooth: true,
        itemStyle: {
          color: '#f56c6c'
        },
        data: [80, 92, 61, 84, 60, 150, 130, 145]
      }
    ]
  }

  chart.setOption(option)
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

// 获取活动类型
const getActivityType = (changeType) => {
  return changeType === 1 ? 'primary' : 'warning'
}

// 获取活动文本
const getActivityText = (changeType) => {
  return changeType === 1 ? '获得' : '消耗'
}

// 获取活动样式
const getActivityClass = (changeType) => {
  return changeType === 1 ? 'activity-add' : 'activity-minus'
}

// 导航方法
const goToRanking = () => {
  router.push('/point/ranking')
}

const goToRules = () => {
  router.push('/point/rules')
}

const goToMall = () => {
  router.push('/mall')
}

// 初始化
onMounted(async () => {
  await Promise.all([
    fetchOverview(),
    fetchCacheStatus(),
    fetchHotItems(),
    fetchRecentActivities()
  ])
  
  nextTick(() => {
    initChart()
  })
})
</script>

<style scoped>
.page {
  padding: 20px;
}

.overview-header h2 {
  margin: 0 0 20px 0;
  color: #CBD5E1;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 16px;
}

.stat-content {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.content-section {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
  margin-bottom: 20px;
}

.quick-actions, .cache-status {
  margin-bottom: 20px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  color: #CBD5E1;
}

.action-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.action-item span {
  margin-top: 10px;
  font-size: 14px;
  color: #CBD5E1;
}

.cache-info {
  space-y: 12px;
}

.cache-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #CBD5E1;
}

.cache-label {
  font-weight: 500;
  color: #CBD5E1;
}

:deep(.el-card__header) {
  color: #CBD5E1;
}

.chart-card {
  color: #CBD5E1;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.hot-list {
  space-y: 12px;
}

.hot-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.hot-item:last-child {
  border-bottom: none;
}

.hot-info {
  margin-left: 12px;
  flex: 1;
}

.hot-name {
  font-size: 14px;
  color: #CBD5E1;
  margin-bottom: 4px;
}

.hot-stats {
  font-size: 12px;
  color: #909399;
}

.recent-activities {
  margin-top: 20px;
}

.activity-content {
  padding-left: 10px;
}

.activity-main {
  margin-bottom: 4px;
}

.activity-user {
  font-weight: 500;
  color: #409eff;
  margin-right: 8px;
}

.activity-add {
  color: #67c23a;
  font-weight: 500;
}

.activity-minus {
  color: #f56c6c;
  font-weight: 500;
}

.activity-reason {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.activity-balance {
  font-size: 12px;
  color: #606266;
}
</style>
