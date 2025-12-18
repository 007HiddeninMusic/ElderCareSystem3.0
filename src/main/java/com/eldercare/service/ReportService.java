// src/main/java/com/eldercare/service/ReportService.java
package com.eldercare.service;

import com.eldercare.model.HealthRecord;
import com.eldercare.model.ServiceRequest;
import com.eldercare.util.DateFormatUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ReportService {
    private static final ReportService INSTANCE = new ReportService();

    private ReportService() {}

    public static ReportService getInstance() {
        return INSTANCE;
    }

    /**
     * 导出健康记录为Excel
     */
    public void exportHealthRecordsToExcel(List<HealthRecord> records, String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("健康记录");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"记录时间", "血压", "心率", "备注"};

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        for (int i = 0; i < records.size(); i++) {
            HealthRecord record = records.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(DateFormatUtil.formatDateTime(record.getRecordTime()));
            row.createCell(1).setCellValue(record.getBloodPressure());
            row.createCell(2).setCellValue(record.getHeartRate());

            // 添加健康评估备注
            String remark = getHealthRemark(record.getBloodPressure(), record.getHeartRate());
            row.createCell(3).setCellValue(remark);

            // 如果异常，设置背景色
            if (remark.contains("异常")) {
                CellStyle warningStyle = workbook.createCellStyle();
                warningStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
                warningStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                row.getCell(3).setCellStyle(warningStyle);
            }
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 写入文件
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }

        workbook.close();
    }

    /**
     * 导出服务申请为Excel
     */
    public void exportServiceRequestsToExcel(List<ServiceRequest> requests, String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("服务申请");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"申请时间", "服务类型", "状态", "申请内容", "处理进展"};

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        for (int i = 0; i < requests.size(); i++) {
            ServiceRequest request = requests.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(DateFormatUtil.formatDateTime(request.getRequestTime()));
            row.createCell(1).setCellValue(request.getServiceType());
            row.createCell(2).setCellValue(request.getStatus());

            // 截断过长的内容
            String content = request.getContent();
            if (content.length() > 100) {
                content = content.substring(0, 100) + "...";
            }
            row.createCell(3).setCellValue(content);

            // 根据状态设置处理进展
            String progress = getRequestProgress(request.getStatus());
            row.createCell(4).setCellValue(progress);

            // 根据状态设置颜色
            CellStyle statusStyle = workbook.createCellStyle();
            if ("已完成".equals(request.getStatus())) {
                statusStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            } else if ("处理中".equals(request.getStatus())) {
                statusStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            } else if ("已取消".equals(request.getStatus())) {
                statusStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            }
            statusStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            row.getCell(2).setCellStyle(statusStyle);
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 写入文件
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }

        workbook.close();
    }

    private String getHealthRemark(String bloodPressure, int heartRate) {
        try {
            String[] bpParts = bloodPressure.split("/");
            int systolic = Integer.parseInt(bpParts[0]);
            int diastolic = Integer.parseInt(bpParts[1].split(" ")[0]);

            StringBuilder remark = new StringBuilder();

            // 血压评估
            if (systolic < 90 || systolic > 140) {
                remark.append("收缩压").append(systolic < 90 ? "偏低" : "偏高").append(" ");
            }
            if (diastolic < 60 || diastolic > 90) {
                remark.append("舒张压").append(diastolic < 60 ? "偏低" : "偏高").append(" ");
            }

            // 心率评估
            if (heartRate < 60 || heartRate > 100) {
                remark.append("心率").append(heartRate < 60 ? "过缓" : "过速");
            }

            return remark.length() > 0 ? "⚠ " + remark.toString() : "正常";
        } catch (Exception e) {
            return "数据格式异常";
        }
    }

    private String getRequestProgress(String status) {
        switch (status) {
            case "待处理": return "等待分配护工";
            case "处理中": return "已分配护工处理";
            case "已完成": return "服务已完成";
            case "已取消": return "申请已取消";
            default: return "未知状态";
        }
    }
}