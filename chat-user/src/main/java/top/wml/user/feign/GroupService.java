package top.wml.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import top.wml.common.entity.Group;
import top.wml.common.entity.GroupUser;
import top.wml.common.entity.User;

import java.util.List;

@FeignClient(name = "group-service",path = "/group")
public interface GroupService {

    @PostMapping("/updateGroupUserInfo")
    Boolean updateUser(@RequestBody User user);

}
