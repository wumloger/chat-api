package top.wml.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import top.wml.common.annotation.TokenRequired;
import top.wml.common.entity.User;
import top.wml.common.exception.BusinessException;
import top.wml.common.req.ChangePasswordEntity;
import top.wml.common.resp.CommonResp;
import top.wml.common.utils.JwtUtil;
import top.wml.common.utils.RedisUtil;
import top.wml.user.service.UserService;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private HttpServletRequest request;

    @PostMapping("/register")
    public CommonResp register(@RequestBody User user){
        if(Objects.isNull(user)){
            throw new BusinessException("注册信息为空");
        }
        boolean register = userService.register(user.getUsername(), user.getPassword());
        CommonResp<Boolean> resp = new CommonResp<>();
        if(register){
            resp.success(true);
            resp.setMsg("注册成功");
        }else{
            resp.fail("注册失败");
        }
        return resp;
    }

    @PostMapping("/login")
    public CommonResp login(@RequestBody User user){
        if(Objects.isNull(user)){
            throw new BusinessException("登录信息为空");
        }
        String token = userService.login(user.getUsername(), user.getPassword());
        CommonResp<String> resp = new CommonResp<>();
        if(Objects.nonNull(token)){
            resp.success(token);
            resp.setMsg("登录成功");
        }else{
            resp.fail("登录失败");
        }
        return resp;
    }

    @GetMapping("/getUser")
    public CommonResp getUser(@RequestParam(required = false) String id){
        //查询全部
        if(Objects.isNull(id) || id.isEmpty()){
            List<User> userList = userService.getUserList();
            CommonResp<List<User>> resp = new CommonResp<>();
            resp.success(userList);
            return resp;
        }
        //id转化为Long
        Long lid = null;
        try{
            lid = Long.parseLong(id);
        }catch (NumberFormatException e){
            throw new BusinessException("id格式错误");
        }
        //查询单个
        User userById = userService.getUserById(lid);
        CommonResp<User> resp = new CommonResp<>();
        resp.success(userById);
        return resp;
    }

    @PutMapping("/updateUser")
    public CommonResp updateUser(@RequestBody User user){
        if(Objects.isNull(user)){
            throw new BusinessException("更新信息为空");
        }
        boolean b = userService.updateUserInfo(user);
        CommonResp<Boolean> resp = new CommonResp<>();
        if(b){
            resp.success(true);
            resp.setMsg("更新成功");
        }else{
            resp.fail("更新失败");
        }
        return resp;
    }

    @GetMapping("/getCode")
    public CommonResp<String> getCode(@RequestParam(required = true) String email){
        String code = userService.sendEmailForCode(email);
        System.out.println("验证码为：" + code);
        CommonResp<String> resp = new CommonResp<>();
        resp.success(code);
        resp.setMsg("发送成功");
        return resp;
    }

    @PostMapping("/forgetPassword")
    public CommonResp<Boolean> forgetPassword(@RequestBody ChangePasswordEntity changePasswordEntity){
        if(Objects.isNull(changePasswordEntity)){
            throw new BusinessException("修改密码信息为空");
        }
        boolean flag = userService.validCode(changePasswordEntity.getEmail(),changePasswordEntity.getCode());
        CommonResp<Boolean> resp = new CommonResp<>();
        if(flag){
            User user = userService.getUserByEmail(changePasswordEntity.getEmail());
            if(Objects.isNull(user)){
                throw new BusinessException("该邮箱没有绑定任何用户！");
            }
            user.setPassword(changePasswordEntity.getPassword());
            boolean b = userService.updateUserInfo(user);
            resp.success(b);
            resp.setMsg("修改成功");
        }else{
            throw new BusinessException("验证码错误");
        }
        return resp;
    }

    @GetMapping("/getUserById")
    public User getUserById(@RequestParam Long id){
        return userService.getUserById(id);
    }


    @GetMapping("/getUserByNickname")
    public CommonResp<List<User>> getUserListByNickName(@RequestParam String nickname){
        CommonResp<List<User>> resp = new CommonResp<>();
        resp.success(userService.selectUserByNickname(nickname));
        return resp;
    }

    @PostMapping("/bind")
    @TokenRequired
    public CommonResp<Boolean> bindEmail(@RequestBody ChangePasswordEntity changePasswordEntity){
        User user = userService.getUserById(getUserId());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if(Objects.isNull(user)){
            throw new BusinessException("用户不存在");
        }
        if(changePasswordEntity.getEmail() == null || changePasswordEntity.getCode() == null){
            throw new BusinessException("缺少关键信息！");
        }
        wrapper.eq(User::getEmail,changePasswordEntity.getEmail());
        User one = userService.getOne(wrapper);
        if(one != null){
            throw new BusinessException("该邮箱已经绑定了用户！");
        }
        boolean codeB = userService.validCode(changePasswordEntity.getEmail(), changePasswordEntity.getCode());
        if(!codeB){
            throw new BusinessException("验证码不正确！");
        }
        user.setEmail(changePasswordEntity.getEmail());
        boolean b = userService.updateUserInfo(user);
        CommonResp<Boolean> resp = new CommonResp<>();
        if(b){
            resp.success(true);
            resp.setMsg("绑定成功");
        }else{
            resp.fail("绑定失败");
        }
        return resp;
    }

    private Long getUserId(){
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserId(token);
        return userId;
    }

}
