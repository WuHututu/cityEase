package nynu.cityEase.api.exception;

import nynu.cityEase.api.vo.constants.StatusEnum;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/7
 * Time: 14:05
 * Description: TODO
 *
 * @author 90924
 */

public class ExceptionUtil {
    public static CommunityException of(StatusEnum statusEnum, Object... args) {
        // 处理可能为null的args参数，避免String.format出现异常
        return new CommunityException(statusEnum, args);
    }
}
