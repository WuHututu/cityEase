import request from '@/utils/request'

// 分页获取工单列表
export const getRepairPage = (data: any) => request.post('/admin/pms/repair/page', data)

// 获取工单详情
export const getRepairDetail = (data: { orderId: number }) => request.post('/admin/pms/repair/detail', data)

// 派发工单
export const dispatchOrder = (data: { orderId: number; handlerId: number }) =>
    request.post('/admin/pms/repair/dispatch', data)

// 提交处理结果
export const completeOrder = (data: { orderId: number; handleResult: string; handleImages?: string[] }) =>
    request.post('/admin/pms/repair/complete', data)

// 获取可派单维修人员列表
export const getHandlers = () => request.get('/admin/pms/repair/handlers')

// 获取字典数据（报修类型）
export const getDictDataByType = (dictType: string) => request.get(`/admin/system/dict/data/byType/${dictType}`)
