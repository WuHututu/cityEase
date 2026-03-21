import request from '@/utils/request'

// 获取我的积分余额
export const getMyPointBalance = () => {
  return request.get('/app/gov/point/balance')
}

// 获取我的房屋排名
export const getMyRanking = () => {
  return request.get('/app/gov/point/ranking')
}

// 分页获取我的积分明细
export const getMyPointLogs = (data: { 
  current?: number
  size?: number
  changeType?: number
}) => {
  return request.post('/app/gov/point/logs/page', data)
}

// 获取我的最新积分动态
export const getLatestPointLogs = (limit?: number) => {
  return request.get('/app/gov/point/logs/latest', {
    params: { limit: limit || 10 }
  })
}

// 获取房屋积分排行榜
export const getRoomRanking = (limit?: number) => {
  return request.get('/app/gov/ranking/room', {
    params: { limit: limit || 20 }
  })
}

// 获取楼栋积分对比统计
export const getBuildingStats = () => {
  return request.get('/app/gov/ranking/building')
}

// 获取指定楼栋的房屋积分排行
export const getBuildingRoomRanking = (areaId: number, limit?: number) => {
  return request.get(`/app/gov/ranking/building/${areaId}/rooms`, {
    params: { limit: limit || 20 }
  })
}
