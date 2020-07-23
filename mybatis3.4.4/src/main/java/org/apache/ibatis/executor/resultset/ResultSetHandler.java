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
package org.apache.ibatis.executor.resultset;

import org.apache.ibatis.cursor.Cursor;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 结果集处理器：Mybastic中使用该处理器处理返回的结果集
 *
 * @author Clinton Begin
 */
public interface ResultSetHandler {

    /**
     * 处理集合类型的结果集，JDBC代码如下：
     *
     *         Class.forName(driver);
     *         Connection connection = DriverManager.getConnection(url,username,password);
     *         Statement statement = connection.createStatement();
     *
     *         statement.execute("SELECT * FROM USER");
     *         ResultSet resultSet = statement.getResultSet();
     *         while(resultSet.next()){
     *             System.out.println("name: " + resultSet.getString("name"));
     *         }
     *
     * Statement 执行完SQL后，可以通过Statement#getResultSet()获取结果集ResultSet对象，该方法用于将ResultSet中的数据转成myBastic
     * 中配置的数据结构
     *
     * @param stmt
     * @param <E>
     * @return
     * @throws SQLException
     */
    <E> List<E> handleResultSets(Statement stmt) throws SQLException;

    /**
     * 处理游标类型的结果集
     *
     * @param stmt
     * @param <E>
     * @return
     * @throws SQLException
     */
    <E> Cursor<E> handleCursorResultSets(Statement stmt) throws SQLException;

    /**
     * 处理存储过程的结果集
     *
     * @param cs
     * @throws SQLException
     */
    void handleOutputParameters(CallableStatement cs) throws SQLException;

}
