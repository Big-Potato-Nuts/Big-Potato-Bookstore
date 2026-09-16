package org.example.common.util;

import java.util.Arrays;
import java.util.List;

/**
 * 简单敏感词过滤（演示用，可后续接入正式词库）
 */
public final class SensitiveWordUtil {

    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "傻逼", "妈的", "操你妈", "垃圾广告", "代充", "加微信", "赌博", "诈骗"
    );

    private SensitiveWordUtil() {
    }

    /**
     * 判断文本是否包含敏感词
     */
    public static boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (String word : SENSITIVE_WORDS) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
