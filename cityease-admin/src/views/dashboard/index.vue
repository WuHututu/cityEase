<template>
  <div class="dashboard-container">
    <div class="welcome-box">
      <h2>数据指挥舱 <span class="subtitle">Data Command Center</span></h2>
      <p>实时监控 CityEase 智慧社区的运行状态</p>
    </div>

    <el-row :gutter="20" class="kpi-row">
      <el-col :span="6">
        <div class="kpi-card tech-blue" @click="navigateToHouse()" style="cursor: pointer;">
          <div class="icon-wrapper"><el-icon><OfficeBuilding /></el-icon></div>
          <div class="info">
            <div class="label">房屋总数</div>
            <div class="value">
              <span class="num">{{ metrics.totalRooms }}</span>
              <span class="unit">套</span>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="kpi-card mint-green">
          <div class="icon-wrapper"><el-icon><User /></el-icon></div>
          <div class="info">
            <div class="label">认证业主/家属</div>
            <div class="value">
              <span class="num">{{ metrics.authenticatedUsers }}</span>
              <span class="unit">人</span>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="kpi-card warning-orange" :class="{ 'pulse-alert': metrics.pendingRepairs > 0 }" @click="navigateToRepair(0)" style="cursor: pointer;">
          <div class="icon-wrapper"><el-icon><Warning /></el-icon></div>
          <div class="info">
            <div class="label">待派发工单</div>
            <div class="value">
              <span class="num">{{ metrics.pendingRepairs }}</span>
              <span class="unit">单</span>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="kpi-card tech-blue" @click="navigateToRepair(1)" style="cursor: pointer;">
          <div class="icon-wrapper"><el-icon><Tools /></el-icon></div>
          <div class="info">
            <div class="label">处理中工单</div>
            <div class="value">
              <span class="num">{{ metrics.processingRepairs }}</span>
              <span class="unit">单</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <div class="chart-card">
          <div class="card-header">近七日工单趋势</div>
          <div ref="chartRef" class="echarts-container" style="flex: 1; width: 100%; min-height: 280px;"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="card-header">系统公告概览</div>
          <div class="notice-info">
            <div class="big-num">{{ metrics.publishedNotices }}</div>
            <p>已发布公告总数</p>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { OfficeBuilding, User, Warning, Tools } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {useRouter} from "vue-router";

interface DashboardMetrics {
  totalRooms: number
  authenticatedUsers: number
  pendingRepairs: number
  processingRepairs: number
  publishedNotices: number
}

// 定义图表数据类型
interface ChartDataVO {
  xAxisData: string[]
  xaxisData: string[]  // 添加小写字段以匹配后端返回
  series: Array<{
    name: string
    data: number[]
  }>
}

const router = useRouter()

const metrics = ref<DashboardMetrics>({
  totalRooms: 0,
  authenticatedUsers: 0,
  pendingRepairs: 0,
  processingRepairs: 0,
  publishedNotices: 0
})

// 图表相关引用
const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

// 初始化 ECharts
const initChart = (chartData: any = null) => {
  // 确保DOM元素已经渲染
  nextTick(() => {
    if (!chartRef.value) {
      console.log('Chart container not ready, delaying initialization');
      // 如果DOM元素还没准备好，延迟一下再尝试
      setTimeout(() => {
        if (!chartRef.value) return;
        initializeChart();
      }, 100);
    } else {
      initializeChart();
    }
  });

  function initializeChart() {
    if (!chartRef.value) return;

    if (chartInstance) {
      chartInstance.dispose();
    }

    chartInstance = echarts.init(chartRef.value);
    
    let xAxisData: string[] = [];
    let seriesData: any[] = [];
    
    if (chartData && (chartData.xAxisData || chartData.xaxisData) && chartData.series) {
      // 兼容大小写不同的字段名
      xAxisData = chartData.xAxisData || chartData.xaxisData;
      
      // 确保seriesData是正确格式
      seriesData = chartData.series.map((seriesItem: any) => {
        return {
          name: seriesItem.name,
          type: 'line',
          smooth: true,
          lineStyle: { 
            color: seriesItem.name === '新增报修' ? '#1890ff' : '#00B96B', 
            width: 3 
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { 
                offset: 0, 
                color: seriesItem.name === '新增报修' ? 'rgba(24,144,255,0.3)' : 'rgba(0,185,107,0.3)' 
              },
              { 
                offset: 1, 
                color: seriesItem.name === '新增报修' ? 'rgba(24,144,255,0.05)' : 'rgba(0,185,107,0.05)' 
              }
            ])
          },
          itemStyle: { 
            color: seriesItem.name === '新增报修' ? '#1890ff' : '#00B96B' 
          },
          data: seriesItem.data
        }
      });
    } else {
      // 如果没有数据，使用默认值
      xAxisData = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
      seriesData = [
        {
          name: '新增工单',
          type: 'line',
          smooth: true,
          lineStyle: { color: '#1890ff', width: 3 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(24,144,255,0.3)' },
              { offset: 1, color: 'rgba(24,144,255,0.05)' }
            ])
          },
          itemStyle: { color: '#1890ff' },
          data: [0, 0, 0, 0, 0, 0, 0]
        }
      ];
    }
    
    const option = {
      tooltip: { 
        trigger: 'axis',
        formatter: (params: any) => {
          let tipHtml = params[0].axisValue + '<br/>';
          params.forEach((param: any) => {
            tipHtml += param.marker + ' ' + param.seriesName + ': ' + param.data + ' 单<br/>';
          });
          return tipHtml;
        }
      },
      grid: { top: '15%', left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: true,
        data: xAxisData,
        axisLine: { lineStyle: { color: '#64748b' } },
        axisLabel: {
          color: '#94a3b8',
          margin: 15
        }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } },
        axisLabel: { color: '#94a3b8' }
      },
      legend: {
        top: '0%',
        boundaryGap: true,
        data: seriesData.map((s: any) => s.name),
        textStyle: { color: '#94a3b8' }
      },
      series: seriesData
    };
    chartInstance.setOption(option);

    // 确保图表尺寸正确
    chartInstance.resize();
  }
}

// 窗口调整防抖处理图表自适应
const handleResize = () => {
  chartInstance?.resize()
}

// 跳转到报修工单页面并按状态筛选
const navigateToRepair = (status: number) => {
  router.push({
    name: 'Repair',
    query: { status: status.toString() }
  })
}

// 跳转到房屋管理页面
const navigateToHouse = () => {
  router.push({
    name: 'House'
  })
}

// 获取后台首页核心指标数据
const fetchMetrics = async () => {
  try {
    const res: any = await request.get('/admin/system/dashboard/metrics')
    if (res) {
      metrics.value = res.data || res // 兼容后端的 ResVo 结构
      // 如果后端有返回真实的图表数据，这里可以传入 initChart(res.chartData)
    }
  } catch (error) {
    ElMessage.error('获取大屏指标数据失败')
  }
}

// 获取近七日工单趋势图表数据
const fetchChartData = async () => {
  try {
    const res: any = await request.get('/admin/system/dashboard/chart/repairTrend')

    // 自动探测真实的数据层级
    let chartData = null;

    if (res?.result?.series) {
      chartData = res.result;             // 场景1: Axios 拦截器已解包响应体
    } else if (res?.data?.result?.series) {
      chartData = res.data.result;        // 场景2: Axios 原生返回，未完全解包
    } else if (res?.data?.series) {
      chartData = res.data;               // 场景3: 备用兼容
    } else if (res?.series) {
      chartData = res;                    // 场景4: 备用兼容
    }

    // 打印出来检查一下是否拿到了 { series: [...], xaxisData: [...] }
    console.log('🔍 解析给 ECharts 的真实数据:', chartData);

    if (chartData && (chartData.xAxisData || chartData.xaxisData) && chartData.series) {
      initChart(chartData) // 使用真实数据初始化图表
    } else {
      console.warn('⚠️ 未匹配到正确的数据结构，使用默认全0配置', res);
      initChart() // 使用默认配置初始化图表
    }
  } catch (error) {
    console.error('❌ 获取图表数据失败', error)
    ElMessage.error('获取图表数据失败')
    initChart() // 使用默认配置初始化图表
  }
}

onMounted(() => {
  fetchMetrics();
  // 延迟一点获取图表数据，确保DOM完全渲染
  setTimeout(() => {
    fetchChartData(); // 获取图表数据
  }, 100);
  // 必须等 DOM 渲染完后初始化图表
  nextTick(() => {
    window.addEventListener('resize', handleResize);
    // 在DOM准备完成后再次尝试初始化图表（如果需要的话）
    setTimeout(() => {
      if (chartRef.value && chartInstance) {
        chartInstance.resize();
      }
    }, 300);
  });
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped lang="scss">
/* 此处保留你原有的 CSS 样式，无需改动 */
.dashboard-container { color: #e2e8f0; }
.welcome-box { margin-bottom: 24px; h2 { margin: 0 0 8px 0; font-size: 24px; font-weight: 600; color: #fff; .subtitle { font-size: 14px; color: #1890ff; margin-left: 10px; font-weight: normal; letter-spacing: 1px; } } p { margin: 0; color: #94a3b8; font-size: 14px; } }
.kpi-row { margin-bottom: 24px; }
.kpi-card { display: flex; align-items: center; background: rgba(30, 41, 59, 0.7); backdrop-filter: blur(10px); border: 1px solid rgba(255, 255, 255, 0.05); border-radius: 12px; padding: 24px 20px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); transition: transform 0.3s ease, box-shadow 0.3s ease; &:hover { transform: translateY(-5px); } .icon-wrapper { width: 56px; height: 56px; border-radius: 12px; display: flex; justify-content: center; align-items: center; font-size: 28px; margin-right: 20px; } .info { flex: 1; .label { font-size: 14px; color: #94a3b8; margin-bottom: 8px; } .value { color: #fff; .num { font-size: 28px; font-weight: bold; font-family: 'Din', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; } .unit { font-size: 12px; color: #64748b; margin-left: 4px; } } } }
.tech-blue { &:hover { box-shadow: 0 10px 20px rgba(24, 144, 255, 0.15); } .icon-wrapper { background: rgba(24, 144, 255, 0.1); color: #1890ff; } }
.mint-green { &:hover { box-shadow: 0 10px 20px rgba(0, 185, 107, 0.15); } .icon-wrapper { background: rgba(0, 185, 107, 0.1); color: #00B96B; } }
.warning-orange { &:hover { box-shadow: 0 10px 20px rgba(250, 140, 22, 0.15); } .icon-wrapper { background: rgba(250, 140, 22, 0.1); color: #FA8C16; } }
.pulse-alert { position: relative; &::after { content: ''; position: absolute; top: 15px; right: 15px; width: 10px; height: 10px; background-color: #ff4d4f; border-radius: 50%; animation: pulse 2s infinite; } }
@keyframes pulse { 0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(255, 77, 79, 0.7); } 70% { transform: scale(1); box-shadow: 0 0 0 10px rgba(255, 77, 79, 0); } 100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(255, 77, 79, 0); } }
.chart-card { background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(255, 255, 255, 0.05); border-radius: 12px; padding: 20px; height: 350px; display: flex; flex-direction: column; .card-header { font-size: 16px; font-weight: 500; color: #e2e8f0; margin-bottom: 20px; } .notice-info { flex: 1; display: flex; flex-direction: column; justify-content: center; align-items: center; .big-num { font-size: 72px; font-weight: bold; color: #1890ff; background: linear-gradient(135deg, #1890ff, #00f0ff); -webkit-background-clip: text; -webkit-text-fill-color: transparent; } p { color: #94a3b8; margin-top: 10px; } } }
</style>