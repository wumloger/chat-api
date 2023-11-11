package top.wml.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wml.common.entity.User;

import java.util.List;

public interface UserService extends IService<User> {
    String login(String username,String password);
    boolean register(String username, String password);
    boolean updateUserInfo(User user);
    User getUserById(Long id);
    List<User> getUserList();
    boolean validCode(String email,String code);
    String sendEmailForCode(String email);
    User getUserByEmail(String email);
}
