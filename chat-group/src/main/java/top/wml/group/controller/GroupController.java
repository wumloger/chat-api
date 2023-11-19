package top.wml.group.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import top.wml.common.annotation.TokenRequired;
import top.wml.common.entity.*;
import top.wml.common.exception.BusinessException;
import top.wml.common.req.GroupVO;
import top.wml.common.resp.CommonResp;
import top.wml.common.utils.JwtUtil;
import top.wml.common.utils.PinyinUtil;
import top.wml.group.service.GroupInvitationService;
import top.wml.group.service.GroupService;
import top.wml.group.service.GroupUserService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/group")
public class GroupController {

    @Resource
    private GroupService groupService;

    @Resource
    private GroupUserService groupUserService;

    @Resource
    private GroupInvitationService groupInvitationService;

    @Resource
    private HttpServletRequest request;

    @PostMapping("/create")
    @TokenRequired
    public CommonResp<Boolean> createGroup(@RequestBody GroupVO groupVO){
        if (groupVO == null){
            throw new BusinessException("参数不能为空!");
        }
        //拿到首字母
        String pinyin = PinyinUtil.getPinyinInitial(groupVO.getName()).toUpperCase();
        String regex = "[a-zA-Z]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pinyin);
        char firstLetter = 'Z';
        if (matcher.find()) {
            firstLetter = matcher.group().charAt(0);
        }
        //创建群聊
        Group group = Group.builder()
                .name(groupVO.getName())
                .adminUserId(getUserId())
                .avatar(groupVO.getAvatar())
                .createBy(getUserId())
                .updateBy(getUserId())
                .alphabetic(String.valueOf(firstLetter))
                .status((byte) 1)
                .intro(groupVO.getIntro())
                .build();
        Long groupId = groupService.createGroup(group);
        List<Friend> members = groupVO.getMembers();
        //自己肯定要进群
        members.add(Friend.builder()
                .nickname(getNickname())
                .friendId(getUserId())
                .avatar(getAvatar())
                .build());
        List<GroupUser> groupUsers = new ArrayList<>();
        //初始人员
        if(members != null){
            members.forEach((member)->{
                GroupUser gUser = GroupUser.builder()
                        .groupId(groupId)
                        .remark("创建时拉入群")
                        .adminable((byte) 0)
                        .source((byte) 0)
                        .createBy(getUserId())
                        .groupName(group.getName())
                        .status((byte) 1)
                        .updateBy(getUserId())
                        .userNickname(member.getNickname())
                        .userId(member.getFriendId())
                        .userAvatar(member.getAvatar())
                        .build();
                groupUsers.add(gUser);
            });
            groupUserService.saveBatch(groupUsers);
        }
        CommonResp<Boolean> resp = new CommonResp<>();
        resp.success(groupId != null);
        return resp;
    }



    @DeleteMapping("/delete/{id}")
    public CommonResp<Boolean> deleteGroup(@PathVariable Long id){
        Group group = groupService.getById(id);
        if(getUserId() != group.getAdminUserId()){
            throw new BusinessException("你没有权限删除该群组!");
        }
        Boolean aBoolean = groupService.deleteGroup(group);
        CommonResp<Boolean> resp = new CommonResp<>();
        resp.success(aBoolean);
        return resp;
    }

    @GetMapping("/myList")
    public CommonResp<List<Group>> getMyGroupList(){
        LambdaQueryWrapper<Group> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Group::getAdminUserId,getUserId());
        List<Group> list = groupService.list(wrapper);
        CommonResp<List<Group>> resp = new CommonResp<>();
        resp.success(list);
        return resp;
    }

    @GetMapping("/list")
    public CommonResp<List<Group>> getGroupList(){
        LambdaQueryWrapper<GroupUser> groupUserWrapper = new LambdaQueryWrapper<>();
        groupUserWrapper.eq(GroupUser::getUserId,getUserId());
        List<GroupUser> groupUserList = groupUserService.list(groupUserWrapper);
        List<Group> list = null;
        CommonResp<List<Group>> resp = new CommonResp<>();

        if(groupUserList.size() == 0){
            resp.success(new ArrayList<>());
            return resp;
        }else{
            List<Long> groupIds = new ArrayList<>();
            groupUserList.forEach((groupUser)->{
                groupIds.add(groupUser.getGroupId());
            });
            LambdaQueryWrapper<Group> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Group::getId,groupIds);
             list = groupService.list(wrapper);
        }
        resp.success(list);
        return resp;
    }

    @GetMapping("/get/{id}")
    public CommonResp<Group> getGroupById(@PathVariable Long id){
        Group group = groupService.getById(id);
        CommonResp<Group> resp = new CommonResp<>();
        resp.success(group);
        return resp;
    }


    @PostMapping("/join")
    public CommonResp<Boolean> applyJoinGroup(@RequestBody GroupInvitation groupInvitation){
        LambdaQueryWrapper<GroupUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupUser::getUserId,groupInvitation.getUserId())
                .eq(GroupUser::getGroupId,groupInvitation.getGroupId());
        GroupUser one = groupUserService.getOne(wrapper);
        if(one!= null){
            throw new BusinessException("你已经在群聊里了！");
        }
        boolean save = groupInvitationService.save(groupInvitation);
        CommonResp<Boolean> resp = new CommonResp<>();
        resp.success(save);
        return resp;
    }

    @GetMapping("/getInvitations")
    public CommonResp<List<GroupInvitation>> getInvitationList(){
        List<GroupInvitation> invitationList = groupInvitationService.getInvitationList(getUserId());
        CommonResp<List<GroupInvitation>> resp = new CommonResp<>();
        resp.success(invitationList);
        return resp;
    }

    @PostMapping("/audit")
    public CommonResp<Boolean> auditInvitation(@RequestBody GroupInvitation groupInvitation){
        boolean audit = groupService.auditInvitation(groupInvitation,getUserId());
        CommonResp<Boolean> resp = new CommonResp<>();
        resp.success(audit);
        return resp;
    }

    @PostMapping("/update")
    public CommonResp<Boolean> updateGroup(@RequestBody Group group){
        if(getUserId() != group.getAdminUserId()){
            throw new BusinessException("你没有权限修改该群组!");
        }
        Boolean aBoolean = groupService.updateGroup(group);
        CommonResp<Boolean> resp = new CommonResp<>();
        resp.success(aBoolean);
        return resp;
    }




    private Long getUserId(){
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserId(token);
        return userId;
    }
    private String getNickname(){
        String token = request.getHeader("token");
        String nickname = JwtUtil.getNickname(token);
        return nickname;
    }

    private String getAvatar() {
        String token = request.getHeader("token");
        return JwtUtil.getAvatar(token);

    }
}