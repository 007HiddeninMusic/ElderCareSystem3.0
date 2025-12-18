// src/main/java/com/eldercare/util/WindowUtil.java
package com.eldercare.util;

import javax.swing.*;
import java.awt.*;

/**
 * 窗口工具类
 * 功能：提供窗口居中、消息弹窗、窗口标题生成等UI辅助方法
 */
public class WindowUtil {
    /**
     * 将窗口居中显示在屏幕上
     * @param window 要居中的窗口对象
     */
    public static void centerWindow(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = window.getSize();
        // 如果窗口大小为0，使用默认尺寸
        if (frameSize.width == 0 || frameSize.height == 0) {
            frameSize = new Dimension(400, 300);
        }
        window.setLocation(
                (screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2
        );
    }

    /**
     * 显示错误消息弹窗
     * @param parent 父组件，用于确定弹窗位置
     * @param message 要显示的错误消息内容
     */
    public static void showErrorMsg(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * 显示成功消息弹窗
     * @param parent 父组件
     * @param message 要显示的成功消息内容
     */
    public static void showSuccessMsg(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 显示警告消息弹窗
     * @param parent 父组件
     * @param message 要显示的警告消息内容
     */
    public static void showWarningMsg(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "警告", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * 显示确认对话框
     * @param parent 父组件
     * @param message 要显示的确认消息
     * @return true-用户选择"是"，false-用户选择"否"
     */
    public static boolean showConfirmMsg(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    
    /**
     * 显示自定义标题的确认对话框
     * @param parent 父组件
     * @param message 要显示的确认消息
     * @param title 对话框标题
     * @return true-用户选择"是"，false-用户选择"否"
     */
    public static boolean showConfirmMsg(Component parent, String message, String title) {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /**
     * 生成系统前缀的窗口标题
     * @param title 原始标题
     * @return 格式化后的完整标题
     */
    public static String getWindowTitle(String title) {
        return "养老院管理系统 - " + title;
    }

    /**
     * 将窗口设置为全屏模式
     * @param frame 要设置为全屏的窗口
     */
    public static void setFullScreen(JFrame frame) {
        if (frame != null) {
            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (device.isFullScreenSupported()) {
                frame.dispose();
                frame.setUndecorated(true);
                device.setFullScreenWindow(frame);
                frame.setVisible(true);
            } else {
                setMaximized(frame); // 如果不支持全屏，则最大化窗口
            }
        }
    }

    /**
     * 将窗口设置为窗口模式
     * @param frame 要设置为窗口模式的窗口
     * @param width 窗口宽度
     * @param height 窗口高度
     */
    public static void setWindowMode(JFrame frame, int width, int height) {
        if (frame != null) {
            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (device.getFullScreenWindow() == frame) {
                device.setFullScreenWindow(null);
            }
            frame.dispose();
            frame.setUndecorated(false);
            frame.setSize(width, height);
            centerWindow(frame);
            frame.setVisible(true);
        }
    }

    /**
     * 最大化窗口
     * @param frame 要最大化的窗口
     */
    public static void setMaximized(JFrame frame) {
        if (frame != null) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    /**
     * 切换窗口的全屏状态
     * @param frame 要切换全屏状态的窗口
     * @param width 窗口模式下的宽度
     * @param height 窗口模式下的高度
     * @return true表示已切换到全屏模式，false表示已切换到窗口模式
     */
    public static boolean toggleFullScreen(JFrame frame, int width, int height) {
        if (frame != null) {
            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (device.getFullScreenWindow() == frame) {
                setWindowMode(frame, width, height);
                return false;
            } else {
                setFullScreen(frame);
                return true;
            }
        }
        return false;
    }
}