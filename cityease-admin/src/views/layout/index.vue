<template>
  <div class="layout-shell">
    <aside class="sidebar" :class="{ collapse: isCollapse }">
      <div class="sidebar__glow"></div>

      <div class="brand">
        <div class="brand__mark">
          <el-icon>
            <Platform />
          </el-icon>
        </div>
        <div v-if="!isCollapse" class="brand__text">
          <span class="brand__title">城易治</span>
          <span class="brand__subtitle">CityEase Admin Console</span>
        </div>
      </div>

      <div v-if="!isCollapse" class="sidebar__summary">
        <span class="sidebar__label">{{ routeContext.eyebrow }}</span>
        <strong>{{ currentRouteTitle }}</strong>
        <p>{{ routeContext.description }}</p>
      </div>

      <el-scrollbar class="sidebar__scroll">
        <el-menu
          class="sidebar__menu"
          :default-active="route.path"
          :collapse="isCollapse"
          background-color="transparent"
          active-text-color="#F8FAFC"
          text-color="rgba(226, 232, 240, 0.72)"
          router
        >
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
              <span>房屋 / 区域</span>
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

          <el-sub-menu index="point">
            <template #title>
              <el-icon>
                <Trophy />
              </el-icon>
              <span>积分管理</span>
            </template>
            <el-menu-item index="/point/overview">积分总览</el-menu-item>
            <el-menu-item index="/mall">积分商城</el-menu-item>
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
      </el-scrollbar>

      <div v-if="!isCollapse" class="sidebar__footer">
        <span>系统时钟</span>
        <strong>{{ timeText }}</strong>
        <small>{{ dateText }}</small>
      </div>
    </aside>

    <main class="main-shell">
      <header class="topbar">
        <div class="topbar__left">
          <button class="topbar__toggle" type="button" @click="toggle">
            <el-icon size="18">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </button>

          <div class="topbar__headline">
            <span class="route-badge">{{ routeContext.eyebrow }}</span>
            <div>
              <h1>{{ currentRouteTitle }}</h1>
              <p>{{ routeContext.description }}</p>
            </div>
          </div>
        </div>

        <div class="topbar__right">
          <div class="route-kicker">
            <div class="route-kicker__metric">
              <span>时间</span>
              <strong>{{ timeText }}</strong>
            </div>
          </div>

          <el-dropdown>
            <span class="user-pill">
              <el-avatar :size="40" :src="userInfo?.avatar">{{ userInitial }}</el-avatar>
              <span class="user-pill__meta">
                <strong>{{ displayName }}</strong>
                <small>系统维护者</small>
              </span>
              <el-icon class="user-pill__arrow">
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
      </header>

      <section class="content-shell">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import {
  Platform,
  DataLine,
  Tools,
  Money,
  House,
  Fold,
  Expand,
  ArrowDown,
  User,
  Connection,
  Setting,
  Trophy
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const isCollapse = ref(false)
const userInfo = ref<any>(null)
const timeText = ref('')
const dateText = ref('')
let timer: number | undefined

const routeContextMap: Record<string, { eyebrow: string; description: string }> = {
  '/dashboard': {
    eyebrow: '运营总览',
    description: '查看社区运行指标、重点波动与全局服务表现。'
  },
  '/repair': {
    eyebrow: '工单中心',
    description: '处理报修派单、进度跟踪与评价闭环。'
  },
  '/area': {
    eyebrow: '空间资产',
    description: '维护公共区域资料、楼宇信息与空间状态。'
  },
  '/room': {
    eyebrow: '空间资产',
    description: '统一管理房屋档案、住户关系与房源基础数据。'
  },
  '/bind': {
    eyebrow: '审核工作台',
    description: '核验住户绑定申请、证明材料与审核快照。'
  },
  '/fee': {
    eyebrow: '财务管理',
    description: '跟踪账单生成、支付状态与催缴节奏。'
  },
  '/dict': {
    eyebrow: '系统配置',
    description: '统一维护字典类型、枚举值与平台基础配置。'
  },
  '/notice': {
    eyebrow: '内容运营',
    description: '发布社区通知、活动公告与富文本内容。'
  },
  '/mall': {
    eyebrow: '积分生态',
    description: '维护商品兑换、库存状态与积分权益。'
  },
  '/point/overview': {
    eyebrow: '积分生态',
    description: '把握积分增长、发放与核销趋势。'
  },
  '/point/ranking': {
    eyebrow: '积分生态',
    description: '观察排行榜变化与社区活跃度分布。'
  },
  '/point/rules': {
    eyebrow: '积分生态',
    description: '管理积分规则、奖惩逻辑与触发条件。'
  },
  '/user': {
    eyebrow: '住户中心',
    description: '维护住户资料、账号状态与关联信息。'
  }
}

const unwrap = (res: any) => res?.result ?? res?.data?.data ?? res?.data ?? res

const currentRouteTitle = computed(() => String(route.meta.title ?? '城易治管理台'))
const routeContext = computed(() => (
  routeContextMap[route.path] ?? {
    eyebrow: 'CityEase Admin',
    description: '统一管理物业运营、空间服务与住户数据。'
  }
))
const displayName = computed(() => (
  userInfo.value?.nickName || userInfo.value?.nickname || userInfo.value?.username || '管理员'
))
const userInitial = computed(() => displayName.value.slice(0, 1))

const toggle = () => {
  isCollapse.value = !isCollapse.value
}

const updateTime = () => {
  const now = new Date()
  timeText.value = now.toLocaleTimeString('zh-CN', { hour12: false })
  dateText.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    weekday: 'short'
  })
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
  ElMessage.success('已退出登录')
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
.layout-shell {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 16px;
  min-height: 100vh;
  padding: 16px;
}

.sidebar {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 292px;
  min-height: calc(100vh - 32px);
  padding: 18px;
  border-radius: 32px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  background:
    linear-gradient(180deg, rgba(8, 15, 27, 0.98), rgba(7, 12, 22, 0.92)),
    radial-gradient(circle at top left, rgba(34, 197, 94, 0.12), transparent 30%);
  box-shadow: 0 30px 90px rgba(1, 6, 14, 0.44);
  overflow: hidden;
  transition: width 0.22s ease, padding 0.22s ease;
}

.sidebar.collapse {
  width: 92px;
  padding-inline: 12px;
}

.sidebar__glow {
  position: absolute;
  inset: -20% auto auto -25%;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(34, 197, 94, 0.2) 0%, transparent 70%);
  pointer-events: none;
}

.brand {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 70px;
}

.brand__mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.22), rgba(6, 182, 212, 0.18));
  border: 1px solid rgba(134, 239, 172, 0.22);
  color: #f8fafc;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.04);
}

.brand__text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.brand__title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.brand__subtitle {
  color: rgba(148, 163, 184, 0.7);
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.sidebar__summary {
  position: relative;
  z-index: 1;
  margin-top: 18px;
  padding: 16px 18px;
  border-radius: 22px;
  border: 1px solid rgba(148, 163, 184, 0.12);
  background: linear-gradient(180deg, rgba(13, 22, 37, 0.84), rgba(7, 12, 22, 0.76));
}

.sidebar__label {
  display: inline-flex;
  margin-bottom: 10px;
  color: rgba(134, 239, 172, 0.86);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.sidebar__summary strong {
  display: block;
  font-size: 18px;
  line-height: 1.2;
}

.sidebar__summary p {
  margin: 10px 0 0;
  color: rgba(203, 213, 225, 0.68);
  line-height: 1.65;
  font-size: 13px;
}

.sidebar__scroll {
  position: relative;
  z-index: 1;
  flex: 1;
  min-height: 0;
  margin-top: 14px;
}

.sidebar__footer {
  position: relative;
  z-index: 1;
  margin-top: 14px;
  padding: 16px 18px;
  border-radius: 22px;
  border: 1px solid rgba(148, 163, 184, 0.12);
  background: rgba(8, 15, 27, 0.74);
}

.sidebar__footer span,
.sidebar__footer small {
  display: block;
  color: rgba(148, 163, 184, 0.74);
}

.sidebar__footer strong {
  display: block;
  margin: 8px 0 6px;
  font-family: var(--admin-font-mono);
  font-size: 24px;
  letter-spacing: 0.04em;
}

.main-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
  min-height: calc(100vh - 32px);
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  min-height: 90px;
  padding: 18px 24px;
  border-radius: 30px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  background:
    linear-gradient(180deg, rgba(8, 15, 27, 0.9), rgba(8, 13, 24, 0.76)),
    radial-gradient(circle at top right, rgba(6, 182, 212, 0.12), transparent 32%);
  backdrop-filter: blur(18px);
  box-shadow: 0 24px 72px rgba(1, 6, 14, 0.34);
}

.topbar__left,
.topbar__right {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.topbar__right {
  justify-content: flex-end;
}

.topbar__toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  border-radius: 16px;
  background: rgba(8, 15, 27, 0.92);
  color: var(--admin-text);
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.topbar__toggle:hover {
  transform: translateY(-1px);
  border-color: rgba(134, 239, 172, 0.32);
  background: rgba(11, 20, 35, 0.96);
}

.topbar__headline {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  min-width: 0;
}

.topbar__headline h1 {
  margin: 6px 0 0;
  font-size: 30px;
  line-height: 1.05;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.topbar__headline p {
  margin: 8px 0 0;
  color: rgba(203, 213, 225, 0.72);
  font-size: 14px;
  line-height: 1.7;
}

.route-badge {
  display: inline-flex;
  align-items: center;
  height: 32px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid rgba(134, 239, 172, 0.2);
  background: rgba(34, 197, 94, 0.1);
  color: rgba(220, 252, 231, 0.92);
  font-family: var(--admin-font-mono);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  white-space: nowrap;
}

.route-kicker {
  display: flex;
  align-items: center;
  gap: 12px;
}

.route-kicker__metric {
  min-width: 138px;
  padding: 12px 14px;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  background: rgba(8, 15, 27, 0.74);
}

.route-kicker__metric span {
  display: block;
  color: rgba(148, 163, 184, 0.72);
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.route-kicker__metric strong {
  display: block;
  margin-top: 6px;
  font-family: var(--admin-font-mono);
  font-size: 18px;
  font-weight: 600;
  color: rgba(248, 250, 252, 0.94);
}

.user-pill {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px 8px 8px;
  border-radius: 20px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  background: rgba(8, 15, 27, 0.92);
  color: var(--admin-text);
  cursor: pointer;
  user-select: none;
}

.user-pill__meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-pill__meta strong {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.user-pill__meta small {
  margin-top: 2px;
  color: rgba(148, 163, 184, 0.7);
  font-size: 12px;
}

.user-pill__arrow {
  color: rgba(148, 163, 184, 0.78);
}

.content-shell {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding-right: 2px;
}

:deep(.sidebar__menu) {
  border-right: 0 !important;
}

:deep(.el-scrollbar__view) {
  min-height: 100%;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 48px;
  margin: 4px 0;
  border-radius: 16px;
  font-weight: 600;
}

:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  font-size: 16px;
}

:deep(.el-sub-menu .el-menu-item) {
  margin-left: 12px;
  padding-left: 42px !important;
  color: rgba(203, 213, 225, 0.72) !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.18), rgba(6, 182, 212, 0.14)) !important;
  box-shadow: inset 0 0 0 1px rgba(134, 239, 172, 0.22);
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: rgba(15, 23, 42, 0.76) !important;
  color: #f8fafc !important;
}

:deep(.el-menu--collapse .el-sub-menu__title),
:deep(.el-menu--collapse .el-menu-item) {
  justify-content: center;
}

@media (max-width: 1280px) {
  .route-kicker {
    display: none;
  }
}

@media (max-width: 960px) {
  .layout-shell {
    gap: 12px;
    padding: 12px;
  }

  .sidebar {
    width: 88px;
    padding-inline: 12px;
  }

  .sidebar__summary,
  .sidebar__footer,
  .brand__text {
    display: none;
  }

  .topbar {
    min-height: 82px;
    padding: 16px 18px;
  }

  .topbar__headline h1 {
    font-size: 24px;
  }
}

@media (max-width: 720px) {
  .layout-shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    width: 100%;
    min-height: auto;
  }

  .main-shell {
    min-height: auto;
  }

  .topbar,
  .topbar__left,
  .topbar__right,
  .topbar__headline {
    align-items: flex-start;
    flex-direction: column;
  }

  .user-pill {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
