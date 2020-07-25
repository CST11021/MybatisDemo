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

import javax.sql.DataSource;

import org.apache.ibatis.transaction.TransactionFactory;

/**
 * 表示 <environments> 标签内的配置，主要是事务和数据源相关的配置， <environments> 可以配置多个环境的数据源，如：
 * <environments default="development">
 * 		<environment id="development"> ... </environment>
 * 		<environment id="daily"> ... </environment>
 * 		<environment id="pre"> ... </environment>
 * 		...
 * 	</environments>
 *
 * 一个Environment实例表示上面是一个<environment>标签，表示单个环境的实例，mybastic启动的时候，会根据<environments default="development">
 * 的default属性，选择对应的<environment>标签配置，从而加载相应环境的数据源
 *
 * @author Clinton Begin
 */
public final class Environment {

    /**
     * 用于表示当前的环境，对应<environment>标签的id配置
     */
    private final String id;

    /**
     * 事务工厂
     */
    private final TransactionFactory transactionFactory;

    /**
     * 数据源
     */
    private final DataSource dataSource;

    public Environment(String id, TransactionFactory transactionFactory, DataSource dataSource) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' must not be null");
        }
        if (transactionFactory == null) {
            throw new IllegalArgumentException("Parameter 'transactionFactory' must not be null");
        }
        this.id = id;
        if (dataSource == null) {
            throw new IllegalArgumentException("Parameter 'dataSource' must not be null");
        }
        this.transactionFactory = transactionFactory;
        this.dataSource = dataSource;
    }

    public static class Builder {
        private String id;
        private TransactionFactory transactionFactory;
        private DataSource dataSource;

        public Builder(String id) {
            this.id = id;
        }
        public Builder transactionFactory(TransactionFactory transactionFactory) {
            this.transactionFactory = transactionFactory;
            return this;
        }
        public Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public String id() {
            return this.id;
        }
        public Environment build() {
            return new Environment(this.id, this.transactionFactory, this.dataSource);
        }

    }

    // getter ...
    public String getId() {
        return this.id;
    }
    public TransactionFactory getTransactionFactory() {
        return this.transactionFactory;
    }
    public DataSource getDataSource() {
        return this.dataSource;
    }

}
