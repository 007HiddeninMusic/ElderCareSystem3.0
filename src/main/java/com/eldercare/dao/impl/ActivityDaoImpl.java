package com.eldercare.dao.impl;

import com.eldercare.dao.ActivityDao;
import com.eldercare.model.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动DAO空实现：标记数据库操作位置
 */
public class ActivityDaoImpl implements ActivityDao {

    @Override
    public void insertActivity(Activity activity) {
        // 预留：插入活动的操作位置
        System.out.println("[DAO空实现] 准备插入活动（名称：" + activity.getName() + "，时间：" + activity.getTime() + "）");

        // 真实数据库操作模板（核心SQL）：
        // String sql = "INSERT INTO activity (activity_id, name, time, location, description, registered_elder_ids) " +
        //              "VALUES (?, ?, ?, ?, ?, ?)";
        // 注：registered_elder_ids可存储为JSON字符串或关联中间表（视数据库设计而定）
    }

    @Override
    public List<Activity> selectAllActivities() {
        // 预留：查询所有活动的操作位置
        System.out.println("[DAO空实现] 准备查询数据库中所有活动");
        return new ArrayList<>();
    }

    @Override
    public Activity selectActivityById(String activityId) {
        // 预留：根据ID查询活动的操作位置
        System.out.println("[DAO空实现] 准备查询活动（ID：" + activityId + "）");
        return null;
    }

    @Override
    public int updateActivityRegistration(String activityId, String elderId) {
        // 预留：添加活动报名的操作位置
        System.out.println("[DAO空实现] 准备为活动（ID：" + activityId + "）添加报名老人（ID：" + elderId + "）");

        // 真实数据库操作模板：更新registered_elder_ids字段（如JSON追加老人ID）
        return 0;
    }

    @Override
    public int updateActivityCancelRegistration(String activityId, String elderId) {
        // 预留：取消活动报名的操作位置
        System.out.println("[DAO空实现] 准备为活动（ID：" + activityId + "）取消老人（ID：" + elderId + "）报名");
        return 0;
    }
}