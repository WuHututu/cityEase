package nynu.cityEase.core.mdc;

import java.lang.annotation.*;

/**
 * 上下文诊断映射
 * bizCode -业务代号/代码
 * Target -被注解的方法 和 在被注解类内的方法
 * @author 90924
 */
@Documented
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface   MdcDot {
    String bizCode() default "";
}
