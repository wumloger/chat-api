package top.wml.message.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import top.wml.common.entity.*;
import top.wml.common.resp.CommonResp;
import top.wml.common.resp.ExchangeResp;
import top.wml.common.utils.RedisUtil;
import top.wml.message.feign.FriendService;
import top.wml.message.feign.GroupService;
import top.wml.message.service.FriendMsgService;
import top.wml.message.service.GroupMsgService;
import top.wml.message.service.MsgUnreadRecordService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {


    @Resource
    private FriendMsgService friendMsgService;

    @Resource
    private GroupMsgService groupMsgService;
    @Resource
    private GroupService groupService;

    @Resource
    private MsgUnreadRecordService msgUnreadRecordService;

    @Resource
    private FriendService friendService;
    @Resource
    private RedisUtil redisUtil;

    //获取所有未读消息列表
    @GetMapping("/getUnread/{userId}")
    public CommonResp<List<ExchangeResp>> getMsgUnreadRecord(@PathVariable Long userId){
        LambdaQueryWrapper<MsgUnreadRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MsgUnreadRecord::getUserId,userId);
        List<MsgUnreadRecord> list = msgUnreadRecordService.list(wrapper);
        List<ExchangeResp> exchangeResps = new ArrayList<>();
        list.forEach((record)->{
            ExchangeResp exchangeResp = new ExchangeResp();
            BeanUtils.copyProperties(record,exchangeResp);
            if(record.getSource() == 1){
                Group groupInfoById = groupService.getGroupInfoById(record.getTargetId());
                GroupUser groupUserById = groupService.getGroupUserById(record.getTargetId(), record.getUserId());
                //如果群聊不存在，就删掉
                if(groupInfoById == null || groupUserById == null){
                    msgUnreadRecordService.removeById(record);
                }else{
                    exchangeResp.setTargetName(groupInfoById.getName());
                    exchangeResp.setTargetAvatar(groupInfoById.getAvatar());
                    LambdaQueryWrapper<GroupMsg> msgWrapper = new LambdaQueryWrapper<>();
                    msgWrapper.eq(GroupMsg::getGroupId,record.getTargetId())
                            .eq(GroupMsg::getStatus,0)
                            .orderByDesc(GroupMsg::getCreateTime);
                    List<GroupMsg> msgs = groupMsgService.list(msgWrapper);
                    if(msgs.size() > 0){
                        GroupMsg groupMsg = msgs.get(0);
                        String newMsg = groupMsg.getMsgContent();
                        switch (groupMsg.getMsgType()){
                            case 2:newMsg = "【图片】";break;
                            case 3:newMsg = "【语音】";break;
                            case 4:newMsg = "【视频】";break;
                        }
                        exchangeResp.setNewMsg(newMsg);
                    }
                    exchangeResp.setMsgTime(new Date());
                }
            }else{
                Friend friendById = friendService.getFriendById(record.getTargetId(), record.getUserId());
                //如果好友存在，则删除
                if(friendById == null){
                    msgUnreadRecordService.removeById(record);
                }else{
                    exchangeResp.setTargetName(friendById.getNickname());
                    exchangeResp.setTargetAvatar(friendById.getAvatar());
                    LambdaQueryWrapper<FriendMsg> msgWrapper = new LambdaQueryWrapper<>();
                    msgWrapper.eq(FriendMsg::getFromUserId,record.getUserId())
                            .eq(FriendMsg::getToUserId,record.getTargetId())
                            .eq(FriendMsg::getStatus,0)
                            .or()
                            .eq(FriendMsg::getFromUserId,record.getTargetId())
                            .eq(FriendMsg::getToUserId,record.getUserId())
                            .eq(FriendMsg::getStatus,0)
                            .orderByDesc(FriendMsg::getCreateTime);
                    List<FriendMsg> msgs = friendMsgService.list(msgWrapper);
                    if(msgs.size() > 0){
                        FriendMsg friendMsg = msgs.get(0);
                        String newMsg = friendMsg.getMsgContent();
                        switch (friendMsg.getMsgType()){
                            case 2:newMsg = "【图片】";break;
                            case 3:newMsg = "【语音】";break;
                            case 4:newMsg = "【视频】";break;
                        }
                        exchangeResp.setNewMsg(newMsg);
                    }
                    exchangeResp.setMsgTime(new Date());
                }
            }
            exchangeResps.add(exchangeResp);
        });
        CommonResp<List<ExchangeResp>> resp = new CommonResp<>();
        resp.success(exchangeResps);
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

    @PutMapping("/clearUnread/{id}")
    public CommonResp<Boolean> clearMsgUnreadRecord(@PathVariable Long id){
        MsgUnreadRecord byId = msgUnreadRecordService.getById(id);
        byId.setUnreadNum(0);
        boolean b = msgUnreadRecordService.updateById(byId);
        CommonResp<Boolean> resp = new CommonResp<>();
        resp.success(b);
        return resp;
    }
    @GetMapping("/getFriendMsg/{userId}/{friendId}/{page}")
    public CommonResp<Page<FriendMsg>> getFriendMsgList(@PathVariable Long userId, @PathVariable Long friendId, @PathVariable int page){
        CommonResp<Page<FriendMsg>> resp = new CommonResp<>();
        if(redisUtil.get("friendMsg:" + userId + "-" + friendId) != null && page == 1){
            resp.success((Page<FriendMsg>) redisUtil.get("friendMsg:" + userId + "-" + friendId));
            return resp;
        }
        //每次查出50条
        LambdaQueryWrapper<FriendMsg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendMsg::getFromUserId,userId).eq(FriendMsg::getToUserId,friendId).eq(FriendMsg::getStatus,0)
                .orderByAsc(FriendMsg::getCreateTime)
                .or()
                .eq(FriendMsg::getFromUserId,friendId).eq(FriendMsg::getToUserId,userId).eq(FriendMsg::getStatus,0)
                .orderByAsc(FriendMsg::getCreateTime);
        Page<FriendMsg> list = friendMsgService.page(new Page<>(page, 50), wrapper);
        //只缓存最新的数据
        if(page == 1){
            //缓存到redis中
            redisUtil.set("friendMsg:" + userId + "-" + friendId,list,1000 * 60 * 60 * 24 * 7);
        }

        resp.success(list);
        return resp;
    }
    @GetMapping("/getGroupMsg/{groupId}/{page}")
    public CommonResp<Page<GroupMsg>> getGroupMsgList(@PathVariable Long groupId, @PathVariable int page){
        CommonResp<Page<GroupMsg>> resp = new CommonResp<>();
        if(redisUtil.get("groupMsg:" + groupId) != null && page == 1){
            resp.success((Page<GroupMsg>) redisUtil.get("groupMsg:" + groupId));
            return resp;
        }
        //每次查出50条
        LambdaQueryWrapper<GroupMsg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMsg::getGroupId,groupId).eq(GroupMsg::getStatus,0).orderByAsc(GroupMsg::getCreateTime);

        Page<GroupMsg> list = groupMsgService.page(new Page<>(page, 100), wrapper);
        if(page == 1){
            //缓存到redis中
            redisUtil.set("groupMsg:" + groupId,list,1000 * 60 * 60 * 24 * 7);
        }
        resp.success(list);
        return resp;
    }

    @PostMapping("/back/{type}/{id}")
    public CommonResp<Boolean> backMsg(@PathVariable Byte type,@PathVariable Long id){
        //0是好友消息撤回，1是群组消息撤回
        CommonResp<Boolean> resp = new CommonResp<>();
        if(type == 0){
            FriendMsg byId = friendMsgService.getById(id);
            byId.setStatus((byte) 1);
            boolean b = friendMsgService.saveOrUpdate(byId);
            resp.success(b);
            redisUtil.delete("friendMsg:" + byId.getFromUserId() + "-" + byId.getToUserId());
            return resp;
        }
        GroupMsg byId = groupMsgService.getById(id);
        byId.setStatus((byte) 1);
        boolean b = groupMsgService.saveOrUpdate(byId);
        resp.success(b);
        redisUtil.delete("groupMsg:" + byId.getGroupId());
        return resp;
    }
}
