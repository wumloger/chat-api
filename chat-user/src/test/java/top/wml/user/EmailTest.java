package top.wml.user;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.wml.user.service.EmailService;

@SpringBootTest
public class EmailTest {

    @Resource
    private EmailService emailService;
    @Test
    void emailTest(){
        emailService.sendEmail("1208361250@qq.com","test","test");
    }
}
