package com.whz;

import com.whz.entity.Employeer;
import com.whz.mapperinterface.IEmployeerMapper;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

/**
 * Created by wb-whz291815 on 2017/8/21.
 */
public class DefaultSqlSessionTest {


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
    public void testSelectMap() {
        Map params = new HashMap();
        params.put("department","产品一部");
        params.put("worktype","开发工程师");

        SqlSession session = sqlSessionFactory.openSession();
        Map<String, Map<String, String>> result = session.selectMap("findEmployeerByDepartmentAndWorktype", params, "employeer_id");
        System.out.println(result);

        session.close();

    }

    @Test
    public void testSelectCursor() throws IOException {
        SqlSession session = sqlSessionFactory.openSession();
        Cursor<Employeer> cursor = session.selectCursor("findAllEmployeer");

        List<Employeer> result = new ArrayList(10);
        Iterator<Employeer> iter = cursor.iterator();
        while (iter.hasNext()) {
            iter.next();
            result.add(iter.next());
        }
        System.out.println(result);

        cursor.close();
    }



}
