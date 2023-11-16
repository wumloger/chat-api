package top.wml.friend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wml.common.entity.Invitation;
import top.wml.friend.mapper.InvitationMapper;
import top.wml.friend.service.InvitationService;

@Service
public class InvitationServiceImpl extends ServiceImpl<InvitationMapper, Invitation> implements InvitationService {
}
