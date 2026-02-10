package nynu.cityEase.api.enums;

import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/2/10
 * Time: 17:05
 * Description: TODO
 */

public enum GenderEnum{
    MALE(1,"男"),
    FAMALE(2,"女"),
    UNKNOWN(0,"保密");
    
    private int sex;
    private String desc;

    GenderEnum(int sex, String desc) {
        this.sex = sex;
        this.desc = desc;
    }

    public static String sex(Integer sexId) {
        if (Objects.equals(sexId, 1)) {
            return MALE.name();
        } else if (Objects.equals(sexId, 2)) {
            return FAMALE.name();
        } else {
            return UNKNOWN.name();
        }
    }
}
