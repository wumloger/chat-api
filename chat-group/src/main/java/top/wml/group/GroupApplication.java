package top.wml.group;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("top.wml")
@MapperScan("top.wml.group.mapper")
//@EnableFeignClients("top.wml.friend.openfeign")
public class GroupApplication {
    public static void main(String[] args) {
        SpringApplication.run(GroupApplication.class,args);
        System.out.println("group-service启动成功!");
    }

}
