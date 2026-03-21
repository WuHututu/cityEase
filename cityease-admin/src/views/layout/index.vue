<template>
  <div class="layout">
    <div class="sidebar" :class="{ collapse: isCollapse }">
      <div class="brand">
        <el-icon>
          <Platform />
        </el-icon>
        <div v-if="!isCollapse" class="brand-text">
          <span class="cn">城易治</span>
          <span class="en">CityEase</span>
        </div>
      </div>

      <el-menu active-text-color="#00f0ff" background-color="#0f172a" class="el-menu-vertical"
        :default-active="route.path" text-color="#94a3b8" :collapse="isCollapse" router>
        <el-menu-item index="/dashboard">
          <el-icon>
            <DataLine />
          </el-icon>
          <template #title>数据大屏</template>
        </el-menu-item>

        <el-menu-item index="/repair">
          <el-icon>
            <Tools />
          </el-icon>
          <template #title>报修工单</template>
        </el-menu-item>

        <el-sub-menu index="pms">
          <template #title>
            <el-icon>
              <House />
            </el-icon>
            <span>房屋/区域</span>
          </template>
          <el-menu-item index="/area">公共区域管理</el-menu-item>
          <el-menu-item index="/room">房屋管理</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/bind">
          <el-icon>
            <Connection />
          </el-icon>
          <template #title>绑定审核</template>
        </el-menu-item>

        <el-menu-item index="/fee">
          <el-icon>
            <Money />
          </el-icon>
          <template #title>物业费账单</template>
        </el-menu-item>

        <el-sub-menu index="system">
          <template #title>
            <el-icon>
              <Setting />
            </el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/dict">字典管理</el-menu-item>
          <el-menu-item index="/notice">公告管理</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/mall">
          <el-icon>
            <ShoppingCart />
          </el-icon>
          <template #title>积分商城</template>
        </el-menu-item>

        <el-sub-menu index="point">
          <template #title>
            <el-icon>
              <Trophy />
            </el-icon>
            <span>积分管理</span>
          </template>
          <el-menu-item index="/point/overview">积分总览</el-menu-item>
          <el-menu-item index="/point/ranking">积分排行榜</el-menu-item>
          <el-menu-item index="/point/rules">积分规则</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/user">
          <el-icon>
            <User />
          </el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="main">
      <div class="header">
        <div class="left">
          <el-button text @click="toggle">
            <el-icon>
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </el-button>
          <div class="header-meta">
            <span class="title">城易治 · 智慧物业管理台</span>
            <span class="time">{{ nowText }}</span>
          </div>
        </div>

        <div class="right">
          <el-dropdown>
            <span class="user">
              <el-avatar :size="28" :src="userInfo?.avatar" />
              <span class="name">{{ userInfo?.username || '管理员' }}</span>
              <el-icon>
                <ArrowDown />
              </el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { Platform, DataLine, Tools, Money, House, Fold, Expand, ArrowDown, User, ShoppingCart, Connection, Setting, Trophy } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const isCollapse = ref(false)
const toggle = () => (isCollapse.value = !isCollapse.value)

const unwrap = (res: any) => res?.result ?? res?.data?.data ?? res?.data ?? res
const userInfo = ref<any>(null)
const nowText = ref('')
let timer: number | undefined

const updateTime = () => {
  const now = new Date()
  nowText.value = now.toLocaleString('zh-CN', { hour12: false })
}

const fetchUser = async () => {
  try {
    const res: any = await request.get('/admin/user/info')
    userInfo.value = unwrap(res)
  } catch {
    // ignore
  }
}

const logout = async () => {
  try {
    await request.post('/logout')
  } catch {
    // ignore
  }
  localStorage.removeItem('satoken')
  ElMessage.success('已退出')
  router.push('/login')
}

onMounted(() => {
  fetchUser()
  updateTime()
  timer = window.setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  background: #0b1220;
}

.sidebar {
  width: 220px;
  background: #0f172a;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.sidebar.collapse {
  width: 64px;
}

.brand {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  font-weight: 700;
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}

.cn {
  font-size: 14px;
  color: #e2e8f0;
}

.en {
  font-size: 12px;
  color: #64748b;
}

.main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  background: #0b1220;
  border-bottom: 1px solid rgba(148, 163, 184, .12);
}

.header-meta {
  display: flex;
  flex-direction: column;
  margin-left: 8px;
}

.title {
  color: #e2e8f0;
  font-size: 14px;
  font-weight: 600;
}

.time {
  color: #64748b;
  font-size: 12px;
}

.content {
  padding: 12px;
  overflow: auto;
}

.user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #e2e8f0;
}

.name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
