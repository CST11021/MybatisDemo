package com.whz.mybatis.resources;

import org.apache.ibatis.io.Resources;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

/**
 * Created by wb-whz291815 on 2017/8/22.
 */
public class ResourcesTest {

    @Test
    public void testGetResourceAsProperties() throws IOException {

        Resources resources = new Resources();
        Properties properties = resources.getUrlAsProperties("file:///D:/test.properties");
        properties = resources.getUrlAsProperties("file:///D:/test.properties");
        InputStream inputStream = resources.getUrlAsStream("file:///D:/test.properties");
        Reader reader = resources.getUrlAsReader("file:///D:/test.properties");

        // 以下这些方法都会相对类路径的，底层调用的一般都是通过ClassLoader 来返回路径
        reader = resources.getResourceAsReader("com/whz/mybatis/resources/test.properties");
        URL url = resources.getResourceURL("com/whz/mybatis/resources/test.properties");
        File file = resources.getResourceAsFile("com/whz/mybatis/resources/test.properties");

        System.out.println(properties);

    }

}
