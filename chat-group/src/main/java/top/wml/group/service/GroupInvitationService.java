package top.wml.group.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wml.common.entity.GroupInvitation;

import java.util.List;

public interface GroupInvitationService extends IService<GroupInvitation> {
    List<GroupInvitation> getInvitationList(Long id);
}
