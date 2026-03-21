<template>
  <div class="home-page">
    <!-- 顶部用户信息 -->
    <div class="user-header" @click="handleUserHeaderClick">
      <div class="user-info">
        <van-image
          round
          width="50"
          height="50"
          src="https://img.yzcdn.cn/vant/cat.jpeg"
        />
        <div class="user-meta">
          <h3>{{ userInfo.phone || '游客' }}</h3>
          <p>{{ roomInfo.roomNum || '未绑定房屋' }}</p>
        </div>
      </div>
    </div>
    
    <!-- 积分卡片 -->
    <div class="point-card">
      <div class="point-header">
        <span class="point-label">我的积分</span>
        <van-tag type="primary" @click="goToPointDetail">查看明细</van-tag>
      </div>
      <div class="point-value">{{ pointBalance }}</div>
      <div class="point-rank">小区排名: 第 {{ pointRank }} 名</div>
    </div>
    
    <!-- 快捷功能 -->
    <div class="quick-actions">
      <h4 class="section-title">快捷服务</h4>
      <div class="action-grid">
        <div class="action-item" @click="goToMall">
          <van-icon name="shop-o" size="28" color="#ff6b6b" />
          <span>积分商城</span>
        </div>
        <div class="action-item" @click="goToRepair">
          <van-icon name="records" size="28" color="#4ecdc4" />
          <span>物业报修</span>
        </div>
        <div class="action-item" @click="goToRank">
          <van-icon name="bar-chart-o" size="28" color="#45b7d1" />
          <span>排行榜</span>
        </div>
        <div class="action-item" @click="goToNotice">
          <van-icon name="bullhorn-o" size="28" color="#f9ca24" />
          <span>小区公告</span>
        </div>
      </div>
    </div>
    
    <!-- 最新公告 -->
    <div class="notice-section">
      <div class="section-header">
        <h4 class="section-title">最新公告</h4>
        <van-button type="primary" size="small" plain @click="goToNotice">查看更多</van-button>
      </div>
      <van-cell-group inset>
        <van-cell 
          v-for="notice in notices" 
          :key="notice.id"
          :title="notice.title"
          :label="notice.createTime"
          is-link
          @click="viewNotice(notice)"
        />
      </van-cell-group>
    </div>
    
    <!-- 最新动态 -->
    <div class="activity-section">
      <h4 class="section-title">积分动态</h4>
      <van-cell-group inset>
        <van-cell 
          v-for="log in pointLogs" 
          :key="log.id"
          :title="log.reason"
          :value="(log.changeType === 1 ? '+' : '-') + log.changeAmount"
          :label="log.createTime"
        />
      </van-cell-group>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUserInfo, getCurrentRoom } from '@/api/user'
import { getMyPointBalance, getMyRanking, getLatestPointLogs } from '@/api/point'
import { getPublishedNotices } from '@/api/notice'

const router = useRouter()

// 用户信息
const userInfo = ref<any>({})
const roomInfo = ref<any>({})

// 积分信息
const pointBalance = ref(0)
const pointRank = ref(0)

// 公告列表
const notices = ref<any[]>([])

// 积分动态
const pointLogs = ref<any[]>([])

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const [userRes, roomRes] = await Promise.all([
      getUserInfo(),
      getCurrentRoom()
    ])
    userInfo.value = userRes || {}
    roomInfo.value = roomRes || {}
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 加载积分信息
const loadPointInfo = async () => {
  try {
    const [balanceRes, rankRes] = await Promise.all([
      getMyPointBalance(),
      getMyRanking()
    ])
    pointBalance.value = balanceRes || 0
    pointRank.value = rankRes?.ranking || 0
  } catch (error) {
    console.error('获取积分信息失败:', error)
  }
}

// 加载公告列表
const loadNotices = async () => {
  try {
    const result = await getPublishedNotices({ current: 1, size: 5 })
    notices.value = result.records || []
  } catch (error) {
    console.error('获取公告列表失败:', error)
  }
}

// 加载积分动态
const loadPointLogs = async () => {
  try {
    const result = await getLatestPointLogs(5)
    pointLogs.value = result || []
  } catch (error) {
    console.error('获取积分动态失败:', error)
  }
}

// 页面跳转
const handleUserHeaderClick = () => {
  if (!roomInfo.value.roomNum) {
    // 未绑定房屋，跳转到绑定页面
    router.push('/user/bind-room')
  }
}
const goToPointDetail = () => router.push('/point')
const goToMall = () => router.push('/mall')
const goToRepair = () => router.push('/repair')
const goToRank = () => router.push('/point/rank')
const goToNotice = () => router.push('/notice')
const viewNotice = (notice: any) => {
  // 查看公告详情
}

onMounted(() => {
  loadUserInfo()
  loadPointInfo()
  loadNotices()
  loadPointLogs()
})
</script>

<style scoped lang="scss">
.home-page {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 20px;
}

.user-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  color: #fff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-meta {
  h3 {
    font-size: 18px;
    margin-bottom: 5px;
  }
  p {
    font-size: 14px;
    opacity: 0.9;
  }
}

.point-card {
  background: #fff;
  margin: 30px 15px 15px;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  position: relative;
  z-index: 10;
}

.point-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.point-label {
  color: #666;
  font-size: 14px;
}

.point-value {
  font-size: 36px;
  font-weight: bold;
  color: #ff6b6b;
  margin-bottom: 5px;
}

.point-rank {
  font-size: 14px;
  color: #999;
}

.quick-actions {
  margin: 15px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 12px;
  padding-left: 5px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  background: #fff;
  padding: 15px;
  border-radius: 12px;
  justify-items: center;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 5px;
  min-height: 70px;
  width: 100%;
  box-sizing: border-box;
  
  span {
    font-size: 12px;
    color: #666;
    text-align: center;
    line-height: 1.2;
    word-break: break-all;
  }
}

.notice-section, .activity-section {
  margin: 15px;
}
</style>
