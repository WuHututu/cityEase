package nynu.cityEase.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author 90924
 */
@Configuration
@ComponentScan("nynu.cityEase.service")
@MapperScan(basePackages = {
        "nynu.cityEase.service.**.repository.mapper",
        "nynu.cityEase.service.**.mapper"
})
public class ServiceAutoConfig {


}
