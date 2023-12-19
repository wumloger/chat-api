package top.wml.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import top.wml.common.entity.Friend;
import top.wml.common.entity.User;

@FeignClient(name = "friend-service",path = "/friend")
public interface FriendService {

    @PutMapping("/updateFriendInfo")
    Boolean updateFriendInfo(@RequestBody User user);
}
