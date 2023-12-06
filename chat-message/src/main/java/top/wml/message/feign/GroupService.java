package top.wml.message.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import top.wml.common.entity.Friend;
import top.wml.common.entity.GroupUser;

import java.util.List;

@FeignClient(name = "group-service",path = "/group")
public interface GroupService {

    @GetMapping("/get/{groupId}/{userId}")
    GroupUser getGroupUserById(@PathVariable Long groupId, @PathVariable Long userId);

    @GetMapping("/list/{groupId}")
    List<GroupUser> getGroupUserList(@PathVariable Long groupId);

}
