package nynu.cityEase.api.exception;

import lombok.Getter;
import nynu.cityEase.api.vo.Status;
import nynu.cityEase.api.vo.constants.StatusEnum;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/7
 * Time: 14:07
 * Description: TODO
 *
 * @author 90924
 */

public class CommunityException extends RuntimeException {
    @Getter
    Status status;

    public CommunityException(Status status) {
        super(status.getMsg());
        this.status = status;}

    public CommunityException(int code, String msg) {
        super(msg);
        this.status = Status.newStatus(code, msg);
    }

    public CommunityException(StatusEnum statusEnum, Object... args) {
        this(Status.newStatus(statusEnum, args));}
}
