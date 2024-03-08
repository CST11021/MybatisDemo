/**
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.session;

import java.sql.Connection;

/**
 * 创建{@link SqlSession}的实例，可以从已有连接对象Connection或数据源来创建.
 *
 * 1、当使用数据源创建会话时，需要指定事务隔离级别和是否自动提交。
 *      1.1 事务隔离级别可以不要用设置，当不设置的情况下，底层会根据不同的数据库厂商，自动设置默认的隔离级别
 *      1.2 是否自动提交在Mybastic中统一默认为否，实际的JDBC会依赖不同的数据库厂商实现
 *
 * 2、当使用连接对象创建会话时，此时不需要指定事务隔离级别和是否自动提交，因为创建连接实例时就已经指定了（创建数据库连接的底层实现也是根据数据源来创建的）
 *
 * @author Clinton Begin
 */
public interface SqlSessionFactory {

    //
    // 创建一个SqlSession实例：
    // 如果入参没有指定执行器类型，默认使用的执行器类型是：ExecutorType.SIMPLE
    // 创建session时，需要创建一个事务实例，此时需要指定：TransactionIsolationLevel（事务隔离级别）和autoCommit（是否自动提交）
    //

    /**
     * 根据数据源创建一个会话
     *
     * @return
     */
    SqlSession openSession();

    /**
     * 根据数据源创建一个会话，并指定是否自动提交
     *
     * @param autoCommit    是否自动提交事务，是否自动提交在Mybatis中统一默认为否
     * @return
     */
    SqlSession openSession(boolean autoCommit);

    /**
     * 从已存在的数据库连接实例，创建一个会话
     *
     * @param connection
     * @return
     */
    SqlSession openSession(Connection connection);

    /**
     * 根据事务隔离级别创建一个会话，默认使用的执行器类型是：ExecutorType.SIMPLE，默认设置为不自动提交
     *
     * @param level     事务隔离级别
     * @return
     */
    SqlSession openSession(TransactionIsolationLevel level);

    /**
     * 根据数据源创建一个会话
     *
     * @return
     */
    SqlSession openSession(ExecutorType execType);

    /**
     * 根据数据源创建一个会话
     *
     * @param execType      执行器类型
     * @param autoCommit    是否自动提交事务，是否自动提交在Mybastic中统一默认为否
     * @return
     */
    SqlSession openSession(ExecutorType execType, boolean autoCommit);

    /**
     * 根据数据源创建一个会话
     *
     * @return
     */
    SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level);

    /**
     * 从已存在的数据库连接实例，创建一个会话
     *
     * @param connection
     * @return
     */
    SqlSession openSession(ExecutorType execType, Connection connection);

    /**
     * 获取Mybastic配置
     *
     * @return
     */
    Configuration getConfiguration();

}
