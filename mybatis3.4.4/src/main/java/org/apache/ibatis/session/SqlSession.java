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
package org.apache.ibatis.session;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;

import java.io.Closeable;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * The primary Java interface for working with MyBatis.
 * 使用MyBatis主要java接口
 * Through this interface you can execute commands, get mappers and manage transactions.
 * 通过这个接口可以执行命令，得到映射和管理事务。
 *
 *
 *
 * Mapper执行的过程是通过Executor、StatementHandler、ParameterHandler和ResultHandler来完成数据库操作和结果返回。
 *
 *   Executor代表执行，由它来调用StatementHandler、ParameterHandler、ResultHandler等来执行对应的SQL
 *   StatementHandler的作用是使用数据库的Statement(PreparedStatement)执行操作，它是四大对象的核心，起到承上启下的作用。
 *   ParameterHandler用于SQL对参数的处理
 *   ResultHandler是进行最后数据集（ResultSet）的封装返回处理的
 *
 * @author Clinton Begin
 */
public interface SqlSession extends Closeable {

    // 声明：所有的入参statement表示select、insert、update、delete的语句的id

    <T> T selectOne(String statement);
    <T> T selectOne(String statement, Object parameter);

    <E> List<E> selectList(String statement);
    <E> List<E> selectList(String statement, Object parameter);
    <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds);

    <K, V> Map<K, V> selectMap(String statement, String mapKey);
    <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey);
    <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds);

    <T> Cursor<T> selectCursor(String statement);
    <T> Cursor<T> selectCursor(String statement, Object parameter);
    <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds);

    void select(String statement, Object parameter, ResultHandler handler);
    void select(String statement, ResultHandler handler);
    void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler);

    int insert(String statement);
    int insert(String statement, Object parameter);

    int update(String statement);
    int update(String statement, Object parameter);

    int delete(String statement);
    int delete(String statement, Object parameter);

    void commit();
    void commit(boolean force);
    void rollback();
    void rollback(boolean force);



    /**
     * 清空Statement实例：
     * 比如，批量执行器，则会将多个Statement实例缓存起来，在该方法中一起执行，执行完成后则关闭和清空相关的Statement实例；
     * 再比如，复用执行器，会将执行过的Statement实例缓存起来，方便下次在里利用；
     *
     * @return
     */
    List<BatchResult> flushStatements();

    /**
     * 关闭session
     */
    @Override
    void close();

    /**
     * 清除session缓存（mybatis一级缓存）
     */
    void clearCache();

    /**
     * Retrieves current configuration
     *
     * @return
     */
    Configuration getConfiguration();

    /**
     * 根据 type 获取Mapper接口
     *
     * @param type
     * @param <T>
     * @return
     */
    <T> T getMapper(Class<T> type);

    /**
     * Retrieves inner database connection
     *
     * @return
     */
    Connection getConnection();
}
