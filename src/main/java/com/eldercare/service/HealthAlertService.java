// src/main/java/com/eldercare/service/HealthAlertService.java
package com.eldercare.service;

import com.eldercare.model.HealthRecord;
import com.eldercare.util.DataStorageUtil;

import java.io.IOException;
import java.util.*;

public class HealthAlertService {
    private static final HealthAlertService INSTANCE = new HealthAlertService();
    private static final String ALERT_DATA_KEY = "health_alerts";

    private HealthAlertService() {}

    public static HealthAlertService getInstance() {
        return INSTANCE;
    }

    /**
     * 检查健康记录是否异常
     */
    public List<String> checkHealthAlert(HealthRecord record) {
        List<String> alerts = new ArrayList<>();

        try {
            String[] bpParts = record.getBloodPressure().split("/");
            int systolic = Integer.parseInt(bpParts[0]);
            int diastolic = Integer.parseInt(bpParts[1].split(" ")[0]);
            int heartRate = record.getHeartRate();

            // 血压异常预警
            if (systolic > 140) {
                alerts.add("⚠ 高血压预警：收缩压" + systolic + "mmHg（正常<140）");
            } else if (systolic < 90) {
                alerts.add("⚠ 低血压预警：收缩压" + systolic + "mmHg（正常>90）");
            }

            if (diastolic > 90) {
                alerts.add("⚠ 高血压预警：舒张压" + diastolic + "mmHg（正常<90）");
            } else if (diastolic < 60) {
                alerts.add("⚠ 低血压预警：舒张压" + diastolic + "mmHg（正常>60）");
            }

            // 心率异常预警
            if (heartRate > 100) {
                alerts.add("⚠ 心率过速预警：" + heartRate + "次/分钟（正常<100）");
            } else if (heartRate < 60) {
                alerts.add("⚠ 心率过缓预警：" + heartRate + "次/分钟（正常>60）");
            }

            // 如果发现异常，保存预警记录
            if (!alerts.isEmpty()) {
                saveAlertRecord(record.getElderId(), alerts, record.getRecordTime());
            }

        } catch (Exception e) {
            // 数据格式异常
            alerts.add("⚠ 健康数据格式异常，请检查录入是否正确");
        }

        return alerts;
    }

    /**
     * 获取指定老人的健康预警记录
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAlertsByElderId(String elderId) throws IOException, ClassNotFoundException {
        Object data = DataStorageUtil.getData(ALERT_DATA_KEY);
        if (data == null) return new ArrayList<>();

        List<Map<String, Object>> allAlerts = (List<Map<String, Object>>) data;
        List<Map<String, Object>> elderAlerts = new ArrayList<>();

        for (Map<String, Object> alert : allAlerts) {
            if (elderId.equals(alert.get("elderId"))) {
                elderAlerts.add(alert);
            }
        }

        // 按时间倒序排序
        elderAlerts.sort((a1, a2) ->
                ((Date) a2.get("alertTime")).compareTo((Date) a1.get("alertTime()")));

        return elderAlerts;
    }

    /**
     * 保存预警记录
     */
    @SuppressWarnings("unchecked")
    private void saveAlertRecord(String elderId, List<String> alerts, Date recordTime) throws IOException, ClassNotFoundException {
        Object data = DataStorageUtil.getData(ALERT_DATA_KEY);
        List<Map<String, Object>> alertList = data == null ? new ArrayList<>() : (List<Map<String, Object>>) data;

        Map<String, Object> alertRecord = new HashMap<>();
        alertRecord.put("alertId", "ALERT_" + System.currentTimeMillis());
        alertRecord.put("elderId", elderId);
        alertRecord.put("alerts", new ArrayList<>(alerts));
        alertRecord.put("alertTime", new Date());
        alertRecord.put("recordTime", recordTime);
        alertRecord.put("status", "未处理");

        alertList.add(alertRecord);
        DataStorageUtil.saveData(ALERT_DATA_KEY, alertList);
    }
}