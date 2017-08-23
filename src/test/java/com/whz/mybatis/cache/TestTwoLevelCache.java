package com.whz.mybatis.cache;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.InputStream;
import java.io.Reader;

// 测试二级缓存
/*

    与一级缓存相比，二级缓存范围更大了一些，可以被多个SqlSession所共用。同样是发送同样的查询sql会先去缓存中找，找不到再去
查询数据库。
每个namespace的mapper都会有自己的一个缓存的空间，如果两个mapper的namespace相同，执行mapper查询到的数据将存储到相同的二级缓存.
同样如果有sqlSession执行了commit 会清空二级缓存.
配置文件(不用配置也是默认开启的):
在sqlMapConfig.xml中：
<setting name="cacheEnabled" value="true"/>

 */
public class TestTwoLevelCache {

    private static SqlSessionFactory sqlSessionFactory;
    private static InputStream inputStream;
    static {
        try {
            inputStream = Resources.getResourceAsStream("com/whz/mybatis/cache/mybatisConfig-cache.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 测试：使用二级缓存，使用两个不同的SqlSession对象去执行相同查询条件的查询，第二次查询时不会再发送SQL语句，而是直接
    // 从缓存中取出数据
    @Test
    public void testFindEmployeerByIDWithCache() {
        // 开启两个不同的SqlSession
        SqlSession session1 = sqlSessionFactory.openSession();
        SqlSession session2 = sqlSessionFactory.openSession();

        // 使用二级缓存时，Employeer类必须实现一个 Serializable 接口
        Employeer employeer1 = session1.selectOne("findEmployeerByID", 5);
        // 这个地方一定要提交事务之后二级缓存才会起作用
        session1.commit();
        System.out.println(employeer1);
        
        //由于使用的是两个不同的SqlSession对象，所以即使查询条件相同，一级缓存也不会开启使用
        Employeer employeer2 = session2.selectOne("findEmployeerByID", 5);
        System.out.println(employeer2);

    }

    // 测试：禁用二级缓存<select  useCache="false">
    @Test
    public void testFindEmployeerByIDWithoutCache() {
        SqlSession session1 = sqlSessionFactory.openSession();
        SqlSession session2 = sqlSessionFactory.openSession();

        Employeer employeer1 = session1.selectOne("findEmployeerByIDWithoutCache", 5);
        session1.commit();
        System.out.println(employeer1);

        // 配置了 <select  useCache="false"> 二级缓存不起作用了
        Employeer employeer2 = session2.selectOne("findEmployeerByIDWithoutCache", 5);
        System.out.println(employeer2);

    }


    // 测试：更新/删除/插入 操作会清理一级和二级缓存，也可以设置不刷新（如：<insert flushCache="false"/>），但是一般不予设置,设置可能会导致脏读.
    @Test
    public void testFindEmployeerByIDAfterOtherMethod() {
        SqlSession session1 = sqlSessionFactory.openSession();
        SqlSession session2 = sqlSessionFactory.openSession();
        SqlSession session3 = sqlSessionFactory.openSession();

        Employeer employeer1 = session1.selectOne("findEmployeerByID", 5);
        session1.commit();
        System.out.println(employeer1);


//        session2.update("updateEmployeer",new Employeer(6, "王五"));
//        session2.delete("deleteEmployeer",6);
//        session2.insert("addEmployeer",new Employeer("王五", 23, "行政部门", "执行主管"));
        session2.insert("addEmployeerWithoutFlushCache",new Employeer("王五", 23, "行政部门", "执行主管"));
        session2.commit() ;

        // 因为上面执行了 更新/删除/插入 操作，所以二级缓存会被清除
        Employeer employeer3 = session3.selectOne("findEmployeerByID", 5);
        System.out.println(employeer3);

    }

    // 测试：更新/删除/插入 操作会清理一级和二级缓存
    @Test
    public void testFindEmployeerByIDAfterOtherMethodWithFlushCache() {
        SqlSession session1 = sqlSessionFactory.openSession();
        SqlSession session2 = sqlSessionFactory.openSession();
        SqlSession session3 = sqlSessionFactory.openSession();

        Employeer employeer1 = session1.selectOne("findEmployeerByID", 5);
        session1.commit();
        System.out.println(employeer1);

        session2.insert("addEmployeerWithoutFlushCache",new Employeer("王五", 23, "行政部门", "执行主管"));

        session2.commit() ;

        // 因为上面执行了 更新/删除/插入 操作，所以二级缓存会被清除
        Employeer employeer3 = session3.selectOne("findEmployeerByID", 5);
        System.out.println(employeer3);

    }



}