package top.wml.friend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import top.wml.common.utils.PinyinUtil;


@SpringBootApplication
@ComponentScan("top.wml")
@MapperScan("top.wml.friend.mapper")
@EnableFeignClients("top.wml.friend.openfeign")
public class FriendApplication {
    public static void main(String[] args) {
        SpringApplication.run(FriendApplication.class,args);
        System.out.println("friend-service启动成功！");
    }
}
