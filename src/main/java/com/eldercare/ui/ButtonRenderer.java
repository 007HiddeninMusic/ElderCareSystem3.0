package com.eldercare.ui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * 按钮渲染器
 * 功能：用于表格中的按钮列，统一按钮显示样式
 */
public class ButtonRenderer extends JButton implements TableCellRenderer {
    private static final String BUTTON_TEXT = "编辑";

    public ButtonRenderer() {
        super(BUTTON_TEXT);
        setOpaque(true);
        setHorizontalAlignment(SwingConstants.CENTER);
        // 强制设置默认字体颜色（避免继承选中样式）
        setForeground(UIManager.getColor("Button.foreground"));
        setBackground(UIManager.getColor("Button.background"));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        setText(BUTTON_TEXT);

        // 关键修复：无论是否选中，都使用默认样式（仅保留背景高亮，文字始终黑色）
        if (isSelected) {
            // 选中时仅背景高亮，文字仍用黑色
            setBackground(table.getSelectionBackground());
            setForeground(Color.BLACK); // 强制黑色文字
        } else {
            setBackground(UIManager.getColor("Button.background"));
            setForeground(UIManager.getColor("Button.foreground"));
        }

        // 清除焦点样式（焦点导致的文字变色）
        setFocusPainted(false);
        setBorderPainted(true); // 保留边框，避免按钮消失
        return this;
    }
}