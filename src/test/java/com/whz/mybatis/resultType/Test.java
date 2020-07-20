package com.whz.mybatis.resultType;


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
            inputStream = Resources.getResourceAsStream("com/whz/mybatis/resultType/mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @org.junit.Test
    public void testGetAllUsers(){
        SqlSession session = sqlSessionFactory.openSession();
        // <setting name="mapUnderscoreToCamelCase" value="true"/> 注意需要设置启用驼峰命名，如果不设置，User#registerTime会为null，
        // 因为使用了resultType，如果不启用驼峰规则，POJO字段名必须和数据库字段一致才行
        List<User> userList = session.selectList("getAllUsers");
        System.out.println(userList.get(0).getRegisterTime());
        session.close();
    }




}
