package top.wml.friend.openfeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import top.wml.common.entity.User;

@FeignClient("user-service")
public interface UserService {

 @GetMapping("/getUserById")
 User getUserById(@RequestParam Long id);

}
