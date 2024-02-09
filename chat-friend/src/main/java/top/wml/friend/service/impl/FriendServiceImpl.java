package top.wml.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wml.common.annotation.TokenRequired;
import top.wml.common.entity.Friend;
import top.wml.common.entity.Invitation;
import top.wml.common.entity.User;
import top.wml.common.exception.BusinessException;
import top.wml.common.utils.JwtUtil;
import top.wml.common.utils.PinyinUtil;
import top.wml.friend.entity.FriendVO;
import top.wml.friend.mapper.FriendMapper;
import top.wml.friend.mapper.InvitationMapper;
import top.wml.friend.openfeign.UserService;
import top.wml.friend.service.FriendService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        //看看是否曾经是好友，防止插入冗余字段
        LambdaQueryWrapper<Friend> wrapperF = new LambdaQueryWrapper<>();
        wrapperF.eq(Friend::getUserId, invitation.getUserId())
              .eq(Friend::getFriendId, invitation.getFriendId());
        Friend friend = friendMapper.selectOne(wrapperF);

        LambdaQueryWrapper<Friend> wrapperF2 = new LambdaQueryWrapper<>();
        wrapperF2.eq(Friend::getUserId, invitation.getFriendId())
                .eq(Friend::getFriendId, invitation.getUserId());
        Friend friend2 = friendMapper.selectOne(wrapperF2);
        if(friend != null && friend2 != null){
           friend.setStatus((byte) 1);
            int i = friendMapper.updateById(friend);
            friend2.setStatus((byte) 1);
            int j = friendMapper.updateById(friend2);
            return i> 0 && j > 0;
        }
        //拿到首字母
        String pinyin = PinyinUtil.getPinyinInitial(friendInfo.getNickname()).toUpperCase();
        String regex = "[a-zA-Z]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pinyin);
        char friendLetter = 'Z';
        if (matcher.find()) {
            friendLetter = matcher.group().charAt(0);
        }
        //拿到首字母
        String pinyin2 = PinyinUtil.getPinyinInitial(invitation.getUserNickname()).toUpperCase();
        Matcher matcher2 = pattern.matcher(pinyin2);
        char myLetter = 'Z';
        if (matcher2.find()) {
            myLetter = matcher2.group().charAt(0);
        }
        //构建朋友对象
        Friend newFriend = Friend.builder()
                .userId(invitation.getUserId())
                .friendId(friendInfo.getId())
                .avatar(friendInfo.getAvatar())
                .alphabetic(String.valueOf(friendLetter))
                .nickname(friendInfo.getNickname())
                .status((byte) 1)
                .remark("")
                .createBy(invitation.getUserId())
                .updateBy(invitation.getUserId())
                .build();
        Friend newFriend2 = Friend.builder()
                .userId(friendInfo.getId())
                .friendId(invitation.getUserId())
                .avatar(invitation.getUserAvatar())
                .alphabetic(String.valueOf(myLetter))
                .nickname(invitation.getUserNickname())
                .status((byte) 1)
                .remark("")
                .createBy(invitation.getUserId())
                .updateBy(invitation.getUserId())
                .build();
        return friendMapper.insert(newFriend) > 0 && friendMapper.insert(newFriend2) > 0;
    }

    /**
     * 查看别人发给自己的好友申请
     * @param id
     * @return
     */
    public List<Invitation> getInvitationList(Long id) {
        LambdaQueryWrapper<Invitation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invitation::getFriendId,id)
                .orderByAsc(Invitation::getStatus);
        return invitationMapper.selectList(wrapper);
    }

    @Override
    public List<FriendVO> getRecommendList(Long userId, List<Friend> allFriends) {
        // 创建一个Map，用于存储每个用户的好友列表
        Map<Long, Set<Long>> userFriendsMap = new HashMap<>();

        // 将所有好友关系按用户分组
        for (Friend friend : allFriends) {
            userFriendsMap.computeIfAbsent(friend.getUserId(), k -> new HashSet<>()).add(friend.getFriendId());
        }
        // 获取给定用户的好友列表
        Set<Long> userFriends = userFriendsMap.getOrDefault(userId, Collections.emptySet());

        // 统计共同好友的数量
        Map<Long, Integer> commonFriendsCountMap = new HashMap<>();
        for (Long friendId : userFriendsMap.keySet()) {
            // 跳过已经是用户的好友的用户
            if (userFriends.contains(friendId)) {
                continue;
            }

            // 统计共同好友的数量
            Set<Long> commonFriends = userFriendsMap.getOrDefault(friendId, Collections.emptySet());
            commonFriendsCountMap.put(friendId, (int) commonFriends.stream().filter(userFriends::contains).count());
        }

        // 按照共同好友的数量从大到小排序
        List<Long> sortedCommonFriends = commonFriendsCountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());


        // 构造FriendVO实体列表
        List<FriendVO> result = sortedCommonFriends.stream()
                .map(friendId -> {
                    FriendVO friendVO = new FriendVO();
                    BeanUtils.copyProperties(allFriends.stream()
                            .filter(friend -> friend.getFriendId().equals(friendId))
                            .findFirst()
                            .orElse(null), friendVO);
                    if (friendVO != null) {
                        // 设置共同好友数量
                        friendVO.setNum(commonFriendsCountMap.get(friendId));
                    }
                    return friendVO;
                })
                .filter(friendVO -> friendVO != null && friendVO.getNum() != 0 && !friendVO.getFriendId().equals(userId))
                .collect(Collectors.toList());

        return result;
    }

    @Override
    @Transactional
    public boolean applyFriend(Invitation invitation) {
        if(invitation == null || invitation.getUserId() == null || invitation.getFriendId() == null){
            throw new BusinessException("提交信息不全");
        }
        //判断是否已经申请过了
        LambdaQueryWrapper<Invitation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invitation::getUserId, invitation.getUserId())
              .eq(Invitation::getFriendId, invitation.getFriendId())
                .ne(Invitation::getStatus, (byte) 2);
        Invitation hasOne = invitationMapper.selectOne(wrapper);
        if(hasOne != null){
            if(hasOne.getStatus() == 0){
                throw new BusinessException("您的申请正在等待对方通过，请不要重复申请！");
            }
        }
        //判断是否已经是好友了
        LambdaQueryWrapper<Friend> wrapperF = new LambdaQueryWrapper<>();
        wrapperF.eq(Friend::getUserId, invitation.getUserId())
            .eq(Friend::getFriendId, invitation.getFriendId())
            .eq(Friend::getStatus, (byte) 1)
                .or()
                .eq(Friend::getUserId, invitation.getFriendId())
               .eq(Friend::getFriendId, invitation.getUserId())
               .eq(Friend::getStatus, (byte) 1);
        Friend has = friendMapper.selectOne(wrapperF);
        if(has != null){
            throw new BusinessException("你们已经是好友了,请不要重复申请");
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
        if(invitation.getStatus() == 1){
            boolean b = addFriend(invitation);
        }
        int update = invitationMapper.update(invitation, wrapper);
        return update > 0;
    }


    @Override
    public boolean deleteFriend(Long userId,Long friendId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId,friendId)
                .or()
                .eq(Friend::getUserId,friendId).eq(Friend::getFriendId, userId);
        List<Friend> friends = friendMapper.selectList(wrapper);
        friends.forEach(friend -> {
            friend.setStatus((byte)0);
            friendMapper.updateById(friend);
        });
        return true;
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
    public List<Friend> getFriendList(Long id) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, id);
        wrapper.eq(Friend::getStatus, (byte) 1);
        List<Friend> friends = friendMapper.selectList(wrapper);
        return friends;
    }

}
