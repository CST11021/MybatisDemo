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
package org.apache.ibatis.executor.keygen;

import java.sql.Statement;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * 当mybatis中<setting> 设置了，允许 JDBC 支持自动生成主键，会创建该接口的一个实例
 * 在平时开发的时候经常会有这样的需求，插入数据返回主键，或者插入数据之前需要获取主键，这样的需求在 mybatis 中也是支持的，其中主要的逻辑部分就在 KeyGenerator 中，下面是他的类图：
 *
 * 其中：
 *
 * NoKeyGenerator：默认空实现，不需要对主键单独处理；
 * Jdbc3KeyGenerator：主要用于数据库的自增主键，比如 MySQL、PostgreSQL；
 * SelectKeyGenerator：主要用于数据库不支持自增主键的情况，比如 Oracle、DB2；
 *
 * @author Clinton Begin
 */
public interface KeyGenerator {

    /**
     * statement 执行sql 前调用该方法，自动生成一个主键
     *
     * @param executor
     * @param ms
     * @param stmt
     * @param parameter
     */
    void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

    /**
     *
     *
     * @param executor
     * @param ms
     * @param stmt
     * @param parameter
     */
    void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

}
