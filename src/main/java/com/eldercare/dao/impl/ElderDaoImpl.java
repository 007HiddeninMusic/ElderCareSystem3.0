package com.eldercare.dao.impl;

import com.eldercare.dao.ElderDao;
import com.eldercare.model.Elder;

import java.util.ArrayList;
import java.util.List;

/**
 * 老人DAO空实现：仅标记数据库操作位置，暂不实现真实SQL逻辑
 * 后续对接数据库时，需替换此类为真实实现（如基于JDBC/MyBatis）
 */
public class ElderDaoImpl implements ElderDao {

    @Override
    public void insertElder(Elder elder) {
        // 预留：向数据库插入老人信息的操作位置
        System.out.println("[DAO空实现] 准备向数据库插入老人：" + elder.getName() + "（ID：" + elder.getElderId() + "）");

        // 真实数据库操作模板（JDBC示例，暂不执行）：
        /*
        try {
            // 1. 加载数据库驱动（MySQL 8.0+）
            Class.forName("com.mysql.cj.jdbc.Driver");
            // 2. 建立数据库连接（URL、账号、密码需替换为实际配置）
            String url = "jdbc:mysql://localhost:3306/elder_care_db?useSSL=false&serverTimezone=UTC";
            String username = "root";
            String password = "123456";
            Connection conn = DriverManager.getConnection(url, username, password);

            // 3. 编写SQL（插入老人信息，字段与数据库表对齐）
            String sql = "INSERT INTO elder (elder_id, name, age, phone, health_status) " +
                         "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            // 4. 设置SQL参数（与实体类字段对应）
            pstmt.setString(1, elder.getElderId());
            pstmt.setString(2, elder.getName());
            pstmt.setInt(3, elder.getAge());
            pstmt.setString(4, elder.getPhone());
            pstmt.setString(5, elder.getHealthStatus());

            // 5. 执行SQL（插入操作）
            int rows = pstmt.executeUpdate();
            System.out.println("数据库插入成功，影响行数：" + rows);

            // 6. 关闭资源（避免内存泄露）
            pstmt.close();
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("数据库插入失败：" + e.getMessage());
        }
        */
    }

    @Override
    public Elder selectElderById(String elderId) {
        // 预留：根据ID查询老人的操作位置
        System.out.println("[DAO空实现] 准备从数据库查询老人（ID：" + elderId + "）");

        // 真实数据库操作模板（JDBC示例）：
        /*
        Elder elder = null;
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String sql = "SELECT elder_id, name, age, phone, health_status FROM elder WHERE elder_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, elderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // 从结果集封装为Elder实体
                elder = new Elder();
                elder.setElderId(rs.getString("elder_id"));
                elder.setName(rs.getString("name"));
                elder.setAge(rs.getInt("age"));
                elder.setPhone(rs.getString("phone"));
                elder.setHealthStatus(rs.getString("health_status"));
            }
        } catch (SQLException e) {
            System.err.println("数据库查询失败：" + e.getMessage());
        }
        return elder;
        */
        return null; // 空实现返回null，不影响当前逻辑
    }

    @Override
    public List<Elder> selectAllElders() {
        // 预留：查询所有老人的操作位置
        System.out.println("[DAO空实现] 准备从数据库查询所有老人");

        // 真实数据库操作模板：查询所有老人并封装为List<Elder>
        return new ArrayList<>(); // 空实现返回空列表
    }

    @Override
    public int deleteElderById(String elderId) {
        // 预留：根据ID删除老人的操作位置
        System.out.println("[DAO空实现] 准备从数据库删除老人（ID：" + elderId + "）");

        // 真实数据库操作模板：执行DELETE SQL，返回影响行数
        return 0; // 空实现返回0，代表无数据影响
    }

    @Override
    public int updateElder(Elder elder) {
        // 预留：更新老人信息的操作位置
        System.out.println("[DAO空实现] 准备更新数据库中老人（ID：" + elder.getElderId() + "）");

        // 真实数据库操作模板：执行UPDATE SQL，返回影响行数
        return 0; // 空实现返回0
    }
}