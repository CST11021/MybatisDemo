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
package org.apache.ibatis.executor;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/**
 * @author Clinton Begin
 */
public class SimpleExecutor extends BaseExecutor {

    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }


    /* ---------------------------------- 三个数据库操作的核心方法：doUpdate、doQuery 和 doQueryCursor----------------------------------- */

    /**
     * 所有的更新、删除、插入 操作最终都将调用这个方法来行执行
     *
     * @param ms
     * @param parameter
     * @return
     * @throws SQLException
     */
    @Override
    public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;
        try {
            Configuration configuration = ms.getConfiguration();
            // 这里默认使用 PreparedStatementHandler 实现
            StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
            // 这里默认返回一个 PreparedStatement 实现
            stmt = prepareStatement(handler, ms.getStatementLog());
            return handler.update(stmt);
        } finally {
            // Simple类型的执行器，执行完后就将statement关闭掉
            closeStatement(stmt);
        }
    }

    /**
     * 所有的查询方法，最终会来掉用这个方法
     *
     * @param ms
     * @param parameter
     * @param rowBounds
     * @param resultHandler
     * @param boundSql
     * @param <E>
     * @return
     * @throws SQLException
     */
    @Override
    public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        Statement stmt = null;
        try {
            Configuration configuration = ms.getConfiguration();
            // 根据配置信息获取一个StatementHandler的一个实现
            StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
            // 通过委托 StatementHandler 创建一个 statement 对象
            stmt = prepareStatement(handler, ms.getStatementLog());
            // 委托给 StatementHandler 来执行查询操作
            return handler.<E>query(stmt, resultHandler);
        } finally {
            closeStatement(stmt);
        }
    }

    /**
     * 如果 SqlSession 调用的是 selectCursor 类型的查询方法，那么执行器最终会来调用这个方法
     *
     * @param ms
     * @param parameter
     * @param rowBounds
     * @param boundSql
     * @param <E>
     * @return
     * @throws SQLException
     */
    @Override
    protected <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, null, boundSql);
        Statement stmt = prepareStatement(handler, ms.getStatementLog());
        return handler.<E>queryCursor(stmt);
    }


    /**
     * {@link #doUpdate(MappedStatement, Object)}、
     * {@link #doQuery(MappedStatement, Object, RowBounds, ResultHandler, BoundSql)}、
     * {@link #doQueryCursor(MappedStatement, Object, RowBounds, BoundSql)}
     * 以上三个核心方法，内部都是通过该方法来获取一个底层JDBC的 Statement 对象，最终的数据库操作方法是交由 Statement 对象来完成的。
     * 另外，mybatis会根据配置信息创建不同的StatementHandler对象，StatementHandler 主要是用来创建 Statement 对象的。
     *
     * @param handler
     * @param statementLog
     * @return
     * @throws SQLException
     */
    private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
        Statement stmt;
        Connection connection = getConnection(statementLog);
        stmt = handler.prepare(connection, transaction.getTimeout());
        handler.parameterize(stmt);
        return stmt;
    }

    /**
     * 简单类型的执行器，方法不需要做其他操作，因为Statement执行完SQL以后就关闭了
     *
     * @param isRollback    是否需要回滚
     * @return
     * @throws SQLException
     */
    @Override
    public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
        return Collections.emptyList();
    }

}
