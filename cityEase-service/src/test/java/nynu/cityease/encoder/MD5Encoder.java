package nynu.cityease.encoder;

import nynu.cityEase.service.user.help.UserInfoEncoder;
import java.lang.reflect.Field;

public class MD5Encoder {
    public static void main(String[] args) throws Exception { // 注意抛出异常
        UserInfoEncoder userInfoEncoder = new UserInfoEncoder();

        // 1. 设置 saltIndex
        Field saltIndexField = UserInfoEncoder.class.getDeclaredField("saltIndex");
        saltIndexField.setAccessible(true);
        saltIndexField.set(userInfoEncoder, 3); // 模拟配置文件里的 3

        // 2. 设置 salt
        Field saltField = UserInfoEncoder.class.getDeclaredField("salt");
        saltField.setAccessible(true);
        saltField.set(userInfoEncoder, "city_ease"); // 模拟配置文件里的值
        // --- 手动注入结束 ---

        String s = userInfoEncoder.encInfo("15538020575");
        System.out.println("加密后: " + s);
        System.out.println("匹配结果: " + userInfoEncoder.match("15538020575", s));
    }
}