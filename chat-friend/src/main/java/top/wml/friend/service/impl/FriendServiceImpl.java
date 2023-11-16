package top.wml.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import top.wml.common.annotation.TokenRequired;
import top.wml.common.entity.Friend;
import top.wml.common.entity.Invitation;
import top.wml.common.entity.User;
import top.wml.common.exception.BusinessException;
import top.wml.common.utils.JwtUtil;
import top.wml.common.utils.PinyinUtil;
import top.wml.friend.mapper.FriendMapper;
import top.wml.friend.mapper.InvitationMapper;
import top.wml.friend.openfeign.UserService;
import top.wml.friend.service.FriendService;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {
    private HttpServletRequest request;

    @Resource
    private FriendMapper friendMapper;
    @Resource
    private UserService userService;

    @Resource
    private InvitationMapper invitationMapper;
    @Override
    public boolean addFriend(Invitation invitation) {
        if (invitation.getFriendId() == invitation.getUserId()){
            throw new BusinessException("不能添加自己为好友");
        }
        if(invitation == null || invitation.getId() == null){
            throw new BusinessException("提交信息不全");
        }
        User friendInfo = userService.getUserById(invitation.getFriendId());
        if(friendInfo == null){
            throw new BusinessException("朋友不存在");
        }

        //拿到首字母
        String pinyin = PinyinUtil.getPinyinInitial(friendInfo.getNickname()).toUpperCase();
        String regex = "[a-zA-Z]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pinyin);
        char firstLetter = 'A';
        if (matcher.find()) {
            firstLetter = matcher.group().charAt(0);
        }
        //看下数据库里有没有这个好友关系，不能重复添加
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, invitation.getUserId())
               .eq(Friend::getFriendId, friendInfo.getId());
        Friend friend = friendMapper.selectOne(wrapper);
        if(friend!= null){
            throw new BusinessException("不能重复添加好友");
        }
        //构建朋友对象
        Friend newFriend = Friend.builder()
                .userId(invitation.getUserId())
                .friendId(friendInfo.getId())
                .avatar(friendInfo.getAvatar())
                .alphabetic(String.valueOf(firstLetter))
                .nickname(friendInfo.getNickname())
                .status((byte) 1)
                .remark("")
                .createBy(invitation.getUserId())
                .updateBy(invitation.getUserId())
                .build();
        return friendMapper.insert(newFriend) > 0;
    }

    /**
     * 查看别人发给自己的好友申请
     * @param id
     * @return
     */
    public List<Invitation> getInvitationList(Long id) {
        LambdaQueryWrapper<Invitation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invitation::getFriendId,id);
        return invitationMapper.selectList(wrapper);
    }

    @Override
    public boolean applyFriend(Invitation invitation) {
        if(invitation == null || invitation.getUserId() == null || invitation.getFriendId() == null){
            throw new BusinessException("提交信息不全");
        }
        LambdaQueryWrapper<Invitation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invitation::getUserId, invitation.getUserId())
              .eq(Invitation::getFriendId, invitation.getFriendId())
                .ne(Invitation::getStatus, (byte) 2);
        Invitation has = invitationMapper.selectOne(wrapper);
        if(has != null){
            throw new BusinessException("请不要重复申请");
        }
        invitation.setStatus((byte)0);
        invitation.setCreateBy(invitation.getUserId());
        invitation.setUpdateBy(invitation.getUserId());
        return invitationMapper.insert(invitation) > 0;
    }

    @Override
    public boolean auditFriend(Invitation invitation) {
        LambdaQueryWrapper<Invitation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invitation::getId, invitation.getId());
        int update = invitationMapper.update(invitation, wrapper);
        boolean b = addFriend(invitation);

        return update > 0 & b;
    }


    @Override
    public boolean deleteFriend(Long id) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getId, id);
        Friend friend = friendMapper.selectOne(wrapper);
        friend.setStatus((byte)0);
        int update = friendMapper.update(friend, wrapper);
        return update > 0;
    }


    @Override
    public Friend getFriendById(Long id) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getStatus, (byte) 1);
        wrapper.eq(Friend::getId, id);
        Friend friend = friendMapper.selectOne(wrapper);
        return friend;
    }

    @Override
    public List<Friend> getFriendList() {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getStatus, (byte) 1);
        List<Friend> friends = friendMapper.selectList(wrapper);
        return friends;
    }

}