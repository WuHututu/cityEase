import request from '@/utils/request'

// 分页查询物业费账单
export const pageFeeBills = (data: any) => request.post('/admin/pms/fee/page', data)

// 查看账单详情
export const getFeeBillDetail = (id: number) => request.get(`/admin/pms/fee/detail/${id}`)

// 新增账单
export const saveFeeBill = (data: any) => request.post('/admin/pms/fee/save', data)

// 修改账单
export const updateFeeBill = (data: any) => request.post('/admin/pms/fee/update', data)

// 删除账单
export const deleteFeeBill = (id: number) => request.delete(`/admin/pms/fee/delete/${id}`)

// 批量生成账单
export const generateFeeBills = (data: { feeMonth: string; amount: number }) => request.post('/admin/pms/fee/generate', data)

// 标记为已缴费
export const markFeePaid = (data: { id: number }) => request.post('/admin/pms/fee/markPaid', data)

// 标记为未缴费
export const markFeeUnpaid = (id: number) => request.post(`/admin/pms/fee/markUnpaid/${id}`)
