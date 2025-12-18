package com.eldercare;

import com.eldercare.model.Elder;
import com.eldercare.model.User;
import com.eldercare.service.ElderService;
import com.eldercare.service.UserService;

import java.io.IOException;
import java.util.List;

/**
 * 测试类：验证修复AdminFrame中老人用户创建时未设置userId字段的问题
 */
public class TestElderUserFix {
    public static void main(String[] args) {
        try {
            // 获取服务实例
            UserService userService = UserService.getInstance();
            ElderService elderService = ElderService.getInstance();

            System.out.println("=== 开始测试老人用户创建修复 ===");

            // 1. 创建一个测试老人用户
            String testUserId = "test_elder_001";
            String testPassword = "123456";
            String testElderName = "测试老人";

            System.out.println("1. 注册测试老人用户：" + testUserId);
            userService.registerUser(testUserId, testPassword, "elder");

            // 2. 模拟AdminFrame中的老人信息创建过程
            System.out.println("2. 创建老人信息...");
            Elder elder = new Elder();
            elder.setElderId(testUserId); // 老人ID与用户账号一致
            elder.setUserId(testUserId); // 设置关联的用户ID（修复的关键行）
            elder.setName(testElderName);
            elder.setAge(75);
            elder.setPhone("13800138000");
            elder.setHealthStatus("健康");

            // 3. 添加老人信息
            elderService.addElder(elder);
            System.out.println("3. 老人信息添加成功");

            // 4. 验证老人信息是否正确保存
            System.out.println("4. 验证老人信息...");
            List<Elder> elders = elderService.queryElders(null);
            boolean found = false;
            for (Elder e : elders) {
                if (e.getElderId().equals(testUserId)) {
                    found = true;
                    System.out.println("   找到老人：" + e.getName() + " (ID: " + e.getElderId() + ")");
                    System.out.println("   关联用户ID：" + e.getUserId());
                    
                    // 验证userId字段是否正确设置
                    if (e.getUserId() != null && e.getUserId().equals(testUserId)) {
                        System.out.println("   ✅ userId字段设置正确！");
                    } else {
                        System.out.println("   ❌ userId字段设置错误！");
                    }
                    break;
                }
            }

            if (!found) {
                System.out.println("   ❌ 未找到创建的老人信息！");
            }

            // 5. 清理测试数据
            System.out.println("5. 清理测试数据...");
            elderService.deleteElder(testUserId);
            userService.deleteUser(testUserId);
            System.out.println("   测试数据已清理");

            System.out.println("=== 测试完成 ===");

        } catch (Exception e) {
            System.err.println("测试过程中发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
}