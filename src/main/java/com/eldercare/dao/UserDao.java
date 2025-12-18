package com.eldercare.dao;

import com.eldercare.model.User;

import java.util.List;

/**
 * 用户信息DAO接口：定义用户的数据库操作规范
 */
public interface UserDao {
    /**
     * 向数据库插入一条用户信息（如新增家属、护工账号）
     * @param user 用户实体（包含账号、密码、角色等）
     */
    void insertUser(User user);

    /**
     * 根据账号查询用户信息（用于登录校验）
     * @param userId 用户账号（如admin、family_001）
     * @return 匹配的用户实体；无匹配时返回null
     */
    User selectUserByUserId(String userId);

    /**
     * 查询数据库中所有用户信息（用于管理员账号管理）
     * @return 用户列表（无数据时返回空列表）
     */
    List<User> selectAllUsers();

    /**
     * 更新用户密码（用于密码重置功能）
     * @param userId 用户账号
     * @param newPassword 新密码（需加密存储，此处仅传递明文，加密在实现层处理）
     * @return 影响的行数（1：更新成功；0：无此用户；-1：更新失败）
     */
    int updateUserPassword(String userId, String newPassword);
    /**
     * 更新用户角色
     * @param userId 用户账号
     * @param newRole 新角色
     * @return 影响的行数（1：成功；0：无此用户）
     */
    int updateUserRole(String userId, String newRole);
    /**
     * 数据库删除用户
     * @param userId 用户ID
     * @return 受影响行数（1=成功，0=无数据）
     */
    int deleteUserById(String userId);
}