package com.whz.mybatis.plugins.pagehelper;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 * Created by wb-whz291815 on 2017/8/21.
 */
public class Test {

    private static SqlSessionFactory sqlSessionFactory;
    private static InputStream inputStream;
    static {
        try {
            inputStream = Resources.getResourceAsStream("com/whz/mybatis/plugins/pagehelper/mybatis-config.xml");
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


    @org.junit.Test
    public void testGetPage(){
        SqlSession session = sqlSessionFactory.openSession();

        // 分页查找，PageHelper使用的是物理分页（设置分页信息保存到threadlocal中）
        PageHelper.startPage(2, 3);
        // 紧跟着的第一个select方法会被分页，contryMapper会被PageInterceptor截拦,截拦器会从threadlocal中取出分页信息，把
        // 分页信息加到sql语句中，实现了分页查旬
        List<User> userList = session.getMapper(UserMapper.class).getAllUsers();
        // userList 会被包装成一个Page对象
        PageInfo pageInfo = new PageInfo<>(userList);
        System.out.println(pageInfo.getList());

        session.close();
    }


}
