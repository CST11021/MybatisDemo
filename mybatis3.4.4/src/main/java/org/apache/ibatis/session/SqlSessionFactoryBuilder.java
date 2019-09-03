/**
 * Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;

// SqlSessionFactoryBuilder通过类名就可以看出这个类的主要作用就是创建一个SqlSessionFactory，SqlSessionFactory 有两个实现类
// DefaultSqlSessionFactory 和 SqlSessionManager，这里SqlSessionFactoryBuilder 创建的SqlSessionFactory都是使用SqlSessionFactoryBuilder实现类，
// 它通过输入mybatis配置文件的字节流或者字符流生成XMLConfigBuilder，XMLConfigBuilder再创建一个Configuration，
// Configuration这个类中包含了mybatis的配置的一切信息，mybatis进行的所有操作都需要根据Configuration中的信息来进行。
public class SqlSessionFactoryBuilder {

    public SqlSessionFactory build(Reader reader) {
        return build(reader, null, null);
    }
    public SqlSessionFactory build(Reader reader, String environment) {
        return build(reader, environment, null);
    }
    public SqlSessionFactory build(Reader reader, Properties properties) {
        return build(reader, null, properties);
    }
    public SqlSessionFactory build(InputStream inputStream) {
        return build(inputStream, null, null);
    }
    public SqlSessionFactory build(InputStream inputStream, String environment) {
        return build(inputStream, environment, null);
    }
    public SqlSessionFactory build(InputStream inputStream, Properties properties) {
        return build(inputStream, null, properties);
    }

    //----------------------------------------- 以下是三个核心的方法 -------------------------------------------

    /**
     * 资源文件可以使用 Reader 和 InputStream 两种形式返回，然后去解析
     *
     * @param reader            将配置文件包装为一个Reader对象，为后续解析做准备
     * @param environment       表示配置文件<environments default="development">中的default属性
     * @param properties        表示配置文件中的 <properties/> 标签
     * @return SqlSessionFactory 接口：该接口用于创建一个 SqlSession 对象
     */
    public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
        try {
            XMLConfigBuilder parser = new XMLConfigBuilder(reader, environment, properties);
            return build(parser.parse());
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                reader.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    /**
     * 类比 {@link SqlSessionFactoryBuilder#build(Reader, String, Properties)} 方法
     *
     * @param inputStream
     * @param environment
     * @param properties
     * @return
     */
    public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        try {
            XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
            return build(parser.parse());
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                inputStream.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    /**
     * 配置文件解析后再内存中保存为一个 Configuration 对象，该方法使用 Configuration 对象创建一个 DefaultSqlSessionFactory
     *
     * @param config
     * @return
     */
    public SqlSessionFactory build(Configuration config) {
        return new DefaultSqlSessionFactory(config);
    }
    //----------------------------------------- 以上是三个核心的方法 -------------------------------------------
}
