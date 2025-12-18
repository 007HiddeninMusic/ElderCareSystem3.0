package com.eldercare.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 健康记录模型类：对应老人的血压、心率等生理数据记录
 */
public class HealthRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    // 核心字段：记录唯一ID、关联老人ID、血压、心率、记录时间
    private String recordId;       // 唯一标识（如HEALTH_20251201_001）
    private String elderId;        // 关联的老人ID（与Elder.elderId对应）
    private String bloodPressure;  // 血压（如“120/80 mmHg”）
    private int heartRate;         // 心率（正常范围：60-100次/分钟）
    private Date recordTime;       // 记录时间（默认当前时间）

    // 1. 无参构造（默认记录时间为当前时间）
    public HealthRecord() {
        this.recordTime = new Date();
    }

    // 2. 全参构造
    public HealthRecord(String recordId, String elderId, String bloodPressure, int heartRate, Date recordTime) {
        this.recordId = recordId;
        this.setElderId(elderId);
        this.setBloodPressure(bloodPressure);
        this.setHeartRate(heartRate);
        // 记录时间允许指定，为空则默认当前时间
        this.recordTime = (recordTime == null) ? new Date() : recordTime;
    }

    // 3. Getter/Setter方法
    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        if (recordId != null && !recordId.trim().isEmpty()) {
            this.recordId = recordId.trim();
        } else {
            throw new IllegalArgumentException("健康记录ID不能为空");
        }
    }

    public String getElderId() {
        return elderId;
    }

    public void setElderId(String elderId) {
        // 必须关联存在的老人ID（此处仅做非空校验，实际业务需关联查询）
        if (elderId != null && !elderId.trim().isEmpty()) {
            this.elderId = elderId.trim();
        } else {
            throw new IllegalArgumentException("必须关联老人ID");
        }
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        // 血压格式简化校验（如“120/80”“130/90 mmHg”）
        String regex = "^\\d{2,3}/\\d{2,3}( mmHg)?$";
        if (bloodPressure != null && bloodPressure.matches(regex)) {
            this.bloodPressure = bloodPressure.trim();
        } else {
            throw new IllegalArgumentException("血压格式错误（示例：120/80 或 120/80 mmHg）");
        }
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        // 心率范围校验（正常60-100，允许特殊情况如运动员50+）
        if (heartRate >= 50 && heartRate <= 150) {
            this.heartRate = heartRate;
        } else {
            throw new IllegalArgumentException("心率必须在50-150次/分钟之间");
        }
    }

    public Date getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(Date recordTime) {
        this.recordTime = (recordTime == null) ? new Date() : recordTime;
    }

    // 4. toString方法（调试用）
    @Override
    public String toString() {
        return "HealthRecord{" +
                "recordId='" + recordId + '\'' +
                ", elderId='" + elderId + '\'' +
                ", bloodPressure='" + bloodPressure + '\'' +
                ", heartRate=" + heartRate +
                ", recordTime=" + recordTime +
                '}';
    }
}