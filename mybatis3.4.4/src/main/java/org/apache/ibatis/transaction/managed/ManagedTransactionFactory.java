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
package org.apache.ibatis.transaction.managed;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

/**
 * Creates {@link ManagedTransaction} instances.
 *
 * @author Clinton Begin
 *
 * @see ManagedTransaction
 */
public class ManagedTransactionFactory implements TransactionFactory {

    private boolean closeConnection = true;

    @Override
    public void setProperties(Properties props) {
        if (props != null) {
            String closeConnectionProperty = props.getProperty("closeConnection");
            if (closeConnectionProperty != null) {
                closeConnection = Boolean.valueOf(closeConnectionProperty);
            }
        }
    }

    /**
     * 从已存在的连接实例，创建一个事务实例
     *
     * @param conn Existing database connection
     * @return
     */
    @Override
    public Transaction newTransaction(Connection conn) {
        return new ManagedTransaction(conn, closeConnection);
    }

    /**
     * 根据数据源，事务隔离级别创建一个事务管理器
     *
     * @param ds
     * @param level         事务隔离级别
     * @param autoCommit    是否自动提交
     * @return
     */
    @Override
    public Transaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
        // Silently ignores autocommit and isolation level, as managed transactions are entirely
        // controlled by an external manager.  It's silently ignored so that
        // code remains portable between managed and unmanaged configurations.
        return new ManagedTransaction(ds, level, closeConnection);
    }
}
