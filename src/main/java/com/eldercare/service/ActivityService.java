package com.eldercare.service;

import com.eldercare.dao.ActivityDao;
import com.eldercare.dao.impl.ActivityDaoImpl;
import com.eldercare.model.Activity;
import com.eldercare.model.Elder;
import com.eldercare.util.DataStorageUtil;
import com.eldercare.util.IdGenerator;
import com.eldercare.util.InputValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 活动业务逻辑类：处理活动创建、报名管理、默认数据初始化
 */
public class ActivityService {
    private static final ActivityService INSTANCE = new ActivityService();
    private static final String ACTIVITY_DATA_KEY = "activities"; // 本地存储key
    private final ElderService elderService = ElderService.getInstance();
    // 预留数据库接口
    private final ActivityDao activityDao = new ActivityDaoImpl();

    private ActivityService() {}
    public static ActivityService getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化默认活动（首次运行时调用，如健康讲座、手工班）
     */
    public void initDefaultActivities() throws IOException, ClassNotFoundException {
        List<Activity> activityList = getActivityList();
        if (!activityList.isEmpty()) {
            System.out.println("[ActivityService] 已有活动数据，无需初始化默认活动");
            return;
        }

        // 1. 创建默认活动1：健康讲座
        Activity lecture = new Activity();
        lecture.setActivityId(IdGenerator.generateActivityId());
        lecture.setName("健康讲座 - 高血压管理");
        lecture.setTime("2025-12-15 09:30-11:00");
        lecture.setLocation("养老院1楼多功能厅");
        lecture.setDescription("邀请三甲医院心内科医生讲解高血压日常管理、饮食建议、用药注意事项，现场提供血压测量服务");

        // 2. 创建默认活动2：手工兴趣班
        Activity craft = new Activity();
        craft.setActivityId(IdGenerator.generateActivityId());
        craft.setName("手工兴趣班 - 剪纸艺术");
        craft.setTime("2025-12-20 14:00-16:00");
        craft.setLocation("养老院2楼活动室");
        craft.setDescription("专业手工老师指导，学习基础剪纸技巧，成品可带回家，材料由养老院提供");

        // 3. 保存默认活动
        activityList.add(lecture);
        activityList.add(craft);
        DataStorageUtil.saveData(ACTIVITY_DATA_KEY, activityList);
        System.out.println("[ActivityService] 默认活动初始化完成，共" + activityList.size() + "个活动");

        // 4. 预留数据库操作：插入默认活动到数据库
        activityDao.insertActivity(lecture);
        activityDao.insertActivity(craft);
    }

    /**
     * 创建新活动（管理员操作）
     * @param activity 活动对象（需包含名称、时间、地点）
     */
    public void createActivity(Activity activity) throws IOException, ClassNotFoundException {
        // 1. 输入校验
        InputValidator.validateNotEmpty(activity.getName(), "活动名称");
        InputValidator.validateActivityTime(activity.getTime());
        InputValidator.validateNotEmpty(activity.getLocation(), "活动地点");

        // 2. 补全活动信息
        activity.setActivityId(IdGenerator.generateActivityId());
        if (activity.getRegisteredElderIds() == null) {
            activity.setRegisteredElderIds(new ArrayList<>()); // 初始化报名列表
        }

        // 3. 保存活动
        List<Activity> activityList = getActivityList();
        activityList.add(activity);
        DataStorageUtil.saveData(ACTIVITY_DATA_KEY, activityList);
        System.out.println("[ActivityService] 新活动创建成功：" + activity.getName() + "（时间：" + activity.getTime() + "）");

        // 4. 预留数据库操作：插入新活动到数据库
        activityDao.insertActivity(activity);
    }

    /**
     * 老人报名活动（确保不重复报名）
     * @param activityId 活动ID
     * @param elderId 老人ID
     * @return true：报名成功；false：活动不存在/老人不存在/已报名
     */
    // 在 ActivityService.java 中
    public boolean registerActivity(String activityId, String elderId) throws IOException, ClassNotFoundException {
        InputValidator.validateNotEmpty(activityId, "活动ID");
        InputValidator.validateNotEmpty(elderId, "老人ID");

        // 1. 校验活动是否存在
        Activity targetActivity = getActivityById(activityId);
        if (targetActivity == null) {
            return false;
        }

        // 2. 校验是否已报名
        if (targetActivity.getRegisteredElderIds().contains(elderId.trim())) {
            System.out.println("[ActivityService] 老人" + elderId + "已报名活动" + activityId + "，无需重复操作");
            return false;
        }

        // 3. 更新活动列表并保存到文件
        List<Activity> activityList = getActivityList();
        
        // 找到对应的活动并更新
        for (Activity activity : activityList) {
            if (activity.getActivityId().equals(activityId)) {
                activity.addRegistration(elderId.trim());
                break;
            }
        }
        
        // 5. 保存更新后的活动列表
        DataStorageUtil.saveData(ACTIVITY_DATA_KEY, activityList);
        
        System.out.println("[ActivityService] 老人" + elderId + "报名活动成功：" + targetActivity.getName());
        
        // 6. 预留数据库操作
        activityDao.updateActivityRegistration(activityId, elderId);
        
        return true;
    }

    /**
     * 老人取消报名活动
     * @param activityId 活动ID
     * @param elderId 老人ID
     * @return true：取消成功；false：活动不存在/老人未报名
     */
    public boolean cancelRegistration(String activityId, String elderId) throws IOException, ClassNotFoundException {
        InputValidator.validateNotEmpty(activityId, "活动ID");
        InputValidator.validateNotEmpty(elderId, "老人ID");

        // 1. 校验活动是否存在
        Activity targetActivity = getActivityById(activityId);
        if (targetActivity == null) {
            return false;
        }

        // 2. 校验是否已报名
        if (!targetActivity.getRegisteredElderIds().contains(elderId.trim())) {
            System.out.println("[ActivityService] 老人" + elderId + "未报名活动" + activityId);
            return false;
        }

        // 3. 更新活动列表并保存到文件
        List<Activity> activityList = getActivityList();
        
        // 找到对应的活动并更新
        for (Activity activity : activityList) {
            if (activity.getActivityId().equals(activityId)) {
                activity.removeRegistration(elderId.trim());
                break;
            }
        }
        
        // 5. 保存更新后的活动列表
        DataStorageUtil.saveData(ACTIVITY_DATA_KEY, activityList);
        
        System.out.println("[ActivityService] 老人" + elderId + "取消报名活动成功：" + targetActivity.getName());
        
        // 6. 预留数据库操作
        activityDao.updateActivityCancelRegistration(activityId, elderId);
        
        return true;
    }
    /**
     * 查询所有活动（供用户浏览）
     * @return 活动列表
     */
    public List<Activity> getAllActivities() throws IOException, ClassNotFoundException {
        List<Activity> activityList = getActivityList();
        System.out.println("[ActivityService] 查询到活动总数：" + activityList.size());
        return activityList;
    }

    /**
     * 根据ID查询活动（用于报名、详情查看）
     * @param activityId 活动ID
     * @return 活动对象；无匹配返回null
     */
    public Activity getActivityById(String activityId) throws IOException, ClassNotFoundException {
        InputValidator.validateNotEmpty(activityId, "活动ID");
        List<Activity> activityList = getActivityList();
        for (Activity activity : activityList) {
            if (activity.getActivityId().equals(activityId.trim())) {
                return activity;
            }
        }
        System.out.println("[ActivityService] 未查询到活动：" + activityId);
        return null;
    }

    /**
     * 私有辅助方法：统一读取活动列表
     */
    @SuppressWarnings("unchecked")
    private List<Activity> getActivityList() throws IOException, ClassNotFoundException {
        Object data = DataStorageUtil.getData(ACTIVITY_DATA_KEY);
        return data == null ? new ArrayList<>() : (List<Activity>) data;
    }
}