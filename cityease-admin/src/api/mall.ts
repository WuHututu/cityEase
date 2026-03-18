import request from '@/utils/request'

export interface PointGoodsPageReq {
  current?: number
  size?: number
  keyword?: string
  status?: number | null
}

export interface PointGoodsSaveReq {
  id?: number | null
  name: string
  description?: string
  imageUrl?: string
  pointsPrice: string | number
  stock: number
  status: number
}

export const pageGoods = (data: PointGoodsPageReq) => request.post('/admin/mall/goods/page', data)

export const goodsDetail = (id: number | string) => request.get('/admin/mall/goods/detail', { params: { id } })

export const saveGoods = (data: PointGoodsSaveReq) => request.post('/admin/mall/goods/save', data)

export const deleteGoods = (id: number | string) => request.post('/admin/mall/goods/delete', null, { params: { id } })

export const changeGoodsStatus = (id: number | string, status: number) =>
  request.post('/admin/mall/goods/status', null, { params: { id, status } })
