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
package org.apache.ibatis.session.defaults;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.result.DefaultMapResultHandler;
import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

/**
 *
 * The default implementation for {@link SqlSession}.
 * Note that this class is not Thread-Safe.
 *
 * @author Clinton Begin
 */
public class DefaultSqlSession implements SqlSession {

    /** 表示配置信息 */
    private Configuration configuration;
    /** SqlSession最终都将委托执行器类执行数据库操作 */
    private Executor executor;
    /** 是否自动提交事务，是否自动提交在Mybastic中统一默认为否，创建会话的时候可以指定是否自动提交 */
    private boolean autoCommit;
    /** 当执行insert、update或delete操作时，该标识设置为true，直到事务提交或者回滚后才置为false */
    private boolean dirty;
    /** 当会话中使用游标的方式查询时，会将游标注册到该字段中，当会话关闭时，统一将游标注销，由于在一次会话中可能执行多次查询，所以这里是一个集合 */
    private List<Cursor<?>> cursorList;


    public DefaultSqlSession(Configuration configuration, Executor executor, boolean autoCommit) {
        this.configuration = configuration;
        this.executor = executor;
        this.dirty = false;
        this.autoCommit = autoCommit;
    }
    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this(configuration, executor, false);
    }


    /* ------------------ 一、内部实现委托给了执行的 update() 方法 ---------------------------------- */
    @Override
    public int insert(String statement) {
        return insert(statement, null);
    }
    @Override
    public int insert(String statement, Object parameter) {
        return update(statement, parameter);
    }
    @Override
    public int update(String statement) {
        return update(statement, null);
    }
    @Override
    public int delete(String statement) {
        return update(statement, null);
    }
    @Override
    public int delete(String statement, Object parameter) {
        return update(statement, parameter);
    }
    // 核心方法：最终调用执行器的 update()方法
    @Override
    public int update(String statement, Object parameter) {
        try {
            dirty = true;
            MappedStatement ms = configuration.getMappedStatement(statement);
            return executor.update(ms, wrapCollection(parameter));
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error updating database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }






    /* ------------------ 二、内部实现委托给了执行器的 query() 方法 ---------------------------------- */
    @Override
    public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
        return this.selectMap(statement, null, mapKey, RowBounds.DEFAULT);
    }
    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return this.selectMap(statement, parameter, mapKey, RowBounds.DEFAULT);
    }
    // 核心方法：查询的真正实现委托给了selectList方法，这里是对查询结果进步包装
    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
        final List<? extends V> list = selectList(statement, parameter, rowBounds);
        final DefaultMapResultHandler<K, V> mapResultHandler = new DefaultMapResultHandler<K, V>(mapKey, configuration.getObjectFactory(), configuration.getObjectWrapperFactory(), configuration.getReflectorFactory());
        final DefaultResultContext<V> context = new DefaultResultContext<V>();
        for (V o : list) {
            context.nextResultObject(o);
            mapResultHandler.handleResult(context);
        }
        return mapResultHandler.getMappedResults();
    }
    @Override
    public <T> T selectOne(String statement) {
        return this.<T>selectOne(statement, null);
    }
    @Override
    public <T> T selectOne(String statement, Object parameter) {
        // Popular vote was to return null on 0 results and throw exception on too many.
        List<T> list = this.<T>selectList(statement, parameter);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
        } else {
            return null;
        }
    }
    @Override
    public <E> List<E> selectList(String statement) {
        return this.selectList(statement, null);
    }
    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return this.selectList(statement, parameter, RowBounds.DEFAULT);
    }
    // 核心方法：最终调用执行器的 query()方法
    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        try {
            MappedStatement ms = configuration.getMappedStatement(statement);
            return executor.query(ms, wrapCollection(parameter), rowBounds, Executor.NO_RESULT_HANDLER);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }






    /* ------------------ 三、内部实现委托给了执行器的 query() 方法 ---------------------------------- */
    @Override
    public void select(String statement, Object parameter, ResultHandler handler) {
        select(statement, parameter, RowBounds.DEFAULT, handler);
    }
    @Override
    public void select(String statement, ResultHandler handler) {
        select(statement, null, RowBounds.DEFAULT, handler);
    }
    // 核心方法：最终调用执行器的 query()方法
    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        try {
            MappedStatement ms = configuration.getMappedStatement(statement);
            executor.query(ms, wrapCollection(parameter), rowBounds, handler);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }






    /* ------------------ 四、内部实现委托给了执行器的 queryCursor() 方法 ---------------------------------- */
    @Override
    public <T> Cursor<T> selectCursor(String statement) {
        return selectCursor(statement, null);
    }
    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter) {
        return selectCursor(statement, parameter, RowBounds.DEFAULT);
    }
    // 核心方法：最终调用执行器的 queryCursor()方法
    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds) {
        try {
            MappedStatement ms = configuration.getMappedStatement(statement);
            Cursor<T> cursor = executor.queryCursor(ms, wrapCollection(parameter), rowBounds);
            registerCursor(cursor);
            return cursor;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }


    /**
     * 提交事务，不强制提交
     */
    @Override
    public void commit() {
        commit(false);
    }
    /**
     * 提交事务
     *
     * @param force 是否强制提交
     */
    @Override
    public void commit(boolean force) {
        try {
            executor.commit(isCommitOrRollbackRequired(force));
            dirty = false;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error committing transaction.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }
    /**
     * 用于判断是否要求提交或回滚事务，如果是强制提交，则每次都返回true；
     * 否则如果是不自动提交，则每次指定完update、insert、delete操作后，该接口放回true；
     *
     * @param force
     * @return
     */
    private boolean isCommitOrRollbackRequired(boolean force) {
        // 如果未设置强制提交，则当是非自动提交并且是之前执行过写操作但未提交时，才提交事务
        return (!autoCommit && dirty) || force;
    }

    /**
     * 回滚事务，不强制回滚
     */
    @Override
    public void rollback() {
        rollback(false);
    }

    /**
     * 回滚事务，强制回滚
     *
     * @param force 是否强制回滚
     */
    @Override
    public void rollback(boolean force) {
        try {
            executor.rollback(isCommitOrRollbackRequired(force));
            dirty = false;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error rolling back transaction.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }


    @Override
    public List<BatchResult> flushStatements() {
        try {
            return executor.flushStatements();
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error flushing statements.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public void close() {
        try {
            executor.close(isCommitOrRollbackRequired(false));
            closeCursors();
            dirty = false;
        } finally {
            ErrorContext.instance().reset();
        }
    }

    /**
     * 关闭所有的游标，批量执行器执行完后通常都是返回多个游标实例
     */
    private void closeCursors() {
        if (cursorList != null && cursorList.size() != 0) {
            for (Cursor<?> cursor : cursorList) {
                try {
                    cursor.close();
                } catch (IOException e) {
                    throw ExceptionFactory.wrapException("Error closing cursor.  Cause: " + e, e);
                }
            }
            cursorList.clear();
        }
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 从 mapperRegistry 注册表中获取一个Mapper实例，如：
     * IEmployeerMapper iEmployeerMapper = sqlSession.getMapper(IEmployeerMapper.class);
     *
     * @param type
     * @param <T>
     * @return
     */
    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.<T>getMapper(type, this);
    }

    @Override
    public Connection getConnection() {
        try {
            return executor.getTransaction().getConnection();
        } catch (SQLException e) {
            throw ExceptionFactory.wrapException("Error getting a new connection.  Cause: " + e, e);
        }
    }

    /**
     * 清理会话缓存，直接委托执行器清除缓存
     */
    @Override
    public void clearCache() {
        executor.clearLocalCache();
    }

    /**
     * 注册游标实例
     *
     * @param cursor
     * @param <T>
     */
    private <T> void registerCursor(Cursor<T> cursor) {
        if (cursorList == null) {
            cursorList = new ArrayList<Cursor<?>>();
        }
        cursorList.add(cursor);
    }

    /**
     * 如果object是collection类型，则返回 StrictMap<"collection", object>;
     * 如果object是List类型，则返回 StrictMap<"list", object>;
     * 如果object是数组类型，则返回 StrictMap<"array", object>;
     * 否则，返回object
     *
     * @param object
     * @return 如果object是集合类型或者数组，则封装为一个StrictMap对象返回，否则直接object
     */
    private Object wrapCollection(final Object object) {
        if (object instanceof Collection) {
            StrictMap<Object> map = new StrictMap<Object>();
            map.put("collection", object);
            if (object instanceof List) {
                map.put("list", object);
            }
            return map;
        } else if (object != null && object.getClass().isArray()) {
            StrictMap<Object> map = new StrictMap<Object>();
            map.put("array", object);
            return map;
        }
        return object;
    }

    public static class StrictMap<V> extends HashMap<String, V> {
        private static final long serialVersionUID = -5741767162221585340L;
        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + this.keySet());
            }
            return super.get(key);
        }
    }

}
