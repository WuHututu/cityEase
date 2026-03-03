package nynu.cityEase.web.global;

import cn.dev33.satoken.exception.NotLoginException;
import nynu.cityEase.api.exception.CommunityException;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.constants.StatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 统一全局异常处理
 * 拦截Controller层抛出的所有异常，封装为标准JSON格式返回给前端
 */
@RestControllerAdvice
public class CityEaseExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CityEaseExceptionHandler.class);

    /**
     * 拦截业务自定义异常 (如：积分不足、防重复提交等我们之前抛出的异常)
     */
    @ExceptionHandler(CommunityException.class)
    public ResVo<String> handleCommunityException(CommunityException e, HttpServletRequest request) {
        // 业务异常通常是用户操作不当引起的，打印普通警告日志即可
        log.warn("业务异常: 请求接口 [{}], 错误代码: {}, 错误信息: {}", 
                request.getRequestURI(), e.getStatus().getCode(), e.getStatus().getMsg());
        
        return ResVo.fail(e.getStatus());
    }

    /**
     * 专门拦截 Sa-Token 的未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public ResVo<String> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        // 这是一个正常的业务拦截，打印 info 或 warn 级别即可，绝对不要把 e 传进去打印堆栈
        log.warn("用户未登录拦截: 请求接口 [{}]", request.getRequestURI());

        // 返回你枚举里定义好的未登录状态码
        return ResVo.fail(StatusEnum.FORBID_NOTLOGIN);
    }

    /**
     * 拦截系统未知异常 (如空指针、数据库宕机、SQL语法错误等)
     */
    @ExceptionHandler(Exception.class)
    public ResVo<String> handleException(Exception e, HttpServletRequest request) {
        // 未知异常属于系统级Bug，必须打印完整的堆栈信息(error级别)，并触发告警
        log.error("系统未知异常: 请求接口 [{}]", request.getRequestURI(), e);
        
        // 绝对不能把包含代码行数、SQL语句的堆栈信息返回给前端（存在安全漏洞）
        // 统一包装为“系统异常，请联系管理员”
        return ResVo.fail(StatusEnum.UNEXPECT_ERROR);
    }
}