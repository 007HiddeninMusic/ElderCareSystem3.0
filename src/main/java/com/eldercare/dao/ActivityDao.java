package com.eldercare.dao;

import com.eldercare.model.Activity;

import java.util.List;

/**
 * 活动信息DAO接口：定义活动的数据库操作规范
 */
public interface ActivityDao {
    /**
     * 向数据库插入一条活动信息
     * @param activity 活动实体（包含活动名称、时间、地点等）
     */
    void insertActivity(Activity activity);

    /**
     * 查询数据库中所有活动信息
     * @return 活动列表（无数据时返回空列表）
     */
    List<Activity> selectAllActivities();

    /**
     * 根据活动ID查询活动详情
     * @param activityId 活动唯一ID
     * @return 匹配的活动实体；无匹配时返回null
     */
    Activity selectActivityById(String activityId);

    /**
     * 更新活动的报名列表（添加老人ID到已报名列表）
     * @param activityId 活动唯一ID
     * @param elderId 报名的老人ID
     * @return 影响的行数（1：更新成功；0：无此活动；-1：更新失败）
     */
    int updateActivityRegistration(String activityId, String elderId);

    /**
     * 更新活动的报名列表（从已报名列表移除老人ID）
     * @param activityId 活动唯一ID
     * @param elderId 取消报名的老人ID
     * @return 影响的行数（1：更新成功；0：无此活动或未报名；-1：更新失败）
     */
    int updateActivityCancelRegistration(String activityId, String elderId);
}