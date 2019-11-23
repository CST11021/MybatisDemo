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

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * BaseStatementHandler 的主要作用是提供一个prepare()方法，创建一个Statement对象，具体的实现通过instantiateStatement()模板方法方法留给了子类实现
 *
 * @author Clinton Begin
 */
public abstract class BaseStatementHandler implements StatementHandler {

    /** Mybastic配置 */
    protected final Configuration configuration;

    protected final ObjectFactory objectFactory;
    /** 类型转换处理器的注册表 */
    protected final TypeHandlerRegistry typeHandlerRegistry;

    /** 结果集处理器 */
    protected final ResultSetHandler resultSetHandler;
    /** 参数处理器 */
    protected final ParameterHandler parameterHandler;
    /** 执行器 */
    protected final Executor executor;
    protected final MappedStatement mappedStatement;
    protected final RowBounds rowBounds;

    /** 封装了本次操作要执行的SQL */
    protected BoundSql boundSql;

    protected BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.rowBounds = rowBounds;

        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.objectFactory = configuration.getObjectFactory();

        // 如果还没有创建好boundSql对象，则
        // issue #435, get the key before calculating the statement
        if (boundSql == null) {
            generateKeys(parameterObject);
            boundSql = mappedStatement.getBoundSql(parameterObject);
        }

        this.boundSql = boundSql;

        this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
        this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowBounds, parameterHandler, resultHandler, boundSql);
    }

    /**
     * 创建一个Statement 对象，实例化的动作留给子类实现，根据子类创建不同的Statement实例，比如：PrepareStatementHandler
     * 使用 connection.prepareStatement(sql)的方式创建；SimpleStatementHandler 通过 connection.createStatement() 方式创建
     *
     * @param connection
     * @param transactionTimeout
     * @return
     * @throws SQLException
     */
    @Override
    public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {
        // 在上下文里设置本次要执行的SQL
        ErrorContext.instance().sql(boundSql.getSql());
        Statement statement = null;
        try {
            // 创建一个 java.sql.Statement 实例
            statement = instantiateStatement(connection);
            // 设置事务超时时间
            setStatementTimeout(statement, transactionTimeout);
            // 设置fetchSize
            setFetchSize(statement);
            return statement;
        } catch (SQLException e) {
            closeStatement(statement);
            throw e;
        } catch (Exception e) {
            closeStatement(statement);
            throw new ExecutorException("Error preparing statement.  Cause: " + e, e);
        }
    }

    @Override
    public BoundSql getBoundSql() {
        return boundSql;
    }

    @Override
    public ParameterHandler getParameterHandler() {
        return parameterHandler;
    }

    /**
     * 如果<setting>标签中有设置 useGeneratedKeys 为 true（默认为false），则调用该方法获取一个 KeyGenerator 的实现
     *
     * @param parameter
     */
    protected void generateKeys(Object parameter) {
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        ErrorContext.instance().store();
        // statement 在执行sql 前，会调用该方法，自动生成一个主键
        keyGenerator.processBefore(executor, mappedStatement, null, parameter);
        ErrorContext.instance().recall();
    }

    /**
     * 有子类决定使用哪种 Statement 实现
     *
     * @param connection
     * @return
     * @throws SQLException
     */
    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

    /**
     * 设置事务超时时间
     *
     * @param stmt
     * @param transactionTimeout
     * @throws SQLException
     */
    protected void setStatementTimeout(Statement stmt, Integer transactionTimeout) throws SQLException {
        Integer queryTimeout = null;
        if (mappedStatement.getTimeout() != null) {
            queryTimeout = mappedStatement.getTimeout();
        } else if (configuration.getDefaultStatementTimeout() != null) {
            queryTimeout = configuration.getDefaultStatementTimeout();
        }
        if (queryTimeout != null) {
            stmt.setQueryTimeout(queryTimeout);
        }
        StatementUtil.applyTransactionTimeout(stmt, queryTimeout, transactionTimeout);
    }

    /**
     * 设置 Statement 的 fetchSize 属性
     *
     * @param stmt
     * @throws SQLException
     */
    protected void setFetchSize(Statement stmt) throws SQLException {
        Integer fetchSize = mappedStatement.getFetchSize();
        if (fetchSize != null) {
            stmt.setFetchSize(fetchSize);
            return;
        }

        Integer defaultFetchSize = configuration.getDefaultFetchSize();
        if (defaultFetchSize != null) {
            stmt.setFetchSize(defaultFetchSize);
        }
    }

    /**
     * 关闭Statement
     *
     * @param statement
     */
    protected void closeStatement(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            //ignore
        }
    }

}
