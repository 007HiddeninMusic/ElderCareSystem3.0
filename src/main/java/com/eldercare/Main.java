// src/main/java/com/eldercare/MainApp.java
package com.eldercare;

import com.eldercare.ui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 使用SwingUtilities确保线程安全
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置系统UI风格
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // 显示登录窗口
                new LoginFrame().setVisible(true);

                System.out.println("养老院管理系统启动成功");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "系统启动失败: " + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}