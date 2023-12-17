package top.wml.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.wml.common.entity.Group;
import top.wml.common.entity.GroupInvitation;
import top.wml.common.entity.GroupUser;
import top.wml.common.entity.Invitation;
import top.wml.group.mapper.GroupInvitationMapper;
import top.wml.group.mapper.GroupMapper;
import top.wml.group.mapper.GroupUserMapper;
import top.wml.group.service.GroupInvitationService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class GroupInvitationServiceImpl extends ServiceImpl<GroupInvitationMapper, GroupInvitation> implements GroupInvitationService {
    @Resource
    private GroupMapper groupMapper;
    @Resource
    private GroupUserMapper groupUserMapper;
    @Resource
    private GroupInvitationMapper groupInvitationMapper;
    @Override
    public List<GroupInvitation> getInvitationList(Long id) {
        //找到我创建的所有群聊
        LambdaQueryWrapper<Group> groupWrapper = new LambdaQueryWrapper<>();
        groupWrapper.eq(Group::getAdminUserId,id)
                .select(Group::getId);
        List<Group> myGroups = groupMapper.selectList(groupWrapper);
        Set<Long> ids  = new LinkedHashSet<>();
        myGroups.forEach((item)->{
            ids.add(item.getId());
        });
        //找到我是管理员的群聊
        LambdaQueryWrapper<GroupUser> groupUserWrapper = new LambdaQueryWrapper<>();
        groupUserWrapper.eq(GroupUser::getUserId,id)
               .eq(GroupUser::getStatus,1)
               .eq(GroupUser::getAdminable,1);
        List<GroupUser> groupUsers = groupUserMapper.selectList(groupUserWrapper);
        groupUsers.forEach((item)->{
            ids.add(item.getGroupId());
        });
        if(ids.size() > 0){
            LambdaQueryWrapper<GroupInvitation> groupInvitationWrapper = new LambdaQueryWrapper<>();
            groupInvitationWrapper.in(GroupInvitation::getGroupId,ids);
            return groupInvitationMapper.selectList(groupInvitationWrapper);
        }

       return new ArrayList<>();

    }
}
