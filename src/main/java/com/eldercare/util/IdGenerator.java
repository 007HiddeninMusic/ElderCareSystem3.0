package com.eldercare.util;

import java.util.Random;

/**
 * 唯一ID生成工具类
 * 功能：生成业务唯一ID，格式可读性强、全局唯一
 * 支持老人、健康记录、服务申请、活动等模块的ID生成
 */
public class IdGenerator {
    // 随机数生成器（静态单例，避免频繁创建对象）
    private static final Random RANDOM = new Random();
    // 随机数位数（3位，确保同一毫秒内生成多个ID也不重复）
    private static final int RANDOM_DIGITS = 3;

    /**
     * 生成老人唯一ID（前缀ELDER_）
     * 格式：ELDER_时间戳_随机数
     * @return 老人ID，如ELDER_20251201123045_123
     */
    public static String generateElderId() {
        return generateBusinessId("ELDER");
    }

    /**
     * 生成健康记录唯一ID（前缀HEALTH_）
     * @return 健康记录ID，如HEALTH_20251201123045_123
     */
    public static String generateHealthRecordId() {
        return generateBusinessId("HEALTH");
    }

    /**
     * 生成服务申请唯一ID（前缀SERVICE_）
     * @return 服务申请ID，如SERVICE_20251201123045_123
     */
    public static String generateServiceRequestId() {
        return generateBusinessId("SERVICE");
    }

    /**
     * 生成活动唯一ID（前缀ACTIVITY_）
     * @return 活动ID，如ACTIVITY_20251201123045_123
     */
    public static String generateActivityId() {
        return generateBusinessId("ACTIVITY");
    }

    /**
     * 生成用户唯一ID（前缀USER_）
     * @return 用户ID，如USER_20251201123045_123
     */
    public static String generateUserId() {
        return generateBusinessId("USER");
    }

    /**
     * 生成通用业务ID（前缀+时间戳+随机数）
     * @param prefix 业务前缀，如ELDER、HEALTH
     * @return 业务唯一ID
     */
    private static String generateBusinessId(String prefix) {
        // 1. 获取时间戳（yyyyMMddHHmmss，14位，如20251201123045）
        String timestamp = DateFormatUtil.formatCurrentDateTime("yyyyMMddHHmmss");
        // 2. 生成随机数（3位，000-999，不足3位补0）
        String randomNum = String.format("%0" + RANDOM_DIGITS + "d", RANDOM.nextInt((int) Math.pow(10, RANDOM_DIGITS)));
        // 3. 拼接ID（前缀_时间戳_随机数）
        return String.format("%s_%s_%s", prefix, timestamp, randomNum);
    }
}