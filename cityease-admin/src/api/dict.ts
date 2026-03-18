import request from '@/utils/request'

// 获取字典类型列表
export const listTypes = () => request.get('/admin/system/dict/type/list')

// 根据 ID 获取字典类型
export const getType = (id: number) => request.get(`/admin/system/dict/type/${id}`)

// 新增字典类型
export const saveType = (data: any) => request.post('/admin/system/dict/type', data)

// 修改字典类型
export const updateType = (data: any) => request.put('/admin/system/dict/type', data)

// 删除字典类型
export const deleteType = (id: number) => request.delete(`/admin/system/dict/type/${id}`)

// 获取字典数据列表（带筛选）
export const listData = (params?: { dictType?: string; label?: string }) =>
    request.get('/admin/system/dict/data/list', { params })

// 根据字典类型获取字典数据（前端下拉框专用，带缓存）
export const getDataByType = (dictType: string) =>
    request.get(`/admin/system/dict/data/byType/${dictType}`)

// 根据 ID 获取字典数据
export const getData = (id: number) => request.get(`/admin/system/dict/data/${id}`)

// 新增字典数据
export const saveData = (data: any) => request.post('/admin/system/dict/data', data)

// 修改字典数据
export const updateData = (data: any) => request.put('/admin/system/dict/data', data)

// 删除字典数据
export const deleteData = (id: number) => request.delete(`/admin/system/dict/data/${id}`)
