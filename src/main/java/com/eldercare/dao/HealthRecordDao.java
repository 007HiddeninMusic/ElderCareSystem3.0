package com.eldercare.dao;

import com.eldercare.model.HealthRecord;

import java.util.List;

/**
 * 健康记录DAO接口：定义健康记录的数据库操作规范
 */
public interface HealthRecordDao {
    /**
     * 向数据库插入一条健康记录
     * @param record 健康记录实体（包含关联老人ID、血压、心率等信息）
     */
    void insertHealthRecord(HealthRecord record);

    /**
     * 根据老人ID查询其所有健康记录（按记录时间倒序）
     * @param elderId 关联的老人唯一ID
     * @return 健康记录列表（无数据时返回空列表）
     */
    List<HealthRecord> selectRecordsByElderId(String elderId);

    /**
     * 根据记录ID删除数据库中的健康记录
     * @param recordId 健康记录唯一ID
     * @return 影响的行数（1：删除成功；0：无此记录；-1：删除失败）
     */
    int deleteRecordById(String recordId);

    /**
     * 根据老人ID批量删除健康记录（用于老人删除时的关联数据清理）
     * @param elderId 老人唯一ID
     * @return 影响的行数（删除的记录总数；-1：删除失败）
     */
    int deleteRecordsByElderId(String elderId);
}