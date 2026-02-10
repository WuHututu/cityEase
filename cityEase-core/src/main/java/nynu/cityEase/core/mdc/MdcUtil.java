package nynu.cityEase.core.mdc;

import org.slf4j.MDC;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/1/27
 * Time: 16:56
 * Description: TODO
 */

public class MdcUtil {
    public static final String TRACE_ID_KEY = "traceId";

    //往MDC中添加一个键值对
    public static void add(String key, String val) {
        MDC.put(key, val);
    }

    //生成一个traceId并添加到MDC中
    public static void addTraceId() {
        // traceId的生成规则
        MDC.put(TRACE_ID_KEY, SelfTraceIdGenerator.generate());
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    //清除MDC中的所有信息，然后把traceId添加回去。
    public static void reset() {
        String traceId = MDC.get(TRACE_ID_KEY);
        //避免线程池线程复用携带脏数据
        MDC.clear();
        MDC.put(TRACE_ID_KEY, traceId);
    }

    //清除MDC中的所有信息
    public static void clear() {
        MDC.clear();
    }
}
