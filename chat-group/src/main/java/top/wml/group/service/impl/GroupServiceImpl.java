package top.wml.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.wml.common.entity.Group;
import top.wml.common.entity.GroupInvitation;
import top.wml.common.entity.GroupUser;
import top.wml.common.exception.BusinessException;
import top.wml.group.mapper.GroupInvitationMapper;
import top.wml.group.mapper.GroupMapper;
import top.wml.group.mapper.GroupUserMapper;
import top.wml.group.service.GroupService;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {

    @Resource
    private GroupMapper groupMapper;

    @Resource
    private GroupInvitationMapper groupInvitationMapper;

    @Resource
    private GroupUserMapper groupUserMapper;
    @Override
    public Long createGroup(Group group) {
        LambdaQueryWrapper<Group> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Group::getName,group.getName())
                .eq(Group::getAdminUserId,group.getAdminUserId());
        Group selectOne = groupMapper.selectOne(wrapper);
        if(selectOne != null && selectOne.getStatus() == 1){
            throw new RuntimeException("该群已存在");
        }
        if(selectOne!= null && selectOne.getStatus() == 0){
            selectOne.setStatus((byte) 1);
            groupMapper.updateById(selectOne);
            return selectOne.getId();
        }

        groupMapper.insert(group);
        Group group1 = groupMapper.selectOne(wrapper);
        return group1.getId();
    }

    @Override
    public Boolean updateGroup(Group group) {
        return groupMapper.updateById(group) > 0;
    }

    @Override
    public Boolean deleteGroup(Group group) {
        group.setStatus((byte) 0);
        return groupMapper.updateById(group) > 0;
    }

    @Override
    public Boolean auditInvitation(GroupInvitation groupInvitation,Long id) {
        Group group = groupMapper.selectById(groupInvitation.getGroupId());
        LambdaQueryWrapper<GroupUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupUser::getGroupId,groupInvitation.getGroupId())
               .eq(GroupUser::getUserId,id);
        GroupUser groupUser = groupUserMapper.selectOne(wrapper);
        if(groupUser == null || groupUser.getStatus()!= 1){
            throw new BusinessException("审核者不是该群成员！");
        }

        if(id != group.getAdminUserId() && groupUser.getAdminable() == 0){
            throw new BusinessException("你没有审核的权限!");
        }
        if(groupInvitation.getStatus() == 1){
            GroupUser user = GroupUser.builder()
                    .groupId(groupInvitation.getGroupId())
                    .status((byte) 1)
                    .adminable((byte) 0)
                    .groupName(groupInvitation.getGroupName())
                    .userId(groupInvitation.getUserId())
                    .userNickname(groupInvitation.getUserNickname())
                    .userAvatar(groupInvitation.getUserAvatar())
                    .createBy(id)
                    .updateBy(id)
                    .build();
            groupUserMapper.insert(user);
        }
        int i = groupInvitationMapper.updateById(groupInvitation);
        return i > 0;
    }
}
