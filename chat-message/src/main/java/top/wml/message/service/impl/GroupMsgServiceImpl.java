package top.wml.message.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wml.common.entity.GroupMsg;
import top.wml.message.mapper.GroupMsgMapper;
import top.wml.message.service.GroupMsgService;
@Service
public class GroupMsgServiceImpl extends ServiceImpl<GroupMsgMapper, GroupMsg> implements GroupMsgService {
}
