package nynu.cityEase.web.hook.filter;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.StopWatch;
import lombok.extern.slf4j.Slf4j;
import nynu.cityEase.api.context.ReqInfoContext;
import nynu.cityEase.api.vo.constants.RedisKeyConstants;
import nynu.cityEase.core.mdc.MdcUtil;
import nynu.cityEase.core.util.CrossUtil;
import nynu.cityEase.core.util.IpUtil;
import nynu.cityEase.web.global.GlobalInitService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/10
 * Time: 13:41
 * Description: TODO
 */
@Slf4j
@WebFilter(urlPatterns = "/*", filterName = "reqRecordFilter", asyncSupported = true)
public class ReqRecordFilter implements Filter {
    private static Logger REQ_LOG = LoggerFactory.getLogger("req");
    /**
     * 返回给前端的traceId，用于日志追踪
     */
    private static final String GLOBAL_TRACE_ID_HEADER = "g-trace-id";
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GlobalInitService globalInitService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        long start = System.currentTimeMillis();
        HttpServletRequest request = null;
        StopWatch stopWatch = new StopWatch("请求耗时");
        try {
            stopWatch.start("请求参数构建");
            request = this.initReqInfo((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);
            stopWatch.stop();
            stopWatch.start("cors");
            //跨域处理
            CrossUtil.buildCors(request, (HttpServletResponse) servletResponse);
            stopWatch.stop();
            stopWatch.start("业务执行");
            //请求继续进行
            filterChain.doFilter(request, servletResponse);
        } finally {
            if (stopWatch.isRunning()) {
                // 避免doFitler执行异常，导致上面的 stopWatch无法结束，这里先首当结束一下上次的计数
                stopWatch.stop();
            }
            stopWatch.start("输出请求日志");
            //拼接请求信息，创建log
            buildRequestLog(ReqInfoContext.getReqInfo(), request, System.currentTimeMillis() - start);
            // 一个链路请求完毕，清空MDC相关的变量(如GlobalTraceId，用户信息)
            MdcUtil.clear();
            ReqInfoContext.clear();
            stopWatch.stop();

            if (!isStaticURI(request)) {
                log.info("{} - cost:\n{}", request.getRequestURI(), stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
            }
        }
    }

    private HttpServletRequest initReqInfo(HttpServletRequest request, HttpServletResponse response) {
        // 静态资源直接放行
        if (isStaticURI(request)) {
            return request;
        }
        StopWatch stopWatch = new StopWatch("请求参数构建");
        try {
            stopWatch.start("traceId");
            // 添加全链路的traceId
            MdcUtil.addTraceId();
            stopWatch.stop();

            try {
                stopWatch.start("请求基本信息");
                //在线状态统计
                if (StpUtil.isLogin()) {
                    String userId = StpUtil.getLoginIdAsString();
                    long now = System.currentTimeMillis();

                    // 更新 ZSet：ZADD app:online:users <now> <userId>
                    redisTemplate.opsForZSet().add(RedisKeyConstants.ONLINE_USER_KEY, userId, now);
                    //24h内无用户使用清空统计，释放内存
                    redisTemplate.expire(RedisKeyConstants.ONLINE_USER_KEY, 24, TimeUnit.HOURS);
                }
            } catch (Exception e) {
                log.error("更新在线状态失败", e);
            }

            //保存IP、User-Agent、设备 ID，不用重复获取 ThreadLocal
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            String forwardedHost = request.getHeader("X-Forwarded-Host");
            String hostHeader = request.getHeader("host");

            if (StringUtils.isNotBlank(forwardedHost)) {
                // 需要配合修改nginx的转发，添加  proxy_set_header X-Forwarded-Host $host;
                reqInfo.setHost(forwardedHost);
            } else if (StringUtils.isNotBlank(hostHeader)) {
                reqInfo.setHost(hostHeader);
            } else {
                URL reqUrl = new URL(request.getRequestURL().toString());
                reqInfo.setHost(reqUrl.getHost());
            }
            //请求路径
            reqInfo.setPath(request.getPathInfo());
            if (reqInfo.getPath() == null) {
                String url = request.getRequestURI();
                int index = url.indexOf("?");
                if (index > 0) {
                    url = url.substring(0, index);
                }
                reqInfo.setPath(url);
            }
            reqInfo.setPath(request.getRequestURI());
            reqInfo.setClientIp(IpUtil.getClientIp(request));
            reqInfo.setReferer(request.getHeader("referer"));
            reqInfo.setUserAgent(request.getHeader("User-Agent"));
            reqInfo.setDeviceId(getOrInitDeviceId(request, response));

            //请求封装
            request = this.wrapperRequest(request, reqInfo);
            stopWatch.stop();

            stopWatch.start("登录用户信息");
            globalInitService.initLoginUser(reqInfo);

            stopWatch.stop();
            ReqInfoContext.addReqInfo(reqInfo);

            stopWatch.start("回写traceId");
            // 返回头中记录traceId
            response.setHeader(GLOBAL_TRACE_ID_HEADER, Optional.ofNullable(MdcUtil.getTraceId()).orElse(""));
            stopWatch.stop();

        } catch (Exception e) {
            log.error("init reqInfo error!", e);
        } finally {
            log.info("{} -> 请求构建耗时: \n{}", request.getRequestURI(), stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
        }
        return request;
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

    /**
     * 初始化设备id (无 Cookie 版)
     */
    private String getOrInitDeviceId(HttpServletRequest request, HttpServletResponse response) {
        // 尝试从 Request Header 中获取
        String deviceId = request.getHeader("X-Device-Id");

        // 兼容性逻辑：尝试从 URL 参数获取
        if (StringUtils.isBlank(deviceId)) {
            deviceId = request.getParameter("deviceId");
        }

        // 空说明是新设备 或清理了缓存
        if (StringUtils.isBlank(deviceId) || "null".equalsIgnoreCase(deviceId)) {
            // 生成一个新的 ID
            deviceId = UUID.randomUUID().toString();

            // 【关键点】将新 ID 放入 Response Header，告诉前端："这是你的新身份证，收好！"
            if (response != null) {
                // 前端需要读取这个 Header，并存入 LocalStorage
                response.setHeader("X-Device-Id", deviceId);
                // 必须允许前端读取这个 Header (解决跨域问题)
                response.setHeader("Access-Control-Expose-Headers", "X-Device-Id");
            }
        }

        return deviceId;
    }

    private boolean isStaticURI(HttpServletRequest request) {
        return request == null
                || request.getRequestURI().endsWith("css")
                || request.getRequestURI().endsWith("js")
                || request.getRequestURI().endsWith("png")
                || request.getRequestURI().endsWith("ico")
                || request.getRequestURI().endsWith("gif")
                || request.getRequestURI().endsWith("svg")
                || request.getRequestURI().endsWith("min.js.map")
                || request.getRequestURI().endsWith("min.css.map");
    }

    /**
     * 原生的 HttpServletRequest 的输入流（Post 请求体）一旦被读取就失效了
     * Filter 在这里使用 BodyReaderHttpServletRequestWrapper 包装了请求，缓存了 Body 内容
     * 这样后续的 Controller 依然能再次读取到 JSON 数据
     */
    private HttpServletRequest wrapperRequest(HttpServletRequest request, ReqInfoContext.ReqInfo reqInfo) {
        if (!HttpMethod.POST.name().equalsIgnoreCase(request.getMethod())) {
            return request;
        }

        BodyReaderHttpServletRequestWrapper requestWrapper = new BodyReaderHttpServletRequestWrapper(request);
        reqInfo.setPayload(requestWrapper.getBodyString());
        return requestWrapper;
    }

    private void buildRequestLog(ReqInfoContext.ReqInfo req, HttpServletRequest request, long costTime) {
        if (req == null || isStaticURI(request)) {
            return;
        }

        StringBuilder msg = new StringBuilder();
        msg.append("method=").append(request.getMethod()).append("; ");
        if (StringUtils.isNotBlank(req.getReferer())) {
            msg.append("referer=").append(URLDecoder.decode(req.getReferer())).append("; ");
        }
        msg.append("remoteIp=").append(req.getClientIp());
        msg.append("; agent=").append(req.getUserAgent());

        if (req.getUserId() != null) {
            // 打印用户信息
            msg.append("; user=").append(req.getUserId());
        }

        msg.append("; uri=").append(request.getRequestURI());
        if (StringUtils.isNotBlank(request.getQueryString())) {
            msg.append('?').append(URLDecoder.decode(request.getQueryString()));
        }

        msg.append("; payload=").append(req.getPayload());
        msg.append("; cost=").append(costTime);
        REQ_LOG.info("{}", msg);

        // 保存请求计数
//        statisticsSettingService.saveRequestCount(req.getClientIp());
    }
}

