// src/main/java/com/eldercare/ui/LoginFrame.java
package com.eldercare.ui;

import com.eldercare.model.User;
import com.eldercare.service.UserService;
import com.eldercare.util.WindowUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 登录界面
 * 功能：用户登录、注册账号
 */
public class LoginFrame extends JFrame {
    // UI组件
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private UserService userService;

    /**
     * 构造方法
     */
    public LoginFrame() {
        userService = UserService.getInstance();
        initComponents();
        setupLayout();
        setupListeners();
    }

    /**
     * 初始化组件
     */
    private void initComponents() {
        setTitle(WindowUtil.getWindowTitle("用户登录"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setResizable(false);
        WindowUtil.centerWindow(this);

        // 创建组件
        JLabel titleLabel = new JLabel("养老院管理系统", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 204));

        JLabel userIdLabel = new JLabel("账号:");
        userIdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        userIdField = new JTextField(20);
        userIdField.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);

        registerButton = new JButton("注册新账号");
        registerButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        registerButton.setForeground(new Color(0, 102, 204));

        // 设置布局
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 表单面板 
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("登录信息"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 账号输入框
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(userIdLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0; // 确保输入框可以随布局变化而扩展
        formPanel.add(userIdField, gbc);
        gbc.weightx = 0; // 重置权重

        // 密码输入框
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0; // 确保输入框可以随布局变化而扩展
        formPanel.add(passwordField, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 使用 SwingUtilities.invokeLater 延迟请求焦点
        SwingUtilities.invokeLater(() -> userIdField.requestFocusInWindow());
    }

    /**
     * 设置布局
     */
    private void setupLayout() {
        //所有布局工作已合并到 initComponents 中
    }

    /**
     * 设置事件监听器
     */
    private void setupListeners() {
        // 登录按钮监听
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        // 注册按钮监听
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegisterDialog();
            }
        });

        // 按回车键登录
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
    }

    /**
     * 执行登录操作
     */
    private void performLogin() {
        String userId = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());

        // 输入验证
        if (userId.isEmpty() || password.isEmpty()) {
            WindowUtil.showErrorMsg(this, "账号和密码不能为空");
            return;
        }

        try {
            User user = userService.login(userId, password);
            if (user != null) {
                WindowUtil.showSuccessMsg(this, "登录成功！欢迎 " + user.getUserId() + " (" + getRoleChinese(user.getRole()) + ")");
                dispose(); // 关闭登录窗口

                // 根据角色打开对应的主界面
                openRoleSpecificFrame(user);
            } else {
                WindowUtil.showErrorMsg(this, "登录失败，请检查账号和密码");
            }
        } catch (Exception ex) {
            WindowUtil.showErrorMsg(this, "登录失败: " + ex.getMessage());
        }
    }

    /**
     * 获取角色中文名称
     * @param role 角色英文标识
     * @return 角色中文名称
     */
    private String getRoleChinese(String role) {
        switch (role) {
            case "admin": return "管理员";
            case "elder": return "老人";
            case "family": return "家属";
            case "caregiver": return "护工";
            default: return role;
        }
    }

    /**
     * 显示注册对话框
     */
    private void showRegisterDialog() {
        // 假设 RegisterDialog 类存在
        RegisterDialog registerDialog = new RegisterDialog(this); 
        registerDialog.setVisible(true);
    }

    /**
     * 根据角色打开对应的主界面
     * @param user 用户对象
     */
    private void openRoleSpecificFrame(User user) {
        String role = user.getRole();
        JFrame roleFrame = null;

        // 根据角色打开不同的主界面
        switch (role) {
            case "admin":
                roleFrame = new AdminFrame(user); 
                break;
            case "elder":
                roleFrame = new ElderFrame(user); 
                break;
            case "family":
                roleFrame = new FamilyFrame(user); 
                break;
            case "caregiver":
                roleFrame = new CaregiverFrame(user); 
                break;
        }

        if (roleFrame != null) {
            roleFrame.setVisible(true);
        }
    }
}