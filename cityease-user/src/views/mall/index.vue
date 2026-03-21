<template>
  <div class="mall-page">
    <van-nav-bar title="积分商城" fixed placeholder />
    
    <!-- 积分显示 -->
    <div class="points-bar">
      <span>我的积分: <strong>{{ points }}</strong></span>
      <van-button size="small" type="primary" @click="goToOrders">兑换记录</van-button>
    </div>
    
    <!-- 商品列表 -->
    <div class="goods-list">
      <div 
        v-for="goods in goodsList" 
        :key="goods.id"
        class="goods-card"
        @click="viewDetail(goods)"
      >
        <van-image :src="goods.imageUrl" fit="cover" class="goods-image" />
        <div class="goods-info">
          <h4 class="goods-name">{{ goods.name }}</h4>
          <p class="goods-desc">{{ goods.description }}</p>
          <div class="goods-bottom">
            <span class="goods-price">{{ goods.pointsPrice }}积分</span>
            <van-button 
              size="small" 
              type="primary" 
              :disabled="goods.stock === 0"
              @click.stop="exchange(goods)"
            >
              {{ goods.stock === 0 ? '缺货' : '兑换' }}
            </van-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 兑换弹窗 -->
    <van-dialog
      v-model:show="showExchange"
      title="确认兑换"
      show-cancel-button
      @confirm="confirmExchange"
    >
      <div class="exchange-content">
        <p>{{ currentGoods?.name }}</p>
        <p>消耗 {{ currentGoods?.pointsPrice }} 积分</p>
        <p>剩余积分: {{ points - (currentGoods?.pointsPrice || 0) }}</p>
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { getGoodsList, redeemGoods } from '@/api/mall'
import { getMyPointBalance } from '@/api/point'
import { getCurrentRoom } from '@/api/user'

const router = useRouter()
const points = ref(0)
const showExchange = ref(false)
const currentGoods = ref<any>(null)
const currentRoom = ref<any>(null)

const goodsList = ref<any[]>([])

// 加载商品列表
const loadGoodsList = async () => {
  try {
    const result = await getGoodsList(1) // 只获取上架商品
    goodsList.value = result || []
  } catch (error) {
    console.error('获取商品列表失败:', error)
  }
}

// 加载用户积分和房屋信息
const loadUserInfo = async () => {
  try {
    const [pointsRes, roomRes] = await Promise.all([
      getMyPointBalance(),
      getCurrentRoom()
    ])
    points.value = pointsRes || 0
    currentRoom.value = roomRes
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const viewDetail = (goods: any) => {
  // 查看详情
}

const exchange = (goods: any) => {
  if (!currentRoom.value) {
    showToast('请先绑定房屋')
    return
  }
  
  if (points.value < goods.pointsPrice) {
    showToast('积分不足')
    return
  }
  currentGoods.value = goods
  showExchange.value = true
}

const confirmExchange = async () => {
  if (!currentRoom.value || !currentGoods.value) return
  
  try {
    await redeemGoods({
      goodsId: currentGoods.value.id,
      roomId: currentRoom.value.roomId
    })
    points.value -= currentGoods.value.pointsPrice
    showSuccessToast('兑换成功')
  } catch (error: any) {
    showToast(error.message || '兑换失败')
  }
}

const goToOrders = () => {
  // 查看兑换记录
}

onMounted(() => {
  loadGoodsList()
  loadUserInfo()
})
</script>

<style scoped lang="scss">
.mall-page {
  background: #f5f5f5;
  min-height: 100vh;
}

.points-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #fff;
  margin-bottom: 10px;
  
  strong {
    color: #ff6b6b;
    font-size: 18px;
  }
}

.goods-list {
  padding: 10px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.goods-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.goods-image {
  width: 100%;
  height: 150px;
}

.goods-info {
  padding: 10px;
}

.goods-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 5px;
}

.goods-desc {
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
}

.goods-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.goods-price {
  color: #ff6b6b;
  font-weight: bold;
  font-size: 16px;
}

.exchange-content {
  padding: 20px;
  text-align: center;
  
  p {
    margin-bottom: 10px;
    
    &:first-child {
      font-weight: bold;
      font-size: 16px;
    }
  }
}
</style>
