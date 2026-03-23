package nynu.cityEase.web.global;

import cn.dev33.satoken.exception.NotLoginException;
import nynu.cityEase.api.exception.CommunityException;
import nynu.cityEase.api.vo.ResVo;
import nynu.cityEase.api.vo.constants.StatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class CityEaseExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CityEaseExceptionHandler.class);

    @ExceptionHandler(CommunityException.class)
    public ResVo<String> handleCommunityException(CommunityException e, HttpServletRequest request) {
        log.warn("业务异常: requestUri={}, code={}, message={}",
                request.getRequestURI(), e.getStatus().getCode(), e.getStatus().getMsg());
        return ResVo.fail(e.getStatus());
    }

    @ExceptionHandler(NotLoginException.class)
    public ResVo<String> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        log.warn("用户未登录拦截: requestUri={}", request.getRequestURI());
        return ResVo.fail(StatusEnum.FORBID_NOTLOGIN);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResVo<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("上传文件超过大小限制: requestUri={}", request.getRequestURI());
        return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "上传文件不能超过 1048576 bytes");
    }

    @ExceptionHandler(Exception.class)
    public ResVo<String> handleException(Exception e, HttpServletRequest request) {
        log.error("系统未知异常: requestUri={}", request.getRequestURI(), e);
        return ResVo.fail(StatusEnum.UNEXPECT_ERROR);
    }
}
