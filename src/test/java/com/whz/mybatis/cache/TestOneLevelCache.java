package com.whz.mybatis.cache;

import com.whz.mybatis.entity.Employeer;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.InputStream;
import java.io.Reader;

// 测试一级缓存： 也就是Session级的缓存（默认开启）
public class TestOneLevelCache {

    private static SqlSessionFactory sqlSessionFactory;
    private static Reader reader;
    private static InputStream inputStream;
    static {
        try {
            inputStream = Resources.getResourceAsStream("mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCache1() {
        SqlSession session = sqlSessionFactory.openSession();
        Employeer employeer1 = session.selectOne("findEmployeerByID", 5);
        System.out.println(employeer1);

        // 因为使用同一个会话对象，
        // 一级缓存默认就会被使用，所以第二次不会去查询数据库。
        // 一级缓存的使用条件是：
        //              1、必须是同一个会话对象；
        //              2、查询条件是一样的；
        //              5、session没有被关闭
        //              3、没有执行过session.clearCache()清理缓存；
        //              4、没有执行过增删改的操作(这些操作都会清理缓存)；
        Employeer employeer2 = session.selectOne("findEmployeerByID", 5);
        System.out.println(employeer2);
        session.close();// 会话关闭后，缓存就会被清除

    }
}