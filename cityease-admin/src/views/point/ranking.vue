<template>
  <div class="point-ranking-page">
    <div class="toolbar">
      <div class="left">
        <el-radio-group v-model="activeTab">
          <el-radio-button style="margin-right: 10px" label="room">房屋排行</el-radio-button>
          <el-radio-button label="building">楼栋统计</el-radio-button>
        </el-radio-group>
        <el-button :loading="refreshing" type="primary" @click="refreshCache">刷新缓存</el-button>
      </div>
      <div class="right">
        <el-tag :type="cacheHealthy ? 'success' : 'warning'">
          {{ cacheHealthy ? '缓存正常' : '缓存待刷新' }}
        </el-tag>
      </div>
    </div>

    <el-card v-show="activeTab === 'room'" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>房屋积分排行</span>
        </div>
      </template>
      <el-table :data="roomPageData" v-loading="loading" border>
        <el-table-column prop="ranking" label="排名" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.ranking <= 3" :type="rankingTagType(row.ranking)">
              {{ row.ranking }}
            </el-tag>
            <span v-else>{{ row.ranking }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="roomNum" label="房屋编号" min-width="140" />
        <el-table-column prop="areaName" label="楼栋" min-width="140" />
        <el-table-column prop="pointsBalance" label="积分余额" min-width="120" align="right" />
        <el-table-column prop="rankingChange" label="排名变化" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.rankingChange > 0" class="rank-up">+{{ row.rankingChange }}</span>
            <span v-else-if="row.rankingChange < 0" class="rank-down">-{{ Math.abs(row.rankingChange) }}</span>
            <span v-else class="rank-stable">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" min-width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" @click="openRoomDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="roomPage"
          :page-size="pageSize"
          background
          layout="total, prev, pager, next, jumper"
          :total="roomSource.length"
        />
      </div>
    </el-card>

    <el-card v-show="activeTab === 'building'" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>楼栋积分统计</span>
        </div>
      </template>
      <el-table :data="buildingPageData" v-loading="loading" border>
        <el-table-column prop="buildingRanking" label="排名" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.buildingRanking <= 3" :type="rankingTagType(row.buildingRanking)">
              {{ row.buildingRanking }}
            </el-tag>
            <span v-else>{{ row.buildingRanking }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="areaName" label="楼栋名称" min-width="150" />
        <el-table-column prop="totalPoints" label="总积分" min-width="110" align="right" />
        <el-table-column prop="roomCount" label="房屋数" min-width="100" align="center" />
        <el-table-column label="平均积分" min-width="110" align="right">
          <template #default="{ row }">
            {{ Number(row.avgPoints || 0).toFixed(1) }}
          </template>
        </el-table-column>
        <el-table-column prop="maxPoints" label="最高积分" min-width="110" align="right" />
        <el-table-column prop="minPoints" label="最低积分" min-width="110" align="right" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" @click="openBuildingRooms(row)">房屋排行</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="buildingPage"
          :page-size="pageSize"
          background
          layout="total, prev, pager, next, jumper"
          :total="buildingSource.length"
        />
      </div>
    </el-card>

    <el-dialog v-model="roomDetailVisible" title="房屋积分详情" width="560px">
      <el-descriptions v-if="roomDetail" :column="2" border>
        <el-descriptions-item label="房屋编号">{{ roomDetail.roomNum }}</el-descriptions-item>
        <el-descriptions-item label="所属楼栋">{{ roomDetail.areaName }}</el-descriptions-item>
        <el-descriptions-item label="当前排名">{{ roomDetail.ranking }}</el-descriptions-item>
        <el-descriptions-item label="积分余额">{{ roomDetail.pointsBalance }}</el-descriptions-item>
        <el-descriptions-item label="排名变化">{{ roomDetail.rankingChange || 0 }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ roomDetail.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog
      v-model="buildingRoomsVisible"
      :title="currentBuilding ? `${currentBuilding.areaName} 房屋排行` : '楼栋房屋排行'"
      width="760px"
    >
      <el-table :data="buildingRooms" v-loading="buildingRoomsLoading" border>
        <el-table-column prop="ranking" label="排名" width="80" align="center" />
        <el-table-column prop="roomNum" label="房屋编号" min-width="140" />
        <el-table-column prop="pointsBalance" label="积分余额" min-width="120" align="right" />
        <el-table-column prop="updateTime" label="更新时间" min-width="180" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { BuildingPointStatsVO, PointRankingVO } from '@/api/point'
import {
  getBuildingRoomRanking,
  getBuildingStats,
  getCacheStatus,
  getRoomRanking,
  getRoomRankingDetail,
  refreshAllCache
} from '@/api/point'

const pageSize = 20

const loading = ref(false)
const refreshing = ref(false)
const activeTab = ref<'room' | 'building'>('room')
const cacheHealthy = ref(false)

const roomPage = ref(1)
const buildingPage = ref(1)
const roomSource = ref<PointRankingVO[]>([])
const buildingSource = ref<BuildingPointStatsVO[]>([])

const roomDetailVisible = ref(false)
const roomDetail = ref<PointRankingVO | null>(null)

const buildingRoomsVisible = ref(false)
const buildingRoomsLoading = ref(false)
const buildingRooms = ref<PointRankingVO[]>([])
const currentBuilding = ref<BuildingPointStatsVO | null>(null)

const roomPageData = computed(() => {
  const start = (roomPage.value - 1) * pageSize
  return roomSource.value.slice(start, start + pageSize)
})

const buildingPageData = computed(() => {
  const start = (buildingPage.value - 1) * pageSize
  return buildingSource.value.slice(start, start + pageSize)
})

const rankingTagType = (ranking: number) => {
  if (ranking === 1) return 'danger'
  if (ranking === 2) return 'warning'
  if (ranking === 3) return 'success'
  return 'info'
}

const loadCacheHealth = async () => {
  const status = await getCacheStatus()
  cacheHealthy.value = Boolean(status?.healthy)
}

const fetchRoomRanking = async () => {
  roomSource.value = (await getRoomRanking(500)) || []
}

const fetchBuildingStats = async () => {
  buildingSource.value = (await getBuildingStats()) || []
}

const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([fetchRoomRanking(), fetchBuildingStats(), loadCacheHealth()])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '积分排行数据加载失败')
  } finally {
    loading.value = false
  }
}

const refreshCache = async () => {
  refreshing.value = true
  try {
    await refreshAllCache()
    ElMessage.success('缓存刷新成功')
    await loadData()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '缓存刷新失败')
  } finally {
    refreshing.value = false
  }
}

const openRoomDetail = async (row: PointRankingVO) => {
  try {
    roomDetail.value = await getRoomRankingDetail(row.roomId)
    roomDetailVisible.value = true
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '房屋详情加载失败')
  }
}

const openBuildingRooms = async (row: BuildingPointStatsVO) => {
  currentBuilding.value = row
  buildingRoomsVisible.value = true
  buildingRoomsLoading.value = true
  try {
    buildingRooms.value = (await getBuildingRoomRanking(row.areaId, 200)) || []
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '楼栋房屋排行加载失败')
  } finally {
    buildingRoomsLoading.value = false
  }
}

watch(activeTab, () => {
  if (activeTab.value === 'room') {
    roomPage.value = 1
  } else {
    buildingPage.value = 1
  }
})

onMounted(loadData)
</script>

<style scoped>
.point-ranking-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.rank-up {
  color: #16a34a;
  font-weight: 600;
}

.rank-down {
  color: #dc2626;
  font-weight: 600;
}

.rank-stable {
  color: #94a3b8;
}
</style>
