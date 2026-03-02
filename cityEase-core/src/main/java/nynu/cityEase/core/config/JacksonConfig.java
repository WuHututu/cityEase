package nynu.cityEase.core.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 Jackson 序列化配置
 * 解决前端 JavaScript 接收雪花算法 Long 型 ID 时出现的精度丢失问题
 * 前端16位，雪花为19为，前端最后三位会被错误转为000
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 将 Long 类型统一序列化为 String
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            // 将基本数据类型 long 统一序列化为 String
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}