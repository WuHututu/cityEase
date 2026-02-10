package nynu.cityEase.service.rank.service.impl;

import nynu.cityEase.api.vo.constants.RedisKeyConstants;
import nynu.cityEase.service.rank.service.IOnlineUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/10
 * Time: 14:54
 * Description: TODO
 */
@Service("OnlineUserServiceImpl")
public class OnlineUserServiceImpl implements IOnlineUserService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public Long countOnlineUsers() {
        {
            long now = System.currentTimeMillis();
            long min = now - RedisKeyConstants.OFFLINE_THRESHOLD_MILLIS;
            // ZCOUNT key min max
            return redisTemplate.opsForZSet().count(RedisKeyConstants.ONLINE_USER_KEY, min, Double.MAX_VALUE);
        }
    }

    @Override
    public Date getLastActiveTime(Long userId){
        // ZSCORE key member
        Double score = redisTemplate.opsForZSet().score(RedisKeyConstants.ONLINE_USER_KEY, String.valueOf(userId));
        if (score == null) {
            // 用户从未上线或已过期清理
            return null;
        }
        return new Date(score.longValue());
    }

    @Override
    public boolean isOnline(Long userId){
        Date lastActive = getLastActiveTime(userId);
        if (lastActive == null) {
            return false;
        }
        // 判断最后活跃时间是否在 30 分钟内
        return System.currentTimeMillis() - lastActive.getTime() < RedisKeyConstants.OFFLINE_THRESHOLD_MILLIS;
    }

    @Override
    public Set<String> listOnlineUserIds(){
        long now = System.currentTimeMillis();
        long min = now - RedisKeyConstants.OFFLINE_THRESHOLD_MILLIS;
        // ZRANGEBYSCORE key min max
        return redisTemplate.opsForZSet().rangeByScore(RedisKeyConstants.ONLINE_USER_KEY, min, Double.MAX_VALUE);
    }
}
