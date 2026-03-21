import request from '@/utils/request'

// 积分排行榜相关接口
export interface PointRankingQuery {
  current?: number
  size?: number
}

export interface BuildingStatsQuery {
  current?: number
  size?: number
}

// 积分规则相关接口
export interface PointRuleQuery {
  current?: number
  size?: number
  ruleType?: number | null
  status?: number | null
}

export interface PointRuleSaveReq {
  id?: number | null
  ruleName: string
  ruleType: number
  pointsAmount: number
  maxDailyTimes?: number | null
  maxMonthlyTimes?: number | null
  triggerCondition?: string
  description?: string
  sortOrder?: number
  status: number
}

export interface PointLogQuery {
  current?: number
  size?: number
  ruleId?: number | null
  roomId?: number | null
  executionResult?: number | null
}

// 积分排行榜接口
export const getRoomRanking = (params: PointRankingQuery) => 
  request.get('/admin/gov/ranking/room', { params })

export const getRoomRankingDetail = (roomId: number) => 
  request.get(`/admin/gov/ranking/room/${roomId}`)

export const getBuildingStats = (params: BuildingStatsQuery = {}) => 
  request.get('/admin/gov/ranking/building', { params })

export const getBuildingRoomRanking = (areaId: number, params: PointRankingQuery = {}) => 
  request.get(`/admin/gov/ranking/building/${areaId}/rooms`, { params })

// 积分规则管理接口
export const getPointRulePage = (params: PointRuleQuery) => 
  request.get('/admin/gov/rule/page', { params })

export const getPointRuleDetail = (id: number) => 
  request.get('/admin/gov/rule/detail', { params: { id } })

export const savePointRule = (data: PointRuleSaveReq) => 
  request.post('/admin/gov/rule/save', data)

export const deletePointRule = (id: number) => 
  request.post('/admin/gov/rule/delete', null, { params: { id } })

export const changePointRuleStatus = (id: number, status: number) => 
  request.post('/admin/gov/rule/status', null, { params: { id, status } })

export const getPointRuleLogPage = (params: PointLogQuery) => 
  request.get('/admin/gov/rule/log/page', { params })

export const executePointRule = (data: {
  ruleId: number
  roomId: number
  userId: number
  triggerEvent: string
  triggerData?: string
}) => 
  request.post('/admin/gov/rule/execute', data)

// 积分缓存管理接口
export const refreshAllCache = () => 
  request.post('/admin/gov/cache/refresh/all')

export const refreshRoomRankingCache = () => 
  request.post('/admin/gov/cache/refresh/room')

export const refreshBuildingStatsCache = () => 
  request.post('/admin/gov/cache/refresh/building')

export const removeRoomRankingCache = (roomId: number) => 
  request.post('/admin/gov/cache/remove/room', null, { params: { roomId } })

export const getCacheStatus = () => 
  request.get('/admin/gov/cache/status')

// 积分流水接口
export const getPointLogPage = (params: PointLogQuery) => 
  request.get('/admin/gov/point/log/page', { params })

// 手动积分操作接口
export const changeRoomPoints = (data: {
  roomId: number
  userId: number
  amount: number
  isAdd: boolean
  reason: string
}) => 
  request.post('/admin/gov/point/change', data)

// 默认导出所有接口
export default {
  // 排行榜
  getRoomRanking,
  getRoomRankingDetail,
  getBuildingStats,
  getBuildingRoomRanking,
  
  // 规则管理
  getPointRulePage,
  getPointRuleDetail,
  savePointRule,
  deletePointRule,
  changePointRuleStatus,
  getPointRuleLogPage,
  executePointRule,
  
  // 缓存管理
  refreshAllCache,
  refreshRoomRankingCache,
  refreshBuildingStatsCache,
  removeRoomRankingCache,
  getCacheStatus,
  
  // 积分流水
  getPointLogPage,
  
  // 手动操作
  changeRoomPoints
}
