package top.wml.friend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import top.wml.common.annotation.TokenRequired;
import top.wml.common.entity.Friend;
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
    @Resource
    private HttpServletRequest request;

    @Resource
    private FriendService friendService;
    /**
     * 申请好友关系
     * @param invitation 申请信息
     * @return 申请结果
     */
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

    /**
     * 获取我发出的邀请列表
     * @return CommonResp<List<Invitation>>对象，包含邀请列表的CommonResp对象
     */
    @GetMapping("/getInvitation")
    public CommonResp<List<Invitation>> getInvitationList(){
        LambdaQueryWrapper<Invitation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invitation::getUserId,getUserId());
        CommonResp<List<Invitation>> resp = new CommonResp<>();
        resp.success(invitationService.list(wrapper));
        return resp;
    }

    /**
     * 审核邀请
     * @param invitation 邀请对象
     * @return CommonResp对象，包含操作结果和信息
     * @throws BusinessException 业务异常
     */
    @PostMapping("/audit")
    public CommonResp<Boolean> auditFriend(@RequestBody Invitation invitation){
        if(invitation == null){
            throw new BusinessException("参数为空");
        }
        CommonResp<Boolean> resp = new CommonResp<>();
        resp.success(friendService.auditFriend(invitation));
        return resp;
    }

    /**
     * 获取好友列表
     * 根据用户ID获取好友列表
     * @return 好友列表
     */
    @GetMapping("/list")
    public CommonResp<List<Friend>> getFriendList(){
        CommonResp<List<Friend>> resp = new CommonResp<>();
        resp.success(friendService.getFriendList(getUserId()));
        return resp;
    }
    /**
     * 删除好友
     *
     * @param id 好友ID
     * @return删除成功返回true，否则返回false
     */
    @DeleteMapping("/delete/{id}")
    public CommonResp<Boolean> deleteFriend(@PathVariable Long id){
        CommonResp<Boolean> resp = new CommonResp<>();
        resp.success(friendService.deleteFriend(id));
        return resp;
    }

    @DeleteMapping("/deleteInvitation/{id}")
    public CommonResp<Boolean> deleteInvitation(@PathVariable Long id){
        CommonResp<Boolean> resp = new CommonResp<>();
        resp.success(invitationService.removeById(id));
        return resp;
    }

    private Long getUserId(){
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserId(token);
        return userId;
    }
}
