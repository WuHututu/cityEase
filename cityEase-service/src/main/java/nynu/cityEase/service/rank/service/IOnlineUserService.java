package nynu.cityEase.service.rank.service;

import nynu.cityEase.api.vo.constants.RedisKeyConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.Set;

public interface IOnlineUserService {
//todo 定时清理不活跃用户数据

    /**
     * 获取当前在线人数
     * 定义：过去 30 分钟内有过操作的用户
     */
    public Long countOnlineUsers();

    /**
     * 获取指定用户的最后活跃时间
     */
    public Date getLastActiveTime(Long userId) ;

    /**
     * 判断用户是否在线
     */
    public boolean isOnline(Long userId) ;
    
    /**
     * (管理后台用) 获取所有在线用户的 ID 列表
     * 注意：如果用户量巨大，慎用此方法，建议分页 scan
     */
    public Set<String> listOnlineUserIds();
}