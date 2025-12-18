package com.eldercare.model;

import java.io.Serializable;

/**
 * 用户模型类
 * 功能：对应系统中的登录用户（管理员、老人、家属等角色）
 */
public class User implements Serializable {
    // 序列化版本号（确保序列化/反序列化兼容性）
    private static final long serialVersionUID = 1L;

    // 登录账号（唯一标识），如admin、family_001
    private String userId;
    // 登录密码（实际项目需加密存储，此处简化）
    private String password;
    // 角色：admin（管理员）、elder（老人）、family（家属）、caregiver（护工）
    private String role;

    // 无参构造方法（序列化、反射创建对象必需）
    public User() {}

    // 全参构造方法（快速创建完整对象）
    public User(String userId, String password, String role) {
        this.userId = userId;
        this.password = password;
        // 角色合法性校验：仅允许指定角色值
        if (isValidRole(role)) {
            this.role = role;
        } else {
            throw new IllegalArgumentException("角色必须是admin、elder、family或caregiver");
        }
    }

    // Getter和Setter方法
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        // 账号非空校验：避免空账号
        if (userId != null && !userId.trim().isEmpty()) {
            this.userId = userId.trim();
        } else {
            throw new IllegalArgumentException("账号不能为空");
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        // 密码长度校验：至少6位（简化安全要求）
        if (password != null && password.length() >= 6) {
            this.password = password;
        } else {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        // 角色合法性校验（与全参构造一致）
        if (isValidRole(role)) {
            this.role = role;
        } else {
            throw new IllegalArgumentException("角色必须是admin、elder、family或caregiver");
        }
    }

    /**
     * 校验角色是否合法（私有，仅内部使用）
     * @param role 角色标识
     * @return true-合法，false-不合法
     */
    private boolean isValidRole(String role) {
        return role != null && (
                "admin".equals(role) ||
                        "elder".equals(role) ||
                        "family".equals(role) ||
                        "caregiver".equals(role)
        );
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", role='" + role + '\'' +
                '}'; // 密码字段不打印，避免安全泄露
    }
}