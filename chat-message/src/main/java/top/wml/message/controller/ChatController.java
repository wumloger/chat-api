package top.wml.message.controller;

import jakarta.annotation.Resource;
import org.springframework.messaging.converter.SimpleMessageConverter;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import top.wml.common.entity.FriendMsg;
import top.wml.common.entity.GroupMsg;

@Controller
public class ChatController {

//    @Autowired
//    private FriendService friendService; // 假设提供检查是否为好友的服务

    @Resource
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/private/{userId}")
    public void sendPrivateMessage(@DestinationVariable String userId, FriendMsg message) {
        // 逻辑上你可能需要验证发送者是否是该好友，但这只是一个简化的示例
        System.out.println("收到了来自"+ message.getFromUserId()+"消息:"+ message.getMsgContent() +"，转发到用户" + message.getToUserId());
        simpMessagingTemplate.convertAndSendToUser(userId, "/queue/private", message);

    }

    @MessageMapping("/groups/{groupId}")
    @SendTo("/topic/groups/{groupId}")
    public GroupMsg sendGroupMessage(@DestinationVariable String groupId, GroupMsg message) {
        // 逻辑上你可能需要验证发送者是否是该群的成员，但这只是一个简化的示例
        System.out.println("收到了用户" + message.getFromUserId() + "的"+message.getGroupId()+"群消息：" + message.getMsgContent());
        return message;
    }
}
