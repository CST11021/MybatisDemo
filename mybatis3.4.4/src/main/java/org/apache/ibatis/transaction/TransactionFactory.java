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
package org.apache.ibatis.transaction;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.session.TransactionIsolationLevel;

/**
 * Creates {@link Transaction} instances.
 *
 * @author Clinton Begin
 */
public interface TransactionFactory {

    /**
     * 设置事务工厂自定义属性
     *
     * @param props
     */
    void setProperties(Properties props);

    /**
     * 从一个数据库连接实例（会话）创建一个事务，connection实例已经指定了事务隔离级别和是否自动提交
     *
     * @param conn Existing database connection
     * @return Transaction
     * @since 3.1.0
     */
    Transaction newTransaction(Connection conn);

    /**
     * 从数据源创建一个事务，根据数据源创建事务实例时需要指定事务隔离级别和是否自动提交
     *
     * @param dataSource    数据源
     * @param level         事务隔离级别
     * @param autoCommit    是否自动提交
     * @return Transaction
     * @since 3.1.0
     */
    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);

}
