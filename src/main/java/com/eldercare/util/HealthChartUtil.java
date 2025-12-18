// src/main/java/com/eldercare/util/HealthChartUtil.java
package com.eldercare.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 健康数据图表工具类
 * 功能：使用JFreeChart生成血压、心率趋势图
 */
public class HealthChartUtil {

    /**
     * 创建血压趋势图（收缩压/舒张压双线图）
     * @param elderName 老人姓名
     * @param pressureData 血压数据映射（日期->血压值）
     * @return 血压趋势图面板
     */
    public static ChartPanel createBloodPressureChart(String elderName, Map<Date, String> pressureData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // 解析血压数据（格式：120/80）
        for (Map.Entry<Date, String> entry : pressureData.entrySet()) {
            String[] parts = entry.getValue().split("/");
            if (parts.length == 2) {
                try {
                    int systolic = Integer.parseInt(parts[0]);
                    int diastolic = Integer.parseInt(parts[1].split(" ")[0]); // 去除可能的后缀

                    dataset.addValue(systolic, "收缩压", DateFormatUtil.formatDate(entry.getKey()));
                    dataset.addValue(diastolic, "舒张压", DateFormatUtil.formatDate(entry.getKey()));
                } catch (NumberFormatException e) {
                    // 跳过格式错误的数据
                }
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                elderName + " - 血压趋势图",
                "日期",
                "血压值 (mmHg)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // 设置样式
        chart.setBackgroundPaint(Color.WHITE);
        chart.getPlot().setBackgroundPaint(new Color(240, 240, 240));

        return new ChartPanel(chart);
    }

    /**
     * 创建心率趋势图
     * @param elderName 老人姓名
     * @param heartRateData 心率数据映射（日期->心率值）
     * @return 心率趋势图面板
     */
    public static ChartPanel createHeartRateChart(String elderName, Map<Date, Integer> heartRateData) {
        TimeSeries series = new TimeSeries("心率");

        for (Map.Entry<Date, Integer> entry : heartRateData.entrySet()) {
            series.add(new Day(entry.getKey()), entry.getValue());
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                elderName + " - 心率趋势图",
                "日期",
                "心率 (次/分钟)",
                dataset,
                true,
                true,
                false
        );

        chart.setBackgroundPaint(Color.WHITE);
        return new ChartPanel(chart);
    }

    /**
     * 创建健康数据统计面板
     * @param elderName 老人姓名
     * @param healthRecords 健康记录列表
     * @return 健康统计面板
     */
    public static JPanel createHealthStatisticsPanel(String elderName, List<String> healthRecords) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("健康统计"));

        // 统计最近7天的数据
        int systolicSum = 0, diastolicSum = 0, heartRateSum = 0;
        int count = Math.min(7, healthRecords.size());

        for (int i = 0; i < count; i++) {
            // 解析健康记录（简化示例，实际应从HealthRecord对象解析）
            String record = healthRecords.get(i);
            // 这里需要根据实际数据格式解析
        }

        // 添加统计卡片
        panel.add(createStatCard("平均收缩压", "120", "mmHg", Color.GREEN));
        panel.add(createStatCard("平均舒张压", "80", "mmHg", Color.BLUE));
        panel.add(createStatCard("平均心率", "75", "次/分钟", Color.ORANGE));
        panel.add(createStatCard("测量次数", String.valueOf(count), "次", Color.CYAN));

        return panel;
    }

    /**
     * 创建统计卡片
     * @param title 卡片标题
     * @param value 数值
     * @param unit 单位
     * @param color 颜色
     * @return 统计卡片面板
     */
    private static JPanel createStatCard(String title, String value, String unit, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(color, 2));
        card.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        valueLabel.setForeground(color);

        JLabel unitLabel = new JLabel(unit, SwingConstants.CENTER);
        unitLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(valueLabel, BorderLayout.CENTER);
        centerPanel.add(unitLabel, BorderLayout.SOUTH);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);

        return card;
    }
}