import request from '@/utils/request'

// 分页查询绑定申请
export const pageBindAudit = (data: any) => request.post('/admin/pms/bind/page', data)

// 查看绑定申请详情
export const getBindDetail = (id: number) => request.get(`/admin/pms/bind/detail?id=${id}`)

// 审核通过
export const approveBind = (data: { id: number }) => request.post('/admin/pms/bind/approve', data)

// 审核拒绝
export const rejectBind = (data: { id: number; remark: string }) => request.post('/admin/pms/bind/reject', data)
