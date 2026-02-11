package nynu.cityEase.api.vo.constants;

public class RedisKeyConstants {
    /**
     * 在线用户 ZSet Key
     * Member: userId
     * Score: timestamp (最后活跃时间)
     */
    public static final String ONLINE_USER_KEY = "system:online:users";
    
    /**
     * 判定离线的阈值（例如 30 分钟无操作视为离线）
     */
    public static final long OFFLINE_THRESHOLD_MILLIS = 30 * 60 * 1000L;

    /**
     * 社区设施树状结构
     */
    public static final String PUBLIC_AREA_TREE_KEY="pms:area:tree";
}