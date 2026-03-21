<template>
  <div class="user-page">
    <van-nav-bar title="我的" fixed placeholder />
    
    <!-- 用户信息卡片 -->
    <div class="user-card" @click="handleUserCardClick">
      <div class="user-info">
        <van-image
          round
          width="60"
          height="60"
          src="https://img.yzcdn.cn/vant/cat.jpeg"
        />
        <div class="user-meta">
          <h3>{{ userInfo.phone || '游客' }}</h3>
          <p>{{ roomInfo.roomNum || '未绑定房屋' }}</p>
        </div>
      </div>
    </div>
    
    <!-- 功能列表 -->
    <van-cell-group inset class="menu-group">
      <van-cell title="我的积分" is-link to="/point">
        <template #icon>
          <van-icon name="points" class="menu-icon" color="#ff6b6b" />
        </template>
      </van-cell>
      <van-cell title="兑换记录" is-link>
        <template #icon>
          <van-icon name="records" class="menu-icon" color="#4ecdc4" />
        </template>
      </van-cell>
      <van-cell title="我的报修" is-link to="/repair">
        <template #icon>
          <van-icon name="setting-o" class="menu-icon" color="#45b7d1" />
        </template>
      </van-cell>
    </van-cell-group>
    
    <van-cell-group inset class="menu-group">
      <van-cell title="联系物业" is-link>
        <template #icon>
          <van-icon name="phone-o" class="menu-icon" color="#6c5ce7" />
        </template>
      </van-cell>
    </van-cell-group>
    
    <!-- 退出登录 -->
    <div class="logout-btn">
      <van-button block round type="danger" plain @click="handleLogout">
        退出登录
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getUserInfo, getCurrentRoom } from '@/api/user'
import { logout } from '@/api/auth'

const router = useRouter()

const userInfo = ref<any>({})
const roomInfo = ref<any>({})

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

const handleUserCardClick = () => {
  if (!roomInfo.value.roomNum) {
    // 未绑定房屋，跳转到绑定页面
    router.push('/user/bind-room')
  }
}

const handleLogout = () => {
  showConfirmDialog({
    title: '提示',
    message: '确定要退出登录吗？'
  })
    .then(async () => {
      try {
        await logout()
      } catch (error) {
        console.error('退出登录失败:', error)
      }
      
      localStorage.removeItem('satoken')
      localStorage.removeItem('userInfo')
      showToast('已退出登录')
      router.push('/login')
    })
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped lang="scss">
.user-page {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 20px;
}

.user-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px 20px;
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
  color: #fff;
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

.menu-group {
  margin-bottom: 15px;
}

.menu-icon {
  margin-right: 8px;
  font-size: 20px;
}

.logout-btn {
  padding: 20px;
}
</style>
