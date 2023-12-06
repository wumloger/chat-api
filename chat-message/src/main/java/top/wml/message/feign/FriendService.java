package top.wml.message.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import top.wml.common.entity.Friend;

@FeignClient(name = "friend-service",path = "/friend")
public interface FriendService {

    @GetMapping("/get/{userId}/{friendId}")
    Friend getFriendById(@PathVariable("userId") Long userId, @PathVariable("friendId") Long friendId);
}
