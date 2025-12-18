// src/main/java/com/eldercare/ui/ElderFrame.java
package com.eldercare.ui;

import com.eldercare.model.*;
import com.eldercare.service.ActivityService;
import com.eldercare.service.CommunityService;
import com.eldercare.service.ElderService;
import com.eldercare.service.HealthRecordService;
import com.eldercare.service.ReportService;
import com.eldercare.service.ServiceRequestService;
import com.eldercare.service.UserService;
import com.eldercare.util.DateFormatUtil;
import com.eldercare.util.WindowUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.io.IOException;


/**
 * 老人用户主界面
 * 功能：提供老人用户的各项功能，包括健康记录查看、服务申请、活动报名等
 */
public class ElderFrame extends JFrame {
    // 当前登录用户
    private User currentUser;

    // 服务类实例
    private ActivityService activityService;
    private HealthRecordService healthRecordService;
    private ServiceRequestService serviceRequestService;

    // UI组件
    private JTabbedPane tabbedPane;
    private JTable activityTable;
    
    // 窗口默认大小
    private static final int DEFAULT_WIDTH = 900;
    private static final int DEFAULT_HEIGHT = 600;

    /**
     * 构造方法
     * @param user 当前登录的用户对象
     */
    public ElderFrame(User user) {
        this.currentUser = user;
        activityService = ActivityService.getInstance();
        healthRecordService = HealthRecordService.getInstance();
        serviceRequestService = ServiceRequestService.getInstance();

        initComponents();
        setupLayout();
    }

    /**
     * 初始化所有组件
     */
    private void initComponents() {
        setTitle(WindowUtil.getWindowTitle("老人面板 - " + currentUser.getUserId()));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setResizable(true); // 确保窗口可以调整大小
        WindowUtil.centerWindow(this);

        // 创建菜单栏
        createMenuBar();

        // 创建选项卡面板
        tabbedPane = new JTabbedPane();

        // 创建各个功能面板
        JPanel healthPanel = createHealthPanel();
        tabbedPane.addTab("我的健康", healthPanel);

        JPanel servicePanel = createServicePanel();
        tabbedPane.addTab("我的服务", servicePanel);

        JPanel activityPanel = createActivityPanel();
        tabbedPane.addTab("活动报名", activityPanel);

        JPanel profilePanel = createProfilePanel();
        tabbedPane.addTab("个人信息", profilePanel);

        JPanel communityPanel = createCommunityPanel();
        tabbedPane.addTab("社区交流", communityPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * 创建社区交流面板
     * @return 社区交流面板
     */
    private JPanel createCommunityPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("老人社区", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 消息列表部分
        JPanel messageListPanel = createMessageListPanel();
        panel.add(messageListPanel, BorderLayout.CENTER);

        // 发布消息部分
        JPanel postPanel = createPostMessagePanel();
        panel.add(postPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建消息列表面板
     * @return 消息列表面板
     */
    private JPanel createMessageListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultListModel<CommunityMessage> listModel = new DefaultListModel<>();
        JList<CommunityMessage> messageList = new JList<>(listModel);
        messageList.setCellRenderer(new MessageCellRenderer());

        JScrollPane scrollPane = new JScrollPane(messageList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 加载社区消息
        loadCommunityMessages(listModel);

        // 刷新按钮
        JButton refreshBtn = new JButton("刷新消息");
        refreshBtn.addActionListener(e -> loadCommunityMessages(listModel));
        panel.add(refreshBtn, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建发布消息面板
     * @return 发布消息面板
     */
    private JPanel createPostMessagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("发布新消息"));

        JTextArea messageArea = new JTextArea(3, 30);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(messageArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton postBtn = new JButton("发布");
        postBtn.addActionListener(e -> {
            String content = messageArea.getText().trim();
            if (content.isEmpty()) {
                WindowUtil.showErrorMsg(this, "消息内容不能为空");
                return;
            }

            try {
                CommunityMessage message = new CommunityMessage();
                message.setElderId(currentUser.getUserId());
                message.setElderName("老人" + currentUser.getUserId());
                message.setContent(content);
                message.setMessageType("text");

                CommunityService.getInstance().postMessage(message);
                messageArea.setText("");
                WindowUtil.showSuccessMsg(this, "消息发布成功");
            } catch (Exception ex) {
                WindowUtil.showErrorMsg(this, "发布失败: " + ex.getMessage());
            }
        });

        panel.add(postBtn, BorderLayout.EAST);

        return panel;
    }

    /**
     * 加载社区消息到列表模型
     * @param listModel 列表模型
     */
    private void loadCommunityMessages(DefaultListModel<CommunityMessage> listModel) {
        try {
            listModel.clear();
            List<CommunityMessage> messages = CommunityService.getInstance().getAllMessages();
            for (CommunityMessage msg : messages) {
                listModel.addElement(msg);
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "加载消息失败: " + e.getMessage());
        }
    }

    /**
     * 自定义消息渲染器
     */
    class MessageCellRenderer extends JPanel implements ListCellRenderer<CommunityMessage> {
        private JLabel nameLabel = new JLabel();
        private JTextArea contentArea = new JTextArea();
        private JLabel timeLabel = new JLabel();
        private JLabel statsLabel = new JLabel();

        public MessageCellRenderer() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.add(nameLabel, BorderLayout.WEST);
            headerPanel.add(timeLabel, BorderLayout.EAST);

            contentArea.setEditable(false);
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setBackground(Color.WHITE);
            contentArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            add(headerPanel, BorderLayout.NORTH);
            add(new JScrollPane(contentArea), BorderLayout.CENTER);
            add(statsLabel, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends CommunityMessage> list,
                                                      CommunityMessage message,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {

            nameLabel.setText(message.getElderName());
            nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));

            contentArea.setText(message.getContent());
            contentArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));

            timeLabel.setText(DateFormatUtil.formatDateTime(message.getCreateTime()));
            timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
            timeLabel.setForeground(Color.GRAY);

            statsLabel.setText("❤ " + message.getLikeCount() + "   💬 " + message.getCommentCount());
            statsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
            statsLabel.setForeground(Color.GRAY);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }
    }

    /**
     * 创建健康记录面板
     * @return 健康记录面板
     */
    private JPanel createHealthPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("我的健康记录", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 健康记录表格
        String[] columns = {"记录时间", "血压", "心率"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // 加载健康记录数据
        try {
            var records = healthRecordService.getRecordsByElderId(currentUser.getUserId());
            for (var record : records) {
                Object[] row = {
                        record.getRecordTime(),
                        record.getBloodPressure(),
                        record.getHeartRate()
                };
                model.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载健康记录失败: " + e.getMessage());
        }

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建服务申请面板
     * @return 服务申请面板
     */
    private JPanel createServicePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("我的服务申请", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 服务申请表格
        String[] columns = {"申请时间", "服务类型", "状态", "内容摘要"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // 加载服务申请数据
        try {
            Elder currentElder = getCurrentElder();
            if (currentElder != null) {
                var requests = serviceRequestService.getRequestsByElderId(currentElder.getElderId());
                for (var request : requests) {
                    String content = request.getContent();
                    // 内容过长时截断显示
                    if (content.length() > 30) {
                        content = content.substring(0, 30) + "...";
                    }

                    Object[] row = {
                            request.getRequestTime(),
                            request.getServiceType(),
                            request.getStatus(),
                            content
                    };
                    model.addRow(row);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载服务申请失败: " + e.getMessage());
        }

        panel.add(scrollPane, BorderLayout.CENTER);

        // 添加新申请按钮
        JButton newRequestButton = new JButton("提交新申请");
        newRequestButton.addActionListener(e -> showNewRequestDialog());
        panel.add(newRequestButton, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建活动报名面板
     * @return 活动报名面板
     */
    private JPanel createActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("可报名活动", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 活动表格
        String[] columns = {"活动名称", "时间", "地点", "已报名人数", "操作"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 只有操作列可编辑
                return column == 4;
            }
        };

        activityTable = new JTable(model);
        activityTable.getColumn("操作").setCellRenderer(new ButtonRenderer());
        activityTable.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(activityTable);

        // 加载活动数据
        loadActivities();

        panel.add(scrollPane, BorderLayout.CENTER);

        // 刷新按钮
        JButton refreshButton = new JButton("刷新活动列表");
        refreshButton.addActionListener(e -> loadActivities());
        panel.add(refreshButton, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 加载活动数据到表格
     */
    private void loadActivities() {
        try {
            DefaultTableModel model = (DefaultTableModel) activityTable.getModel();
            model.setRowCount(0);

            var activities = activityService.getAllActivities();
            // 获取当前老人信息
            Elder currentElder = getCurrentElder();
            String elderId = currentElder != null ? currentElder.getElderId() : currentUser.getUserId();
            
            for (var activity : activities) {
                // 检查当前老人是否已报名
                boolean isRegistered = activity.getRegisteredElderIds().contains(elderId);
                String buttonText = isRegistered ? "取消报名" : "报名";

                Object[] row = {
                        activity.getName(),
                        activity.getTime(),
                        activity.getLocation(),
                        activity.getRegisteredElderIds().size(),
                        buttonText
                };
                model.addRow(row);
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "加载活动失败: " + e.getMessage());
        }
    }

    /**
     * 创建个人信息面板
     * @return 个人信息面板
     */
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel infoLabel = new JLabel("个人信息");
        infoLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(infoLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(new JLabel("账号:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(currentUser.getUserId()), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("角色:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(getRoleChinese(currentUser.getRole())), gbc);

        // 获取老人详细信息
        Elder currentElder = getCurrentElder();
        if (currentElder != null) {
            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(new JLabel("姓名:"), gbc);
            gbc.gridx = 1;
            panel.add(new JLabel(currentElder.getName()), gbc);

            gbc.gridx = 0;
            gbc.gridy = 4;
            panel.add(new JLabel("年龄:"), gbc);
            gbc.gridx = 1;
            panel.add(new JLabel(String.valueOf(currentElder.getAge())), gbc);

            gbc.gridx = 0;
            gbc.gridy = 5;
            panel.add(new JLabel("手机号:"), gbc);
            gbc.gridx = 1;
            panel.add(new JLabel(currentElder.getPhone() != null ? currentElder.getPhone() : "未填写"), gbc);

            gbc.gridx = 0;
            gbc.gridy = 6;
            panel.add(new JLabel("健康状况:"), gbc);
            gbc.gridx = 1;
            panel.add(new JLabel(currentElder.getHealthStatus() != null ? currentElder.getHealthStatus() : "未填写"), gbc);
        }

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        // 修改密码按钮
        JButton changePasswordButton = new JButton("修改密码");
        changePasswordButton.addActionListener(e -> showChangePasswordDialog());
        buttonPanel.add(changePasswordButton);
        
        // 修改个人信息按钮
        JButton editProfileButton = new JButton("修改个人信息");
        editProfileButton.addActionListener(e -> showEditProfileDialog());
        buttonPanel.add(editProfileButton);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    /**
     * 设置布局（各组件已在创建方法中设置）
     */
    private void setupLayout() {
        // 布局已在各组件创建方法中设置
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
     * 显示提交新服务申请对话框
     */
    private void showNewRequestDialog() {
        JDialog dialog = new JDialog(this, "提交新服务申请", true);
        dialog.setSize(500, 400);
        WindowUtil.centerWindow(dialog);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // 服务类型选择
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("服务类型:"), gbc);

        gbc.gridx = 1;
        String[] serviceTypes = {
                "日常照护", "健康咨询", "康复辅助",
                "药品配送", "紧急求助", "饮食服务",
                "清洁服务", "陪伴聊天", "其他"
        };
        JComboBox<String> typeCombo = new JComboBox<>(serviceTypes);
        formPanel.add(typeCombo, gbc);

        // 紧急程度选择
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("紧急程度:"), gbc);

        gbc.gridx = 1;
        String[] urgencyLevels = {"一般", "重要", "紧急"};
        JComboBox<String> urgencyCombo = new JComboBox<>(urgencyLevels);
        formPanel.add(urgencyCombo, gbc);

        // 申请内容输入
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("申请内容:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 3;
        JTextArea contentArea = new JTextArea(8, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);
        formPanel.add(contentScroll, gbc);

        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;

        // 期望时间输入
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("期望时间:"), gbc);

        gbc.gridx = 1;
        JTextField timeField = new JTextField(20);
        timeField.setText("尽快处理");
        formPanel.add(timeField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton submitBtn = new JButton("提交申请");
        JButton cancelBtn = new JButton("取消");

        submitBtn.addActionListener(e -> {
            String serviceType = (String) typeCombo.getSelectedItem();
            String urgency = (String) urgencyCombo.getSelectedItem();
            String content = contentArea.getText().trim();
            String expectedTime = timeField.getText().trim();

            // 内容长度校验
            if (content.length() < 10) {
                WindowUtil.showErrorMsg(dialog, "申请内容至少需要10个字");
                return;
            }

            try {
                // 获取当前老人信息
                Elder currentElder = getCurrentElder();
                if (currentElder == null) {
                    WindowUtil.showErrorMsg(dialog, "无法获取您的老人信息，请联系管理员");
                    return;
                }
                
                // 创建服务申请对象
                ServiceRequest request = new ServiceRequest();
                request.setElderId(currentElder.getElderId());
                request.setServiceType(serviceType);
                request.setContent(content + " [紧急程度:" + urgency + ", 期望时间:" + expectedTime + "]");

                // 提交申请
                serviceRequestService.submitRequest(request);

                WindowUtil.showSuccessMsg(dialog, "服务申请提交成功！");
                dialog.dispose();

            } catch (Exception ex) {
                WindowUtil.showErrorMsg(dialog, "提交失败: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * 处理活动报名/取消报名
     * @param row 表格行索引
     */
    private void handleActivityRegistration(int row) {
        // 获取当前用户对应的老人信息
        Elder currentElder = getCurrentElder();
        if (currentElder == null) {
            WindowUtil.showErrorMsg(this, "无法获取您的老人信息，请联系管理员");
            return;
        }

        String activityName = (String) ((DefaultTableModel) activityTable.getModel()).getValueAt(row, 0);
        String activityId = getActivityIdByName(activityName);

        if (activityId == null) {
            WindowUtil.showErrorMsg(this, "未找到活动信息");
            return;
        }

        // 检查是否已报名
        if (isAlreadyRegistered(activityId, currentElder.getElderId())) {
            // 已报名则询问是否取消
            if (WindowUtil.showConfirmMsg(this, "您已报名该活动，是否取消报名？", "取消报名确认")) {
                cancelActivityRegistration(activityId, currentElder.getElderId());
            }
        } else {
            // 未报名则询问是否报名
            if (WindowUtil.showConfirmMsg(this, "确定要报名参加 " + activityName + " 吗？", "报名确认")) {
                registerForActivity(activityId, currentElder.getElderId());
            }
        }
    }

    /**
     * 为老人报名活动
     * @param activityId 活动ID
     * @param elderId 老人ID
     */
    private void registerForActivity(String activityId, String elderId) {
        try {
            boolean success = activityService.registerActivity(activityId, elderId);
            if (success) {
                WindowUtil.showSuccessMsg(this, "活动报名成功！");
                loadActivities();  // 刷新活动列表
            } else {
                WindowUtil.showErrorMsg(this, "报名失败，可能已满或已报名");
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "报名失败: " + e.getMessage());
        }
    }

    /**
     * 取消活动报名
     * @param activityId 活动ID
     * @param elderId 老人ID
     */
    private void cancelActivityRegistration(String activityId, String elderId) {
        try {
            boolean success = activityService.cancelRegistration(activityId, elderId);
            if (success) {
                WindowUtil.showSuccessMsg(this, "已成功取消报名");
                loadActivities();  // 刷新活动列表
            } else {
                WindowUtil.showErrorMsg(this, "取消失败，可能未报名");
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "取消失败: " + e.getMessage());
        }
    }

    /**
     * 根据活动名称获取活动ID
     * @param activityName 活动名称
     * @return 活动ID，未找到返回null
     */
    private String getActivityIdByName(String activityName) {
        try {
            List<Activity> activities = activityService.getAllActivities();
            for (Activity activity : activities) {
                if (activity.getName().equals(activityName)) {
                    return activity.getActivityId();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前用户对应的老人信息
     * @return 老人对象，未找到返回null
     */
    private Elder getCurrentElder() {
        try {
            System.out.println("[Debug-ElderFrame] 开始获取当前老人信息");
            System.out.println("[Debug-ElderFrame] currentUser是否为null: " + (currentUser == null));
            if (currentUser != null) {
                System.out.println("[Debug-ElderFrame] currentUser的用户ID: '" + currentUser.getUserId() + "'");
                System.out.println("[Debug-ElderFrame] currentUser的角色: '" + currentUser.getRole() + "'");
                
                String userId = currentUser.getUserId();
                System.out.println("[Debug-ElderFrame] userId是否为null: " + (userId == null));
                if (userId != null) {
                    System.out.println("[Debug-ElderFrame] userId内容: '" + userId + "'");
                    System.out.println("[Debug-ElderFrame] userId长度: " + userId.length());
                    System.out.println("[Debug-ElderFrame] userId.trim()后内容: '" + userId.trim() + "'");
                    System.out.println("[Debug-ElderFrame] userId.trim()后长度: " + userId.trim().length());
                }
                
                Elder elder = ElderService.getInstance().getElderByUserId(currentUser.getUserId());
                System.out.println("[Debug-ElderFrame] 获取老人信息结果: " + (elder != null ? elder.getName() : "null"));
                return elder;
            } else {
                System.err.println("[Debug-ElderFrame] currentUser为null");
                return null;
            }
        } catch (Exception e) {
            System.err.println("[Debug-ElderFrame] 获取老人信息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 检查老人是否已报名活动
     * @param activityId 活动ID
     * @param elderId 老人ID
     * @return true-已报名，false-未报名
     */
    private boolean isAlreadyRegistered(String activityId, String elderId) {
        try {
            Activity activity = activityService.getActivityById(activityId);
            return activity != null && activity.getRegisteredElderIds().contains(elderId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 显示修改密码对话框
     */
    private void showChangePasswordDialog() {
        JDialog dialog = new JDialog(this, "修改密码", true);
        dialog.setSize(400, 300);
        WindowUtil.centerWindow(dialog);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // 原密码输入
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("原密码:"), gbc);

        gbc.gridx = 1;
        JPasswordField oldPasswordField = new JPasswordField(20);
        formPanel.add(oldPasswordField, gbc);

        // 新密码输入
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("新密码:"), gbc);

        gbc.gridx = 1;
        JPasswordField newPasswordField = new JPasswordField(20);
        formPanel.add(newPasswordField, gbc);

        // 确认新密码输入
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("确认新密码:"), gbc);

        gbc.gridx = 1;
        JPasswordField confirmPasswordField = new JPasswordField(20);
        formPanel.add(confirmPasswordField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton submitBtn = new JButton("确认修改");
        JButton cancelBtn = new JButton("取消");

        submitBtn.addActionListener(e -> {
            String oldPassword = new String(oldPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            // 验证输入
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                WindowUtil.showErrorMsg(dialog, "所有密码字段都不能为空");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                WindowUtil.showErrorMsg(dialog, "两次输入的新密码不一致");
                return;
            }

            if (newPassword.length() < 6) {
                WindowUtil.showErrorMsg(dialog, "新密码长度不能少于6位");
                return;
            }

            try {
                // 验证原密码是否正确
                User user = UserService.getInstance().login(currentUser.getUserId(), oldPassword);
                if (user != null) {
                    // 修改密码
                    UserService.getInstance().updateUserPassword(currentUser.getUserId(), newPassword);
                    WindowUtil.showSuccessMsg(this, "密码修改成功");
                    dialog.dispose();
                } else {
                    WindowUtil.showErrorMsg(dialog, "原密码错误");
                }
            } catch (Exception ex) {
                WindowUtil.showErrorMsg(dialog, "修改密码失败: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
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
            dispose();
            new LoginFrame().setVisible(true);
        });
        systemMenu.add(logoutItem);
        
        // 添加全屏切换菜单项
        JMenuItem toggleFullScreenItem = new JMenuItem("切换全屏");
        toggleFullScreenItem.addActionListener(e -> {
            WindowUtil.toggleFullScreen(this, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        });
        systemMenu.add(toggleFullScreenItem);

        // 报表菜单
        JMenu reportMenu = new JMenu("报表");
        JMenuItem exportHealthReportItem = new JMenuItem("导出健康记录");
        JMenuItem exportServiceReportItem = new JMenuItem("导出服务申请");

        exportHealthReportItem.addActionListener(e -> exportHealthReport());
        exportServiceReportItem.addActionListener(e -> exportServiceReport());

        reportMenu.add(exportHealthReportItem);
        reportMenu.add(exportServiceReportItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "养老院管理系统 - 老人版",
                        "关于",
                        JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(systemMenu);
        menuBar.add(reportMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    /**
     * 导出健康记录报表
     */
    private void exportHealthReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出健康记录");
        fileChooser.setSelectedFile(new java.io.File(
                "健康记录_" + currentUser.getUserId() + "_" +
                        DateFormatUtil.formatCurrentDate() + ".xlsx"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try {
                List<HealthRecord> records = healthRecordService.getRecordsByElderId(currentUser.getUserId());
                ReportService.getInstance().exportHealthRecordsToExcel(records, file.getAbsolutePath());
                WindowUtil.showSuccessMsg(this, "健康记录导出成功：" + file.getAbsolutePath());
            } catch (Exception e) {
                WindowUtil.showErrorMsg(this, "导出失败: " + e.getMessage());
            }
        }
    }

    /**
     * 导出服务申请报表
     */
    private void exportServiceReport() {
        // 获取当前老人信息
        Elder currentElder = getCurrentElder();
        if (currentElder == null) {
            WindowUtil.showErrorMsg(this, "无法获取您的老人信息，请联系管理员");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出服务申请");
        fileChooser.setSelectedFile(new java.io.File(
                "服务申请_" + currentElder.getElderId() + "_" +
                        DateFormatUtil.formatCurrentDate() + ".xlsx"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try {
                List<ServiceRequest> requests = serviceRequestService.getRequestsByElderId(currentElder.getElderId());
                ReportService.getInstance().exportServiceRequestsToExcel(requests, file.getAbsolutePath());
                WindowUtil.showSuccessMsg(this, "服务申请导出成功：" + file.getAbsolutePath());
            } catch (Exception e) {
                WindowUtil.showErrorMsg(this, "导出失败: " + e.getMessage());
            }
        }
    }

    /**
     * 按钮渲染器（用于表格中的按钮列）
     */
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    /**
     * 按钮编辑器（用于表格中的按钮列）
     */
    class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private JButton button;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.row = row;
            button.setText((value == null) ? "" : value.toString());
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped();
            handleActivityRegistration(row);
        }
    }
    
    /**
     * 显示修改个人信息对话框
     */
    private void showEditProfileDialog() {
        // 获取当前老人信息
        Elder currentElder = getCurrentElder();
        if (currentElder == null) {
            WindowUtil.showErrorMsg(this, "无法获取您的老人信息，请联系管理员");
            return;
        }

        // 创建对话框
        JDialog dialog = new JDialog(this, "修改个人信息", true);
        dialog.setSize(500, 400);
        WindowUtil.centerWindow(dialog);

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // 姓名输入
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("姓名:"), gbc);

        gbc.gridx = 1;
        JTextField nameField = new JTextField(currentElder.getName(), 20);
        formPanel.add(nameField, gbc);

        // 年龄输入
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("年龄:"), gbc);

        gbc.gridx = 1;
        JTextField ageField = new JTextField(String.valueOf(currentElder.getAge()), 20);
        formPanel.add(ageField, gbc);

        // 手机号输入
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("手机号:"), gbc);

        gbc.gridx = 1;
        JTextField phoneField = new JTextField(currentElder.getPhone() != null ? currentElder.getPhone() : "", 20);
        formPanel.add(phoneField, gbc);

        // 健康状况输入
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("健康状况:"), gbc);

        gbc.gridx = 1;
        JTextField healthField = new JTextField(currentElder.getHealthStatus() != null ? currentElder.getHealthStatus() : "", 20);
        formPanel.add(healthField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton saveBtn = new JButton("保存");
        JButton cancelBtn = new JButton("取消");

        saveBtn.addActionListener(e -> {
            try {
                // 验证输入（姓名、年龄、手机号格式）
                String name = nameField.getText().trim();
                String ageStr = ageField.getText().trim();
                String phone = phoneField.getText().trim();
                String health = healthField.getText().trim();

                // 验证姓名
                if (name.isEmpty()) {
                    WindowUtil.showErrorMsg(dialog, "姓名不能为空");
                    return;
                }

                // 验证年龄
                if (ageStr.isEmpty()) {
                    WindowUtil.showErrorMsg(dialog, "年龄不能为空");
                    return;
                }
                int age = Integer.parseInt(ageStr);
                if (age < 1 || age > 120) {
                    WindowUtil.showErrorMsg(dialog, "年龄必须在1-120之间");
                    return;
                }

                // 验证手机号（如果输入了）
                if (!phone.isEmpty()) {
                    if (!phone.matches("1[3-9]\\d{9}")) {
                        WindowUtil.showErrorMsg(dialog, "手机号格式不正确");
                        return;
                    }
                }

                // 更新老人信息
                currentElder.setName(name);
                currentElder.setAge(age);
                currentElder.setPhone(phone.isEmpty() ? null : phone);
                currentElder.setHealthStatus(health.isEmpty() ? "未填写" : health);

                // 保存到数据存储
                updateElderInfo(currentElder);
                
                // 关闭对话框并刷新面板
                dialog.dispose();
                WindowUtil.showSuccessMsg(this, "个人信息修改成功");
                refreshProfilePanel();
            } catch (NumberFormatException ex) {
                WindowUtil.showErrorMsg(dialog, "年龄必须是数字");
            } catch (Exception ex) {
                WindowUtil.showErrorMsg(dialog, "修改失败: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * 更新老人信息到数据存储
     * @param elder 更新后的老人对象
     */
    private void updateElderInfo(Elder elder) throws IOException, ClassNotFoundException {
        ElderService elderService = ElderService.getInstance();
        elderService.updateElder(elder);
        System.out.println("[ElderFrame] 老人信息更新成功：" + elder.getElderId());
    }
    
    /**
     * 刷新个人信息面板
     */
    private void refreshProfilePanel() {
        // 移除原有的个人信息面板
        tabbedPane.remove(3); // 假设个人信息是第4个标签页（索引为3）
        
        // 创建新的个人信息面板
        JPanel newProfilePanel = createProfilePanel();
        tabbedPane.addTab("个人信息", newProfilePanel);
        
        // 选中新创建的个人信息面板
        tabbedPane.setSelectedIndex(3);
    }
}