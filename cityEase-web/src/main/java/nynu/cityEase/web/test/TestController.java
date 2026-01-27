package nynu.cityEase.web.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA.
 * User: 90924
 * Date: 2026/1/27
 * Time: 13:29
 * Description: TODO
 */
@RestController
@RequestMapping("/test")
public class TestController{
    @RequestMapping
    public String test(){
        return "hello world";
    }
}
