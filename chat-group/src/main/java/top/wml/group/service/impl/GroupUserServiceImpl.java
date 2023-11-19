package top.wml.group.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wml.common.entity.GroupUser;
import top.wml.group.mapper.GroupUserMapper;
import top.wml.group.service.GroupUserService;

@Service
public class GroupUserServiceImpl extends ServiceImpl<GroupUserMapper, GroupUser> implements GroupUserService {
}
