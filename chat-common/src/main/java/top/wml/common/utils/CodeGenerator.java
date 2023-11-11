package top.wml.common.utils;
import java.security.SecureRandom;

public class CodeGenerator {

    // 生成数字验证码
    public static String generateNumericCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10)); // 生成0到9之间的随机数字
        }
        return code.toString();
    }

    // 生成包含数字和字母的混合验证码
    public static String generateAlphaNumericCode(int length) {
        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }

}
