<template>
  <div class="point-overview">
    <el-row :gutter="16" class="kpi-row">
      <el-col :span="6"><div class="kpi-card"><div class="label">总积分发放</div><div class="value">{{ overview.totalGranted }}</div></div></el-col>
      <el-col :span="6"><div class="kpi-card"><div class="label">总积分消耗</div><div class="value">{{ overview.totalConsumed }}</div></div></el-col>
      <el-col :span="6"><div class="kpi-card"><div class="label">参与用户</div><div class="value">{{ overview.participantUsers }}</div></div></el-col>
      <el-col :span="6"><div class="kpi-card"><div class="label">今日积分变动</div><div class="value" :class="overview.todayNetChange >= 0 ? 'up' : 'down'">{{ overview.todayNetChange }}</div></div></el-col>
    </el-row>

    <el-card shadow="hover">
      <template #header>
        <div class="header-row">
          <span>近7日积分趋势</span>
          <el-button size="small" :loading="loading" @click="loadData">刷新</el-button>
        </div>
      </template>
      <div ref="chartRef" class="chart"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getPointOverview, getPointTrend } from '@/api/point'

const overview = ref({ totalGranted: 0, totalConsumed: 0, participantUsers: 0, todayNetChange: 0 })
const loading = ref(false)
const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const renderChart = (data: any) => {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['发放', '消耗', '净变动'] },
    xAxis: { type: 'category', data: data?.dates || [] },
    yAxis: { type: 'value' },
    series: [
      { name: '发放', type: 'bar', data: data?.grantSeries || [] },
      { name: '消耗', type: 'bar', data: data?.consumeSeries || [] },
      { name: '净变动', type: 'line', smooth: true, data: data?.netSeries || [] }
    ]
  })
}

const loadData = async () => {
  loading.value = true
  try {
    const [overviewRes, trendRes] = await Promise.all([getPointOverview(), getPointTrend(7)])
    overview.value = overviewRes || overview.value
    renderChart(trendRes || {})
  } catch (e) {
    ElMessage.error('积分总览数据加载失败')
  } finally {
    loading.value = false
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
.point-overview { display: flex; flex-direction: column; gap: 16px; }
.kpi-row { margin-bottom: 4px; }
.kpi-card { background: #0f172a; border-radius: 10px; padding: 16px; color: #e2e8f0; }
.label { font-size: 13px; opacity: .85; }
.value { margin-top: 10px; font-size: 28px; font-weight: 700; color: #22d3ee; }
.value.up { color: #22c55e; }
.value.down { color: #f97316; }
.chart { height: 340px; width: 100%; }
.header-row { display: flex; justify-content: space-between; align-items: center; }
</style>
