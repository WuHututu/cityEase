import request from '@/utils/request'

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

export const getPointOverview = () => request.get('/admin/gov/point/overview')

export const getPointTrend = (days = 7) => request.get('/admin/gov/point/trend', { params: { days } })
