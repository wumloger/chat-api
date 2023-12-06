package top.wml.message.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wml.common.entity.FriendMsg;
import top.wml.message.feign.FriendService;
import top.wml.message.mapper.FriendMsgMapper;
import top.wml.message.service.FriendMsgService;

@Service
public class FriendMsgServiceImpl extends ServiceImpl<FriendMsgMapper, FriendMsg>  implements FriendMsgService {
}
