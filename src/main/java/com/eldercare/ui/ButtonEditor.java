package com.eldercare.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 按钮编辑器
 * 功能：用于表格中的按钮列，处理按钮点击事件
 */
public abstract class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private static final String BUTTON_TEXT = "编辑";
    private boolean isPushed;

    public ButtonEditor(JCheckBox checkBox) {
        super(checkBox);
        button = new JButton(BUTTON_TEXT);
        button.setOpaque(true);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setFocusPainted(false); // 禁用焦点绘制，避免文字变白
        button.setForeground(Color.BLACK); // 强制默认黑色文字

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
                // 点击后立即重置按钮样式
                button.setBackground(UIManager.getColor("Button.background"));
                button.setForeground(Color.BLACK);
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        button.setText(BUTTON_TEXT);

        // 编辑时仅背景高亮，文字仍为黑色
        if (isSelected) {
            button.setBackground(table.getSelectionBackground());
            button.setForeground(Color.BLACK);
        } else {
            button.setBackground(table.getBackground());
            button.setForeground(Color.BLACK);
        }

        isPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        isPushed = false;
        return BUTTON_TEXT;
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        // 停止编辑时强制取消选中样式
        button.setBackground(UIManager.getColor("Button.background"));
        button.setForeground(Color.BLACK);
        return super.stopCellEditing();
    }
}