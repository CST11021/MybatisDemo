package com.whz.mybatis.resources;

import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class TestURL {

    // 获取URL指定的资源
    @Test
    public void test4() throws IOException {
        URL url = new URL("http://lavasoft.blog.51cto.com/attachment/200811/200811271227767778082.jpg");
        //获得此 URL 的内容。
        Object obj = url.getContent();
        System.out.println(obj.getClass().getName());
    }

    // 获取URL指定的资源
    @Test
    public void test3() throws IOException {
        URL url = new URL("http://www.hrtsea.com/down/soft/45.htm");
        //返回一个 URLConnection 对象，它表示到 URL 所引用的远程对象的连接。
        URLConnection uc = url.openConnection();
        //打开的连接读取的输入流。
        InputStream in = uc.getInputStream();
        int c;
        while ((c = in.read()) != -1)
            System.out.print(c);
        in.close();
    }

    // 读取URL指定的网页内容
    @Test
    public void test2() throws IOException {
        URL url = new URL("http://www.hrtsea.com/down/soft/45.htm");
        //打开到此 URL 的连接并返回一个用于从该连接读入的 InputStream。
        Reader reader = new InputStreamReader(new BufferedInputStream(url.openStream()));
        int c;
        while ((c = reader.read()) != -1) {
            System.out.print((char) c);
        }
        reader.close();
    }

    // 获取URL的输入流，并输出
    @Test
    public void test() throws IOException {
        URL url = new URL("http://lavasoft.blog.51cto.com/62575/120430");
        //打开到此 URL 的连接并返回一个用于从该连接读入的 InputStream。
        InputStream in = url.openStream();
        int c;
        while ((c = in.read()) != -1)
            System.out.print(c);
        in.close();
    }

    // 测试URL支持的Scheme
    @Test
    public void testSupportedUrlSchemes() {

        String host = "www.java2s.com";
        String file = "/index.html";

        String[] schemes = {"http", "https", "ftp", "mailto", "telnet", "file", "ldap", "gopher",
                "jdbc", "rmi", "jndi", "jar", "doc", "netdoc", "nfs", "verbatim", "finger", "daytime",
                "systemresource"};

        for (int i = 0; i < schemes.length; i++) {
            try {
                URL u = new URL(schemes[i], host, file);
                System.out.println(schemes[i] + " is supported");
            } catch (Exception ex) {
//                System.out.println(schemes[i] + " is not supported");
            }
        }

    }

}