package com.eldercare.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 活动模型类
 * 功能：对应养老机构发布的健康讲座、文娱活动等，包含活动信息和报名管理
 */
public class Activity implements Serializable {
    private static final long serialVersionUID = 1L;

    // 活动唯一ID，格式如ACTIVITY_20251201_001
    private String activityId;
    // 活动名称，如"健康讲座-高血压管理"
    private String name;
    // 活动时间，格式如"2025-12-10 09:00-11:00"
    private String time;
    // 活动地点，如"养老院1楼多功能厅"
    private String location;
    // 活动详细描述
    private String description;
    // 已报名老人ID列表，与Elder.elderId对应
    private List<String> registeredElderIds;

    // 无参构造方法，初始化报名列表为空
    public Activity() {
        this.registeredElderIds = new ArrayList<>();
    }

    // 全参构造方法
    public Activity(String activityId, String name, String time, String location, String description, List<String> registeredElderIds) {
        this.activityId = activityId;
        this.setName(name);
        this.setTime(time);
        this.setLocation(location);
        this.description = description;
        // 如果传入的报名列表为null，初始化为空列表
        this.registeredElderIds = (registeredElderIds == null) ? new ArrayList<>() : registeredElderIds;
    }

    // Getter和Setter方法
    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        if (activityId != null && !activityId.trim().isEmpty()) {
            this.activityId = activityId.trim();
        } else {
            throw new IllegalArgumentException("活动ID不能为空");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        } else {
            throw new IllegalArgumentException("活动名称不能为空");
        }
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        // 时间格式校验：支持"2025-12-10 09:00"和"2025-12-10 09:00-11:00"两种格式
        String regex = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}(-\\d{2}:\\d{2})?$";
        if (time != null && time.matches(regex)) {
            this.time = time.trim();
        } else {
            throw new IllegalArgumentException("时间格式错误（示例：2025-12-10 09:00 或 2025-12-10 09:00-11:00）");
        }
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (location != null && !location.trim().isEmpty()) {
            this.location = location.trim();
        } else {
            throw new IllegalArgumentException("活动地点不能为空");
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        // 描述允许为空，默认值为"无详细描述"
        this.description = (description == null || description.trim().isEmpty())
                ? "无详细描述"
                : description.trim();
    }

    public List<String> getRegisteredElderIds() {
        return registeredElderIds;
    }

    public void setRegisteredElderIds(List<String> registeredElderIds) {
        this.registeredElderIds = (registeredElderIds == null) ? new ArrayList<>() : registeredElderIds;
    }

    /**
     * 添加老人报名
     * @param elderId 老人ID
     */
    public void addRegistration(String elderId) {
        if (elderId != null && !elderId.trim().isEmpty() && !registeredElderIds.contains(elderId.trim())) {
            registeredElderIds.add(elderId.trim());
        }
    }

    /**
     * 取消老人报名
     * @param elderId 老人ID
     */
    public void removeRegistration(String elderId) {
        if (elderId != null) {
            registeredElderIds.remove(elderId.trim());
        }
    }

    @Override
    public String toString() {
        return "Activity{" +
                "activityId='" + activityId + '\'' +
                ", name='" + name + '\'' +
                ", time='" + time + '\'' +
                ", location='" + location + '\'' +
                ", registeredCount=" + registeredElderIds.size() +
                '}';
    }
}