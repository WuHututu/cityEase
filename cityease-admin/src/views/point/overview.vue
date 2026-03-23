<template>
  <div class="point-overview-page">
    <section class="hero-card">
      <div>
        <h2>积分治理总览</h2>
        <p>从趋势、缓存和最近流水三个角度看当前积分系统运行状态。</p>
      </div>
      <div class="hero-actions">
        <el-button :loading="loading" type="primary" @click="loadData">刷新数据</el-button>
        <el-button @click="goToRanking">查看排行</el-button>
        <el-button @click="goToRules">规则管理</el-button>
      </div>
    </section>

    <el-row :gutter="16" class="kpi-row">
      <el-col :span="6">
        <div class="kpi-card">
          <div class="label">累计发放积分</div>
          <div class="value">{{ overview.totalGranted }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="kpi-card">
          <div class="label">累计消耗积分</div>
          <div class="value">{{ overview.totalConsumed }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="kpi-card">
          <div class="label">参与用户数</div>
          <div class="value">{{ overview.participantUsers }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="kpi-card">
          <div class="label">今日净变化</div>
          <div class="value" :class="overview.todayNetChange >= 0 ? 'positive' : 'negative'">
            {{ overview.todayNetChange }}
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="content-grid">
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>近 7 日积分趋势</span>
          </div>
        </template>
        <div ref="chartRef" class="chart"></div>
      </el-card>

      <el-card class="side-card" shadow="hover">
        <template #header>
      <div class="card-header">
        <span>缓存状态</span>
        <div class="cache-actions">
              <el-button link type="primary" @click="refreshCache">刷新</el-button>
              <el-button link type="danger" @click="clearCache">清空</el-button>
            </div>
          </div>
        </template>
        <div class="cache-panel">
        <div class="cache-row">
          <span>健康状态</span>
          <el-tag :type="cacheStatus.healthy ? 'success' : 'warning'">
            {{ cacheStatus.healthy ? '正常' : '待刷新' }}
          </el-tag>
        </div>
        <div class="cache-row">
          <span>最后刷新</span>
          <span class="muted">{{ cacheStatus.lastCheckedAt || '-' }}</span>
        </div>
        </div>
      </el-card>
    </div>

    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>最近积分流水</span>
        </div>
      </template>
      <el-table :data="latestLogs" v-loading="logsLoading" border>
        <el-table-column label="房屋" min-width="120">
          <template #default="{ row }">
            {{ row.roomId ? `房屋 #${row.roomId}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="用户ID" prop="userId" min-width="100" />
        <el-table-column label="变化类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.changeType === 1 ? 'success' : 'warning'">
              {{ row.changeType === 1 ? '增加' : '扣减' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变化值" width="120" align="right">
          <template #default="{ row }">
            <span :class="row.changeType === 1 ? 'positive-text' : 'negative-text'">
              {{ row.changeType === 1 ? '+' : '-' }}{{ Math.abs(row.changeAmount || 0) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变更后余额" prop="afterBalance" width="120" align="right" />
        <el-table-column label="原因" prop="reason" min-width="240" show-overflow-tooltip />
        <el-table-column label="时间" prop="createTime" min-width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { CacheStatusResult, GovPointLogVO, PointOverviewVO, PointTrendVO } from '@/api/point'
import {
  clearAllCache,
  getCacheStatus,
  getLatestPointLogs,
  getPointOverview,
  getPointTrend,
  refreshAllCache
} from '@/api/point'

type CacheState = CacheStatusResult & { lastCheckedAt: string }

const router = useRouter()

const loading = ref(false)
const logsLoading = ref(false)
const chartRef = ref<HTMLElement | null>(null)
const latestLogs = ref<GovPointLogVO[]>([])

const overview = ref<PointOverviewVO>({
  totalGranted: 0,
  totalConsumed: 0,
  participantUsers: 0,
  todayNetChange: 0
})

const cacheStatus = ref<CacheState>({
  status: '',
  healthy: false,
  lastCheckedAt: ''
})

let chart: echarts.ECharts | null = null

const goToRanking = () => router.push('/point/ranking')
const goToRules = () => router.push('/point/rules')

const renderChart = (trend: PointTrendVO) => {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: {
      top: 10,
      left: 'center',
      itemWidth: 14,
      itemHeight: 10,
      itemGap: 20,
      textStyle: {
        color: '#f8fafc',
        fontWeight: 600
      },
      data: ['发放', '消耗', '净变化']
    },
    grid: { left: 28, right: 24, top: 84, bottom: 44, containLabel: true },
    xAxis: {
      type: 'category',
      data: trend.dates || [],
      axisLine: {
        lineStyle: { color: 'rgba(148, 163, 184, 0.24)' }
      },
      axisTick: { show: false },
      axisLabel: {
        color: '#f8fafc',
        margin: 16
      }
    },
    yAxis: {
      type: 'value',
      splitLine: {
        lineStyle: { color: 'rgba(148, 163, 184, 0.08)' }
      },
      axisLabel: {
        color: '#f8fafc'
      }
    },
    series: [
      {
        name: '发放',
        type: 'bar',
        itemStyle: { color: '#22c55e' },
        data: trend.grantSeries || []
      },
      {
        name: '消耗',
        type: 'bar',
        itemStyle: { color: '#f97316' },
        data: trend.consumeSeries || []
      },
      {
        name: '净变化',
        type: 'line',
        smooth: true,
        itemStyle: { color: '#38bdf8' },
        areaStyle: { color: 'rgba(56, 189, 248, 0.10)' },
        data: trend.netSeries || []
      }
    ]
  })
}

const loadCacheStatus = async () => {
  const result = await getCacheStatus()
  cacheStatus.value = {
    status: result?.status || '',
    healthy: Boolean(result?.healthy),
    lastCheckedAt: new Date().toLocaleString('zh-CN', { hour12: false })
  }
}

const loadLatestLogs = async () => {
  logsLoading.value = true
  try {
    latestLogs.value = (await getLatestPointLogs(10)) || []
  } finally {
    logsLoading.value = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const [overviewRes, trendRes] = await Promise.all([getPointOverview(), getPointTrend(7)])
    overview.value = overviewRes || overview.value
    await nextTick()
    renderChart(trendRes || { dates: [], grantSeries: [], consumeSeries: [], netSeries: [] })
    await Promise.all([loadCacheStatus(), loadLatestLogs()])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '积分总览数据加载失败')
  } finally {
    loading.value = false
  }
}

const refreshCache = async () => {
  try {
    await refreshAllCache()
    ElMessage.success('缓存刷新成功')
    await loadCacheStatus()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '缓存刷新失败')
  }
}

const clearCache = async () => {
  try {
    await ElMessageBox.confirm('确认清空当前积分排行缓存吗？', '清空缓存', { type: 'warning' })
    await clearAllCache()
    ElMessage.success('缓存已清空')
    await loadCacheStatus()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '缓存清空失败')
    }
  }
}

const onResize = () => chart?.resize()

onMounted(() => {
  loadData()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.point-overview-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-radius: 14px;
  background:
    linear-gradient(135deg, rgba(8, 15, 27, 0.96), rgba(8, 15, 27, 0.88)),
    radial-gradient(circle at top left, rgba(34, 197, 94, 0.18), transparent 34%),
    radial-gradient(circle at top right, rgba(6, 182, 212, 0.16), transparent 30%);
  color: #e2e8f0;
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.hero-card h2 {
  margin: 0 0 8px;
  font-size: 24px;
}

.hero-card p {
  margin: 0;
  color: #f8fafc;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.kpi-row {
  margin-bottom: 0;
}

.kpi-card {
  padding: 18px;
  border-radius: 12px;
  background: #0f172a;
  color: #e2e8f0;
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.label {
  font-size: 13px;
  color: #94a3b8;
}

.value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
  color: #38bdf8;
}

.value.positive {
  color: #22c55e;
}

.value.negative {
  color: #f97316;
}

.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.chart-card,
.side-card {
  min-height: 360px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.cache-actions {
  display: flex;
  gap: 8px;
}

.chart {
  height: 320px;
  width: 100%;
}

.cache-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.cache-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.cache-row > span:first-child {
  color: #f8fafc;
  font-weight: 600;
}

.muted {
  color: #f8fafc;
  text-align: right;
}

.positive-text {
  color: #16a34a;
  font-weight: 600;
}

.negative-text {
  color: #ea580c;
  font-weight: 600;
}
</style>
