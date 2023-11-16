package top.wml.friend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wml.common.entity.Friend;
import top.wml.common.entity.Invitation;
import top.wml.common.entity.User;

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

    boolean deleteFriend(Long id);


    Friend getFriendById(Long id);

    List<Friend> getFriendList();

    List<Invitation> getInvitationList(Long id);

}
