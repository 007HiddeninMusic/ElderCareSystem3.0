package com.eldercare.service;

import com.eldercare.dao.HealthRecordDao;
import com.eldercare.dao.impl.HealthRecordDaoImpl;
import com.eldercare.model.Elder;
import com.eldercare.model.HealthRecord;
import com.eldercare.util.DataStorageUtil;
import com.eldercare.util.IdGenerator;
import com.eldercare.util.InputValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 健康记录业务逻辑类：处理健康数据添加、查询、关联老人
 */
public class HealthRecordService {
    private static volatile HealthRecordService INSTANCE;
    private static final String HEALTH_DATA_KEY = "health_records";
    private ElderService elderService;
    private final HealthRecordDao healthRecordDao = new HealthRecordDaoImpl();

    protected HealthRecordService() {
        // 延迟初始化
        this.elderService = ElderService.getInstance();
    }

    public static HealthRecordService getInstance() {
        if (INSTANCE == null) {
            synchronized (HealthRecordService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HealthRecordService();
                }
            }
        }
        return INSTANCE;
    }

    // 安全获取 elderService 的方法
    private ElderService getElderService() {
        if (elderService == null) {
            elderService = ElderService.getInstance();
        }
        return elderService;
    }

    /**
     * 添加健康记录（仅允许为已存在的老人添加）
     */
    public void addHealthRecord(HealthRecord record) throws IOException, ClassNotFoundException {
        // 1. 输入校验
        InputValidator.validateNotEmpty(record.getElderId(), "关联老人ID");
        InputValidator.validateBloodPressure(record.getBloodPressure());
        InputValidator.validateHeartRate(record.getHeartRate());

        // 2. 校验老人是否存在
        Elder existElder = getElderService().getElderById(record.getElderId());
        if (existElder == null) {
            throw new IllegalArgumentException("关联老人不存在（ID：" + record.getElderId() + "），无法添加健康记录");
        }

        // 3. 补全记录信息
        record.setRecordId(IdGenerator.generateHealthRecordId());
        if (record.getRecordTime() == null) {
            record.setRecordTime(new Date());
        }

        // 4. 保存记录
        List<HealthRecord> recordList = getRecordList();
        recordList.add(record);
        DataStorageUtil.saveData(HEALTH_DATA_KEY, recordList);
        System.out.println("[HealthRecordService] 健康记录添加成功：老人" + existElder.getName() + "（心率：" + record.getHeartRate() + "）");

        // 5. 预留数据库操作
        healthRecordDao.insertHealthRecord(record);
    }

    /**
     * 查询指定老人的所有健康记录（按时间倒序，最新记录在前）
     * @param elderId 老人ID
     * @return 健康记录列表（无数据返回空列表）
     */
    public List<HealthRecord> getRecordsByElderId(String elderId) throws IOException, ClassNotFoundException {
        InputValidator.validateNotEmpty(elderId, "老人ID");
        List<HealthRecord> recordList = getRecordList();
        // 筛选指定老人的记录，并按时间倒序排序
        List<HealthRecord> elderRecords = recordList.stream()
                .filter(record -> record.getElderId().equals(elderId.trim()))
                .sorted((r1, r2) -> r2.getRecordTime().compareTo(r1.getRecordTime())) // 时间倒序
                .collect(Collectors.toList());
        System.out.println("[HealthRecordService] 查询到老人" + elderId + "的健康记录：" + elderRecords.size() + "条");
        return elderRecords;
    }

    /**
     * 内部方法：根据老人ID删除关联健康记录（供ElderService调用，实现数据联动）
     */
    void deleteRecordsByElderId(String elderId) throws IOException, ClassNotFoundException {
        List<HealthRecord> recordList = getRecordList();
        // 筛选出非当前老人的记录（即删除当前老人的记录）
        List<HealthRecord> remainingRecords = recordList.stream()
                .filter(record -> !record.getElderId().equals(elderId.trim()))
                .collect(Collectors.toList());
        // 保存剩余记录
        DataStorageUtil.saveData(HEALTH_DATA_KEY, remainingRecords);
        int deleteCount = recordList.size() - remainingRecords.size();
        System.out.println("[HealthRecordService] 同步删除老人" + elderId + "的健康记录：" + deleteCount + "条");
    }

    /**
     * 私有辅助方法：统一读取健康记录列表
     */
    @SuppressWarnings("unchecked")
    private List<HealthRecord> getRecordList() throws IOException, ClassNotFoundException {
        Object data = DataStorageUtil.getData(HEALTH_DATA_KEY);
        return data == null ? new ArrayList<>() : (List<HealthRecord>) data;
    }
}