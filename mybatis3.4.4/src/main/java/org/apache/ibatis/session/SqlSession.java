/**
 *    Copyright 2009-2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.session;

import java.io.Closeable;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;

/**
 * The primary Java interface for working with MyBatis.
 * 使用MyBatis主要java接口
 * Through this interface you can execute commands, get mappers and manage transactions.
 * 通过这个接口可以执行命令，得到映射和管理事务。
 *
 * @author Clinton Begin
 */

/*
Mapper执行的过程是通过Executor、StatementHandler、ParameterHandler和ResultHandler来完成数据库操作和结果返回。

  Executor代表执行，由它来调用StatementHandler、ParameterHandler、ResultHandler等来执行对应的SQL
  StatementHandler的作用是使用数据库的Statement(PreparedStatement)执行操作，它是四大对象的核心，起到承上启下的作用。
  ParameterHandler用于SQL对参数的处理
  ResultHandler是进行最后数据集（ResultSet）的封装返回处理的
 */
public interface SqlSession extends Closeable {

  // 返回一条结果集，statement表示要调用的接口方法的全限定名
  <T> T selectOne(String statement);
  <T> T selectOne(String statement, Object parameter);

  // 返回多条结果集
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

  // Flushes batch statements.
  List<BatchResult> flushStatements();

  // Closes the session
  @Override
  void close();

  // Clears local session cache
  void clearCache();

  // Retrieves current configuration
  Configuration getConfiguration();

  // Retrieves a mapper.
  <T> T getMapper(Class<T> type);

  // Retrieves inner database connection
  Connection getConnection();
}
