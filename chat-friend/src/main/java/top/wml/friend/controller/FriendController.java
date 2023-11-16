package top.wml.friend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import top.wml.common.annotation.TokenRequired;
import top.wml.common.entity.Invitation;
import top.wml.common.exception.BusinessException;
import top.wml.common.resp.CommonResp;
import top.wml.common.utils.JwtUtil;
import top.wml.friend.service.FriendService;
import top.wml.friend.service.InvitationService;

import java.util.List;

@RestController
@RequestMapping("/friend")
public class FriendController {

    @Resource
    private InvitationService invitationService;
    private HttpServletRequest request;

    @Resource
    private FriendService friendService;
    @PostMapping("/apply")
    public CommonResp<Boolean> applyFriend(@RequestBody Invitation invitation){
        if(invitation == null){
            throw new BusinessException("参数为空");
        }
        if(invitation.getFriendId() == null || invitation.getUserId() ==  null){
            throw new BusinessException("缺少关键信息");
        }
        boolean b = friendService.applyFriend(invitation);
        CommonResp<Boolean> resp = new CommonResp<>();
        resp.success(b);
        return resp;
    }

    /**
     * 拿到别人给我的申请列表
     * @return
     */
    @TokenRequired
    @GetMapping("/getForMe")
    public CommonResp<List<Invitation>> getForMyInvitationList(){
        Long userId = getUserId();
        CommonResp<List<Invitation>> resp = new CommonResp<>();
        resp.success(friendService.getInvitationList(userId));
        return resp;
    }

    @GetMapping("/getInvitation")
    public CommonResp<List<Invitation>> getInvitationList(){
        LambdaQueryWrapper<Invitation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invitation::getUserId,getUserId());
        CommonResp<List<Invitation>> resp = new CommonResp<>();
        resp.success(invitationService.list(wrapper));
        return resp;
    }

    @PostMapping("/audit")
    public CommonResp<Boolean> auditFriend(@RequestBody Invitation invitation){
        if(invitation == null){
            throw new BusinessException("参数为空");
        }
        CommonResp<Boolean> resp = new CommonResp<>();
        resp.success(friendService.auditFriend(invitation));
        return resp;
    }

//    public CommonResp<>


    private Long getUserId(){
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserId(token);
        return userId;
    }
}
