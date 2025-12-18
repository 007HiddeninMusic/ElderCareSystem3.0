// src/main/java/com/eldercare/ui/RegisterDialog.java
package com.eldercare.ui;

import com.eldercare.service.UserService;
import com.eldercare.util.WindowUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 注册对话框
 * 功能：新用户注册
 */
public class RegisterDialog extends JDialog {
    // UI组件
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> roleComboBox;
    private JButton registerButton;
    private JButton cancelButton;
    private UserService userService;

    /**
     * 构造方法
     * @param parent 父窗口
     */
    public RegisterDialog(JFrame parent) {
        super(parent, "用户注册", true);
        userService = UserService.getInstance();
        initComponents();
        setupLayout();
        setupListeners();
    }

    /**
     * 初始化组件
     */
    private void initComponents() {
        setSize(400, 400);
        setResizable(false);
        WindowUtil.centerWindow(this);

        JLabel titleLabel = new JLabel("用户注册", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 102, 204));

        JLabel userIdLabel = new JLabel("账号:");
        userIdField = new JTextField(20);

        JLabel passwordLabel = new JLabel("密码:");
        passwordField = new JPasswordField(20);

        JLabel confirmPasswordLabel = new JLabel("确认密码:");
        confirmPasswordField = new JPasswordField(20);

        JLabel roleLabel = new JLabel("角色:");
        String[] roles = {"请选择角色", "admin-管理员", "elder-老人", "family-家属", "caregiver-护工"};
        roleComboBox = new JComboBox<>(roles);

        registerButton = new JButton("注册");
        registerButton.setBackground(new Color(70, 130, 180));
        registerButton.setForeground(Color.BLACK);

        cancelButton = new JButton("取消");
    }

    /**
     * 设置布局
     */
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("新用户注册"));
        titlePanel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 账号输入
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("账号:"), gbc);

        gbc.gridx = 1;
        formPanel.add(userIdField, gbc);

        // 密码输入
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("密码:"), gbc);

        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        // 确认密码输入
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("确认密码:"), gbc);

        gbc.gridx = 1;
        formPanel.add(confirmPasswordField, gbc);

        // 角色选择
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("角色:"), gbc);

        gbc.gridx = 1;
        formPanel.add(roleComboBox, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * 设置事件监听器
     */
    private void setupListeners() {
        // 注册按钮监听
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performRegistration();
            }
        });

        // 取消按钮监听
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    /**
     * 执行注册操作
     */
    private void performRegistration() {
        String userId = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String selectedRole = (String) roleComboBox.getSelectedItem();

        // 验证输入
        if (userId.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            WindowUtil.showErrorMsg(this, "所有字段都必须填写");
            return;
        }

        if (!password.equals(confirmPassword)) {
            WindowUtil.showErrorMsg(this, "两次输入的密码不一致");
            return;
        }

        if (selectedRole == null || selectedRole.equals("请选择角色")) {
            WindowUtil.showErrorMsg(this, "请选择用户角色");
            return;
        }

        // 提取角色代码
        String role = selectedRole.split("-")[0];

        try {
            userService.registerUser(userId, password, role);
            WindowUtil.showSuccessMsg(this, "注册成功！请使用新账号登录");
            dispose();
        } catch (Exception ex) {
            WindowUtil.showErrorMsg(this, "注册失败: " + ex.getMessage());
        }
    }
}