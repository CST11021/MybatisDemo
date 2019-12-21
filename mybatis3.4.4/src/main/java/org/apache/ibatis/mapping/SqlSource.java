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
package org.apache.ibatis.mapping;

/**
 * 解析配置文件时，会将SQL语句封装成一个SQLSource的对象。SqlSource接口只有一个getBoundSql(Object parameterObject)方法，返回一个BoundSql对象。
 * 一个BoundSql对象，代表了一次sql语句的实际执行，而SqlSource对象的责任，就是根据传入的参数对象，动态计算出这个BoundSql，也就是说Mapper文件中的
 * <if/>节点的计算，是由SqlSource对象完成的。SqlSource最常用的实现类是DynamicSqlSource
 *
 *
 * 表示从XML文件或注释读取的映射语句的内容。该接口的实例，仅对应带有占位符的SQL和参数类型进行封装，不封装实际的参数值，SqlSource + 参数值后，得到一个可以交给Statement处理的BoundSql实例
 *
 * It creates the SQL that will be passed to the database out of the input parameter received from the user.（它创建将从用户接收的输入参数传递到数据库的SQL。）
 *
 * @author Clinton Begin
 */
public interface SqlSource {

    /**
     * 实现该方法：将SQL和占位符参数封装为一个 BoundSql 对象
     * SqlSource + 参数值后，得到一个可以交给Statement处理的BoundSql实例
     *
     * @param parameterObject   表示SQL的入参
     * @return
     */
    BoundSql getBoundSql(Object parameterObject);

}
