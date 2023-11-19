package top.wml.group.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wml.common.entity.Group;
import top.wml.common.entity.GroupInvitation;

public interface GroupService extends IService<Group> {
    Long createGroup(Group group);

    Boolean updateGroup(Group group);

    Boolean deleteGroup(Group group);

    Boolean auditInvitation(GroupInvitation groupInvitation,Long id);
}
