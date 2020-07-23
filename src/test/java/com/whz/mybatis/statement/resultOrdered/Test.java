package com.whz.mybatis.statement.resultOrdered;


import com.whz.mybatis.resultType.User;
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
            inputStream = Resources.getResourceAsStream("com/whz/mybatis/statement/resultOrdered/mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @org.junit.Test
    public void testGetAllUsers(){
        SqlSession session = sqlSessionFactory.openSession();
        List<CompositeIdTestTableVO> list = session.selectList("getAll");
        System.out.println(list);
        session.close();
    }




}
