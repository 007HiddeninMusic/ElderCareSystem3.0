package com.eldercare.dao.impl;

import com.eldercare.dao.HealthRecordDao;
import com.eldercare.model.HealthRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * 健康记录DAO空实现：标记数据库操作位置
 */
public class HealthRecordDaoImpl implements HealthRecordDao {

    @Override
    public void insertHealthRecord(HealthRecord record) {
        // 预留：插入健康记录的操作位置
        System.out.println("[DAO空实现] 准备插入健康记录（老人ID：" + record.getElderId() + "，心率：" + record.getHeartRate() + "）");

        // 真实数据库操作模板（核心SQL）：
        // String sql = "INSERT INTO health_record (record_id, elder_id, blood_pressure, heart_rate, record_time) " +
        //              "VALUES (?, ?, ?, ?, ?)";
        // 后续步骤：设置参数、执行SQL、关闭资源（参考ElderDaoImpl）
    }

    @Override
    public List<HealthRecord> selectRecordsByElderId(String elderId) {
        // 预留：根据老人ID查询健康记录的操作位置
        System.out.println("[DAO空实现] 准备查询老人（ID：" + elderId + "）的所有健康记录");

        // 真实数据库操作模板：执行SELECT SQL，按record_time DESC排序，封装为List<HealthRecord>
        return new ArrayList<>();
    }

    @Override
    public int deleteRecordById(String recordId) {
        // 预留：删除单条健康记录的操作位置
        System.out.println("[DAO空实现] 准备删除健康记录（ID：" + recordId + "）");
        return 0; // 空实现返回0
    }

    @Override
    public int deleteRecordsByElderId(String elderId) {
        // 预留：批量删除老人健康记录的操作位置
        System.out.println("[DAO空实现] 准备批量删除老人（ID：" + elderId + "）的所有健康记录");

        // 真实数据库操作模板：执行DELETE FROM health_record WHERE elder_id = ?
        return 0; // 空实现返回0
    }
}