// src/main/java/com/eldercare/ui/CaregiverFrame.java
package com.eldercare.ui;

import com.eldercare.model.*;
import com.eldercare.service.*;
import com.eldercare.util.WindowUtil;
import com.eldercare.util.DateFormatUtil;
import com.eldercare.util.DataStorageUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * 护工用户主界面
 * 功能：提供护工用户的各项功能，包括照护管理、健康记录、服务处理、工作日志等
 */
public class CaregiverFrame extends JFrame {
    // 当前登录用户
    private User currentUser;

    // 窗口默认大小
    private static final int DEFAULT_WIDTH = 900;
    private static final int DEFAULT_HEIGHT = 600;

    // 服务类实例
    private ElderService elderService;
    private HealthRecordService healthRecordService;
    private ServiceRequestService serviceRequestService;

    // UI组件
    private JTabbedPane tabbedPane;
    private DefaultTableModel careTableModel;
    private DefaultTableModel serviceTableModel;
    private JComboBox<String> elderComboBox;

    // 工作日志相关字段
    private JTextArea logArea;
    private static final String WORK_LOG_KEY = "caregiver_work_logs";
    private Timer autoSaveTimer;
    private boolean isAutoSaveEnabled = true;
    private JButton autoSaveToggleBtn;

    // 静态初始化块，确保数据加载
    static {
        try {
            // 触发 ElderService 初始化，确保数据加载
            ElderService elderService = ElderService.getInstance();
            System.out.println("静态初始化: ElderService 已加载");
        } catch (Exception e) {
            System.err.println("静态初始化 ElderService 失败: " + e.getMessage());
        }
    }

    /**
     * 构造方法
     * @param user 当前登录的用户对象
     */
    public CaregiverFrame(User user) {
        this.currentUser = user;

        // 初始化服务类
        initializeServices();

        initComponents();
        setupLayout();
        loadInitialData();

        // 添加窗口关闭监听器
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // 保存当前日志
                if (logArea != null) {
                    String content = logArea.getText().trim();
                    if (!content.isEmpty()) {
                        saveWorkLogToStorage(content);
                    }
                }

                // 停止定时器
                if (autoSaveTimer != null) {
                    autoSaveTimer.stop();
                }

                // 正常关闭
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
    }

    /**
     * 初始化服务类实例
     */
    private void initializeServices() {
        // 先初始化 ElderService（最基础的服务）
        try {
            elderService = ElderService.getInstance();
            System.out.println("ElderService 已初始化，有 " + elderService.queryElders(null).size() + " 个老人");
        } catch (Exception e) {
            System.err.println("ElderService 初始化失败: " + e.getMessage());
            elderService = createMockElderService();
        }

        // 然后初始化依赖 ElderService 的其他服务
        try {
            healthRecordService = HealthRecordService.getInstance();
            System.out.println("HealthRecordService 已初始化");
        } catch (Exception e) {
            System.err.println("HealthRecordService 初始化失败: " + e.getMessage());
            healthRecordService = createMockHealthRecordService();
        }

        try {
            serviceRequestService = ServiceRequestService.getInstance();
            System.out.println("ServiceRequestService 已初始化");
        } catch (Exception e) {
            System.err.println("ServiceRequestService 初始化失败: " + e.getMessage());
            serviceRequestService = createMockServiceRequestService();
        }
    }

    /**
     * 创建模拟ElderService（用于服务初始化失败时）
     */
    private ElderService createMockElderService() {
        return new ElderService() {
            @Override
            public List<Elder> queryElders(String keyword) {
                System.out.println("调用模拟 queryElders");
                List<Elder> elders = new ArrayList<>();
                elders.add(new Elder("E001", "USER_001", "张三", 75, "13800138000", "健康状况良好"));
                elders.add(new Elder("E002", "USER_002", "李四", 80, "13800138001", "血压偏高"));
                return elders;
            }

            @Override
            public Elder getElderById(String elderId) {
                System.out.println("调用模拟 getElderById: " + elderId);
                return new Elder(elderId, "USER_" + elderId.substring(1) + "", "模拟老人", 70, "00000000000", "良好");
            }
        };
    }

    /**
     * 创建模拟HealthRecordService（用于服务初始化失败时）
     */
    private HealthRecordService createMockHealthRecordService() {
        return new HealthRecordService() {
            @Override
            public void addHealthRecord(HealthRecord record) {
                System.out.println("模拟添加健康记录: " + record.getElderId());
            }

            @Override
            public List<HealthRecord> getRecordsByElderId(String elderId) {
                System.out.println("模拟获取健康记录: " + elderId);
                return new ArrayList<>();
            }
        };
    }

    /**
     * 创建模拟ServiceRequestService（用于服务初始化失败时）
     */
    private ServiceRequestService createMockServiceRequestService() {
        return new ServiceRequestService() {
            @Override
            public boolean updateRequestStatus(String requestId, String status) {
                System.out.println("模拟更新申请状态: " + requestId + " -> " + status);
                return true;
            }

            public List<ServiceRequest> getRequestList() {
                return new ArrayList<>();
            }
        };
    }

    /**
     * 初始化所有组件
     */
    private void initComponents() {
        setTitle(WindowUtil.getWindowTitle("护工面板 - " + currentUser.getUserId()));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setResizable(true); // 确保窗口可以调整大小
        WindowUtil.centerWindow(this);

        // 创建菜单栏
        createMenuBar();

        // 创建选项卡面板
        tabbedPane = new JTabbedPane();

        // 创建各个功能面板
        JPanel carePanel = createCarePanel();
        tabbedPane.addTab("照护管理", carePanel);

        JPanel healthPanel = createHealthPanel();
        tabbedPane.addTab("健康记录", healthPanel);

        JPanel servicePanel = createServicePanel();
        tabbedPane.addTab("服务处理", servicePanel);

        JPanel logPanel = createLogPanel();
        tabbedPane.addTab("工作日志", logPanel);

        add(tabbedPane, BorderLayout.CENTER);
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
            // 保存当前日志
            if (logArea != null) {
                String content = logArea.getText().trim();
                if (!content.isEmpty()) {
                    saveWorkLogToStorage(content);
                }
            }
            dispose();
            new LoginFrame().setVisible(true);
        });
        
        // 添加全屏切换菜单项
        JMenuItem toggleFullScreenItem = new JMenuItem("切换全屏");
        toggleFullScreenItem.addActionListener(e -> {
            WindowUtil.toggleFullScreen(this, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        });
        
        JMenuItem changePwdItem = new JMenuItem("修改密码");
        changePwdItem.addActionListener(e -> showChangePasswordDialog());

        // 日志相关菜单项
        JMenuItem viewHistoryItem = new JMenuItem("查看历史日志");
        JMenuItem exportLogItem = new JMenuItem("导出当前日志");

        viewHistoryItem.addActionListener(e -> showLogHistory());
        exportLogItem.addActionListener(e -> exportCurrentLog());

        systemMenu.add(logoutItem);
        systemMenu.add(toggleFullScreenItem);
        systemMenu.addSeparator();
        systemMenu.add(changePwdItem);
        systemMenu.addSeparator();
        systemMenu.add(viewHistoryItem);
        systemMenu.add(exportLogItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "养老院管理系统 - 护工版 v1.0\n" +
                                "当前用户: " + currentUser.getUserId() + "\n" +
                                "角色: 护工",
                        "关于",
                        JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(systemMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    /**
     * 创建照护管理面板
     * @return 照护管理面板
     */
    private JPanel createCarePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("负责照护的老人列表", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 老人表格
        String[] columns = {"老人ID", "姓名", "年龄", "手机号", "健康状况", "操作"};
        careTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 只有操作列可编辑
                return column == 5;
            }
        };
        JTable table = new JTable(careTableModel);

        // 设置操作列的按钮
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), "查看详情") {
            @Override
            public void buttonClicked(int row) {
                showElderDetails(row);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 搜索和刷新面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(15);
        JButton searchButton = new JButton("搜索");
        JButton refreshButton = new JButton("刷新列表");

        searchButton.addActionListener(e -> searchElders(searchField.getText()));
        refreshButton.addActionListener(e -> loadCaredElders());

        searchPanel.add(new JLabel("姓名搜索:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        panel.add(searchPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建健康记录面板
     * @return 健康记录面板
     */
    private JPanel createHealthPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("老人健康记录管理", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 健康记录输入表单
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("新增健康记录"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 选择老人下拉框
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("选择老人:"), gbc);

        gbc.gridx = 1;
        elderComboBox = new JComboBox<>();
        formPanel.add(elderComboBox, gbc);

        // 血压输入
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("血压 (如 120/80):"), gbc);

        gbc.gridx = 1;
        JTextField bloodPressureField = new JTextField();
        formPanel.add(bloodPressureField, gbc);

        // 心率输入
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("心率:"), gbc);

        gbc.gridx = 1;
        JTextField heartRateField = new JTextField();
        formPanel.add(heartRateField, gbc);

        // 提交按钮
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JButton submitButton = new JButton("提交记录");
        submitButton.addActionListener(e -> {
            try {
                String elderSelection = (String) elderComboBox.getSelectedItem();
                String bp = bloodPressureField.getText();
                String hr = heartRateField.getText();
                submitHealthRecord(elderSelection, bp, hr);
                bloodPressureField.setText("");
                heartRateField.setText("");
                WindowUtil.showSuccessMsg(this, "健康记录提交成功！");
            } catch (Exception ex) {
                WindowUtil.showErrorMsg(this, "提交失败: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        formPanel.add(submitButton, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        // 查看历史记录按钮
        JButton viewHistoryButton = new JButton("查看历史记录");
        viewHistoryButton.addActionListener(e -> showHealthHistory());
        panel.add(viewHistoryButton, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建服务处理面板
     * @return 服务处理面板
     */
    private JPanel createServicePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("待处理服务申请", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 服务申请表格
        String[] columns = {"申请ID", "老人", "服务类型", "申请时间", "状态", "操作"};
        serviceTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 只有操作列可编辑
                return column == 5;
            }
        };
        JTable table = new JTable(serviceTableModel);

        // 设置操作列的按钮
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), "处理") {
            @Override
            public void buttonClicked(int row) {
                processServiceRequest(row);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 状态筛选
        JPanel filterPanel = new JPanel(new FlowLayout());
        filterPanel.add(new JLabel("筛选状态:"));
        String[] statuses = {"全部", "待处理", "处理中", "已完成", "已取消"};
        JComboBox<String> statusComboBox = new JComboBox<>(statuses);
        statusComboBox.addActionListener(e -> filterServiceRequests((String) statusComboBox.getSelectedItem()));
        filterPanel.add(statusComboBox);

        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> loadServiceRequests());
        filterPanel.add(refreshButton);

        panel.add(filterPanel, BorderLayout.NORTH);

        return panel;
    }

    /**
     * 创建工作日志面板
     * @return 工作日志面板
     */
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("工作日志", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 日志编辑区域
        logArea = new JTextArea();
        logArea.setEditable(true);
        logArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 初始化日志内容
        initLogContent();

        // 添加文档监听器实现自动保存
        logArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                autoSaveTriggered();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                autoSaveTriggered();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                autoSaveTriggered();
            }

            private void autoSaveTriggered() {
                if (isAutoSaveEnabled) {
                    autoSaveTimer.restart();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("保存日志");
        JButton resetButton = new JButton("重置模板");
        JButton clearButton = new JButton("清空");
        autoSaveToggleBtn = new JButton("自动保存: 开");

        saveButton.addActionListener(e -> {
            saveWorkLog();
            WindowUtil.showSuccessMsg(this, "工作日志保存成功");
        });

        resetButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "重置模板会覆盖当前内容，是否继续？",
                    "确认",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                logArea.setText(createDefaultLogTemplate());
                WindowUtil.showSuccessMsg(this, "已重置为默认模板");
            }
        });

        clearButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要清空日志内容吗？",
                    "确认",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                logArea.setText("");
                WindowUtil.showSuccessMsg(this, "日志内容已清空");
            }
        });

        // 自动保存开关
        autoSaveToggleBtn.addActionListener(e -> toggleAutoSave());

        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(autoSaveToggleBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // 初始化自动保存定时器
        initAutoSaveTimer();

        return panel;
    }

    /**
     * 设置布局（各组件已在创建方法中设置）
     */
    private void setupLayout() {
        // 布局已在各组件创建方法中设置
    }

    /**
     * 加载初始数据
     */
    private void loadInitialData() {
        loadCaredElders();
        loadElderComboBox();
        loadServiceRequests();
    }

    /**
     * 加载照护老人列表
     */
    private void loadCaredElders() {
        try {
            careTableModel.setRowCount(0);
            List<Elder> elders = elderService.queryElders(null);
            for (Elder elder : elders) {
                Object[] row = {
                        elder.getElderId(),
                        elder.getName(),
                        elder.getAge(),
                        elder.getPhone(),
                        elder.getHealthStatus(),
                        "查看详情"
                };
                careTableModel.addRow(row);
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "加载老人信息失败: " + e.getMessage());
        }
    }

    /**
     * 加载老人下拉框
     */
    private void loadElderComboBox() {
        try {
            elderComboBox.removeAllItems();
            List<Elder> elders = elderService.queryElders(null);
            for (Elder elder : elders) {
                elderComboBox.addItem(elder.getElderId() + " - " + elder.getName());
            }
            if (elders.size() > 0) {
                elderComboBox.setSelectedIndex(0);
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "加载老人列表失败: " + e.getMessage());
        }
    }

    /**
     * 加载服务申请列表
     */
    private void loadServiceRequests() {
        try {
            serviceTableModel.setRowCount(0);
            // 获取所有服务申请
            List<ServiceRequest> requests = getAllServiceRequests();
            for (ServiceRequest request : requests) {
                try {
                    Elder elder = elderService.getElderById(request.getElderId());
                    Object[] row = {
                            request.getRequestId(),
                            elder != null ? elder.getName() : "未知",
                            request.getServiceType(),
                            DateFormatUtil.formatDateTime(request.getRequestTime()),
                            request.getStatus(),
                            "处理"
                    };
                    serviceTableModel.addRow(row);
                } catch (Exception ex) {
                    // 跳过有问题的记录
                }
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "加载服务申请失败: " + e.getMessage());
        }
    }

    /**
     * 搜索老人
     * @param keyword 搜索关键词
     */
    private void searchElders(String keyword) {
        try {
            careTableModel.setRowCount(0);
            List<Elder> elders = elderService.queryElders(keyword);
            for (Elder elder : elders) {
                Object[] row = {
                        elder.getElderId(),
                        elder.getName(),
                        elder.getAge(),
                        elder.getPhone(),
                        elder.getHealthStatus(),
                        "查看详情"
                };
                careTableModel.addRow(row);
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "搜索失败: " + e.getMessage());
        }
    }

    /**
     * 提交健康记录
     * @param elderSelection 老人选择项
     * @param bloodPressure 血压值
     * @param heartRate 心率值
     */
    private void submitHealthRecord(String elderSelection, String bloodPressure, String heartRate)
            throws IOException, ClassNotFoundException {
        if (elderSelection == null || elderSelection.isEmpty()) {
            throw new IllegalArgumentException("请选择老人");
        }

        String elderId = elderSelection.split(" - ")[0];
        int heartRateInt = Integer.parseInt(heartRate);

        HealthRecord record = new HealthRecord();
        record.setElderId(elderId);
        record.setBloodPressure(bloodPressure);
        record.setHeartRate(heartRateInt);
        record.setRecordTime(new Date());

        healthRecordService.addHealthRecord(record);

        // 自动记录到工作日志
        String elderName = elderSelection.split(" - ")[1];
        addHealthRecordToLog(elderName, bloodPressure, String.valueOf(heartRateInt));
    }

    /**
     * 显示老人详细信息
     * @param rowIndex 表格行索引
     */
    private void showElderDetails(int rowIndex) {
        String elderId = (String) careTableModel.getValueAt(rowIndex, 0);
        String elderName = (String) careTableModel.getValueAt(rowIndex, 1);

        try {
            Elder elder = elderService.getElderById(elderId);
            List<HealthRecord> records = healthRecordService.getRecordsByElderId(elderId);

            StringBuilder details = new StringBuilder();
            details.append("老人详细信息\n");
            details.append("==============\n");
            details.append("姓名: ").append(elder.getName()).append("\n");
            details.append("ID: ").append(elder.getElderId()).append("\n");
            details.append("年龄: ").append(elder.getAge()).append("\n");
            details.append("手机号: ").append(elder.getPhone()).append("\n");
            details.append("健康状况: ").append(elder.getHealthStatus()).append("\n\n");
            details.append("最近健康记录:\n");

            if (records.isEmpty()) {
                details.append("暂无健康记录\n");
            } else {
                for (int i = 0; i < Math.min(3, records.size()); i++) {
                    HealthRecord record = records.get(i);
                    details.append(DateFormatUtil.formatDateTime(record.getRecordTime()))
                            .append(" 血压: ").append(record.getBloodPressure())
                            .append(" 心率: ").append(record.getHeartRate())
                            .append("\n");
                }
            }

            JOptionPane.showMessageDialog(this, details.toString(),
                    elderName + " - 详情", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "获取详情失败: " + e.getMessage());
        }
    }

    /**
     * 处理服务申请
     * @param rowIndex 表格行索引
     */
    private void processServiceRequest(int rowIndex) {
        // 检查行索引是否有效
        if (rowIndex < 0 || rowIndex >= serviceTableModel.getRowCount()) {
            WindowUtil.showErrorMsg(this, "无效的行选择");
            return;
        }

        String requestId = (String) serviceTableModel.getValueAt(rowIndex, 0);
        String currentStatus = (String) serviceTableModel.getValueAt(rowIndex, 4);

        String[] options;
        if ("待处理".equals(currentStatus)) {
            options = new String[]{"开始处理", "标记完成", "查看详情"};
        } else if ("处理中".equals(currentStatus)) {
            options = new String[]{"标记完成", "取消申请", "查看详情"};
        } else {
            options = new String[]{"查看详情"};
        }

        String action = (String) JOptionPane.showInputDialog(
                this,
                "请选择操作:",
                "处理服务申请",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (action != null) {
            try {
                String newStatus = currentStatus;
                switch (action) {
                    case "开始处理":
                        newStatus = "处理中";
                        break;
                    case "标记完成":
                        newStatus = "已完成";
                        break;
                    case "取消申请":
                        newStatus = "已取消";
                        break;
                    case "查看详情":
                        showRequestDetails(requestId);
                        return;
                }

                // 直接使用文件操作更新状态
                boolean success = updateServiceRequestStatusInFile(requestId, newStatus);
                if (success) {
                    // 自动记录到工作日志
                    String elderName = (String) serviceTableModel.getValueAt(rowIndex, 1);
                    String serviceType = (String) serviceTableModel.getValueAt(rowIndex, 2);
                    addServiceRequestToLog(elderName, serviceType, newStatus);

                    // 刷新表格
                    loadServiceRequests();
                    WindowUtil.showSuccessMsg(this, "操作成功！状态已更新为: " + newStatus);
                } else {
                    WindowUtil.showErrorMsg(this, "操作失败：申请不存在");
                }
            } catch (Exception e) {
                WindowUtil.showErrorMsg(this, "操作失败: " + e.getMessage());
            }
        }
    }

    /**
     * 显示申请详情
     * @param requestId 申请ID
     */
    private void showRequestDetails(String requestId) {
        try {
            List<ServiceRequest> requests = getServiceRequestsFromFile();
            ServiceRequest targetRequest = null;

            for (ServiceRequest request : requests) {
                if (request.getRequestId().equals(requestId)) {
                    targetRequest = request;
                    break;
                }
            }

            if (targetRequest == null) {
                JOptionPane.showMessageDialog(this,
                        "未找到申请ID: " + requestId,
                        "申请详情",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Elder elder = elderService.getElderById(targetRequest.getElderId());

            StringBuilder details = new StringBuilder();
            details.append("服务申请详情\n");
            details.append("==============\n\n");
            details.append("申请ID: ").append(targetRequest.getRequestId()).append("\n");
            details.append("老人姓名: ").append(elder != null ? elder.getName() : "未知").append("\n");
            details.append("老人ID: ").append(targetRequest.getElderId()).append("\n");
            details.append("服务类型: ").append(targetRequest.getServiceType()).append("\n");
            details.append("申请时间: ").append(DateFormatUtil.formatDateTime(targetRequest.getRequestTime())).append("\n");
            details.append("当前状态: ").append(targetRequest.getStatus()).append("\n\n");
            details.append("申请内容:\n");
            details.append(targetRequest.getContent() != null ? targetRequest.getContent() : "无").append("\n");

            JTextArea textArea = new JTextArea(details.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 300));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "申请详情 - " + requestId,
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "获取申请详情失败: " + e.getMessage());
        }
    }

    /**
     * 筛选服务申请
     * @param status 状态筛选条件
     */
    private void filterServiceRequests(String status) {
        if ("全部".equals(status)) {
            loadServiceRequests();
            return;
        }

        try {
            serviceTableModel.setRowCount(0);
            List<ServiceRequest> requests = getServiceRequestsFromFile();

            for (ServiceRequest request : requests) {
                if (status.equals(request.getStatus())) {
                    try {
                        Elder elder = elderService.getElderById(request.getElderId());
                        Object[] row = {
                                request.getRequestId(),
                                elder != null ? elder.getName() : "未知",
                                request.getServiceType(),
                                DateFormatUtil.formatDateTime(request.getRequestTime()),
                                request.getStatus(),
                                "处理"
                        };
                        serviceTableModel.addRow(row);
                    } catch (Exception ex) {
                        // 跳过有问题的记录
                    }
                }
            }
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "筛选服务申请失败: " + e.getMessage());
        }
    }

    /**
     * 显示健康记录历史
     */
    private void showHealthHistory() {
        if (elderComboBox.getSelectedItem() == null) {
            WindowUtil.showErrorMsg(this, "请先选择老人");
            return;
        }

        String elderId = ((String) elderComboBox.getSelectedItem()).split(" - ")[0];
        try {
            List<HealthRecord> records = healthRecordService.getRecordsByElderId(elderId);

            StringBuilder history = new StringBuilder();
            history.append("健康记录历史\n");
            history.append("==============\n");

            if (records.isEmpty()) {
                history.append("暂无健康记录\n");
            } else {
                for (HealthRecord record : records) {
                    history.append(DateFormatUtil.formatDateTime(record.getRecordTime()))
                            .append(" 血压: ").append(record.getBloodPressure())
                            .append(" 心率: ").append(record.getHeartRate())
                            .append("\n");
                }
            }

            JTextArea textArea = new JTextArea(history.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "健康记录历史", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "获取历史记录失败: " + e.getMessage());
        }
    }

    /**
     * 初始化日志内容
     */
    private void initLogContent() {
        try {
            // 尝试从文件加载今日日志
            Map<String, String> logs = loadAllWorkLogs();
            String today = DateFormatUtil.formatCurrentDate();
            String todayLog = logs.get(today);

            if (todayLog != null && !todayLog.trim().isEmpty()) {
                logArea.setText(todayLog);
            } else {
                logArea.setText(createDefaultLogTemplate());
            }
        } catch (Exception e) {
            System.err.println("初始化日志失败: " + e.getMessage());
            logArea.setText(createDefaultLogTemplate());
        }
    }

    /**
     * 创建默认日志模板
     * @return 默认日志模板内容
     */
    private String createDefaultLogTemplate() {
        return "护工工作日志\n" +
                "==================\n" +
                "护工：" + currentUser.getUserId() + "\n" +
                "日期：" + DateFormatUtil.formatCurrentDate() + "\n" +
                "时间：" + DateFormatUtil.formatCurrentDateTime() + "\n" +
                "\n" +
                "【今日工作计划】\n" +
                "1. 晨间护理（体温测量、血压监测）\n" +
                "2. 按时给药管理\n" +
                "3. 协助老人用餐\n" +
                "4. 组织康复活动\n" +
                "5. 晚间护理检查\n" +
                "\n" +
                "【健康记录】\n" +
                "\n" +
                "【服务处理】\n" +
                "\n" +
                "【特殊事项】\n" +
                "1. 老人家属来电，请及时处理\n" +
                "2. 请老人注意饮食安全\n" +
                "\n" +
                "【明日计划】\n" +
                "1. 按时给药管理\n" +
                "2. 协助老人用餐\n" +
                "3. 组织康复活动\n" +
                "4. 晚间护理检查\n" +
                "\n" +
                "备注：\n";
    }

    /**
     * 初始化自动保存定时器
     */
    private void initAutoSaveTimer() {
        autoSaveTimer = new Timer(30000, e -> { // 30秒自动保存
            if (isAutoSaveEnabled && logArea != null) {
                String content = logArea.getText().trim();
                if (!content.isEmpty()) {
                    saveWorkLogToStorage(content);
                    System.out.println("[" + DateFormatUtil.formatCurrentDateTime() + "] 工作日志自动保存完成");
                }
            }
        });
        autoSaveTimer.start();
    }

    /**
     * 获取当前时间（HH:mm:ss格式）
     * @return 当前时间字符串
     */
    private String getCurrentTime() {
        try {
            // 使用现有的 DateFormatUtil 方法
            String fullDateTime = DateFormatUtil.formatCurrentDateTime(); // "yyyy-MM-dd HH:mm:ss"
            // 提取时间部分
            if (fullDateTime.length() > 10) {
                return fullDateTime.substring(11); // "HH:mm:ss"
            }
            return fullDateTime;
        } catch (Exception e) {
            // 如果出错，返回简单时间
            return new SimpleDateFormat("HH:mm:ss").format(new Date());
        }
    }

    /**
     * 切换自动保存开关
     */
    private void toggleAutoSave() {
        isAutoSaveEnabled = !isAutoSaveEnabled;
        if (isAutoSaveEnabled) {
            autoSaveToggleBtn.setText("自动保存: 开");
            autoSaveTimer.start();
        } else {
            autoSaveToggleBtn.setText("自动保存: 关");
            autoSaveTimer.stop();
        }
        WindowUtil.showSuccessMsg(this, isAutoSaveEnabled ? "已开启自动保存" : "已关闭自动保存");
    }

    /**
     * 保存工作日志
     */
    private void saveWorkLog() {
        try {
            String content = logArea.getText().trim();
            if (content.isEmpty()) {
                WindowUtil.showWarningMsg(this, "日志内容为空，无需保存");
                return;
            }

            saveWorkLogToStorage(content);
        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "保存失败: " + e.getMessage());
        }
    }

    /**
     * 保存工作日志到存储
     * @param content 日志内容
     */
    @SuppressWarnings("unchecked")
    private void saveWorkLogToStorage(String content) {
        try {
            // 读取所有日志
            Map<String, String> allLogs = new HashMap<>();
            try {
                Object data = DataStorageUtil.getData(WORK_LOG_KEY);
                if (data != null) {
                    allLogs = (Map<String, String>) data;
                }
            } catch (Exception e) {
                System.err.println("读取日志数据失败: " + e.getMessage());
            }

            // 保存今日日志
            String today = DateFormatUtil.formatCurrentDate();
            allLogs.put(today, content);

            // 保存到存储
            DataStorageUtil.saveData(WORK_LOG_KEY, allLogs);

        } catch (Exception e) {
            System.err.println("保存工作日志失败: " + e.getMessage());
            // 尝试备份到本地文件
            backupLogToFile(content);
        }
    }

    /**
     * 加载所有工作日志
     * @return 日志映射表
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> loadAllWorkLogs() {
        try {
            Object data = DataStorageUtil.getData(WORK_LOG_KEY);
            return data == null ? new HashMap<>() : (Map<String, String>) data;
        } catch (Exception e) {
            System.err.println("加载工作日志失败: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 备份日志到本地文件
     * @param content 日志内容
     */
    private void backupLogToFile(String content) {
        try {
            // 创建备份目录
            java.io.File backupDir = new java.io.File("log_backup");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            // 生成文件名
            String filename = "worklog_" + currentUser.getUserId() + "_" +
                    DateFormatUtil.formatCurrentDateTime().replaceAll("[-: ]", "_") + ".txt";
            java.io.File file = new java.io.File(backupDir, filename);

            // 写入文件
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                writer.println("工作日志备份");
                writer.println("护工：" + currentUser.getUserId());
                writer.println("时间：" + DateFormatUtil.formatCurrentDateTime());
                writer.println("==========================");
                writer.println(content);
            }

            System.out.println("日志已备份到文件: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("备份日志到文件失败: " + e.getMessage());
        }
    }

    /**
     * 添加健康记录到工作日志
     * @param elderName 老人姓名
     * @param bloodPressure 血压值
     * @param heartRate 心率值
     */
    private void addHealthRecordToLog(String elderName, String bloodPressure, String heartRate) {
        String currentTime = DateFormatUtil.formatDateTime(new Date());
        String timePart = currentTime.substring(11); // 提取时间部分 "HH:mm:ss"
        String logEntry = "[" + timePart + "] " +
                "为老人 " + elderName + " 记录健康数据：血压 " + bloodPressure + "，心率 " + heartRate;
        addToWorkLogSection("【健康记录】", logEntry);
    }

    /**
     * 添加服务处理记录到工作日志
     * @param elderName 老人姓名
     * @param serviceType 服务类型
     * @param newStatus 新状态
     */
    private void addServiceRequestToLog(String elderName, String serviceType, String newStatus) {
        String currentTime = DateFormatUtil.formatDateTime(new Date());
        String timePart = currentTime.substring(11); // 提取时间部分 "HH:mm:ss"
        String logEntry = "[" + timePart + "] " +
                "处理 " + elderName + " 的 " + serviceType + " 申请，状态更新为：" + newStatus;
        addToWorkLogSection("【服务处理】", logEntry);
    }

    /**
     * 通用的添加日志条目方法
     * @param sectionHeader 章节标题
     * @param logEntry 日志条目
     */
    private void addToWorkLogSection(String sectionHeader, String logEntry) {
        if (logArea == null) return;

        String currentText = logArea.getText();
        int sectionIndex = currentText.indexOf(sectionHeader);

        if (sectionIndex != -1) {
            // 找到对应章节，在章节后添加
            int insertPosition = sectionIndex + sectionHeader.length();
            String newText = currentText.substring(0, insertPosition) +
                    "\n• " + logEntry +
                    currentText.substring(insertPosition);
            logArea.setText(newText);
        } else {
            // 如果找不到章节，添加到末尾
            logArea.append("\n" + sectionHeader + "\n• " + logEntry);
        }

        // 自动保存
        if (isAutoSaveEnabled) {
            SwingUtilities.invokeLater(() -> saveWorkLogToStorage(logArea.getText()));
        }
    }

    /**
     * 显示历史日志
     */
    private void showLogHistory() {
        try {
            Map<String, String> allLogs = loadAllWorkLogs();

            if (allLogs.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "暂无历史日志记录",
                        "提示",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            StringBuilder history = new StringBuilder("工作日志历史记录\n");
            history.append("==================\n\n");

            java.util.List<String> dates = new ArrayList<>(allLogs.keySet());
            Collections.sort(dates, Collections.reverseOrder()); // 按日期倒序

            for (String date : dates) {
                history.append("日期：").append(date).append("\n");
                String content = allLogs.get(date);
                String preview = content.length() > 50 ? content.substring(0, 50) + "..." : content;
                history.append("内容：").append(preview.replace("\n", " ")).append("\n");
                history.append("---\n");
            }

            JTextArea textArea = new JTextArea(history.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "历史日志", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            WindowUtil.showErrorMsg(this, "查看历史失败：" + e.getMessage());
        }
    }

    /**
     * 导出当前日志
     */
    private void exportCurrentLog() {
        String content = logArea.getText().trim();
        if (content.isEmpty()) {
            WindowUtil.showWarningMsg(this, "当前没有日志内容可导出");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出工作日志");
        fileChooser.setSelectedFile(new java.io.File(
                "护工工作日志_" + currentUser.getUserId() + "_" +
                        DateFormatUtil.formatCurrentDate() + ".txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                writer.write(content);
                WindowUtil.showSuccessMsg(this, "日志导出成功：" + file.getAbsolutePath());
            } catch (Exception e) {
                WindowUtil.showErrorMsg(this, "导出失败：" + e.getMessage());
            }
        }
    }

    /**
     * 获取所有服务申请
     * @return 服务申请列表
     */
    private List<ServiceRequest> getAllServiceRequests() {
        return getServiceRequestsFromFile();
    }

    /**
     * 从文件获取服务申请
     * @return 服务申请列表
     */
    private List<ServiceRequest> getServiceRequestsFromFile() {
        List<ServiceRequest> requests = new ArrayList<>();
        try {
            Object data = DataStorageUtil.getData("service_requests");
            if (data != null) {
                requests = (List<ServiceRequest>) data;
                System.out.println("从文件读取了 " + requests.size() + " 个服务申请");
            } else {
                System.out.println("服务申请文件为空或不存在");
            }
        } catch (Exception e) {
            System.err.println("从文件读取服务申请失败: " + e.getMessage());
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * 在文件中更新服务申请状态
     * @param requestId 申请ID
     * @param newStatus 新状态
     * @return true-更新成功，false-更新失败
     */
    private boolean updateServiceRequestStatusInFile(String requestId, String newStatus) {
        try {
            // 读取所有服务申请
            List<ServiceRequest> allRequests = getServiceRequestsFromFile();
            System.out.println("从文件读取到 " + allRequests.size() + " 个申请");

            // 查找并更新指定的申请
            boolean found = false;
            for (ServiceRequest request : allRequests) {
                if (request.getRequestId().equals(requestId)) {
                    System.out.println("找到申请: " + requestId +
                            ", 旧状态: " + request.getStatus() +
                            ", 新状态: " + newStatus);
                    request.setStatus(newStatus);
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.err.println("未找到申请: " + requestId);
                return false;
            }

            // 保存回文件
            DataStorageUtil.saveData("service_requests", allRequests);
            System.out.println("申请状态已更新并保存到文件");

            return true;
        } catch (Exception e) {
            System.err.println("文件操作更新状态失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 显示修改密码对话框
     */
    private void showChangePasswordDialog() {
        JPasswordField oldPwdField = new JPasswordField(20);
        JPasswordField newPwdField = new JPasswordField(20);
        JPasswordField confirmPwdField = new JPasswordField(20);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.add(new JLabel("原密码:"));
        panel.add(oldPwdField);
        panel.add(new JLabel("新密码:"));
        panel.add(newPwdField);
        panel.add(new JLabel("确认密码:"));
        panel.add(confirmPwdField);

        // 添加密码要求提示
        JLabel hintLabel = new JLabel("<html><font color='gray'>密码要求：6-20位, 不能与原密码相同</font></html>");
        panel.add(new JLabel(""));
        panel.add(hintLabel);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "修改密码", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String oldPassword = new String(oldPwdField.getPassword()).trim();
            String newPassword = new String(newPwdField.getPassword()).trim();
            String confirmPassword = new String(confirmPwdField.getPassword()).trim();

            // 验证输入
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                WindowUtil.showErrorMsg(this, "所有字段都不能为空！");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                WindowUtil.showErrorMsg(this, "新密码和确认密码不一致！");
                return;
            }

            if (newPassword.length() < 6 || newPassword.length() > 20) {
                WindowUtil.showErrorMsg(this, "密码长度必须在6-20位之间！");
                return;
            }

            // 新密码不能与原密码相同
            if (oldPassword.equals(newPassword)) {
                WindowUtil.showErrorMsg(this, "新密码不能与原密码相同！");
                return;
            }

            try {
                // 调用用户服务验证并修改密码
                boolean success = changePassword(currentUser.getUserId(), oldPassword, newPassword);

                if (success) {
                    WindowUtil.showSuccessMsg(this, "密码修改成功！请重新登录。");

                    // 密码修改成功后，保存日志并退出
                    if (logArea != null) {
                        String content = logArea.getText().trim();
                        if (!content.isEmpty()) {
                            saveWorkLogToStorage(content);
                        }
                    }

                    // 停止定时器
                    if (autoSaveTimer != null) {
                        autoSaveTimer.stop();
                    }

                    // 返回登录界面
                    dispose();
                    new LoginFrame().setVisible(true);
                } else {
                    WindowUtil.showErrorMsg(this, "原密码错误或修改失败！");
                }
            } catch (Exception e) {
                WindowUtil.showErrorMsg(this, "修改密码失败: " + e.getMessage());
            }
        }
    }

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return true-修改成功，false-修改失败
     */
    private boolean changePassword(String userId, String oldPassword, String newPassword) {
        try {
            // 获取 UserService 实例
            UserService userService = UserService.getInstance();

            // 先验证原密码（通过登录方式验证）
            try {
                User user = userService.login(userId, oldPassword);
                if (user == null) {
                    return false;
                }
            } catch (Exception e) {
                System.err.println("原密码验证失败: " + e.getMessage());
                return false;
            }

            // 更新密码
            return updateUserPassword(userService, userId, newPassword);

        } catch (Exception e) {
            System.err.println("修改密码过程出错: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新用户密码
     * @param userService 用户服务实例
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return true-更新成功，false-更新失败
     */
    private boolean updateUserPassword(UserService userService, String userId, String newPassword) {
        try {
            // 读取所有用户
            List<User> allUsers = userService.getAllUsers();

            // 查找当前用户并更新密码
            boolean found = false;
            for (User user : allUsers) {
                if (user.getUserId().equals(userId)) {
                    user.setPassword(newPassword);
                    found = true;
                    break;
                }
            }

            if (!found) {
                WindowUtil.showErrorMsg(this, "用户不存在！");
                return false;
            }

            // 保存用户数据
            saveUsersToFile(allUsers);

            // 记录到工作日志
            addPasswordChangeToLog();

            return true;

        } catch (Exception e) {
            System.err.println("更新用户密码失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 保存用户数据到文件
     * @param users 用户列表
     */
    private void saveUsersToFile(List<User> users) {
        try {
            // 使用 DataStorageUtil 保存用户数据
            DataStorageUtil.saveData("users", users);
            System.out.println("用户密码已更新并保存");
        } catch (Exception e) {
            System.err.println("保存用户数据失败: " + e.getMessage());
            // 尝试备用方法
            saveUsersToBackupFile(users);
        }
    }

    /**
     * 保存用户数据到备份文件
     * @param users 用户列表
     */
    private void saveUsersToBackupFile(List<User> users) {
        try {
            // 创建备份目录
            java.io.File backupDir = new java.io.File("user_backup");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            // 生成文件名
            String filename = "users_backup_" + DateFormatUtil.formatCurrentDate() + ".txt";
            java.io.File file = new java.io.File(backupDir, filename);

            // 写入文件（注意：实际项目中应该加密存储）
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                writer.println("用户数据备份 - " + DateFormatUtil.formatCurrentDateTime());
                writer.println("==========================");
                for (User user : users) {
                    writer.println("用户ID: " + user.getUserId() + ", 角色: " + user.getRole());
                    // 注意：实际项目中不应该明文存储密码
                    writer.println("密码: [已加密]");
                    writer.println("---");
                }
            }

            System.out.println("用户数据已备份到文件: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("备份用户数据失败: " + e.getMessage());
        }
    }

    /**
     * 添加密码修改记录到工作日志
     */
    private void addPasswordChangeToLog() {
        if (logArea != null) {
            String timestamp = DateFormatUtil.formatCurrentDateTime();
            String logEntry = "[" + timestamp + "] 修改了登录密码";

            // 查找特殊事项章节
            String sectionHeader = "【特殊事项】";
            String currentText = logArea.getText();
            int sectionIndex = currentText.indexOf(sectionHeader);

            if (sectionIndex != -1) {
                // 在章节后添加
                int insertPosition = sectionIndex + sectionHeader.length();
                String newText = currentText.substring(0, insertPosition) +
                        "\n• " + logEntry +
                        currentText.substring(insertPosition);
                logArea.setText(newText);
            } else {
                // 如果找不到章节，添加到末尾
                logArea.append("\n" + sectionHeader + "\n• " + logEntry);
            }

            // 自动保存日志
            if (isAutoSaveEnabled) {
                saveWorkLogToStorage(logArea.getText());
            }
        }
    }

    /**
     * 显示新建任务对话框
     */
    private void showNewTaskDialog() {
        JOptionPane.showMessageDialog(this, "新建任务功能正在开发中...");
    }

    /**
     * 刷新所有数据
     */
    private void refreshAllData() {
        loadCaredElders();
        loadElderComboBox();
        loadServiceRequests();
        WindowUtil.showSuccessMsg(this, "数据刷新完成！");
    }

    /**
     * 自定义按钮编辑器
     */
    abstract class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox, String buttonText) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // 获取当前行索引
                JTable table = null;
                Component parent = button.getParent();
                while (parent != null && !(parent instanceof JTable)) {
                    parent = parent.getParent();
                }
                if (parent instanceof JTable) {
                    table = (JTable) parent;
                }

                int row = -1;
                if (table != null) {
                    row = table.getEditingRow();
                }
                if (row >= 0) {
                    buttonClicked(row);
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }

        public abstract void buttonClicked(int row);
    }
}