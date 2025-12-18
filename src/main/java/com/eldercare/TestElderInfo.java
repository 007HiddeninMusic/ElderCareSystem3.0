package com.eldercare;

import com.eldercare.model.Elder;
import com.eldercare.model.User;
import com.eldercare.service.ElderService;
import com.eldercare.service.UserService;

/**
 * 测试程序：检查老人信息获取功能
 */
public class TestElderInfo {
    public static void main(String[] args) {
        try {
            // 1. 检查用户1111是否存在
            UserService userService = UserService.getInstance();
            User user = userService.getUserById("1111");
            System.out.println("用户信息: ID=" + user.getUserId() + ", 角色=" + user.getRole());

            // 2. 直接测试getElderByUserId方法
            ElderService elderService = ElderService.getInstance();
            Elder elder = elderService.getElderByUserId("1111");
            
            if (elder != null) {
                System.out.println("成功获取老人信息:");
                System.out.println("  老人ID: " + elder.getElderId());
                System.out.println("  用户ID: " + elder.getUserId());
                System.out.println("  姓名: " + elder.getName());
                System.out.println("  年龄: " + elder.getAge());
                System.out.println("  手机号: " + elder.getPhone());
                System.out.println("  健康状况: " + elder.getHealthStatus());
            } else {
                System.out.println("未能获取老人信息，检查getElderByUserId方法");
            }

        } catch (Exception e) {
            System.err.println("测试过程中出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}