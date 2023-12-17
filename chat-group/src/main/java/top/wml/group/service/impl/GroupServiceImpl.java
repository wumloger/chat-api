package top.wml.group.service.impl;

import cn.hutool.core.util.ObjectUtil;
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

import java.util.List;

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
        //删除群聊所有成员
        LambdaQueryWrapper<GroupUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupUser::getGroupId,group.getId());
        List<GroupUser> groupUsers = groupUserMapper.selectList(wrapper);
        for(GroupUser groupUser : groupUsers){
            groupUser.setStatus((byte) 0);
            groupUserMapper.updateById(groupUser);
        }
        group.setStatus((byte) 0);
        return groupMapper.updateById(group) > 0;
    }

    @Override
    public Boolean auditInvitation(GroupInvitation groupInvitation,Long id) {
        Group group = groupMapper.selectById(groupInvitation.getGroupId());
        if(ObjectUtil.isNull(group)){
            throw new BusinessException("该群不存在！");
        }

        LambdaQueryWrapper<GroupUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupUser::getGroupId,groupInvitation.getGroupId())
               .eq(GroupUser::getUserId,id);
        GroupUser groupUser = groupUserMapper.selectOne(wrapper);

        if(groupUser == null || groupUser.getStatus()!= 1){
            throw new BusinessException("审核者不是该群成员！");
        }

        if(id.compareTo(group.getAdminUserId()) != 0 && groupUser.getAdminable() == 0){
            throw new BusinessException("你没有审核的权限!");
        }

        LambdaQueryWrapper<GroupUser> addWrapper = new LambdaQueryWrapper<>();
        addWrapper.eq(GroupUser::getUserId,groupInvitation.getUserId())
                .eq(GroupUser::getGroupId,groupInvitation.getGroupId());
        groupUser = groupUserMapper.selectOne(addWrapper);
        if(groupUser!= null && groupUser.getStatus() == 1){
            throw new BusinessException("该用户已经加入该群！");
        }
        if(groupInvitation.getStatus() == 1){
            //有了就不要插入了
            if(groupUser != null && (groupUser.getStatus() == 0 || groupUser.getStatus() == 2)){
                groupUser.setStatus((byte) 1);
                groupUserMapper.updateById(groupUser);
            }else{
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
        }
        int i = groupInvitationMapper.updateById(groupInvitation);
        return i > 0;
    }
}
