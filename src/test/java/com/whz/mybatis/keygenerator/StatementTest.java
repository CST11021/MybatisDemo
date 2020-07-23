package com.whz.mybatis.keygenerator;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @Author: wanghz
 * @Date: 2019/11/4 9:25 AM
 */
public class StatementTest {

    @Test
    public void getGeneratedKeysTest() {
        try {
            String url = "jdbcBase:mysql://localhost:3306/group_meal?serverTimezone=GMT";
            String sql = "INSERT INTO t_employeer(employeer_name, employeer_age, employeer_department, employeer_worktype) VALUES (?, ?, ?, ?)";
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, "root", "123456");

            String[] columnNames = {"id", "employeer_name"};
            PreparedStatement stmt = conn.prepareStatement(sql, columnNames);

            stmt.setString(1, "www");
            stmt.setInt(2, 24);
            stmt.setString(3, "部门");
            stmt.setString(4, "类型");

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                // 这里的 t_employeer 表以 employeer_id 为主键，但是代码中我传的 columnNames 都不符合，而结果仍然可以正确的返回主键，
                // 主要是因为在 mybatis 的驱动中只要 columnNames.length > 1就可以了，所以在具体使用的时候还要注意不同数据库驱动实现不同所带来的影响；
                int id = rs.getInt(1);
                System.out.println("----------" + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}



