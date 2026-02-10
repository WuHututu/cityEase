package nynu.cityEase.web;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/1/27
 * Time: 13:23
 * Description: TODO
 */
@SpringBootApplication(scanBasePackages = "nynu.cityEase")
@EnableAspectJAutoProxy
@ServletComponentScan
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
