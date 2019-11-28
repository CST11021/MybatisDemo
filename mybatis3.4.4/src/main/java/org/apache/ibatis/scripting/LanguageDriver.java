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
package org.apache.ibatis.scripting;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;

/**
 * MyBatis提供了简单的Java注解，使得我们可以不配置XML格式的Mapper文件，也能方便的编写简单的数据库操作代码：
 *
 * public interface UserMapper {
 *   @Select("SELECT * FROM users WHERE id = #{userId}")
 *   User getUser(@Param("userId") String userId);
 * }
 *
 * 但是注解对动态SQL的支持一直差强人意，即使MyBatis提供了InsertProvider等*Provider注解来支持注解的Dynamic SQL，也没有降低SQL的编写难度，甚至比XML格式的SQL语句更难编写和维护。
 *
 * 注解的优势在于能清晰明了的看见接口所使用的SQL语句，抛弃了繁琐的XML编程方式。但没有良好的动态SQL支持，往往就会导致所编写的DAO层中的接口冗余，所编写的SQL语句很长，易读性差……
 * Mybatis在3.2版本之后，提供了LanguageDriver接口，我们可以使用该接口自定义SQL的解析方式
 */
public interface LanguageDriver {

    /**
     * Creates a {@link ParameterHandler} that passes the actual parameters to the the JDBC statement.
     *
     * @param mappedStatement   正在执行的映射语句
     * @param parameterObject   SQL参数
     * @param boundSql          动态语言执行后的结果SQL。
     * @return
     * @author Frank D. Martinez [mnesarco]
     * @see DefaultParameterHandler
     */
    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);

    /**
     * 创建一个{@link SqlSource}来保存从映射器xml文件中读取的语句。
     *
     * @param configuration     MyBatis配置
     * @param script            从XML文件解析的XNode
     * @param parameterType     从mapper方法获取或在parameterType xml属性中指定的输入参数类型。可以为空。
     * @return
     */
    SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType);

    /**
     * 创建一个{@link SqlSource}来保存从映射器xml文件中读取的语句。
     *
     * @param configuration     MyBatis配置
     * @param script            注解配置的内容
     * @param parameterType     从mapper方法获取或在parameterType xml属性中指定的输入参数类型。可以为空。
     * @return
     */
    SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);

}
