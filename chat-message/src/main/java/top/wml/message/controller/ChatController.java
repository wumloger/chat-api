package top.wml.message.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import top.wml.common.entity.*;
import top.wml.common.exception.BusinessException;
import top.wml.common.resp.CommonResp;
import top.wml.common.utils.RedisUtil;
import top.wml.message.feign.FriendService;
import top.wml.message.feign.GroupService;
import top.wml.message.service.FriendMsgService;
import top.wml.message.service.GroupMsgService;
import top.wml.message.service.MsgUnreadRecordService;

import java.util.ArrayList;
import java.util.List;

@Controller()
public class ChatController {
    @Resource
    private FriendService friendService;

    @Resource
    private GroupService groupService;

    @Resource
    private SimpMessagingTemplate simpMessagingTemplate;

    @Resource
    private FriendMsgService friendMsgService;

    @Resource
    private GroupMsgService groupMsgService;

    @Resource
    private MsgUnreadRecordService msgUnreadRecordService;


    @Resource
    private RedisUtil redisUtil;

    @MessageMapping("/private/{userId}")
    public void sendPrivateMessage(@DestinationVariable String userId, FriendMsg message) {
        Friend friendById = friendService.getFriendById(message.getFromUserId(), message.getToUserId());
        if (friendById == null) {
            throw new BusinessException("你们还不是好友,无法发送消息");
        }
        if(friendById.getIsAttention() == 1){
            //走消息推送的逻辑
        }
        //记录消息到friendMsg表
        message.setCreateBy(message.getFromUserId());
        message.setUpdateBy(message.getFromUserId());
        message.setStatus((byte) 0);
        friendMsgService.save(message);
        //看下我有没有好友的消息列表
        LambdaQueryWrapper<MsgUnreadRecord> myWrapper = new LambdaQueryWrapper<>();
        myWrapper.eq(MsgUnreadRecord::getUserId, message.getFromUserId())
                .eq(MsgUnreadRecord::getTargetId, message.getToUserId());
        MsgUnreadRecord my = msgUnreadRecordService.getOne(myWrapper);
        //没有就创建
        if(my == null){
            my = new MsgUnreadRecord();
            my.setUserId(message.getFromUserId());
            my.setTargetId(message.getToUserId());
            my.setSource((byte) 0);
            my.setUnreadNum(1);
            my.setCreateBy(message.getFromUserId());
            my.setUpdateBy(message.getFromUserId());
            msgUnreadRecordService.save(my);
        }
        //消息列表更新，未读记录加1
        LambdaQueryWrapper<MsgUnreadRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MsgUnreadRecord::getUserId, message.getToUserId())
                .eq(MsgUnreadRecord::getTargetId, message.getFromUserId());
        MsgUnreadRecord one = msgUnreadRecordService.getOne(queryWrapper);
        //如果没有消息记录就创建一个
        if(one == null){
            one = new MsgUnreadRecord();
            one.setUserId(message.getToUserId());
            one.setTargetId(message.getFromUserId());
            one.setSource((byte) 0);
            one.setUnreadNum(1);
            one.setCreateBy(message.getToUserId());
            one.setUpdateBy(message.getToUserId());
            msgUnreadRecordService.save(one);
        }else{
            one.setUnreadNum(one.getUnreadNum() + 1);
            msgUnreadRecordService.updateById(one);
        }
        //跟新缓存
        redisUtil.delete("friendMsg:" + message.getFromUserId() + "-" + message.getToUserId());

        System.out.println("收到了来自"+ message.getFromUserId()+"消息:"+ message.getMsgContent() +"，转发到用户" + message.getToUserId());
        simpMessagingTemplate.convertAndSendToUser(userId, "/queue/private", message);

    }

    @MessageMapping("/groups/{groupId}")
    @SendTo("/topic/groups/{groupId}")
    public GroupMsg sendGroupMessage(@DestinationVariable String groupId, GroupMsg message) {
        GroupUser groupUserById = groupService.getGroupUserById(message.getGroupId(), message.getFromUserId());
        if(groupUserById == null){
            throw new BusinessException("不是该群的成员");
        }
        if(groupUserById.getAdminable() == 1){
            //走消息推送的逻辑
        }
        //存储群消息
        message.setStatus((byte) 0);
        message.setCreateBy(message.getFromUserId());
        message.setUpdateBy(message.getFromUserId());
        groupMsgService.save(message);

        //找到该群组的所有成员
        List<GroupUser> groupUserList = groupService.getGroupUserList(message.getGroupId());
        List<Long> ids = new ArrayList<>();
        groupUserList.forEach((item)->{
            ids.add(item.getUserId());
        });
        LambdaQueryWrapper<MsgUnreadRecord> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(MsgUnreadRecord::getTargetId,message.getGroupId())
                .in(MsgUnreadRecord::getUserId,ids);
        List<MsgUnreadRecord> updateList = msgUnreadRecordService.list(updateWrapper);
        //所有有记录的用户未读加1
        updateList.forEach((user)->{
            user.setUnreadNum(user.getUnreadNum() + 1);
            ids.remove(user.getUserId());
        });
        msgUnreadRecordService.updateBatchById(updateList);
        //剩下的都是没有消息列表的，创建一个
        ids.forEach((user)->{
            MsgUnreadRecord build = MsgUnreadRecord.builder()
                    .userId(user)
                    .targetId(message.getGroupId())
                    .source((byte) 1)
                    .unreadNum(1)
                    .createBy(message.getFromUserId())
                    .updateBy(message.getFromUserId())
                    .build();
            msgUnreadRecordService.save(build);
        });

        //更新缓存
        redisUtil.delete("groupMsg:" + message.getGroupId());
        // 逻辑上你可能需要验证发送者是否是该群的成员，但这只是一个简化的示例
        System.out.println("收到了用户" + message.getFromUserId() + "的"+message.getGroupId()+"群消息：" + message.getMsgContent());
        return message;
    }

}
