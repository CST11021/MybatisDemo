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
package org.apache.ibatis.executor.statement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.ResultHandler;

/**
 * StatementHandler 是对JDBC的 Statement 做进一步的封装，所有的数据库操作，最终其实都是由 java.sql.Statement 来完成的
 *
 * @author Clinton Begin
 */
public interface StatementHandler {

    /**
     * 根据数据库的连接实例创建一个Statement对象
     *
     * @param connection            数据库连接实例
     * @param transactionTimeout    事务超时时间
     * @return
     * @throws SQLException
     */
    Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException;

    /**
     * 用来设置参数：
     * 因为当使用预编译时，一开始SQL编译的时候是不需要设置参数的，所以当要真正执行SQL时，需要设置参数；
     * 如果是使用 Statement 的方式，则该实现为空，因为Statement执行SQL的时候，参数就已经设置好了
     *
     * @param statement
     * @throws SQLException
     */
    void parameterize(Statement statement) throws SQLException;

    /**
     * 批处理的方式，JDBC仅支持批量新增、更新和删除操作，不支持批量查询
     *
     * @param statement
     * @throws SQLException
     */
    void batch(Statement statement) throws SQLException;

    /**
     * 执行更新操作时会调用给方法，这里的更新操作包括：insert、update和delete
     *
     * @param statement
     * @return 返回影响的行数
     * @throws SQLException
     */
    int update(Statement statement) throws SQLException;

    /**
     * 执行查询
     *
     * @param statement
     * @param resultHandler
     * @param <E>
     * @return
     * @throws SQLException
     */
    <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException;

    /**
     * 执行查询，并以游标的方式返回结果集
     *
     * @param statement
     * @param <E>
     * @return
     * @throws SQLException
     */
    <E> Cursor<E> queryCursor(Statement statement) throws SQLException;

    /**
     * 获取本次要执行的SQL相关信息
     *
     * @return
     */
    BoundSql getBoundSql();

    /**
     * 获取参数处理器
     *
     * @return
     */
    ParameterHandler getParameterHandler();

}
