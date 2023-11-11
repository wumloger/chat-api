package top.wml.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.wml.common.entity.User;
import top.wml.common.exception.BusinessException;
import top.wml.common.utils.CodeGenerator;
import top.wml.common.utils.JwtUtil;
import top.wml.common.utils.RedisUtil;
import top.wml.common.utils.StringUtil;
import top.wml.user.mapper.UserMapper;
import top.wml.user.service.EmailService;
import top.wml.user.service.UserService;

import java.util.List;
import java.util.Objects;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private EmailService emailService;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public String login(String username, String password) {
        // 检查账号和密码是否为空
        if(StringUtil.isBlank(username) || StringUtil.isBlank(password)){
            throw new BusinessException("账号和密码不能为空！");
        }

        // 创建查询条件，查询指定账号的用户信息
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);

        // 根据查询条件查询用户信息
        User user = userMapper.selectOne(wrapper);

        // 判断用户是否存在
        if(Objects.isNull(user)){
            throw new BusinessException("不存在这个用户,请先去注册！");
        }

        // 判断密码是否正确
        if(!user.getPassword().equals(password)){
            throw new BusinessException("密码错误！");
        }

        // 登录成功，返回token
        String token = JwtUtil.createToken(user);
        return token;
    }

    /**
     * 注册用户
     * @param username 用户名
     * @param password 密码
     * @return 注册结果
     */
    public boolean register(String username, String password) {
        // 检查账号和密码是否为空
        if (StringUtil.isBlank(username) || StringUtil.isBlank(password)) {
            throw new BusinessException("账号和密码不能为空！");
        }
        // 检查用户名是否已存在
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0) {
            throw new BusinessException("账号已存在！");
        }
        // 构建用户对象
        User user = User.builder()
                .username(username)
                .password(password)
                .nickname("新用户" + username)
                .avatar("https://pic.imgdb.cn/item/64721a94f024cca1737ae3a0.png")
                .sex("未知")
                .status((byte) 1)
                .build();
        // 插入用户数据到数据库
        int insert = userMapper.insert(user);
        return insert > 0;
    }

    public boolean updateUserInfo(User user) {
        // 检查用户是否为空
        if (Objects.isNull(user)) {
            throw new BusinessException("用户信息不能为空！");
        }
        // 更新用户信息
        int update = userMapper.updateById(user);
        return update > 0;
    }

    @Override
    public User getUserById(Long id) {
        if(Objects.isNull(id)){
            throw new BusinessException("用户id不能为空！");
        }
        User user = userMapper.selectById(id);
        if(Objects.isNull(user)){
            throw new BusinessException("用户不存在！");
        }
        return user;
    }

    @Override
    public List<User> getUserList() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, 1);
        List<User> users = userMapper.selectList(wrapper);
        return users;
    }

    public boolean validCode(String email,String code){
        if(!this.verifyEmailFormat(email)){
            throw new BusinessException("邮箱格式不正确！");
        }
        Object obj = redisUtil.get(email);
        if(obj == null){
            throw new BusinessException("验证码已过期，请重新获取！");
        }
        String validCode = obj.toString();

        if(!validCode.equals(code)){
            return false;
        }
        return true;
    }

    public String sendEmailForCode(String email){
        if(!this.verifyEmailFormat(email)){
            throw new BusinessException("邮箱格式不正确！");
        }
        //生成验证码
        String code = CodeGenerator.generateNumericCode(6);
        if(redisUtil.get(email)!= null){
            throw new BusinessException("验证码仍然有效，若没有收到请等待三分钟后重发！");
        }
        redisUtil.set(email,code,60 * 3);
        emailService.sendEmail(email,"SimpleChat验证码",
                """
                        尊敬的用户，
                                               
                        您的验证码是：< %s >
                                                
                        请在 3分钟 内使用该验证码完成验证操作。
                                                
                        请注意，此验证码仅用于一次验证，不要将验证码泄露给他人。""".formatted(code)
        );
        return code;
    }

    @Override
    public User getUserByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail,email);
        User user = userMapper.selectOne(wrapper);
        return user;
    }

    /**
     * 验证邮箱格式是否正确
     * @param email 待验证的邮箱
     * @return 邮箱格式是否正确
     */
    private boolean verifyEmailFormat(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}
