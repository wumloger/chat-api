package top.wml.friend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wml.common.entity.Friend;
import top.wml.common.entity.Invitation;
import top.wml.common.entity.User;
import top.wml.friend.entity.FriendVO;

import java.util.List;

public interface FriendService extends IService<Friend> {
    /**
     * 好友添加
     * @param user
     * @return
     */
    boolean addFriend(Invitation invitation);

    boolean applyFriend(Invitation invitation);

    boolean auditFriend(Invitation invitation);

    boolean deleteFriend(Long userId,Long friendId);


    Friend getFriendById(Long id);

    List<Friend> getFriendList(Long id);

    List<Invitation> getInvitationList(Long id);

    List<FriendVO> getRecommendList(Long userId, List<Friend> allFriends);

}
