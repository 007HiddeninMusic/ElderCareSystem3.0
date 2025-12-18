package com.eldercare.dao;

import com.eldercare.model.Elder;

import java.util.List;

/**
 * 老人信息DAO接口：定义老人信息的数据库操作规范
 * 后续对接数据库时，需实现此接口完成真实SQL操作
 */
public interface ElderDao {
    /**
     * 向数据库插入一条老人信息
     * @param elder 老人实体对象（包含老人ID、姓名、年龄等完整信息）
     */
    void insertElder(Elder elder);

    /**
     * 根据老人ID从数据库查询老人信息
     * @param elderId 老人唯一ID（如ELDER_20251201_001）
     * @return 匹配的老人实体对象；无匹配时返回null
     */
    Elder selectElderById(String elderId);

    /**
     * 查询数据库中所有老人信息
     * @return 老人列表（无数据时返回空列表，非null）
     */
    List<Elder> selectAllElders();

    /**
     * 根据老人ID从数据库删除老人信息
     * @param elderId 老人唯一ID
     * @return 影响的行数（1：删除成功；0：无此老人；-1：删除失败）
     */
    int deleteElderById(String elderId);

    /**
     * 更新数据库中的老人信息（全字段更新，需传入完整老人对象）
     * @param elder 包含最新信息的老人实体（elderId不可修改）
     * @return 影响的行数（1：更新成功；0：无此老人；-1：更新失败）
     */
    int updateElder(Elder elder);
}