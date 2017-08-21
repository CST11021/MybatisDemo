package com.whz.mybatis.typeHandler;


import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Created by wb-whz291815 on 2017/8/21.
 */
public class Test {

    private static SqlSessionFactory sqlSessionFactory;
    private static InputStream inputStream;
    static {
        try {
            inputStream = Resources.getResourceAsStream("com/whz/mybatis/typeHandler/mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @org.junit.Test
    public void testGetUserById(){
        SqlSession session = sqlSessionFactory.openSession();
        User user = session.selectOne("getUserById",1);
        System.out.println(user);
        session.close();
    }

    @org.junit.Test
    public void testGetAllUsers(){
        SqlSession session = sqlSessionFactory.openSession();
        List<User> userList = session.selectList("getAllUsers");
        System.out.println(userList);
        session.close();
    }

    @org.junit.Test
    public void testInsertUser() {
        User user = new User("张三","123456", new Date());
        SqlSession session = sqlSessionFactory.openSession();
        List<User> userList = session.selectList("insertUser1", user);
//        List<User> userList = session.selectList("insertUser2", user);
//        List<User> userList = session.selectList("insertUser3", user);
        session.commit();
        session.close();
        System.out.println(userList);

    }

    @org.junit.Test
    public void test() {

        System.out.println(new Date().toString());

    }



}
