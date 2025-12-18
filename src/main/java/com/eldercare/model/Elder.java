package com.eldercare.model;

import java.io.Serializable;

/**
 * 老人模型类
 * 功能：对应系统中接受养老服务的核心用户
 */
public class Elder implements Serializable {
    private static final long serialVersionUID = 1L;

    // 老人唯一ID，格式如ELDER_20251201_001
    private String elderId;
    // 关联的用户ID，与User表的userId对应
    private String userId;
    // 姓名
    private String name;
    // 年龄（1-120岁合理范围）
    private int age;
    // 手机号（11位数字）
    private String phone;
    // 健康状况，如"良好""高血压""糖尿病"
    private String healthStatus;

    // 无参构造方法
    public Elder() {}

    // 全参构造方法
    public Elder(String elderId, String userId, String name, int age, String phone, String healthStatus) {
        this.elderId = elderId;
        this.setUserId(userId);    // 复用Setter的校验逻辑
        this.setName(name);       // 复用Setter的校验逻辑
        this.setAge(age);
        this.setPhone(phone);
        this.healthStatus = healthStatus;
    }

    // Getter和Setter方法
    public String getElderId() {
        return elderId;
    }

    public void setElderId(String elderId) {
        // 老人ID非空校验
        if (elderId != null && !elderId.trim().isEmpty()) {
            this.elderId = elderId.trim();
        } else {
            throw new IllegalArgumentException("老人ID不能为空");
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        // 用户ID非空校验
        if (userId != null && !userId.trim().isEmpty()) {
            this.userId = userId.trim();
        } else {
            throw new IllegalArgumentException("用户ID不能为空");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        // 姓名非空校验
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        } else {
            throw new IllegalArgumentException("姓名不能为空");
        }
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        // 年龄范围校验（1-120岁）
        if (age >= 1 && age <= 120) {
            this.age = age;
        } else {
            throw new IllegalArgumentException("年龄必须在1-120岁之间");
        }
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        // 手机号格式校验（11位数字，简化版）
        String regex = "^1[3-9]\\d{9}$"; // 匹配中国大陆手机号
        if (phone != null && phone.matches(regex)) {
            this.phone = phone;
        } else {
            throw new IllegalArgumentException("请输入合法的11位手机号");
        }
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        // 健康状况允许为空（默认"未填写"）
        this.healthStatus = (healthStatus == null || healthStatus.trim().isEmpty())
                ? "未填写"
                : healthStatus.trim();
    }

    @Override
    public String toString() {
        return "Elder{" +
                "elderId='" + elderId + '\'' +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", phone='" + phone + '\'' +
                ", healthStatus='" + healthStatus + '\'' +
                '}';
    }
}