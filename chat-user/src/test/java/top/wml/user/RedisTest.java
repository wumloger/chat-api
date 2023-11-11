package top.wml.user;

import com.sun.jdi.PrimitiveValue;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.wml.common.entity.User;
import top.wml.common.utils.RedisUtil;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisUtil redisUtil;
    @Test
    void testRedis(){
        redisUtil.set("test",new User(),1000 * 60);
        System.out.println(redisUtil.get("test") instanceof User);
    }
}
