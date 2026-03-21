<template>
  <div class="page">
    <div class="toolbar">
      <div class="left">
        <el-button @click="refreshCache" type="primary" :loading="refreshing">
          <el-icon><Refresh /></el-icon>
          刷新缓存
        </el-button>
        <el-button @click="fetchList">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
      </div>
      <div class="right">
        <el-radio-group v-model="activeTab" @change="handleTabChange">
          <el-radio-button label="room">房屋排行</el-radio-button>
          <el-radio-button label="building">楼栋统计</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 房屋排行榜 -->
    <div v-show="activeTab === 'room'" class="table-container">
      <el-table :data="roomList" border v-loading="loading" class="full-width-table">
        <el-table-column prop="ranking" label="排名" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.ranking <= 3" :type="getRankingType(row.ranking)" size="small">
              {{ row.ranking }}
            </el-tag>
            <span v-else>{{ row.ranking }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="roomNum" label="房屋编号" min-width="120" />
        <el-table-column prop="areaName" label="楼栋" min-width="120" />
        <el-table-column prop="pointsBalance" label="积分余额" min-width="120" align="right">
          <template #default="{ row }">
            <span class="points-value">{{ row.pointsBalance }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rankingChange" label="排名变化" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.rankingChange > 0" class="rank-up">↑{{ row.rankingChange }}</span>
            <span v-else-if="row.rankingChange < 0" class="rank-down">↓{{ Math.abs(row.rankingChange) }}</span>
            <span v-else class="rank-same">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" min-width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewRoomDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination 
          v-model:current-page="roomQuery.current" 
          :page-size="roomQuery.size" 
          layout="prev, pager, next, total"
          :total="roomTotal" 
          @current-change="fetchRoomRanking" 
        />
      </div>
    </div>

    <!-- 楼栋统计 -->
    <div v-show="activeTab === 'building'" class="table-container">
      <el-table :data="buildingList" border v-loading="loading" class="full-width-table">
        <el-table-column prop="buildingRanking" label="排名" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.buildingRanking <= 3" :type="getRankingType(row.buildingRanking)" size="small">
              {{ row.buildingRanking }}
            </el-tag>
            <span v-else>{{ row.buildingRanking }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="areaName" label="楼栋名称" min-width="150" />
        <el-table-column prop="totalPoints" label="总积分" min-width="120" align="right">
          <template #default="{ row }">
            <span class="points-value">{{ row.totalPoints }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="roomCount" label="房屋数量" min-width="100" align="center" />
        <el-table-column prop="avgPoints" label="平均积分" min-width="120" align="right">
          <template #default="{ row }">
            {{ row.avgPoints?.toFixed(1) || '0.0' }}
          </template>
        </el-table-column>
        <el-table-column prop="maxPoints" label="最高积分" min-width="120" align="right" />
        <el-table-column prop="minPoints" label="最低积分" min-width="120" align="right" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewBuildingRooms(row)">查看房屋排行</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination 
          v-model:current-page="buildingQuery.current" 
          :page-size="buildingQuery.size" 
          layout="prev, pager, next, total"
          :total="buildingTotal" 
          @current-change="fetchBuildingStats" 
        />
      </div>
    </div>

    <!-- 房屋详情弹窗 -->
    <el-dialog v-model="roomDetailVisible" title="房屋积分详情" width="600px">
      <div v-if="roomDetail" class="room-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="房屋编号">{{ roomDetail.roomNum }}</el-descriptions-item>
          <el-descriptions-item label="所属楼栋">{{ roomDetail.areaName }}</el-descriptions-item>
          <el-descriptions-item label="当前排名">{{ roomDetail.ranking }}</el-descriptions-item>
          <el-descriptions-item label="积分余额">
            <span class="points-value">{{ roomDetail.pointsBalance }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="排名变化">
            <span v-if="roomDetail.rankingChange > 0" class="rank-up">↑{{ roomDetail.rankingChange }}</span>
            <span v-else-if="roomDetail.rankingChange < 0" class="rank-down">↓{{ Math.abs(roomDetail.rankingChange) }}</span>
            <span v-else class="rank-same">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="最后更新" :span="2">{{ roomDetail.updateTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 楼栋房屋排行弹窗 -->
    <el-dialog v-model="buildingRoomsVisible" :title="`${currentBuilding?.areaName} - 房屋积分排行`" width="80%">
      <el-table :data="buildingRoomList" border style="width: 100%" v-loading="buildingRoomsLoading">
        <el-table-column prop="ranking" label="排名" width="80" align="center" />
        <el-table-column prop="roomNum" label="房屋编号" width="120" />
        <el-table-column prop="pointsBalance" label="积分余额" width="120" align="right">
          <template #default="{ row }">
            <span class="points-value">{{ row.pointsBalance }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import * as pointApi from '@/api/point'

// 响应式数据
const loading = ref(false)
const refreshing = ref(false)
const activeTab = ref('room')

// 房屋排行数据
const roomList = ref([])
const roomTotal = ref(0)
const roomQuery = reactive({
  current: 1,
  size: 20
})

// 楼栋统计数据
const buildingList = ref([])
const buildingTotal = ref(0)
const buildingQuery = reactive({
  current: 1,
  size: 20
})

// 弹窗数据
const roomDetailVisible = ref(false)
const roomDetail = ref(null)
const buildingRoomsVisible = ref(false)
const buildingRoomList = ref([])
const buildingRoomsLoading = ref(false)
const currentBuilding = ref(null)

// 获取排名类型样式
const getRankingType = (ranking) => {
  if (ranking === 1) return 'danger'
  if (ranking === 2) return 'warning'
  if (ranking === 3) return 'success'
  return ''
}

// 获取房屋排行榜
const fetchRoomRanking = async () => {
  try {
    loading.value = true
    const response = await pointApi.getRoomRanking(roomQuery)
    // response直接就是数据，因为request拦截器已经提取了res.result
    roomList.value = response || []
    roomTotal.value = response?.length || 0
  } catch (error) {
    ElMessage.error('获取房屋排行榜失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 获取楼栋统计
const fetchBuildingStats = async () => {
  try {
    loading.value = true
    const response = await pointApi.getBuildingStats(buildingQuery)
    // response直接就是数据
    buildingList.value = response || []
    buildingTotal.value = response?.length || 0
  } catch (error) {
    ElMessage.error('获取楼栋统计失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 刷新缓存
const refreshCache = async () => {
  try {
    refreshing.value = true
    await pointApi.refreshAllCache()
    ElMessage.success('缓存刷新成功')
    // 重新加载数据
    if (activeTab.value === 'room') {
      await fetchRoomRanking()
    } else {
      await fetchBuildingStats()
    }
  } catch (error) {
    ElMessage.error('缓存刷新失败: ' + error.message)
  } finally {
    refreshing.value = false
  }
}

// Tab切换
const handleTabChange = (tab) => {
  if (tab === 'room') {
    fetchRoomRanking()
  } else {
    fetchBuildingStats()
  }
}

// 查看房屋详情
const viewRoomDetail = async (row) => {
  try {
    const response = await pointApi.getRoomRankingDetail(row.roomId)
    roomDetail.value = response
    roomDetailVisible.value = true
  } catch (error) {
    ElMessage.error('获取房屋详情失败: ' + error.message)
  }
}

// 查看楼栋房屋排行
const viewBuildingRooms = async (row) => {
  try {
    currentBuilding.value = row
    buildingRoomsLoading.value = true
    buildingRoomsVisible.value = true
    
    const response = await pointApi.getBuildingRoomRanking(row.areaId, { size: 100 })
    buildingRoomList.value = response || []
  } catch (error) {
    ElMessage.error('获取楼栋房屋排行失败: ' + error.message)
  } finally {
    buildingRoomsLoading.value = false
  }
}

// 统一查询方法
const fetchList = () => {
  if (activeTab.value === 'room') {
    fetchRoomRanking()
  } else {
    fetchBuildingStats()
  }
}

// 初始化
onMounted(() => {
  fetchRoomRanking()
})
</script>

<style scoped>
.page {
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
}

.table-container {
  width: 100%;
}

.full-width-table {
  width: 100% !important;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.toolbar .left {
  display: flex;
  gap: 10px;
  align-items: center;
}

.points-value {
  font-weight: bold;
  color: #409eff;
  font-size: 16px;
}

.rank-up {
  color: #67c23a;
  font-weight: bold;
}

.rank-down {
  color: #f56c6c;
  font-weight: bold;
}

.rank-same {
  color: #909399;
}

.room-detail {
  padding: 20px 0;
}

.pager {
  margin-top: 20px;
  text-align: center;
}
</style>
