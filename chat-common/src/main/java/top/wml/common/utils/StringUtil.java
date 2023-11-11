package top.wml.common.utils;

/**
 * String工具类
 */
public class StringUtil {
    /**
     * 判断字符串是否为空
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
    /**
     * 判断字符串是否不为空
     * @param str 字符串
     * @return 是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    /**
     * 判断字符串是否为空白
     * @param str 字符串
     * @return 是否为空白
     */
    public static boolean isBlank(String str) {
        int length;
        if ((str == null) || ((length = str.length()) == 0)) {
            return true;
        }
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否不为空或不为空格
     *
     * @param str 要判断的字符串
     * @return 如果字符串不为空或不为空格，则返回true；否则返回false
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    /**
     * 去除字符串两端的空格
     * @param str 字符串
     * @return 去除两端空格后的字符串
     */
    public static String trim(String str) {
        return str == null? null : str.trim();
    }
    /**
     * 将字符串中的大写字母转换为小写字母，并用下划线将大写字母连接起来。
     * 如果输入字符串为null，则返回null。
     *
     * @param str 要转换的字符串
     * @return 转换后的字符串
     */
    public static String toUnderScoreCase(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean upperCase = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (!upperCase || i == 0) {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
    }
}
