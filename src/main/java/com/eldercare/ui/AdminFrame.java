// src/main/java/com/eldercare/ui/AdminFrame.java
package com.eldercare.ui;

import com.eldercare.model.Activity;
import com.eldercare.model.Elder;
import com.eldercare.model.User;
import com.eldercare.service.*;
import com.eldercare.util.WindowUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * 管理员用户主界面
 * 功能：提供管理员用户的各项功能，包括用户管理、老人管理、活动管理等
 */
public class AdminFrame extends JFrame {
    // 当前登录用户
    private User currentUser;
    private JTabbedPane tabbedPane;
    
    // 窗口默认大小
    private static final int DEFAULT_WIDTH = 900;
    private static final int DEFAULT_HEIGHT = 600;

    // 服务类实例
    private UserService userService;
    private ElderService elderService;
    private ActivityService activityService;

    // 用户管理组件
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private JButton refreshUsersButton;
    private JButton addUserButton;

    // 老人管理组件
    private JTable elderTable;
    private DefaultTableModel elderTableModel;
    private JButton refreshEldersButton;
    private JButton addElderButton;
    private JButton deleteElderButton;

    // 活动管理组件
    private JTable activityTable;
    private DefaultTableModel activityTableModel;
    private JButton refreshActivitiesButton;
    private JButton createActivityButton;

    /**
     * 构造方法
     * @param user 当前登录的用户对象
     */
    public AdminFrame(User user) {
        this.currentUser = user;
        userService = UserService.getInstance();
        elderService = ElderService.getInstance();
        activityService = ActivityService.getInstance();

        initComponents();
        setupLayout();
        setupListeners();
        loadInitialData();
    }

    /**
     * 初始化所有组件
     */
    private void initComponents() {
        setTitle(WindowUtil.getWindowTitle("管理员面板 - " + currentUser.getUserId()));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setResizable(true); // 确保窗口可以调整大小
        WindowUtil.centerWindow(this);

        // 创建菜单栏
        createMenuBar();

        // 创建选项卡面板
        tabbedPane = new JTabbedPane();

        // 创建各个功能面板
        JPanel userPanel = createUserManagementPanel();
        tabbedPane.addTab("用户管理", userPanel);

        JPanel elderPanel = createElderManagementPanel();
        tabbedPane.addTab("老人管理", elderPanel);

        JPanel activityPanel = createActivityManagementPanel();
        tabbedPane.addTab("活动管理", activityPanel);

        JPanel systemPanel = createSystemInfoPanel();
        tabbedPane.addTab("系统信息", systemPanel);

        add(tabbedPane, BorderLayout.CENTER);
        setupUserTableEditors();
    }

    /**
     * 创建菜单栏
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 系统菜单
        JMenu systemMenu = new JMenu("系统");
        JMenuItem logoutItem = new JMenuItem("退出登录");
        logoutItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要退出登录吗？",
                    "确认退出",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        
        // 添加全屏切换菜单项
        JMenuItem toggleFullScreenItem = new JMenuItem("切换全屏");
        toggleFullScreenItem.addActionListener(e -> {
            WindowUtil.toggleFullScreen(this, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        });
        
        JMenuItem exitItem = new JMenuItem("退出系统");
        exitItem.addActionListener(e -> System.exit(0));
        systemMenu.add(logoutItem);
        systemMenu.add(toggleFullScreenItem);
        systemMenu.addSeparator();
        systemMenu.add(exitItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "养老院管理系统 v1.0\n管理员面板",
                        "关于",
                        JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(systemMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    /**
     * 创建用户管理面板
     * @return 用户管理面板
     */
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshUsersButton = new JButton("刷新列表");
        addUserButton = new JButton("添加用户");
        buttonPanel.add(refreshUsersButton);
        buttonPanel.add(addUserButton);

        // 用户表格
        String[] columns = {"账号", "角色", "操作"};
        userTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 只有操作列可编辑
                return column == 2;
            }
        };
        userTable = new JTable(userTableModel);
        JScrollPane scrollPane = new JScrollPane(userTable);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建老人管理面板
     * @return 老人管理面板
     */
    private JPanel createElderManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshEldersButton = new JButton("刷新列表");
        addElderButton = new JButton("添加老人");
        deleteElderButton = new JButton("删除选中");
        buttonPanel.add(refreshEldersButton);
        buttonPanel.add(addElderButton);
        buttonPanel.add(deleteElderButton);

        // 老人表格
        String[] columns = {"老人ID", "姓名", "年龄", "手机号", "健康状况"};
        elderTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 所有单元格不可编辑
            }
        };
        elderTable = new JTable(elderTableModel);
        JScrollPane scrollPane = new JScrollPane(elderTable);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建活动管理面板
     * @return 活动管理面板
     */
    private JPanel createActivityManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshActivitiesButton = new JButton("刷新列表");
        createActivityButton = new JButton("创建活动");
        buttonPanel.add(refreshActivitiesButton);
        buttonPanel.add(createActivityButton);

        // 活动表格
        String[] columns = {"活动ID", "名称", "时间", "地点", "报名人数", "描述"};
        activityTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        activityTable = new JTable(activityTableModel);
        JScrollPane scrollPane = new JScrollPane(activityTable);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建系统信息面板
     * @return 系统信息面板
     */
    private JPanel createSystemInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        infoArea.setText(
                "养老院管理系统 - 管理员面板\n\n" +
                        "当前用户: " + currentUser.getUserId() + " (管理员)\n" +
                        "系统时间: " + java.time.LocalDateTime.now() + "\n\n" +
                        "管理员权限:\n" +
                        "1. 用户账号管理\n" +
                        "2. 老人信息管理\n" +
                        "3. 活动创建与管理\n" +
                        "4. 系统数据维护\n\n" +
                        "使用说明:\n" +
                        "- 通过选项卡切换不同功能模块\n" +
                        "- 使用菜单栏进行系统操作\n" +
                        "- 定期备份系统数据"
        );

        panel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        return panel;
    }

    /**
     * 设置布局（各组件已在创建方法中设置）
     */
    private void setupLayout() {
        // 布局已在各组件创建方法中设置
    }

    /**
     * 设置事件监听器
     */
    private void setupListeners() {
        // 用户管理按钮监听
        refreshUsersButton.addActionListener(e -> loadUsers());
        addUserButton.addActionListener(e -> showAddUserDialog());

        // 老人管理按钮监听
        refreshEldersButton.addActionListener(e -> loadElders());
        addElderButton.addActionListener(e -> showAddElderDialog());
        deleteElderButton.addActionListener(e -> deleteSelectedElder());

        // 活动管理按钮监听
        refreshActivitiesButton.addActionListener(e -> loadActivities());
        createActivityButton.addActionListener(e -> showCreateActivityDialog());
    }

    /**
     * 加载初始数据
     */
    private void loadInitialData() {
        loadUsers();
        loadElders();
        loadActivities();
    }

    /**
     * 加载用户数据
     */
    private void loadUsers() {
        try {
            userTableModel.setRowCount(0);
            var users = userService.getAllUsers();
            for (User user : users) {
                Object[] row = {
                        user.getUserId(),
                        getRoleChinese(user.getRole()),
                        "编辑"
                };
                userTableModel.addRow(row);
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "加载用户失败: " + e.getMessage());
        }
    }

    /**
     * 设置用户表格编辑器
     */
    private void setupUserTableEditors() {
        // 为操作列添加编辑按钮
        userTable.getColumn("操作").setCellRenderer(new ButtonRenderer());
        userTable.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()) {
            @Override
            public Object getCellEditorValue() {
                int row = userTable.getSelectedRow();
                if (row != -1) {
                    String userId = (String) userTableModel.getValueAt(row, 0);
                    showEditUserDialog(userId);
                }
                return "";
            }
        });

        userTable.setFocusable(false);
        userTable.setRowHeight(30);
    }

    /**
     * 显示编辑用户对话框
     * @param userId 用户ID
     */
    private void showEditUserDialog(String userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                WindowUtil.showErrorMsg(this, "用户不存在");
                return;
            }

            JDialog dialog = new JDialog(this, "编辑用户", true);
            dialog.setSize(350, 250);
            WindowUtil.centerWindow(dialog);

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // 账号（不可编辑）
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("账号:"), gbc);

            JTextField userIdField = new JTextField(userId);
            userIdField.setEditable(false);
            gbc.gridx = 1;
            panel.add(userIdField, gbc);

            // 角色选择
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("角色:"), gbc);

            String[] roles = {"elder-老人", "family-家属", "caregiver-护工", "admin-管理员"};
            JComboBox<String> roleComboBox = new JComboBox<>(roles);
            roleComboBox.setSelectedItem(user.getRole() + "-" + getRoleChinese(user.getRole()));
            gbc.gridx = 1;
            panel.add(roleComboBox, gbc);

            // 新密码
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("新密码(可选):"), gbc);

            JPasswordField passwordField = new JPasswordField();
            gbc.gridx = 1;
            panel.add(passwordField, gbc);

            // 按钮
            JPanel buttonPanel = new JPanel();
            JButton saveBtn = new JButton("保存");
            JButton cancelBtn = new JButton("取消");

            saveBtn.addActionListener(e -> {
                try {
                    String newRole = ((String) roleComboBox.getSelectedItem()).split("-")[0];
                    String newPassword = new String(passwordField.getPassword());

                    userService.updateUserRole(userId, newRole);
                    if (!newPassword.isEmpty()) {
                        userService.updateUserPassword(userId, newPassword);
                    }

                    WindowUtil.showSuccessMsg(dialog, "用户信息更新成功");
                    loadUsers();
                    dialog.dispose();
                } catch (Exception ex) {
                    WindowUtil.showErrorMsg(dialog, "更新失败: " + ex.getMessage());
                }
            });

            cancelBtn.addActionListener(e -> dialog.dispose());
            buttonPanel.add(saveBtn);
            buttonPanel.add(cancelBtn);

            dialog.setLayout(new BorderLayout());
            dialog.add(panel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);

        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "加载用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 加载老人数据
     */
    private void loadElders() {
        try {
            elderTableModel.setRowCount(0);
            var elders = elderService.queryElders(null);
            for (var elder : elders) {
                Object[] row = {
                        elder.getElderId(),
                        elder.getName(),
                        elder.getAge(),
                        elder.getPhone(),
                        elder.getHealthStatus()
                };
                elderTableModel.addRow(row);
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "加载老人信息失败: " + e.getMessage());
        }
    }

    /**
     * 加载活动数据
     */
    private void loadActivities() {
        try {
            activityTableModel.setRowCount(0);
            var activities = activityService.getAllActivities();
            for (var activity : activities) {
                Object[] row = {
                        activity.getActivityId(),
                        activity.getName(),
                        activity.getTime(),
                        activity.getLocation(),
                        activity.getRegisteredElderIds().size(),
                        activity.getDescription().length() > 30 ?
                                activity.getDescription().substring(0, 30) + "..." :
                                activity.getDescription()
                };
                activityTableModel.addRow(row);
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "加载活动失败: " + e.getMessage());
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
     * 显示添加用户对话框
     */
    private void showAddUserDialog() {
        JDialog dialog = new JDialog(this, "添加新用户", true);
        dialog.setSize(350, 300);
        WindowUtil.centerWindow(dialog);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 账号输入
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("账号:"), gbc);

        JTextField userIdField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(userIdField, gbc);

        // 密码输入
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("密码:"), gbc);

        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        // 角色选择
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("角色:"), gbc);

        String[] roles = {"elder-老人", "family-家属", "caregiver-护工"};
        JComboBox<String> roleComboBox = new JComboBox<>(roles);
        gbc.gridx = 1;
        panel.add(roleComboBox, gbc);

        // 老人信息扩展字段（仅角色为老人时显示）
        JPanel elderInfoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcElder = new GridBagConstraints();
        gbcElder.insets = new Insets(5, 10, 5, 10);
        gbcElder.fill = GridBagConstraints.HORIZONTAL;

        // 老人姓名
        gbcElder.gridx = 0;
        gbcElder.gridy = 0;
        elderInfoPanel.add(new JLabel("老人姓名:"), gbcElder);
        JTextField elderNameField = new JTextField(15);
        gbcElder.gridx = 1;
        elderInfoPanel.add(elderNameField, gbcElder);

        // 老人年龄
        gbcElder.gridx = 0;
        gbcElder.gridy = 1;
        elderInfoPanel.add(new JLabel("年龄:"), gbcElder);
        JTextField elderAgeField = new JTextField(8);
        gbcElder.gridx = 1;
        elderInfoPanel.add(elderAgeField, gbcElder);

        // 老人手机号
        gbcElder.gridx = 0;
        gbcElder.gridy = 2;
        elderInfoPanel.add(new JLabel("手机号:"), gbcElder);
        JTextField elderPhoneField = new JTextField(13);
        gbcElder.gridx = 1;
        elderInfoPanel.add(elderPhoneField, gbcElder);

        // 初始隐藏老人信息面板，仅选择老人角色时显示
        elderInfoPanel.setVisible(false);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // 跨两列显示
        panel.add(elderInfoPanel, gbc);

        // 角色选择监听：切换为老人时显示扩展字段
        roleComboBox.addActionListener(e -> {
            String selectedRole = (String) roleComboBox.getSelectedItem();
            boolean isElder = selectedRole.startsWith("elder");
            elderInfoPanel.setVisible(isElder);
            // 调整对话框高度，适配显示/隐藏
            dialog.setSize(350, isElder ? 400 : 300);
            WindowUtil.centerWindow(dialog);
        });

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        JButton confirmBtn = new JButton("确认添加");
        JButton cancelBtn = new JButton("取消");

        confirmBtn.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String password = new String(passwordField.getPassword());
            String selectedRole = (String) roleComboBox.getSelectedItem();

            if (userId.isEmpty() || password.isEmpty()) {
                WindowUtil.showErrorMsg(dialog, "账号和密码不能为空");
                return;
            }

            String role = selectedRole.split("-")[0];
            try {
                // 1. 先注册用户
                userService.registerUser(userId, password, role);

                // 2. 如果是老人角色，自动添加到老人列表
                if ("elder".equals(role)) {
                    String elderName = elderNameField.getText().trim();
                    if (elderName.isEmpty()) {
                        WindowUtil.showErrorMsg(dialog, "老人姓名不能为空");
                        // 回滚：删除刚注册的用户（避免空信息的老人用户）
                        userService.deleteUser(userId);
                        return;
                    }

                    // 构建老人信息（用户ID作为老人ID）
                    Elder elder = new Elder();
                    elder.setElderId(userId); // 老人ID与用户账号一致
                    elder.setUserId(userId); // 设置关联的用户ID（必填字段）
                    elder.setName(elderName);

                    // 年龄（可选，非空则校验）
                    if (!elderAgeField.getText().trim().isEmpty()) {
                        try {
                            elder.setAge(Integer.parseInt(elderAgeField.getText().trim()));
                        } catch (NumberFormatException ex) {
                            WindowUtil.showErrorMsg(dialog, "年龄必须是数字");
                            userService.deleteUser(userId); // 回滚
                            return;
                        }
                    }

                    // 手机号（可选）
                    elder.setPhone(elderPhoneField.getText().trim());
                    elder.setHealthStatus("健康"); // 默认健康状态

                    // 添加到老人列表
                    elderService.addElder(elder);
                    WindowUtil.showSuccessMsg(dialog, "用户添加成功，并自动同步到老人列表！");
                    // 刷新老人列表
                    loadElders();
                } else {
                    WindowUtil.showSuccessMsg(dialog, "用户添加成功");
                }

                // 刷新用户列表
                loadUsers();
                dialog.dispose();
            } catch (Exception ex) {
                WindowUtil.showErrorMsg(dialog, "添加失败: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);

        // 组装对话框
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 显示添加老人对话框
     */
    private void showAddElderDialog() {
        JDialog dialog = new JDialog(this, "添加老人信息", true);
        dialog.setSize(400, 400);
        WindowUtil.centerWindow(dialog);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 老人ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("老人ID:"), gbc);

        JTextField elderIdField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(elderIdField, gbc);

        // 姓名
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("姓名:"), gbc);

        JTextField nameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        // 年龄
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("年龄:"), gbc);

        JTextField ageField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(ageField, gbc);

        // 手机号
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("手机号:"), gbc);

        JTextField phoneField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        // 健康状况
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("健康状况:"), gbc);

        JTextField healthField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(healthField, gbc);

        // 按钮
        JPanel buttonPanel = new JPanel();
        JButton confirmBtn = new JButton("确认添加");
        JButton cancelBtn = new JButton("取消");

        confirmBtn.addActionListener(e -> {
            try {
                Elder elder = new Elder();
                elder.setElderId(elderIdField.getText().trim());
                elder.setName(nameField.getText().trim());
                elder.setAge(Integer.parseInt(ageField.getText().trim()));
                elder.setPhone(phoneField.getText().trim());
                elder.setHealthStatus(healthField.getText().trim());

                // 验证必要字段
                if (elder.getElderId().isEmpty() || elder.getName().isEmpty()) {
                    WindowUtil.showErrorMsg(dialog, "ID和姓名不能为空");
                    return;
                }

                elderService.addElder(elder);
                WindowUtil.showSuccessMsg(dialog, "老人信息添加成功");
                loadElders(); // 刷新列表
                dialog.dispose();
            } catch (NumberFormatException ex) {
                WindowUtil.showErrorMsg(dialog, "年龄必须是数字");
            } catch (Exception ex) {
                WindowUtil.showErrorMsg(dialog, "添加失败: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 删除选中的老人
     */
    private void deleteSelectedElder() {
        int selectedRow = elderTable.getSelectedRow();
        if (selectedRow == -1) {
            WindowUtil.showErrorMsg(this, "请先选择要删除的老人");
            return;
        }

        String elderId = (String) elderTableModel.getValueAt(selectedRow, 0);
        String elderName = (String) elderTableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除老人 " + elderName + " 吗？\n此操作将同时删除相关健康记录和服务申请。",
                "确认删除",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = elderService.deleteElder(elderId);
                if (success) {
                    WindowUtil.showSuccessMsg(this, "老人删除成功");
                    loadElders();
                } else {
                    WindowUtil.showErrorMsg(this, "删除失败，老人可能不存在");
                }
            } catch (Exception e) {
                WindowUtil.showErrorMsg(this, "删除失败: " + e.getMessage());
            }
        }
    }

    /**
     * 显示创建活动对话框
     */
    private void showCreateActivityDialog() {
        JDialog dialog = new JDialog(this, "创建新活动", true);
        dialog.setSize(450, 400);
        WindowUtil.centerWindow(dialog);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 活动名称
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("活动名称:"), gbc);

        JTextField nameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        // 活动时间
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("活动时间:"), gbc);

        JTextField timeField = new JTextField(20);
        timeField.setText("格式: yyyy-MM-dd HH:mm-ss");
        gbc.gridx = 1;
        panel.add(timeField, gbc);

        // 活动地点
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("活动地点:"), gbc);

        JTextField locationField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(locationField, gbc);

        // 活动描述
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.ipady = 60;
        panel.add(new JLabel("活动描述:"), gbc);

        JTextArea descArea = new JTextArea();
        descArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(descArea);
        gbc.gridx = 1;
        panel.add(scrollPane, gbc);

        // 按钮
        JPanel buttonPanel = new JPanel();
        JButton confirmBtn = new JButton("创建活动");
        JButton cancelBtn = new JButton("取消");

        confirmBtn.addActionListener(e -> {
            try {
                Activity activity = new Activity();
                activity.setName(nameField.getText().trim());
                activity.setTime(timeField.getText().trim());
                activity.setLocation(locationField.getText().trim());
                activity.setDescription(descArea.getText().trim());
                activity.setRegisteredElderIds(new ArrayList<>());

                activityService.createActivity(activity);
                WindowUtil.showSuccessMsg(dialog, "活动创建成功");
                loadActivities(); // 刷新活动列表
                dialog.dispose();
            } catch (Exception ex) {
                WindowUtil.showErrorMsg(dialog, "创建失败: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}