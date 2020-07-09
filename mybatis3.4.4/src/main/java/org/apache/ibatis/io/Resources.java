/**
 * Copyright 2009-2015 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Properties;


public class Resources {

    private static ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper();
    /** 调用getResourceAsReader时使用的字符集。 null表示使用系统默认值 */
    private static Charset charset;

    public Resources() {
    }

    // --------------------------------
    // 基于类的跟路径返回资源文件对应的 URL
    // --------------------------------

    /**
     * 如：
     * URL url = resources.getResourceURL("log4j.properties");
     *
     * @param resource
     * @return
     * @throws IOException
     */
    public static URL getResourceURL(String resource) throws IOException {
        return getResourceURL(null, resource);
    }
    public static URL getResourceURL(ClassLoader loader, String resource) throws IOException {
        URL url = classLoaderWrapper.getResourceAsURL(resource, loader);
        if (url == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return url;
    }

    // --------------------------------
    // 基于类的跟路径返回资源文件对应的 InputStream
    // --------------------------------

    /**
     * 根据资源文件的类路径返回一个 InputStream 对象
     *
     * @param resource
     * @return
     * @throws IOException
     */
    public static InputStream getResourceAsStream(String resource) throws IOException {
        return getResourceAsStream(null, resource);
    }
    /**
     * 根据资源文件的类路径返回一个InputStream 对象，优先使用指定类加载器加载资源文件
     *
     * @param loader
     * @param resource
     * @return
     * @throws IOException
     */
    public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
        InputStream in = classLoaderWrapper.getResourceAsStream(resource, loader);
        if (in == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return in;
    }

    // 解析一个properties文件，然后返回一个Properties实例
    public static Properties getResourceAsProperties(String resource) throws IOException {
        Properties props = new Properties();
        InputStream in = getResourceAsStream(resource);
        props.load(in);
        in.close();
        return props;
    }
    public static Properties getResourceAsProperties(ClassLoader loader, String resource) throws IOException {
        Properties props = new Properties();
        InputStream in = getResourceAsStream(loader, resource);
        props.load(in);
        in.close();
        return props;
    }

    // 如：Reader reader = resources.getResourceAsReader("com/whz/mybatis/resources/test.properties");
    public static Reader getResourceAsReader(String resource) throws IOException {
        Reader reader;
        if (charset == null) {
            reader = new InputStreamReader(getResourceAsStream(resource));
        } else {
            reader = new InputStreamReader(getResourceAsStream(resource), charset);
        }
        return reader;
    }
    public static Reader getResourceAsReader(ClassLoader loader, String resource) throws IOException {
        Reader reader;
        if (charset == null) {
            reader = new InputStreamReader(getResourceAsStream(loader, resource));
        } else {
            reader = new InputStreamReader(getResourceAsStream(loader, resource), charset);
        }
        return reader;
    }

    // File file = resources.getResourceAsFile("com/whz/mybatis/resources/test.properties");
    public static File getResourceAsFile(String resource) throws IOException {
        return new File(getResourceURL(resource).getFile());
    }
    public static File getResourceAsFile(ClassLoader loader, String resource) throws IOException {
        return new File(getResourceURL(loader, resource).getFile());
    }


    // 如：InputStream inputStream = resources.getUrlAsStream("file:///D:/test.properties");
    public static InputStream getUrlAsStream(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        return conn.getInputStream();
    }
    // 如：Reader reader = resources.getUrlAsReader("file:///D:/test.properties");
    public static Reader getUrlAsReader(String urlString) throws IOException {
        Reader reader;
        if (charset == null) {
            reader = new InputStreamReader(getUrlAsStream(urlString));
        } else {
            reader = new InputStreamReader(getUrlAsStream(urlString), charset);
        }
        return reader;
    }
    // 比如：Properties p = getUrlAsProperties("file:///D:/test.properties");
    public static Properties getUrlAsProperties(String urlString) throws IOException {
        Properties props = new Properties();
        InputStream in = getUrlAsStream(urlString);
        props.load(in);
        in.close();
        return props;
    }


    /**
     * 加载一个类
     *
     * @param className - the class to fetch
     * @return The loaded class
     * @throws ClassNotFoundException If the class cannot be found (duh!)
     */
    public static Class<?> classForName(String className) throws ClassNotFoundException {
        return classLoaderWrapper.classForName(className);
    }
    /**
     * Returns the default classloader (may be null).
     *
     * @return The default classloader
     */
    public static ClassLoader getDefaultClassLoader() {
        return classLoaderWrapper.defaultClassLoader;
    }
    public static void setDefaultClassLoader(ClassLoader defaultClassLoader) {
        classLoaderWrapper.defaultClassLoader = defaultClassLoader;
    }
    public static Charset getCharset() {
        return charset;
    }
    public static void setCharset(Charset charset) {
        Resources.charset = charset;
    }

}
