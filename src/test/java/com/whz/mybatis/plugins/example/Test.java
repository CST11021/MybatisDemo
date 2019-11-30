package com.whz.mybatis.plugins.example;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.List;

/**
 * Created by wb-whz291815 on 2017/8/21.
 */
public class Test {

    private static SqlSessionFactory sqlSessionFactory;
    private static InputStream inputStream;
    static {
        try {
            inputStream = Resources.getResourceAsStream("com/whz/mybatis/plugins/example/mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @org.junit.Test
    public void testGetAll(){
        SqlSession session = sqlSessionFactory.openSession();

        // 查找全部
        List<User> userList = session.getMapper(UserMapper.class).getAllUsers();
        System.out.println(userList);

        session.close();
    }

}
