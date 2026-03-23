import request from '@/utils/request'

// 分页查询公告列表（支持类型、状态、标题筛选）
export const pageNotices = (data: any): Promise<any> => request.post('/admin/system/notice/page', data)

// 查看公告详情
export const getNoticeDetail = (id: number): Promise<any> => request.get(`/admin/system/notice/detail/${id}`)

// 新增/修改公告
export const saveOrUpdateNotice = (data: any): Promise<any> => request.post('/admin/system/notice/save', data)

// 删除公告
export const deleteNotice = (id: number): Promise<any> => request.post(`/admin/system/notice/delete/${id}`)

// 切换置顶状态
export const toggleTop = (id: number): Promise<any> => request.post(`/admin/system/notice/toggleTop/${id}`)
