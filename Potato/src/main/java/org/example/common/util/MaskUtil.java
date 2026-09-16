package org.example.common.util;

/**
 * 敏感信息脱敏工具
 */
public final class MaskUtil {

    private MaskUtil() {
    }

    /**
     * 身份证号脱敏：保留前 6 位与后 4 位，中间用 * 填充
     * 示例：360101199001011234 -> 360101********1234
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        int keepHead = 6;
        int keepTail = 4;
        return idCard.substring(0, keepHead)
                + "*".repeat(idCard.length() - keepHead - keepTail)
                + idCard.substring(idCard.length() - keepTail);
    }
}
