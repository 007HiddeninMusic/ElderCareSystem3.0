package com.eldercare.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日期格式化工具类
 * 功能：统一日期/时间显示格式，避免格式混乱
 * 支持当前时间、指定时间的格式化，适配不同业务场景
 */
public class DateFormatUtil {
    // 线程安全：每个线程持有独立的SimpleDateFormat实例（避免多线程并发问题）
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = ThreadLocal.withInitial(() ->
            new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    );
    private static final ThreadLocal<SimpleDateFormat> DATETIME_FORMATTER = ThreadLocal.withInitial(() ->
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    );
    private static final ThreadLocal<SimpleDateFormat> TIME_FORMATTER = ThreadLocal.withInitial(() ->
            new SimpleDateFormat("HH:mm:ss", Locale.CHINA)
    );

    /**
     * 格式化当前日期（短日期：yyyy-MM-dd）
     * @return 当前日期，如2025-12-01
     */
    public static String formatCurrentDate() {
        return formatCurrentDateTime("yyyy-MM-dd");
    }

    /**
     * 格式化当前时间（完整时间：yyyy-MM-dd HH:mm:ss）
     * @return 当前时间，如2025-12-01 12:30:45
     */
    public static String formatCurrentDateTime() {
        return formatCurrentDateTime("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 格式化指定日期（短日期：yyyy-MM-dd）
     * @param date 待格式化的日期
     * @return 格式化后的日期，如2025-12-01
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return DATE_FORMATTER.get().format(date);
    }

    /**
     * 格式化指定时间（完整时间：yyyy-MM-dd HH:mm:ss）
     * @param date 待格式化的时间
     * @return 格式化后的时间，如2025-12-01 12:30:45
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return DATETIME_FORMATTER.get().format(date);
    }

    /**
     * 格式化指定时间（仅时间：HH:mm:ss）
     * @param date 待格式化的时间
     * @return 格式化后的时间，如12:30:45
     */
    public static String formatTime(Date date) {
        if (date == null) {
            return "";
        }
        return TIME_FORMATTER.get().format(date);
    }

    /**
     * 格式化当前时间（自定义格式）
     * @param pattern 格式模板，如"yyyyMMddHHmmss"生成20251201123045
     * @return 自定义格式的当前时间
     */
    public static String formatCurrentDateTime(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("日期格式模板【pattern】不能为空");
        }
        return new SimpleDateFormat(pattern, Locale.CHINA).format(new Date());
    }

    /**
     * 格式化指定时间（自定义格式）
     * @param date 待格式化的时间
     * @param pattern 格式模板
     * @return 自定义格式的时间
     */
    public static String formatDateTime(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("日期格式模板【pattern】不能为空");
        }
        return new SimpleDateFormat(pattern, Locale.CHINA).format(date);
    }

    /**
     * 资源释放（避免内存泄露）
     */
    public static void remove() {
        DATE_FORMATTER.remove();
        DATETIME_FORMATTER.remove();
        TIME_FORMATTER.remove();
    }
}