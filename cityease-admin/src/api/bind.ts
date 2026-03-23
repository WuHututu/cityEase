import request from '@/utils/request'

export interface BindAuditQuery {
  pageNo: number
  pageSize: number
  status?: number
  relationType?: number
}

export interface BindAuditRecord {
  relId: number
  userId: number
  userName?: string
  roomId: number
  fullRoomName?: string
  relationType: number
  status: number
  attachmentsList?: string[]
  createTime?: string
}

export const pageBindAudit = (data: BindAuditQuery) => request.post('/admin/pms/userRoom/page', data)

export const approveBind = (relId: number) =>
  request.post('/admin/pms/userRoom/audit', { relId, status: 1 })

export const rejectBind = (relId: number, remark: string) =>
  request.post('/admin/pms/userRoom/audit', { relId, status: 2, remark })
