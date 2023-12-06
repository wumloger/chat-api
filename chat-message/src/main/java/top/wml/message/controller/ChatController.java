package top.wml.message.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

@Controller("message")
public class ChatController {

    @Resource
    private FriendService friendService;
    @Resource
    private FriendMsgService friendMsgService;

    @Resource
    private GroupMsgService groupMsgService;

    @Resource
    private GroupService groupService;

    @Resource
    private MsgUnreadRecordService msgUnreadRecordService;

    @Resource
    private SimpMessagingTemplate simpMessagingTemplate;

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

        //消息列表更新，未读记录加1
        LambdaQueryWrapper<MsgUnreadRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MsgUnreadRecord::getUserId, message.getToUserId())
                .eq(MsgUnreadRecord::getTargetId, message.getFromUserId());
        MsgUnreadRecord one = msgUnreadRecordService.getOne(queryWrapper);
        //如果没有消息记录就创建一个
        if(one == null){
            one = new MsgUnreadRecord();
            one.setUserId(message.getFromUserId());
            one.setTargetId(message.getToUserId());
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

    //获取所有未读消息列表
    @GetMapping("/getUnread/userid")
    public CommonResp<List<MsgUnreadRecord>> getMsgUnreadRecord(@PathVariable Long userId){
        LambdaQueryWrapper<MsgUnreadRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MsgUnreadRecord::getUserId,userId);
        List<MsgUnreadRecord> list = msgUnreadRecordService.list(wrapper);
        CommonResp<List<MsgUnreadRecord>> resp = new CommonResp<>();
        resp.success(list);
        return resp;
    }

    //删除消息列表
    @DeleteMapping("/deleteUnread/{id}")
    public CommonResp<Boolean> deleteMsgUnreadRecord(@PathVariable Long id){
        boolean b = msgUnreadRecordService.removeById(id);
        CommonResp<Boolean> resp = new CommonResp<>();
        resp.success(b);
        return resp;
    }
    @GetMapping("/getFriendMsg/{userId}/{friendId}/{page}")
    public CommonResp<Page<FriendMsg>> getFriendMsgList(@PathVariable Long userId,@PathVariable Long friendId,@PathVariable int page){
        CommonResp<Page<FriendMsg>> resp = new CommonResp<>();
        if(redisUtil.get("friendMsg:" + userId + "-" + friendId) != null && page == 1){
            resp.success((Page<FriendMsg>) redisUtil.get("friendMsg:" + userId + "-" + friendId));
            return resp;
        }
        //每次查出50条
        LambdaQueryWrapper<FriendMsg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendMsg::getFromUserId,userId).eq(FriendMsg::getToUserId,friendId).eq(FriendMsg::getStatus,0)
                .or()
                .eq(FriendMsg::getFromUserId,friendId).eq(FriendMsg::getToUserId,userId).eq(FriendMsg::getStatus,0);
        Page<FriendMsg> list = friendMsgService.page(new Page<>(page, 50), wrapper);
        //只缓存最新的数据
        if(page == 1){
            //缓存到redis中
            redisUtil.set("friendMsg:" + userId + "-" + friendId,list,1000 * 60 * 60 * 24 * 7);
        }

        resp.success(list);
        return resp;
    }
    @GetMapping("/getGroupMsg/{groupId}/{userId}/{page}")
    public CommonResp<Page<GroupMsg>> getGroupMsgList(@PathVariable Long groupId,@PathVariable int page){
        CommonResp<Page<GroupMsg>> resp = new CommonResp<>();
        if(redisUtil.get("groupMsg:" + groupId) != null && page == 1){
            resp.success((Page<GroupMsg>) redisUtil.get("groupMsg:" + groupId));
            return resp;
        }
        //每次查出50条
        LambdaQueryWrapper<GroupMsg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMsg::getGroupId,groupId).eq(GroupMsg::getStatus,0);

        Page<GroupMsg> list = groupMsgService.page(new Page<>(page, 100), wrapper);
        if(page == 1){
            //缓存到redis中
            redisUtil.set("groupMsg:" + groupId,list,1000 * 60 * 60 * 24 * 7);
        }
        resp.success(list);
        return resp;
    }

    @PostMapping("/back/{type}/{id}")
    public CommonResp<Boolean> backMsg(byte type,Long id){
        //0是好友消息撤回，1是群组消息撤回
        CommonResp<Boolean> resp = new CommonResp<>();
        if(type == 0){
            FriendMsg byId = friendMsgService.getById(id);
            byId.setStatus((byte) 1);
            boolean b = friendMsgService.saveOrUpdate(byId);
            resp.success(b);
            return resp;
        }
        GroupMsg byId = groupMsgService.getById(id);
        byId.setStatus((byte) 1);
        boolean b = groupMsgService.saveOrUpdate(byId);
        resp.success(b);
        return resp;
    }
}
