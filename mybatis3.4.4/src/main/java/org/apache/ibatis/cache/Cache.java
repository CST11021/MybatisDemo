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
package org.apache.ibatis.cache;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * 用于缓存提供程序的SPI。
 *
 * 将为每个名称空间创建一个缓存实例。
 *
 * 缓存实现必须有一个将缓存id作为字符串参数接收的构造函数。
 *
 * MyBatis将名称空间作为id传递给构造函数。
 *
 * <pre>
 * public MyCache(final String id) {
 *  if (id == null) {
 *    throw new IllegalArgumentException("Cache instances require an ID");
 *  }
 *  this.id = id;
 *  initialize();
 * }
 * </pre>
 *
 * @author Clinton Begin
 */
public interface Cache {

    /**
     * 获取缓存id，一般我们将命名空间作为缓存id，命名空间对应：<mapper namespace="com.whz.mybatis.cache.IEmployeerMapper">配置
     *
     * @return The identifier of this cache
     */
    String getId();

    /**
     * 添加缓存
     *
     * @param key       可能是任何一个Object，但通常是{@link CacheKey}
     * @param value     缓存对象
     */
    void putObject(Object key, Object value);

    /**
     * 根据缓存key获取缓存
     *
     * @param key The key
     * @return The object stored in the cache.
     */
    Object getObject(Object key);

    /**
     * As of 3.3.0 this method is only called during a rollback for any previous value that was missing in the cache.
     * This lets any blocking cache to release the lock that may have previously put on the key.
     * A blocking cache puts a lock when a value is null and releases it when the value is back again.
     * This way other threads will wait for the value to be available instead of hitting the database.
     *
     * 从3.3.0开始，此方法仅在回滚缓存中丢失的任何先前值时调用。
     * 这让任何阻塞缓存释放锁，可能之前放在关键。
     * 当一个值为空时，一个阻塞缓存放置一个锁，当该值再次返回时释放它。
     * 这样，其他线程将等待该值可用，而不是命中数据库。
     *
     * @param key The key
     * @return Not used
     */
    Object removeObject(Object key);

    /**
     * Clears this cache instance
     */
    void clear();

    /**
     * Optional. This method is not called by the core.
     * 可选的。这个方法不是由核心调用的。
     *
     * @return 返回缓存中存储的元素数量(而不是容量)。
     */
    int getSize();

    /**
     * Optional. As of 3.2.6 this method is no longer called by the core.
     * 可选的。从3.2.6开始，这个方法不再被核心调用。
     *
     * Any locking needed by the cache must be provided internally by the cache provider.
     * 缓存所需的任何锁定都必须由缓存提供程序在内部提供。
     *
     * @return A ReadWriteLock
     */
    ReadWriteLock getReadWriteLock();

}