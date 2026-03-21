import request from '@/utils/request'

// 获取用户信息
export const getUserInfo = () => {
  return request.get('/app/user/info')
}

// 获取我的房屋列表
export const getMyRooms = () => {
  return request.get('/app/user/rooms')
}

// 获取当前绑定的房屋
export const getCurrentRoom = () => {
  return request.get('/app/user/current-room')
}

// 绑定房屋
export const bindRoom = (roomId: number) => {
  return request.post('/app/user/bind-room', null, {
    params: { roomId }
  })
}

// 切换当前房屋
export const switchRoom = (roomId: number) => {
  return request.post('/app/user/switch-room', null, {
    params: { roomId }
  })
}
