package com.eldercare.dao.impl;

import com.eldercare.dao.UserDao;
import com.eldercare.model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户DAO空实现：标记数据库操作位置
 */
public class UserDaoImpl implements UserDao {

    @Override
    public void insertUser(User user) {
        // 预留：插入用户的操作位置
        System.out.println("[DAO空实现] 准备插入用户（账号：" + user.getUserId() + "，角色：" + user.getRole() + "）");

        // 真实数据库操作模板（核心SQL）：
        // String sql = "INSERT INTO user (user_id, password, role) VALUES (?, ?, ?)";
        // 注：密码需加密存储（如使用BCrypt算法），此处SQL参数应为加密后的密码
    }

    @Override
    public User selectUserByUserId(String userId) {
        // 预留：根据账号查询用户的操作位置（登录校验核心逻辑）
        System.out.println("[DAO空实现] 准备查询用户（账号：" + userId + "）");

        // 真实数据库操作模板：查询user表，返回用户实体（含加密后的密码）
        return null;
    }

    @Override
    public List<User> selectAllUsers() {
        // 预留：查询所有用户的操作位置
        System.out.println("[DAO空实现] 准备查询数据库中所有用户");
        return new ArrayList<>();
    }

    @Override
    public int updateUserPassword(String userId, String newPassword) {
        // 预留：更新用户密码的操作位置
        System.out.println("[DAO空实现] 准备更新用户（账号：" + userId + "）的密码");

        // 真实数据库操作模板（核心SQL）：
        // String sql = "UPDATE user SET password = ? WHERE user_id = ?";
        // 注：newPassword需先加密再存入数据库
        return 0;
    }
    @Override
    public int updateUserRole(String userId, String newRole) {
        // 预留：更新用户角色的数据库操作位置
        System.out.println("[DAO空实现] 准备更新用户（账号：" + userId + "）的角色为：" + newRole);

        // 真实数据库操作模板（核心SQL）：
        // String sql = "UPDATE user SET role = ? WHERE user_id = ?";
        return 0;
    }

    @Override
    public int deleteUserById(String userId) {
        System.out.println("[DAO空实现] 准备删除用户：" + userId);
        return 0;
    }
}