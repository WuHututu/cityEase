package nynu.cityEase.core.mdc;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/1/27
 * Time: 16:55
 * Description: MDC日志切面，用于记录方法执行耗时
 */
@Aspect
@Slf4j
@Component
public class MdcAspect {

    //定义切点 -通知应用的范围
    @Pointcut("@annotation(MdcDot) || @within(MdcDot)")
    public void getLogAnnotation() {
    }

    //通知
    @Around("getLogAnnotation()")
    public Object handle(ProceedingJoinPoint pjPoint) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            // 先执行原方法
            Object result = pjPoint.proceed();

            // 计算执行时间
            long executionTime = System.currentTimeMillis() - start;

            log.info("执行耗时: {}#{} = {}ms",
                    pjPoint.getSignature().getDeclaringType().getSimpleName(),
                    pjPoint.getSignature().getName(),
                    executionTime);

            return result;
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - start;
            log.error("执行异常耗时: {}#{} = {}ms",
                    pjPoint.getSignature().getDeclaringType().getSimpleName(),
                    pjPoint.getSignature().getName(),
                    executionTime, throwable);
            throw throwable;
        } finally {
            long totalTime = System.currentTimeMillis() - start;
            log.debug("方法执行完成，总耗时: {}ms", totalTime);
        }
    }
}
