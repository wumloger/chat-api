package top.wml.common.utils;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import top.wml.common.entity.User;

import java.util.HashMap;
import java.util.Map;


@Slf4j
public class JwtUtil {

    /**
     * 盐值很重要，不能泄漏，且每个项目都应该不一样，可以放到配置文件中
     */
    private static final String KEY = "wml";

    public static String createToken(User user) {
        DateTime now = DateTime.now();
        DateTime expTime = now.offsetNew(DateField.HOUR, 48);
        Map<String, Object> payload = new HashMap<>();
        // 签发时间
        payload.put(JWTPayload.ISSUED_AT, now);
        // 过期时间
        payload.put(JWTPayload.EXPIRES_AT, expTime);
        // 生效时间
        payload.put(JWTPayload.NOT_BEFORE, now);
        // 内容
        payload.put("id", user.getId().toString());
        payload.put("username",user.getUsername());
        payload.put("nickname",user.getNickname());
        payload.put("sex",user.getSex());
        payload.put("avatar",user.getAvatar());
        payload.put("email",user.getEmail());
        payload.put("birthday",user.getBirthday());

        String token = JWTUtil.createToken(payload, KEY.getBytes());
        log.info("生成 JWT token：{}", token);
        return token;
    }

    public static boolean validate(String token) {
        JWT jwt = JWTUtil.parseToken(token).setKey(KEY.getBytes());
        // validate包含了verify
        boolean validate = jwt.validate(0);
        log.info("JWT token 校验结果：{}", validate);
        return validate;
    }

    public static JSONObject getJSONObject(String token) {
        JWT jwt = JWTUtil.parseToken(token).setKey(KEY.getBytes());
        JSONObject payloads = jwt.getPayloads();
        payloads.remove(JWTPayload.ISSUED_AT);
        payloads.remove(JWTPayload.EXPIRES_AT);
        payloads.remove(JWTPayload.NOT_BEFORE);
        log.info("根据 token 获取原始内容：{}", payloads);
        return payloads;
    }

    public static Long getUserId(String token){
        return getJSONObject(token).get("id", Long.class);
    }
    public static String getNickname(String token){
        return getJSONObject(token).get("nickname", String.class);
    }
    public static String getAvatar(String token){
        return getJSONObject(token).get("avatar", String.class);
    }

}