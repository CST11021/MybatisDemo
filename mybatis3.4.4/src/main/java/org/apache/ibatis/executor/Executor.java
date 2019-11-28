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
package org.apache.ibatis.executor;

import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.transaction.Transaction;

/**
 * 执行器Executor起到了至关重要的作用。它是一个真正执行java和数据库交互的东西。在MyBatis中存在三种执行器。我们可以在配置文件进行配置。
 *
 * SIMPLE，简单执行，它是默认的执行器
 * REUSE，是一种执行重用预处理语句。
 * BATCH，执行器重用语句和批量更新，它是针对批量专用的执行器
 *
 *
 * 执行进行数据操作时，需要准备好：MappedStatement、parameter、rowBounds以及一个ResultHandler对象
 *
 * @author Clinton Begin
 */
public interface Executor {

    /** 表示一个null的查询结果 */
    ResultHandler NO_RESULT_HANDLER = null;

    // --------------------
    // Mybatis 的所有 更新、插入和删除 操作，最终都将调用该方法来操作数据库
    // --------------------

    int update(MappedStatement ms, Object parameter) throws SQLException;

    // --------------------
    // 查询方法最终调用以下三个方法的其中一个来操作数据库
    // --------------------

    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException;
    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;
    <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException;


    /**
     * 清空Statement实例：
     * 比如，批量执行器，则会将多个Statement实例缓存起来，在该方法中一起执行，执行完成后则关闭和清空相关的Statement实例；
     * 再比如，复用执行器，会将执行过的Statement实例缓存起来，方便下次在里利用；
     *
     * @return
     * @throws SQLException
     */
    List<BatchResult> flushStatements() throws SQLException;

    /**
     * 提交事务：执行器的事务提交实现委托给了{@link Transaction}去实现，执行器层的事务提交是对Statement实例缓存和查询结果缓存做了一层封装，保证了事务提交前都能清楚Statement缓存和Mybastic的一级缓存
     *
     * @param required      是否执行事务提交操作
     * @throws SQLException
     */
    void commit(boolean required) throws SQLException;

    /**
     * 回滚事务：执行器的事务回滚实现委托给了{@link Transaction}去实现，执行器层的事务回滚是对Statement实例缓存和查询结果缓存做了一层封装，保证了事务回滚前都能清除Statement缓存和Mybastic的一级缓存
     *
     * @param required
     * @throws SQLException
     */
    void rollback(boolean required) throws SQLException;

    /**
     * 创建一个CacheKey对象
     *
     * @param ms                    MappedStatement
     * @param parameterObject       本次执行的参数
     * @param rowBounds             分页信息
     * @param boundSql              本次执行的SQL相关信息
     * @return
     */
    CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);

    /**
     * 判断该缓存key是否存在缓存
     *
     * @param ms
     * @param key
     * @return
     */
    boolean isCached(MappedStatement ms, CacheKey key);

    /**
     * 用于清除会话缓存，当执行器执行更新操作前，会调用该方法清除会话缓存；也可以通过{@link SqlSession#clearCache()}方法手动清除缓存
     */
    void clearLocalCache();

    /**
     * 结果集处理器在每次处理执行结果的时候会来调该方法，缓存查询结果
     *
     * @param ms                本次执行MappedStatement
     * @param resultObject      结果集对应的MetaObject
     * @param property
     * @param key               缓存key
     * @param targetType
     */
    void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType);

    /**
     * 获取一个事务实例
     *
     * @return
     */
    Transaction getTransaction();

    /**
     * 关闭执行器，会话关闭时会关闭对应的执行器
     *
     * @param forceRollback 关闭会话时，是否强制回滚事务，为false时，事务会默认提交
     */
    void close(boolean forceRollback);

    /**
     * 执行器是否已经关闭
     *
     * @return
     */
    boolean isClosed();

    /**
     * 设置保存执行器，{@link BatchExecutor}、{@link ReuseExecutor}和{@link SimpleExecutor}会调用该方法，一般设置的包装执行器是缓存执行器，缓存执行器在最外层
     *
     * @param executor
     */
    void setExecutorWrapper(Executor executor);

}
