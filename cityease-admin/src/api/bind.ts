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

export interface BindAuditDetail extends BindAuditRecord {
  phone?: string
  auditId?: number
  auditorName?: string
  auditTime?: string
  remark?: string
}

export const pageBindAudit = (data: BindAuditQuery): Promise<any> =>
  request.post('/admin/pms/userRoom/page', data)

export const getBindDetail = (relId: number): Promise<BindAuditDetail> =>
  request.get('/admin/pms/userRoom/detail', { params: { relId } })

export const approveBind = (relId: number): Promise<any> =>
  request.post('/admin/pms/userRoom/audit', { relId, status: 1 })

export const rejectBind = (relId: number, remark: string): Promise<any> =>
  request.post('/admin/pms/userRoom/audit', { relId, status: 2, remark })

export const updateBindAttachments = (relId: number, attachments: string[]): Promise<any> =>
  request.post('/admin/pms/userRoom/attachments', { relId, attachments })
