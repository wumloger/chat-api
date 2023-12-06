package top.wml.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("top.wml")
@MapperScan("top.wml.message.mapper")
@EnableFeignClients("top.wml.message.feign")
public class MessageApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageApplication.class,args);
        System.out.println("message-service启动成功！");
    }
}
