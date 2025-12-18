package com.eldercare;

import com.eldercare.model.Elder;
import com.eldercare.model.User;
import com.eldercare.service.ElderService;
import com.eldercare.service.UserService;

import java.util.List;

/**
 * 修复老人数据的临时程序
 * 功能：为用户1111创建关联的老人数据，解决老人信息缺失问题
 */
public class FixElderData {
    /**
     * 主方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            // 1. 检查当前系统中的用户数据
            UserService userService = UserService.getInstance();
            System.out.println("=== 当前系统用户列表 ===");
            List<User> users = userService.getAllUsers();
            for (User user : users) {
                System.out.println("用户ID: " + user.getUserId() + ", 角色: " + user.getRole());
            }

            // 2. 检查用户1111是否存在并具有正确角色
            User user1111 = userService.getUserById("1111");
            if (user1111 == null) {
                System.out.println("\n错误: 用户1111不存在！");
                // 创建用户1111
                userService.registerUser("1111", "1111", "elder");
                System.out.println("已创建用户1111，角色为elder");
            } else {
                System.out.println("\n用户1111存在，角色: " + user1111.getRole());
                // 确保角色是elder
                if (!"elder".equals(user1111.getRole())) {
                    userService.updateUserRole("1111", "elder");
                    System.out.println("已将用户1111角色更新为elder");
                }
            }

            // 3. 检查当前系统中的老人数据
            ElderService elderService = ElderService.getInstance();
            System.out.println("\n=== 当前系统老人列表 ===");
            List<Elder> elders = elderService.queryElders(null);
            for (Elder elder : elders) {
                System.out.println("老人ID: " + elder.getElderId() + ", 关联用户ID: " + elder.getUserId() + ", 姓名: " + elder.getName());
            }

            // 4. 检查是否已有关联用户1111的老人数据
            boolean hasElderForUser1111 = false;
            for (Elder elder : elders) {
                if ("1111".equals(elder.getUserId())) {
                    hasElderForUser1111 = true;
                    System.out.println("\n已存在关联用户1111的老人数据: " + elder);
                    break;
                }
            }

            // 5. 如果没有，创建关联的老人数据
            if (!hasElderForUser1111) {
                Elder elder = new Elder();
                elder.setUserId("1111");
                elder.setName("张老人");
                elder.setAge(78);
                elder.setPhone("13800138000");
                elder.setHealthStatus("健康状况良好");
                
                elderService.addElder(elder);
                System.out.println("\n已为用户1111创建关联的老人数据");
                
                // 再次检查
                List<Elder> updatedElders = elderService.queryElders(null);
                System.out.println("\n=== 更新后的老人列表 ===");
                for (Elder e : updatedElders) {
                    System.out.println("老人ID: " + e.getElderId() + ", 关联用户ID: " + e.getUserId() + ", 姓名: " + e.getName());
                }
            }

            System.out.println("\n修复完成！现在用户1111应该可以正常获取老人信息并提交服务申请了。");
            
        } catch (Exception e) {
            System.err.println("修复过程中出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}