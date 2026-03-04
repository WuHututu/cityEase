<template>
  <div class="dashboard-container">
    <div class="welcome-box">
      <h2>数据指挥舱 <span class="subtitle">Data Command Center</span></h2>
      <p>实时监控 CityEase 智慧社区的运行状态</p>
    </div>

    <el-row :gutter="20" class="kpi-row">
      <el-col :span="6">
        <div class="kpi-card tech-blue">
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
        <div class="kpi-card warning-orange" :class="{ 'pulse-alert': metrics.pendingRepairs > 0 }">
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
        <div class="kpi-card tech-blue">
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
          <div class="card-header">近七日工单趋势 (图表占位)</div>
          <div class="chart-placeholder">
            <el-icon class="is-loading" :size="40"><Loading /></el-icon>
            <p>ECharts 接入中...</p>
          </div>
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
import { ref, onMounted } from 'vue'
import { OfficeBuilding, User, Warning, Tools, Loading } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

// 定义指标数据结构
interface DashboardMetrics {
  totalRooms: number
  authenticatedUsers: number
  pendingRepairs: number
  processingRepairs: number
  publishedNotices: number
}

// 响应式数据绑定，设定初始值为 0
const metrics = ref<DashboardMetrics>({
  totalRooms: 0,
  authenticatedUsers: 0,
  pendingRepairs: 0,
  processingRepairs: 0,
  publishedNotices: 0
})

// 获取后台首页核心指标数据
const fetchMetrics = async () => {
  try {
    // 调用后端的 /admin/system/dashboard/metrics 接口
    const res: any = await request.get('/admin/system/dashboard/metrics')
    if (res) {
      metrics.value = res
    }
  } catch (error) {
    ElMessage.error('获取大屏指标数据失败')
  }
}

// 组件挂载时拉取数据
onMounted(() => {
  fetchMetrics()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  color: #e2e8f0;
}

.welcome-box {
  margin-bottom: 24px;
  h2 {
    margin: 0 0 8px 0;
    font-size: 24px;
    font-weight: 600;
    color: #fff;
    .subtitle {
      font-size: 14px;
      color: #1890ff;
      margin-left: 10px;
      font-weight: normal;
      letter-spacing: 1px;
    }
  }
  p {
    margin: 0;
    color: #94a3b8;
    font-size: 14px;
  }
}

/* KPI 卡片设计 - 玻璃拟态与科技感 */
.kpi-row {
  margin-bottom: 24px;
}

.kpi-card {
  display: flex;
  align-items: center;
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 24px 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-5px);
  }

  .icon-wrapper {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 28px;
    margin-right: 20px;
  }

  .info {
    flex: 1;
    .label {
      font-size: 14px;
      color: #94a3b8;
      margin-bottom: 8px;
    }
    .value {
      color: #fff;
      .num {
        font-size: 28px;
        font-weight: bold;
        font-family: 'Din', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      }
      .unit {
        font-size: 12px;
        color: #64748b;
        margin-left: 4px;
      }
    }
  }
}

/* 颜色变体 */
.tech-blue {
  &:hover { box-shadow: 0 10px 20px rgba(24, 144, 255, 0.15); }
  .icon-wrapper {
    background: rgba(24, 144, 255, 0.1);
    color: #1890ff;
  }
}

.mint-green {
  &:hover { box-shadow: 0 10px 20px rgba(0, 185, 107, 0.15); }
  .icon-wrapper {
    background: rgba(0, 185, 107, 0.1);
    color: #00B96B;
  }
}

.warning-orange {
  &:hover { box-shadow: 0 10px 20px rgba(250, 140, 22, 0.15); }
  .icon-wrapper {
    background: rgba(250, 140, 22, 0.1);
    color: #FA8C16;
  }
}

/* 待办事项红点呼吸灯预警 */
.pulse-alert {
  position: relative;
  &::after {
    content: '';
    position: absolute;
    top: 15px;
    right: 15px;
    width: 10px;
    height: 10px;
    background-color: #ff4d4f;
    border-radius: 50%;
    animation: pulse 2s infinite;
  }
}

@keyframes pulse {
  0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(255, 77, 79, 0.7); }
  70% { transform: scale(1); box-shadow: 0 0 0 10px rgba(255, 77, 79, 0); }
  100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(255, 77, 79, 0); }
}

/* 占位图表区 */
.chart-card {
  background: rgba(30, 41, 59, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
  height: 350px;
  display: flex;
  flex-direction: column;

  .card-header {
    font-size: 16px;
    font-weight: 500;
    color: #e2e8f0;
    margin-bottom: 20px;
  }

  .chart-placeholder {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #64748b;
    border: 1px dashed rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    background: rgba(15, 23, 42, 0.3);

    p { margin-top: 10px; }
  }

  .notice-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;

    .big-num {
      font-size: 72px;
      font-weight: bold;
      color: #1890ff;
      background: linear-gradient(135deg, #1890ff, #00f0ff);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    p {
      color: #94a3b8;
      margin-top: 10px;
    }
  }
}
</style>