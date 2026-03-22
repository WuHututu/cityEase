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

    /**
     * 社区公告详情
     */
    public static final String NOTICE_DETAIL_KEY= "cityease:notice:detail:";

    /**
     * 后台首页核心指标数据
     */
    public static final String DASHBOARD_METRICS_KEY = "cityease:dashboard:metrics";

    /**
     * 积分排行榜缓存
     */
    public static final String GOV_POINT_ROOM_RANKING_KEY = "gov:point:ranking:room";
    public static final String GOV_POINT_BUILDING_STATS_KEY = "gov:point:ranking:building:stats";
    public static final String GOV_POINT_BUILDING_ROOM_RANKING_KEY_PREFIX = "gov:point:ranking:building:";

}
