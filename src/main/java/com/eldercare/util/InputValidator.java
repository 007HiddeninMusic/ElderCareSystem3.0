package com.eldercare.util;

import java.util.regex.Pattern;

/**
 * 输入校验工具类
 * 功能：统一校验用户输入的合法性
 * 覆盖手机号、年龄、心率、血压、活动时间等核心业务字段
 */
public class InputValidator {
    // 正则表达式：中国大陆手机号（11位数字，以13-9开头）
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    // 正则表达式：血压格式（如120/80、130/90 mmHg，支持带单位和不带单位）
    private static final Pattern BLOOD_PRESSURE_PATTERN = Pattern.compile("^\\d{2,3}/\\d{2,3}( mmHg)?$");
    // 正则表达式：活动时间格式（如2025-12-10 09:00、2025-12-10 09:00-11:00）
    private static final Pattern ACTIVITY_TIME_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}(-\\d{2}:\\d{2})?$");
    // 正则表达式：账号格式（4-20位字母/数字/下划线）
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{4,20}$");

    /**
     * 校验手机号格式
     * @param phone 手机号，如13800138000
     * @return true-格式合法，false-格式非法
     */
    public static boolean isPhoneValid(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * 校验年龄范围（1-120岁，符合养老场景合理范围）
     * @param age 年龄
     * @return true-范围合法，false-范围非法
     */
    public static boolean isAgeValid(int age) {
        return age >= 1 && age <= 120;
    }

    /**
     * 校验心率范围（50-150次/分钟，覆盖老人正常/特殊情况）
     * @param heartRate 心率
     * @return true-范围合法，false-范围非法
     */
    public static boolean isHeartRateValid(int heartRate) {
        return heartRate >= 50 && heartRate <= 150;
    }

    /**
     * 校验血压格式
     * @param bloodPressure 血压，如120/80、130/90 mmHg
     * @return true-格式合法，false-格式非法
     */
    public static boolean isBloodPressureValid(String bloodPressure) {
        if (bloodPressure == null || bloodPressure.trim().isEmpty()) {
            return false;
        }
        return BLOOD_PRESSURE_PATTERN.matcher(bloodPressure.trim()).matches();
    }

    /**
     * 校验活动时间格式
     * @param time 活动时间，如2025-12-10 09:00
     * @return true-格式合法，false-格式非法
     */
    public static boolean isActivityTimeValid(String time) {
        if (time == null || time.trim().isEmpty()) {
            return false;
        }
        return ACTIVITY_TIME_PATTERN.matcher(time.trim()).matches();
    }

    /**
     * 校验账号格式（4-20位字母/数字/下划线）
     * @param userId 账号，如admin、elder_001
     * @return true-格式合法，false-格式非法
     */
    public static boolean isUserIdValid(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        return USER_ID_PATTERN.matcher(userId.trim()).matches();
    }

    /**
     * 校验字符串非空（避免空字符串、纯空格）
     * @param str 待校验字符串
     * @return true-非空，false-空或纯空格
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 校验手机号，非法则抛出异常
     * @param phone 手机号
     * @throws IllegalArgumentException 手机号非法时抛出
     */
    public static void validatePhone(String phone) {
        if (!isPhoneValid(phone)) {
            throw new IllegalArgumentException("手机号格式非法！请输入11位中国大陆手机号（如13800138000）");
        }
    }

    /**
     * 校验年龄，非法则抛出异常
     * @param age 年龄
     * @throws IllegalArgumentException 年龄非法时抛出
     */
    public static void validateAge(int age) {
        if (!isAgeValid(age)) {
            throw new IllegalArgumentException("年龄范围非法！请输入1-120岁之间的年龄");
        }
    }

    /**
     * 校验心率，非法则抛出异常
     * @param heartRate 心率
     * @throws IllegalArgumentException 心率非法时抛出
     */
    public static void validateHeartRate(int heartRate) {
        if (!isHeartRateValid(heartRate)) {
            throw new IllegalArgumentException("心率范围非法！请输入50-150次/分钟之间的心率");
        }
    }

    /**
     * 校验血压，非法则抛出异常
     * @param bloodPressure 血压
     * @throws IllegalArgumentException 血压非法时抛出
     */
    public static void validateBloodPressure(String bloodPressure) {
        if (!isBloodPressureValid(bloodPressure)) {
            throw new IllegalArgumentException("血压格式非法！请输入正确格式（如120/80 或 120/80 mmHg）");
        }
    }

    /**
     * 校验活动时间，非法则抛出异常
     * @param time 活动时间
     * @throws IllegalArgumentException 时间格式非法时抛出
     */
    public static void validateActivityTime(String time) {
        if (!isActivityTimeValid(time)) {
            throw new IllegalArgumentException("活动时间格式非法！示例：2025-12-10 09:00 或 2025-12-10 09:00-11:00");
        }
    }

    /**
     * 校验账号格式，非法则抛出异常
     * @param userId 账号
     * @throws IllegalArgumentException 账号格式非法时抛出
     */
    public static void validateUserId(String userId) {
        if (!isUserIdValid(userId)) {
            throw new IllegalArgumentException("账号格式非法！请输入4-20位字母/数字/下划线（如admin、elder_001）");
        }
    }

    /**
     * 校验字符串非空，空则抛出异常
     * @param str 待校验字符串
     * @param fieldName 字段名称（用于异常提示，如"姓名""活动名称"）
     * @throws IllegalArgumentException 字符串为空时抛出
     */
    public static void validateNotEmpty(String str, String fieldName) {
        if (!isNotEmpty(str)) {
            throw new IllegalArgumentException(fieldName + "不能为空！");
        }
    }
}