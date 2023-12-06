package top.wml.message.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wml.common.entity.MsgUnreadRecord;
import top.wml.message.mapper.MsgUnreadRecordMapper;
import top.wml.message.service.MsgUnreadRecordService;

@Service
public class MsgUnreadRecordServiceImpl extends ServiceImpl<MsgUnreadRecordMapper, MsgUnreadRecord> implements MsgUnreadRecordService {
}
