import request from '@/utils/request'

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface PointOverviewVO {
  totalGranted: number
  totalConsumed: number
  participantUsers: number
  todayNetChange: number
}

export interface PointTrendVO {
  dates: string[]
  grantSeries: number[]
  consumeSeries: number[]
  netSeries: number[]
}

export interface PointRankingVO {
  ranking: number
  roomId: number
  roomNum: string
  areaId: number
  areaName: string
  pointsBalance: number
  rankingChange: number
  updateTime: string
}

export interface BuildingPointStatsVO {
  buildingRanking: number
  areaId: number
  areaName: string
  totalPoints: number
  roomCount: number
  avgPoints: number
  maxPoints: number
  minPoints: number
  updateTime: string
}

export interface CacheStatusResult {
  status: string
  healthy: boolean
}

export interface GovPointLogVO {
  id: number
  roomId: number
  userId: number
  changeType: number
  changeAmount: number
  afterBalance: number
  reason: string
  createTime: string
}

export interface PointRuleVO {
  id: number
  ruleName: string
  ruleType: number
  ruleTypeName: string
  triggerCondition?: string
  pointsAmount: number
  maxDailyTimes?: number | null
  maxMonthlyTimes?: number | null
  status: number
  statusName: string
  description?: string
  sortOrder?: number
  createTime?: string
  updateTime?: string
}

export interface PointRuleLogVO {
  id: number
  ruleId: number
  ruleName: string
  roomId: number
  roomNum: string
  userId: number
  triggerEvent: string
  triggerData?: string
  pointsAwarded: number
  executionResult: number
  executionResultName: string
  failureReason?: string
  createTime: string
}

export interface PointRuleQuery {
  current?: number
  size?: number
  ruleType?: number | null
  status?: number | null
}

export interface PointRuleLogQuery {
  current?: number
  size?: number
  ruleId?: number | null
  roomId?: number | null
  executionResult?: number | null
}

export interface PointRuleSaveReq {
  id?: number | null
  ruleName: string
  ruleType: number | null
  triggerCondition?: string
  pointsAmount: number | null
  maxDailyTimes?: number | null
  maxMonthlyTimes?: number | null
  status: number
  description?: string
  sortOrder?: number | null
}

export interface PointLogQuery {
  current?: number
  size?: number
  roomId?: number | null
  userId?: number | null
  changeType?: number | null
}

export const getPointOverview = () => request.get('/admin/gov/point/overview') as Promise<PointOverviewVO>

export const getPointTrend = (days = 7) =>
  request.get('/admin/gov/point/trend', { params: { days } }) as Promise<PointTrendVO>

export const getRoomRanking = (limit = 200) =>
  request.get('/admin/gov/ranking/room', { params: { limit } }) as Promise<PointRankingVO[]>

export const getRoomRankingDetail = (roomId: number | string) =>
  request.get(`/admin/gov/ranking/room/${roomId}`) as Promise<PointRankingVO>

export const getBuildingStats = () => request.get('/admin/gov/ranking/building') as Promise<BuildingPointStatsVO[]>

export const getBuildingRoomRanking = (areaId: number | string, limit = 200) =>
  request.get(`/admin/gov/ranking/building/${areaId}/rooms`, { params: { limit } }) as Promise<PointRankingVO[]>

export const refreshAllCache = () => request.post('/admin/gov/point/cache/refresh') as Promise<string>

export const clearAllCache = () => request.post('/admin/gov/point/cache/clear') as Promise<string>

export const getCacheStatus = () => request.get('/admin/gov/point/cache/status') as Promise<CacheStatusResult>

export const getPointLogPage = (params: PointLogQuery) =>
  request.get('/admin/gov/point/log/page', { params }) as Promise<PageResult<GovPointLogVO>>

export const getLatestPointLogs = (limit = 20) =>
  request.get('/admin/gov/point/log/latest', { params: { limit } }) as Promise<GovPointLogVO[]>

export const getPointRulePage = (params: PointRuleQuery) =>
  request.get('/admin/gov/rule/page', { params }) as Promise<PageResult<PointRuleVO>>

export const savePointRule = (data: PointRuleSaveReq) =>
  request.post('/admin/gov/rule/save', data) as Promise<boolean>

export const deletePointRule = (id: number | string) =>
  request.post('/admin/gov/rule/delete', null, { params: { id } }) as Promise<boolean>

export const changePointRuleStatus = (id: number | string, status: number) =>
  request.post('/admin/gov/rule/status', null, { params: { id, status } }) as Promise<boolean>

export const getPointRuleLogPage = (params: PointRuleLogQuery) =>
  request.get('/admin/gov/rule/log/page', { params }) as Promise<PageResult<PointRuleLogVO>>
