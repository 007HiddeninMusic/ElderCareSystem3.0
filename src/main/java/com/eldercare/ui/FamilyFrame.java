// src/main/java/com/eldercare/ui/FamilyFrame.java
package com.eldercare.ui;

import com.eldercare.model.Elder;
import com.eldercare.model.ServiceRequest;
import com.eldercare.model.User;
import com.eldercare.model.Activity;
import com.eldercare.model.HealthRecord;
import com.eldercare.service.*;
import com.eldercare.util.WindowUtil;
import com.eldercare.util.InputValidator;
import com.eldercare.util.DataStorageUtil;
import com.eldercare.util.DateFormatUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * 家属用户主界面
 * 功能：提供家属用户的各项功能，包括关联老人管理、服务申请、消息通知、活动报名等
 */
public class FamilyFrame extends JFrame {
    // 当前登录用户
    private User currentUser;
    // 关联老人统计标签
    private JLabel elderCountLabel;
    // 家属-老人关系存储键
    private static final String FAMILY_ELDER_RELATIONS_KEY = "family_elder_relations";
    
    // 窗口默认大小
    private static final int DEFAULT_WIDTH = 900;
    private static final int DEFAULT_HEIGHT = 600;
    
    // 服务类实例
    private ElderService elderService;
    private ServiceRequestService serviceRequestService;
    private HealthRecordService healthRecordService;
    private ActivityService activityService;
    private UserService userService;

    // UI组件
    private JTabbedPane tabbedPane;
    private JTable elderTable;
    private DefaultTableModel elderTableModel;
    private JButton refreshEldersButton;
    private JButton addAssociationButton;
    private JButton removeAssociationButton;
    private JButton viewHealthRecordsButton;
    private JTable requestTable;
    private DefaultTableModel requestTableModel;
    private JButton refreshRequestsButton;
    private JButton submitRequestButton;
    private JButton cancelRequestButton;
    private JTextArea messageArea;
    private JButton refreshMessagesButton;
    private JButton clearMessagesButton;

    // 家属-老人关系映射
    private java.util.Map<String, List<String>> familyElderRelations = new java.util.HashMap<>();

    // 活动相关组件
    private JTable activityTable;
    private DefaultTableModel activityTableModel;

    /**
     * 构造方法
     * @param user 当前登录的用户对象
     */
    public FamilyFrame(User user) {
        this.currentUser = user;
        elderService = ElderService.getInstance();
        serviceRequestService = ServiceRequestService.getInstance();
        healthRecordService = HealthRecordService.getInstance();
        activityService = ActivityService.getInstance();
        userService = UserService.getInstance();

        // 调试信息
        System.out.println("创建FamilyFrame，用户: " + currentUser.getUserId());

        // 初始化家庭-老人关系
        initFamilyElderRelations();

        initComponents();
        setupLayout();
        setupListeners();
        loadInitialData();
        
        // 初始加载后立即更新统计
        SwingUtilities.invokeLater(() -> {
            loadElders();
        });
    }

    /**
     * 初始化家属-老人关系
     */
    private void initFamilyElderRelations() {
        try {
            // 从文件加载家属-老人关系
            Object data = DataStorageUtil.getData(FAMILY_ELDER_RELATIONS_KEY);
            if (data != null) {
                familyElderRelations = (java.util.Map<String, List<String>>) data;
                System.out.println("已加载家属-老人关系: " + familyElderRelations);
            } else {
                familyElderRelations = new java.util.HashMap<>();
                System.out.println("创建新的家属-老人关系映射");
                
                // 初始化示例数据
                List<String> sampleElderIds = new ArrayList<>();
                sampleElderIds.add("ELDER_001");
                sampleElderIds.add("ELDER_002");
                familyElderRelations.put("wzx1234", sampleElderIds); 
            }
            
            // 确保当前用户有对应的条目
            if (!familyElderRelations.containsKey(currentUser.getUserId())) {
                familyElderRelations.put(currentUser.getUserId(), new ArrayList<>());
                saveFamilyElderRelations();
            }
            
        } catch (Exception e) {
            System.err.println("加载家属-老人关系失败: " + e.getMessage());
            familyElderRelations = new java.util.HashMap<>();
            // 初始化当前用户
            familyElderRelations.put(currentUser.getUserId(), new ArrayList<>());
        }
    }

    /**
     * 保存家属-老人关系到文件
     */
    private void saveFamilyElderRelations() {
        try {
            DataStorageUtil.saveData(FAMILY_ELDER_RELATIONS_KEY, familyElderRelations);
            System.out.println("家属-老人关系已保存: " + familyElderRelations);
        } catch (IOException e) {
            System.err.println("保存家属-老人关系失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 初始化所有组件
     */
    private void initComponents() {
        setTitle(WindowUtil.getWindowTitle("家属面板 - " + currentUser.getUserId()));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 改为手动处理
        
        // 添加窗口关闭监听器
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    // 保存所有数据
                    saveFamilyElderRelations();
                    System.out.println("窗口关闭，数据已保存");
                    
                    // 返回登录界面
                    dispose();
                    new LoginFrame().setVisible(true);
                } catch (Exception e) {
                    System.err.println("保存数据失败: " + e.getMessage());
                }
            }
        });
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setResizable(true); // 确保窗口可以调整大小
        WindowUtil.centerWindow(this);

        // 创建菜单栏
        createMenuBar();

        // 创建选项卡面板
        tabbedPane = new JTabbedPane();

        // 创建各个功能面板
        JPanel eldersPanel = createEldersPanel();
        tabbedPane.addTab("关联老人", eldersPanel);

        JPanel requestPanel = createRequestPanel();
        tabbedPane.addTab("服务申请", requestPanel);

        JPanel messagePanel = createMessagePanel();
        tabbedPane.addTab("消息通知", messagePanel);

        // 老人活动面板 - 这里不直接创建，而是在首次访问时创建
        tabbedPane.addTab("老人活动", new JLabel("点击查看活动...", SwingConstants.CENTER));
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * 创建菜单栏
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 系统菜单
        JMenu systemMenu = new JMenu("系统");
        
        JMenuItem profileItem = new JMenuItem("个人信息");
        profileItem.addActionListener(e -> showProfileDialog());
        
        JMenuItem changePasswordItem = new JMenuItem("修改密码");
        changePasswordItem.addActionListener(e -> showChangePasswordDialog());
        
        JMenuItem logoutItem = new JMenuItem("退出登录");
        logoutItem.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        
        // 添加全屏切换菜单项
        JMenuItem toggleFullScreenItem = new JMenuItem("切换全屏");
        toggleFullScreenItem.addActionListener(e -> {
            WindowUtil.toggleFullScreen(this, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        });
        
        systemMenu.add(profileItem);
        systemMenu.add(changePasswordItem);
        systemMenu.add(toggleFullScreenItem);
        systemMenu.addSeparator();
        systemMenu.add(logoutItem);

        // 工具菜单
        JMenu toolsMenu = new JMenu("工具");
        JMenuItem exportItem = new JMenuItem("导出数据");
        exportItem.addActionListener(e -> exportFamilyData());
        toolsMenu.add(exportItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "养老院管理系统 - 家属版\n\n" +
                        "功能说明：\n" +
                        "1. 管理关联老人信息\n" +
                        "2. 为老人提交服务申请\n" +
                        "3. 查看老人健康状况\n" +
                        "4. 接收系统通知\n" +
                        "5. 管理老人活动报名",
                        "关于",
                        JOptionPane.INFORMATION_MESSAGE));
        
        JMenuItem guideItem = new JMenuItem("使用指南");
        guideItem.addActionListener(e -> showUserGuide());
        
        helpMenu.add(aboutItem);
        helpMenu.add(guideItem);

        menuBar.add(systemMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    /**
     * 创建关联老人面板
     * @return 关联老人面板
     */
    private JPanel createEldersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 标题
        JLabel titleLabel = new JLabel("我关联的老人", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        refreshEldersButton = new JButton("刷新列表");
        addAssociationButton = new JButton("关联新老人");
        removeAssociationButton = new JButton("解除关联");
        viewHealthRecordsButton = new JButton("查看健康记录");
        
        buttonPanel.add(refreshEldersButton);
        buttonPanel.add(addAssociationButton);
        buttonPanel.add(removeAssociationButton);
        buttonPanel.add(viewHealthRecordsButton);
        
        panel.add(buttonPanel, BorderLayout.NORTH);

        // 老人表格
        String[] columns = {"老人ID", "姓名", "年龄", "健康状况", "联系方式", "关联时间"};
        elderTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 所有单元格不可编辑
            }
        };
        elderTable = new JTable(elderTableModel);
        elderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        elderTable.setRowHeight(30);
        
        // 设置表格列宽
        elderTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        elderTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        elderTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        elderTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        elderTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        elderTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(elderTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 信息统计面板
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        elderCountLabel = new JLabel("关联老人总数: 0"); // 这里初始化
        infoPanel.add(elderCountLabel);
        panel.add(infoPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建服务申请面板
     * @return 服务申请面板
     */
    private JPanel createRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 标题
        JLabel titleLabel = new JLabel("服务申请管理", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        refreshRequestsButton = new JButton("刷新列表");
        submitRequestButton = new JButton("提交新申请");
        cancelRequestButton = new JButton("取消选中申请");
        
        buttonPanel.add(refreshRequestsButton);
        buttonPanel.add(submitRequestButton);
        buttonPanel.add(cancelRequestButton);
        
        panel.add(buttonPanel, BorderLayout.NORTH);

        // 申请表格
        String[] columns = {"申请ID", "老人姓名", "服务类型", "申请内容", "申请时间", "状态", "处理人"};
        requestTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 所有单元格不可编辑
            }
        };
        requestTable = new JTable(requestTableModel);
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestTable.setRowHeight(30);
        
        // 设置表格列宽
        requestTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        requestTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        requestTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        requestTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        requestTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        requestTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        requestTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(requestTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 状态筛选
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("状态筛选:"));
        String[] statuses = {"全部", "待处理", "处理中", "已完成", "已取消"};
        JComboBox<String> statusComboBox = new JComboBox<>(statuses);
        statusComboBox.addActionListener(e -> filterRequestsByStatus((String) statusComboBox.getSelectedItem()));
        filterPanel.add(statusComboBox);
        
        panel.add(filterPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建消息通知面板
     * @return 消息通知面板
     */
    private JPanel createMessagePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 标题
        JLabel titleLabel = new JLabel("系统消息和通知", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 消息区域
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(messageArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        refreshMessagesButton = new JButton("刷新消息");
        clearMessagesButton = new JButton("清空消息");
        
        buttonPanel.add(refreshMessagesButton);
        buttonPanel.add(clearMessagesButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建老人活动面板
     * @return 老人活动面板
     */
    private JPanel createActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 标题
        JLabel titleLabel = new JLabel("老人活动管理", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton refreshButton = new JButton("刷新活动列表");
        refreshButton.addActionListener(e -> refreshActivityPanel());
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.NORTH);

        // 活动表格
        String[] columns = {"活动ID", "活动名称", "时间", "地点", "关联老人", "报名状态", "操作"};
        DefaultTableModel activityTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 操作列可编辑
                return column == 6;
            }
        };
        
        JTable activityTable = new JTable(activityTableModel);
        activityTable.setRowHeight(30);
        activityTable.setName("activityTable"); // 设置名称以便查找
        
        // 存储activityTable和model作为类变量以便访问
        this.activityTable = activityTable;
        this.activityTableModel = activityTableModel;
        
        // 加载活动数据
        loadActivityData(activityTableModel);
        
        // 为操作列添加按钮编辑器
        activityTable.getColumn("操作").setCellRenderer(new ButtonRenderer());
        activityTable.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()) {
            @Override
            public Object getCellEditorValue() {
                int row = activityTable.getSelectedRow();
                if (row != -1) {
                    String activityId = (String) activityTableModel.getValueAt(row, 0);
                    String elderName = (String) activityTableModel.getValueAt(row, 4);
                    String currentStatus = (String) activityTableModel.getValueAt(row, 5);
                    
                    handleActivityRegistration(row, activityId, elderName, currentStatus);
                }
                return "";
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(activityTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 加载活动数据到表格
     * @param activityTableModel 活动表格模型
     */
    private void loadActivityData(DefaultTableModel activityTableModel) {
        try {
            activityTableModel.setRowCount(0); // 清空现有数据
            List<Elder> myElders = getMyElders();
            List<Activity> activities = activityService.getAllActivities();
            
            System.out.println("加载活动数据 - 关联老人数: " + myElders.size() + ", 活动数: " + activities.size());
            
            if (myElders.isEmpty()) {
                messageArea.append("您还没有关联任何老人，无法查看活动\n");
                return;
            }
            
            if (activities.isEmpty()) {
                messageArea.append("当前没有可参加的活动\n");
                return;
            }
            
            for (Activity activity : activities) {
                System.out.println("活动: " + activity.getName() + ", 已报名老人: " + activity.getRegisteredElderIds());
                for (Elder elder : myElders) {
                    boolean isRegistered = activity.getRegisteredElderIds().contains(elder.getElderId());
                    System.out.println("老人 " + elder.getName() + " 是否报名: " + isRegistered);
                    
                    String registrationStatus = isRegistered ? "已报名" : "未报名";
                    
                    Object[] row = {
                        activity.getActivityId(),
                        activity.getName(),
                        activity.getTime(),
                        activity.getLocation(),
                        elder.getName(),
                        registrationStatus,
                        registrationStatus.equals("已报名") ? "取消报名" : "报名"
                    };
                    activityTableModel.addRow(row);
                }
            }
            
            messageArea.append("加载了 " + activities.size() + " 个活动\n");
        } catch (Exception e) {
            messageArea.append("加载活动失败: " + e.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, "加载活动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 刷新活动面板
     */
    private void refreshActivityPanel() {
        try {
            if (activityTableModel != null) {
                // 清空现有数据并重新加载
                activityTableModel.setRowCount(0);
                loadActivityData(activityTableModel);
                WindowUtil.showSuccessMsg(this, "活动列表刷新成功");
            } else {
                // 重新创建活动面板
                Component currentTab = tabbedPane.getComponentAt(3);
                if (currentTab instanceof JPanel) {
                    tabbedPane.remove(3);
                }
                tabbedPane.insertTab("老人活动", null, createActivityPanel(), null, 3);
                tabbedPane.setSelectedIndex(3); // 切换到活动面板
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "刷新活动失败: " + e.getMessage());
            e.printStackTrace();
        }
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
        // 关联老人面板按钮监听
        refreshEldersButton.addActionListener(e -> loadElders());
        addAssociationButton.addActionListener(e -> showAddAssociationDialog());
        removeAssociationButton.addActionListener(e -> removeSelectedAssociation());
        viewHealthRecordsButton.addActionListener(e -> showHealthRecordsDialog());
        
        // 服务申请面板按钮监听
        refreshRequestsButton.addActionListener(e -> loadServiceRequests());
        submitRequestButton.addActionListener(e -> showSubmitRequestDialog());
        cancelRequestButton.addActionListener(e -> cancelSelectedRequest());
        
        // 消息通知面板按钮监听
        refreshMessagesButton.addActionListener(e -> loadMessages());
        clearMessagesButton.addActionListener(e -> clearMessages());

        // 添加选项卡切换监听器，当切换到活动面板时刷新
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            String title = tabbedPane.getTitleAt(selectedIndex);
            if ("老人活动".equals(title)) {
                Component currentComp = tabbedPane.getComponentAt(selectedIndex);
                if (currentComp instanceof JLabel && "点击查看活动...".equals(((JLabel) currentComp).getText())) {
                    // 首次访问，创建活动面板
                    tabbedPane.setComponentAt(selectedIndex, createActivityPanel());
                } else {
                    // 已经创建过，刷新数据
                    refreshActivityPanel();
                }
            }
        });
    }

    /**
     * 加载初始数据
     */
    private void loadInitialData() {
        loadElders();
        loadServiceRequests();
        loadMessages();
    }

    /**
     * 获取当前用户关联的老人列表
     * @return 关联的老人列表
     */
    private List<Elder> getMyElders() {
        List<Elder> myElders = new ArrayList<>();
        try {
            List<String> elderIds = familyElderRelations.get(currentUser.getUserId());
            System.out.println("当前用户ID: " + currentUser.getUserId()); // 调试
            System.out.println("关联的老人ID列表: " + elderIds); // 调试
            
            if (elderIds != null) {
                for (String elderId : elderIds) {
                    System.out.println("正在加载老人: " + elderId); // 调试
                    Elder elder = elderService.getElderById(elderId);
                    if (elder != null) {
                        System.out.println("找到老人: " + elder.getName()); // 调试
                        myElders.add(elder);
                    } else {
                        System.out.println("未找到老人: " + elderId); // 调试
                    }
                }
            } else {
                System.out.println("elderIds为null"); // 调试
            }
            
            System.out.println("最终返回老人数量: " + myElders.size()); // 调试
        } catch (Exception e) {
            System.err.println("获取关联老人失败: " + e.getMessage());
            e.printStackTrace();
        }
        return myElders;
    }

    /**
     * 加载关联老人列表
     */
    private void loadElders() {
        try {
            elderTableModel.setRowCount(0); // 清空表格
            List<Elder> myElders = getMyElders();
            
            System.out.println("加载关联老人，数量: " + myElders.size()); // 调试信息
            
            for (Elder elder : myElders) {
                Object[] row = {
                    elder.getElderId(),
                    elder.getName(),
                    elder.getAge(),
                    elder.getHealthStatus(),
                    elder.getPhone(),
                    "2025-12-01 10:00" // 关联时间（应从关系表中读取）
                };
                elderTableModel.addRow(row);
            }
            
            // 更新统计标签
            if (elderCountLabel != null) {
                elderCountLabel.setText("关联老人总数: " + myElders.size());
                System.out.println("已更新统计标签: " + myElders.size()); // 调试信息
            } else {
                System.out.println("elderCountLabel为null"); // 调试信息
            }
            
            WindowUtil.showSuccessMsg(this, "老人列表刷新成功，共" + myElders.size() + "位老人");
            
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "加载老人信息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 加载服务申请列表
     */
    private void loadServiceRequests() {
        try {
            requestTableModel.setRowCount(0);
            List<Elder> myElders = getMyElders();
            
            int totalCount = 0;
            for (Elder elder : myElders) {
                List<ServiceRequest> requests = serviceRequestService.getRequestsByElderId(elder.getElderId());
                for (ServiceRequest request : requests) {
                    String content = request.getContent();
                    // 内容过长时截断显示
                    if (content.length() > 30) {
                        content = content.substring(0, 30) + "...";
                    }
                    
                    Object[] row = {
                        request.getRequestId(),
                        elder.getName(),
                        request.getServiceType(),
                        content,
                        DateFormatUtil.formatDateTime(request.getRequestTime()),
                        request.getStatus(),
                        "护工001" // 应从服务申请中获取处理人信息
                    };
                    requestTableModel.addRow(row);
                    totalCount++;
                }
            }
            
            messageArea.append("加载了 " + totalCount + " 条服务申请记录\n");
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "加载服务申请失败: " + e.getMessage());
        }
    }

    /**
     * 按状态筛选服务申请
     * @param status 状态筛选条件
     */
    private void filterRequestsByStatus(String status) {
        if ("全部".equals(status)) {
            loadServiceRequests();
            return;
        }
        
        try {
            requestTableModel.setRowCount(0);
            List<Elder> myElders = getMyElders();
            
            for (Elder elder : myElders) {
                List<ServiceRequest> requests = serviceRequestService.getRequestsByElderId(elder.getElderId());
                for (ServiceRequest request : requests) {
                    if (status.equals(request.getStatus())) {
                        String content = request.getContent();
                        if (content.length() > 30) {
                            content = content.substring(0, 30) + "...";
                        }
                        
                        Object[] row = {
                            request.getRequestId(),
                            elder.getName(),
                            request.getServiceType(),
                            content,
                            DateFormatUtil.formatDateTime(request.getRequestTime()),
                            request.getStatus(),
                            "护工001"
                        };
                        requestTableModel.addRow(row);
                    }
                }
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "筛选服务申请失败: " + e.getMessage());
        }
    }

    /**
     * 加载消息通知
     */
    private void loadMessages() {
        try {
            messageArea.setText("");
            messageArea.append("=== 系统消息和通知 ===\n");
            messageArea.append("时间: " + DateFormatUtil.formatCurrentDateTime() + "\n");
            messageArea.append("用户: " + currentUser.getUserId() + " (家属)\n\n");
            
            // 加载关联老人的健康提醒
            List<Elder> myElders = getMyElders();
            messageArea.append("=== 老人健康提醒 ===\n");
            for (Elder elder : myElders) {
                messageArea.append("• " + elder.getName() + " (" + elder.getHealthStatus() + ")\n");
                
                // 检查是否需要特别关注
                if ("高血压".equals(elder.getHealthStatus()) || "心脏病".equals(elder.getHealthStatus())) {
                    messageArea.append("   ⚠ 需要特别关注健康状况\n");
                }
            }
            
            // 加载服务申请状态更新
            messageArea.append("\n=== 服务申请更新 ===\n");
            int pendingCount = 0;
            int completedCount = 0;
            
            for (Elder elder : myElders) {
                List<ServiceRequest> requests = serviceRequestService.getRequestsByElderId(elder.getElderId());
                for (ServiceRequest request : requests) {
                    if ("待处理".equals(request.getStatus())) {
                        pendingCount++;
                    } else if ("已完成".equals(request.getStatus())) {
                        completedCount++;
                    }
                }
            }
            
            messageArea.append("待处理申请: " + pendingCount + " 条\n");
            messageArea.append("已完成申请: " + completedCount + " 条\n");
            
            // 加载活动通知
            messageArea.append("\n=== 活动通知 ===\n");
            try {
                List<Activity> activities = activityService.getAllActivities();
                for (Activity activity : activities) {
                    messageArea.append("• " + activity.getName() + " (" + activity.getTime() + ")\n");
                }
            } catch (Exception e) {
                messageArea.append("加载活动通知失败\n");
            }
            
            WindowUtil.showSuccessMsg(this, "消息刷新成功");
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "加载消息失败: " + e.getMessage());
        }
    }

    /**
     * 清空消息
     */
    private void clearMessages() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要清空所有消息吗？",
                "确认清空",
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            messageArea.setText("");
            WindowUtil.showSuccessMsg(this, "消息已清空");
        }
    }

    /**
     * 显示关联新老人对话框
     */
    private void showAddAssociationDialog() {
        JDialog dialog = new JDialog(this, "关联新老人", true);
        dialog.setSize(500, 400);
        WindowUtil.centerWindow(dialog);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 老人选择面板
        JPanel selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.setBorder(BorderFactory.createTitledBorder("选择老人"));
        
        // 老人列表
        DefaultListModel<Elder> listModel = new DefaultListModel<>();
        JList<Elder> elderList = new JList<>(listModel);
        elderList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Elder) {
                    Elder elder = (Elder) value;
                    setText(elder.getName() + " (ID: " + elder.getElderId() + ", 年龄: " + elder.getAge() + ")");
                }
                return this;
            }
        });
        
        JScrollPane listScrollPane = new JScrollPane(elderList);
        selectionPanel.add(listScrollPane, BorderLayout.CENTER);
        
        // 搜索面板
        JPanel searchPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("搜索");
        
        searchPanel.add(new JLabel("搜索老人姓名:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        
        selectionPanel.add(searchPanel, BorderLayout.NORTH);
        
        // 加载所有老人
        try {
            List<Elder> allElders = elderService.queryElders(null);
            for (Elder elder : allElders) {
                // 排除已经关联的老人
                if (!isElderAssociated(elder.getElderId())) {
                    listModel.addElement(elder);
                }
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(dialog, "加载老人列表失败: " + e.getMessage());
        }
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton associateButton = new JButton("确认关联");
        JButton cancelButton = new JButton("取消");
        
        associateButton.addActionListener(e -> {
            Elder selectedElder = elderList.getSelectedValue();
            if (selectedElder == null) {
                WindowUtil.showErrorMsg(dialog, "请选择要关联的老人");
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "确定要关联老人 " + selectedElder.getName() + " 吗？",
                    "确认关联",
                    JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    associateElder(selectedElder.getElderId());
                    WindowUtil.showSuccessMsg(dialog, "老人关联成功");
                    loadElders();
                    dialog.dispose();
                } catch (Exception ex) {
                    WindowUtil.showErrorMsg(dialog, "关联失败: " + ex.getMessage());
                }
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        // 搜索功能
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            try {
                listModel.clear();
                List<Elder> allElders = elderService.queryElders(keyword);
                for (Elder elder : allElders) {
                    if (!isElderAssociated(elder.getElderId())) {
                        listModel.addElement(elder);
                    }
                }
            } catch (Exception ex) {
                WindowUtil.showErrorMsg(dialog, "搜索失败: " + ex.getMessage());
            }
        });
        
        buttonPanel.add(associateButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(selectionPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    /**
     * 检查老人是否已关联
     * @param elderId 老人ID
     * @return true-已关联，false-未关联
     */
    private boolean isElderAssociated(String elderId) {
        List<String> myElderIds = familyElderRelations.get(currentUser.getUserId());
        return myElderIds != null && myElderIds.contains(elderId);
    }
    
    /**
     * 关联老人
     * @param elderId 老人ID
     */
    private void associateElder(String elderId) {
        List<String> myElderIds = familyElderRelations.get(currentUser.getUserId());
        if (myElderIds == null) {
            myElderIds = new ArrayList<>();
            familyElderRelations.put(currentUser.getUserId(), myElderIds);
        }
        
        if (!myElderIds.contains(elderId)) {
            myElderIds.add(elderId);
            saveFamilyElderRelations(); // 保存到文件
            
            // 记录到消息
            try {
                Elder elder = elderService.getElderById(elderId);
                if (elder != null) {
                    String message = "已成功关联老人: " + elder.getName() + " (" + elderId + ")";
                    messageArea.append("\n" + DateFormatUtil.formatCurrentDateTime() + " " + message + "\n");
                }
            } catch (Exception e) {
                // 忽略获取老人信息失败
            }
        }
    }

    /**
     * 解除选中的关联
     */
    private void removeSelectedAssociation() {
        int selectedRow = elderTable.getSelectedRow();
        if (selectedRow == -1) {
            WindowUtil.showErrorMsg(this, "请先选择要解除关联的老人");
            return;
        }
        
        String elderId = (String) elderTableModel.getValueAt(selectedRow, 0);
        String elderName = (String) elderTableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要解除与老人 " + elderName + " 的关联吗？",
                "确认解除关联",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                List<String> myElderIds = familyElderRelations.get(currentUser.getUserId());
                if (myElderIds != null) {
                    myElderIds.remove(elderId);
                    saveFamilyElderRelations(); // 保存到文件
                    
                    WindowUtil.showSuccessMsg(this, "已成功解除关联");
                    loadElders();
                    
                    // 记录到消息
                    String message = "已解除与老人 " + elderName + " 的关联";
                    messageArea.append("\n" + DateFormatUtil.formatCurrentDateTime() + " " + message + "\n");
                }
            } catch (Exception e) {
                WindowUtil.showErrorMsg(this, "解除关联失败: " + e.getMessage());
            }
        }
    }

    /**
     * 显示健康记录对话框
     */
    private void showHealthRecordsDialog() {
        int selectedRow = elderTable.getSelectedRow();
        if (selectedRow == -1) {
            WindowUtil.showErrorMsg(this, "请先选择要查看健康记录的老人");
            return;
        }
        
        String elderId = (String) elderTableModel.getValueAt(selectedRow, 0);
        String elderName = (String) elderTableModel.getValueAt(selectedRow, 1);
        
        JDialog dialog = new JDialog(this, "健康记录 - " + elderName, true);
        dialog.setSize(600, 400);
        WindowUtil.centerWindow(dialog);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 健康记录表格
        String[] columns = {"记录时间", "血压", "心率", "备注"};
        DefaultTableModel healthTableModel = new DefaultTableModel(columns, 0);
        JTable healthTable = new JTable(healthTableModel);
        healthTable.setRowHeight(25);
        
        // 加载健康记录
        try {
            List<HealthRecord> records = healthRecordService.getRecordsByElderId(elderId);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            for (HealthRecord record : records) {
                Object[] row = {
                    sdf.format(record.getRecordTime()),
                    record.getBloodPressure(),
                    record.getHeartRate(),
                    getHealthRemark(record.getBloodPressure(), record.getHeartRate())
                };
                healthTableModel.addRow(row);
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(dialog, "加载健康记录失败: " + e.getMessage());
        }
        
        JScrollPane scrollPane = new JScrollPane(healthTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 统计信息
        JPanel statsPanel = new JPanel(new GridLayout(1, 3));
        statsPanel.setBorder(BorderFactory.createTitledBorder("健康统计"));
        
        try {
            List<HealthRecord> records = healthRecordService.getRecordsByElderId(elderId);
            int totalRecords = records.size();
            int normalCount = 0;
            int warningCount = 0;
            
            for (HealthRecord record : records) {
                if (isHealthNormal(record.getBloodPressure(), record.getHeartRate())) {
                    normalCount++;
                } else {
                    warningCount++;
                }
            }
            
            statsPanel.add(new JLabel("总记录数: " + totalRecords, SwingConstants.CENTER));
            statsPanel.add(new JLabel("正常记录: " + normalCount, SwingConstants.CENTER));
            statsPanel.add(new JLabel("异常记录: " + warningCount, SwingConstants.CENTER));
        } catch (Exception e) {
            statsPanel.add(new JLabel("统计信息加载失败", SwingConstants.CENTER));
        }
        
        mainPanel.add(statsPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    /**
     * 获取健康备注
     * @param bloodPressure 血压值
     * @param heartRate 心率值
     * @return 健康备注信息
     */
    private String getHealthRemark(String bloodPressure, int heartRate) {
        if (!isHealthNormal(bloodPressure, heartRate)) {
            return "⚠ 需关注";
        }
        return "正常";
    }
    
    /**
     * 检查健康状况是否正常
     * @param bloodPressure 血压值
     * @param heartRate 心率值
     * @return true-正常，false-异常
     */
    private boolean isHealthNormal(String bloodPressure, int heartRate) {
        try {
            String[] bpParts = bloodPressure.split("/");
            int systolic = Integer.parseInt(bpParts[0]); // 收缩压
            int diastolic = Integer.parseInt(bpParts[1].split(" ")[0]); // 舒张压
            
            return systolic >= 90 && systolic <= 140 && 
                   diastolic >= 60 && diastolic <= 90 && 
                   heartRate >= 60 && heartRate <= 100;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 显示提交服务申请对话框
     */
    private void showSubmitRequestDialog() {
        List<Elder> myElders = getMyElders();
        if (myElders.isEmpty()) {
            WindowUtil.showErrorMsg(this, "您还没有关联任何老人，请先关联老人");
            return;
        }
        
        JDialog dialog = new JDialog(this, "提交服务申请", true);
        dialog.setSize(500, 450);
        WindowUtil.centerWindow(dialog);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // 选择老人
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("选择老人:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> elderComboBox = new JComboBox<>();
        for (Elder elder : myElders) {
            elderComboBox.addItem(elder.getName() + " (" + elder.getElderId() + ")");
        }
        formPanel.add(elderComboBox, gbc);
        
        // 服务类型
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("服务类型:"), gbc);
        
        gbc.gridx = 1;
        String[] serviceTypes = {"日常照护", "健康咨询", "康复辅助", "药品配送", "紧急求助", "其他服务"};
        JComboBox<String> typeComboBox = new JComboBox<>(serviceTypes);
        formPanel.add(typeComboBox, gbc);
        
        // 申请内容
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("申请内容:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea contentArea = new JTextArea(6, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        formPanel.add(contentScrollPane, gbc);
        
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 紧急程度
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("紧急程度:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> urgencyComboBox = new JComboBox<>(new String[]{"普通", "重要", "紧急"});
        formPanel.add(urgencyComboBox, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton submitButton = new JButton("提交申请");
        JButton cancelButton = new JButton("取消");
        
        submitButton.addActionListener(e -> {
            String selectedElder = (String) elderComboBox.getSelectedItem();
            String serviceType = (String) typeComboBox.getSelectedItem();
            String content = contentArea.getText().trim();
            String urgency = (String) urgencyComboBox.getSelectedItem();
            
            // 内容长度校验
            if (content.length() < 10) {
                WindowUtil.showErrorMsg(dialog, "申请内容不能少于10个字");
                return;
            }
            
            try {
                // 提取老人ID
                String elderId = selectedElder.substring(selectedElder.lastIndexOf("(") + 1, selectedElder.lastIndexOf(")"));
                
                // 创建服务申请对象
                ServiceRequest request = new ServiceRequest();
                request.setElderId(elderId);
                request.setServiceType(serviceType);
                request.setContent(content + " [紧急程度:" + urgency + "]");
                
                // 调试信息
                System.out.println("提交服务申请 - 老人ID: " + elderId + ", 服务类型: " + serviceType);
                
                // 检查elderService是否正常
                if (elderService == null) {
                    WindowUtil.showErrorMsg(dialog, "系统服务未初始化，请重新登录");
                    return;
                }
                
                // 先检查老人是否存在
                Elder elder = elderService.getElderById(elderId);
                if (elder == null) {
                    WindowUtil.showErrorMsg(dialog, "老人不存在或已被删除，ID: " + elderId);
                    return;
                }
                
                // 提交申请
                serviceRequestService.submitRequest(request); 
                
                WindowUtil.showSuccessMsg(dialog, "服务申请提交成功");
                
                // 记录到消息
                String message = "为老人 " + elder.getName() + " 提交了 " + serviceType + " 申请";
                messageArea.append("\n" + DateFormatUtil.formatCurrentDateTime() + " " + message + "\n");
                
                loadServiceRequests();
                dialog.dispose();
            } catch (IllegalArgumentException ex) {
                WindowUtil.showErrorMsg(dialog, "提交失败: " + ex.getMessage());
            } catch (NullPointerException ex) {
                WindowUtil.showErrorMsg(dialog, "系统服务异常，请重启系统: " + ex.getMessage());
                ex.printStackTrace();
            } catch (Exception ex) {
                WindowUtil.showErrorMsg(dialog, "提交失败: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * 取消选中的服务申请
     */
    private void cancelSelectedRequest() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) {
            WindowUtil.showErrorMsg(this, "请先选择要取消的申请");
            return;
        }
        
        String requestId = (String) requestTableModel.getValueAt(selectedRow, 0);
        String elderName = (String) requestTableModel.getValueAt(selectedRow, 1);
        String status = (String) requestTableModel.getValueAt(selectedRow, 5);
        
        if ("已完成".equals(status) || "已取消".equals(status)) {
            WindowUtil.showErrorMsg(this, "该申请已完成或已取消，不能再次取消");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要取消 " + elderName + " 的服务申请吗？",
                "确认取消",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = serviceRequestService.updateRequestStatus(requestId, "已取消");
                if (success) {
                    WindowUtil.showSuccessMsg(this, "申请已取消");
                    loadServiceRequests();
                    
                    // 记录到消息
                    String message = "取消了 " + elderName + " 的服务申请";
                    messageArea.append("\n" + DateFormatUtil.formatCurrentDateTime() + " " + message + "\n");
                } else {
                    WindowUtil.showErrorMsg(this, "取消失败，申请可能不存在");
                }
            } catch (Exception e) {
                WindowUtil.showErrorMsg(this, "取消失败: " + e.getMessage());
            }
        }
    }

    /**
     * 处理活动报名/取消报名
     * @param row 表格行索引
     * @param activityId 活动ID
     * @param elderName 老人姓名
     * @param currentStatus 当前状态
     */
    private void handleActivityRegistration(int row, String activityId, String elderName, String currentStatus) {
        try {
            // 找到对应的老人
            Elder targetElder = null;
            List<Elder> myElders = getMyElders();
            for (Elder elder : myElders) {
                if (elder.getName().equals(elderName)) {
                    targetElder = elder;
                    break;
                }
            }
            
            if (targetElder == null) {
                WindowUtil.showErrorMsg(this, "找不到对应的老人");
                return;
            }
            
            String message;
            if ("已报名".equals(currentStatus)) {
                // 取消报名
                int confirm = JOptionPane.showConfirmDialog(this,
                        "确定要取消 " + elderName + " 的活动报名吗？",
                        "确认取消报名",
                        JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    // 调用取消报名方法
                    boolean success = activityService.cancelRegistration(activityId, targetElder.getElderId());
                    if (success) {
                        // 更新UI
                        if (activityTableModel != null && row < activityTableModel.getRowCount()) {
                            activityTableModel.setValueAt("未报名", row, 5);
                            activityTableModel.setValueAt("报名", row, 6);
                        }
                        
                        message = "已取消 " + elderName + " 的活动报名";
                        WindowUtil.showSuccessMsg(this, message);
                        messageArea.append("\n" + DateFormatUtil.formatCurrentDateTime() + " " + message + "\n");
                    } else {
                        WindowUtil.showErrorMsg(this, "取消失败，可能未报名或活动不存在");
                    }
                }
            } else {
                // 报名
                int confirm = JOptionPane.showConfirmDialog(this,
                        "确定要为 " + elderName + " 报名参加活动吗？",
                        "确认报名",
                        JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = activityService.registerActivity(activityId, targetElder.getElderId());
                    if (success) {
                        // 更新UI
                        if (activityTableModel != null && row < activityTableModel.getRowCount()) {
                            activityTableModel.setValueAt("已报名", row, 5);
                            activityTableModel.setValueAt("取消报名", row, 6);
                        }
                        
                        message = "已为 " + elderName + " 成功报名活动";
                        WindowUtil.showSuccessMsg(this, message);
                        messageArea.append("\n" + DateFormatUtil.formatCurrentDateTime() + " " + message + "\n");
                        
                        // 通知管理员或护工
                        String notification = "家属 " + currentUser.getUserId() + " 为老人 " + elderName + 
                                            " 报名参加了活动: " + getActivityNameById(activityId);
                        messageArea.append("系统消息: " + notification + "\n");
                    } else {
                        WindowUtil.showErrorMsg(this, "报名失败，可能已报满或已报名");
                    }
                }
            }
            
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据活动ID获取活动名称
     * @param activityId 活动ID
     * @return 活动名称
     */
    private String getActivityNameById(String activityId) {
        try {
            Activity activity = activityService.getActivityById(activityId);
            return activity != null ? activity.getName() : "未知活动";
        } catch (Exception e) {
            return "未知活动";
        }
    }

    /**
     * 显示个人信息对话框
     */
    private void showProfileDialog() {
        JDialog dialog = new JDialog(this, "个人信息", true);
        dialog.setSize(300, 250);
        WindowUtil.centerWindow(dialog);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("账号:"), gbc);
        
        gbc.gridx = 1;
        panel.add(new JLabel(currentUser.getUserId()), gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("角色:"), gbc);
        
        gbc.gridx = 1;
        panel.add(new JLabel("家属"), gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("关联老人数:"), gbc);
        
        gbc.gridx = 1;
        List<Elder> myElders = getMyElders();
        panel.add(new JLabel(String.valueOf(myElders.size())), gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dialog.dispose());
        panel.add(closeButton, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * 显示修改密码对话框
     */
    private void showChangePasswordDialog() {
        JDialog dialog = new JDialog(this, "修改密码", true);
        dialog.setSize(300, 200);
        WindowUtil.centerWindow(dialog);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("当前密码:"), gbc);
        
        gbc.gridx = 1;
        JPasswordField currentPasswordField = new JPasswordField(15);
        panel.add(currentPasswordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("新密码:"), gbc);
        
        gbc.gridx = 1;
        JPasswordField newPasswordField = new JPasswordField(15);
        panel.add(newPasswordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("确认新密码:"), gbc);
        
        gbc.gridx = 1;
        JPasswordField confirmPasswordField = new JPasswordField(15);
        panel.add(confirmPasswordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton confirmButton = new JButton("确认修改");
        JButton cancelButton = new JButton("取消");
        
        confirmButton.addActionListener(e -> {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (!newPassword.equals(confirmPassword)) {
                WindowUtil.showErrorMsg(dialog, "两次输入的新密码不一致");
                return;
            }
            
            if (newPassword.length() < 6) {
                WindowUtil.showErrorMsg(dialog, "新密码长度不能少于6位");
                return;
            }
            
            try {
                // 验证当前密码
                UserService userService = UserService.getInstance();
                userService.login(currentUser.getUserId(), currentPassword);
                
                // 更新密码
                userService.updateUserPassword(currentUser.getUserId(), newPassword);
                
                WindowUtil.showSuccessMsg(dialog, "密码修改成功");
                dialog.dispose();
            } catch (Exception ex) {
                WindowUtil.showErrorMsg(dialog, "密码修改失败: " + ex.getMessage());
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * 导出家属数据
     */
    private void exportFamilyData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出家属数据");
        fileChooser.setSelectedFile(new java.io.File("家属数据_" + currentUser.getUserId() + "_" + 
            new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                writer.println("=== 养老院管理系统家属数据导出 ===");
                writer.println("导出时间: " + DateFormatUtil.formatCurrentDateTime());
                writer.println("家属账号: " + currentUser.getUserId());
                writer.println();
                
                // 导出关联老人信息
                writer.println("=== 关联老人信息 ===");
                List<Elder> myElders = getMyElders();
                for (Elder elder : myElders) {
                    writer.println("老人ID: " + elder.getElderId());
                    writer.println("姓名: " + elder.getName());
                    writer.println("年龄: " + elder.getAge());
                    writer.println("健康状况: " + elder.getHealthStatus());
                    writer.println("联系方式: " + elder.getPhone());
                    writer.println();
                }
                
                // 导出服务申请信息
                writer.println("=== 服务申请记录 ===");
                for (Elder elder : myElders) {
                    List<ServiceRequest> requests = serviceRequestService.getRequestsByElderId(elder.getElderId());
                    for (ServiceRequest request : requests) {
                        writer.println("申请ID: " + request.getRequestId());
                        writer.println("老人: " + elder.getName());
                        writer.println("服务类型: " + request.getServiceType());
                        writer.println("申请内容: " + request.getContent());
                        writer.println("申请时间: " + DateFormatUtil.formatDateTime(request.getRequestTime()));
                        writer.println("状态: " + request.getStatus());
                        writer.println();
                    }
                }
                
                WindowUtil.showSuccessMsg(this, "数据导出成功: " + file.getAbsolutePath());
            } catch (Exception e) {
                WindowUtil.showErrorMsg(this, "导出失败: " + e.getMessage());
            }
        }
    }

    /**
     * 显示使用指南
     */
    private void showUserGuide() {
        JDialog dialog = new JDialog(this, "使用指南", true);
        dialog.setSize(500, 400);
        WindowUtil.centerWindow(dialog);
        
        JTextArea guideArea = new JTextArea();
        guideArea.setEditable(false);
        guideArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        guideArea.setText(
                "养老院管理系统 - 家属使用指南\n\n" +
                "一、关联老人管理\n" +
                "1. 在'关联老人'选项卡查看已关联的老人\n" +
                "2. 点击'关联新老人'可以添加关联老人\n" +
                "3. 选择老人后点击'解除关联'可以删除关联\n" +
                "4. 点击'查看健康记录'查看老人健康数据\n\n" +
                "二、服务申请管理\n" +
                "1. 在'服务申请'选项卡查看所有申请\n" +
                "2. 点击'提交新申请'为老人申请服务\n" +
                "3. 可以使用状态筛选查看不同状态的申请\n" +
                "4. 对未处理的申请可以点击'取消选中申请'\n\n" +
                "三、消息通知\n" +
                "1. 系统自动推送老人健康提醒\n" +
                "2. 查看服务申请处理进度\n" +
                "3. 接收活动通知信息\n\n" +
                "四、老人活动管理\n" +
                "1. 查看养老院组织的所有活动\n" +
                "2. 为关联老人报名参加活动\n" +
                "3. 可以取消已报名的活动\n\n" +
                "五、系统功能\n" +
                "1. 在'系统'菜单中修改个人信息和密码\n" +
                "2. 使用'工具'菜单导出数据\n" +
                "3. 定期查看系统消息，及时处理\n"
        );
        
        dialog.add(new JScrollPane(guideArea));
        dialog.setVisible(true);
    }
}