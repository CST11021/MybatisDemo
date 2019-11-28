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
 * 将为每个名称空间创建一个缓存实例。
 * 缓存实现必须有一个将缓存id作为字符串参数接收的构造函数。
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
 *
 * 1.FIFOCache：先进先出算法 回收策略，装饰类，内部维护了一个队列，来保证FIFO，一旦超出指定的大小，则从队列中获取Key并从被包装的Cache中移除该键值对。
 * 2.LoggingCache：输出缓存命中的日志信息,如果开启了DEBUG模式，则会输出命中率日志。
 * 3.LruCache：最近最少使用算法，缓存回收策略,在内部保存一个LinkedHashMap
 * 4.ScheduledCache：定时清空Cache，但是并没有开始一个定时任务，而是在使用Cache的时候，才去检查时间是否到了。
 * 5.SerializedCache：序列化功能，将值序列化后存到缓存中。该功能用于缓存返回一份实例的Copy，用于保存线程安全。
 * 6.SoftCache：基于软引用实现的缓存管理策略,软引用回收策略，软引用只有当内存不足时才会被垃圾收集器回收
 * 7.SynchronizedCache：同步的缓存装饰器，用于防止多线程并发访问
 * 8.PerpetualCache 永久缓存，一旦存入就一直保持，内部就是一个HashMap
 * 9.WeakCache：基于弱引用实现的缓存管理策略
 * 10.TransactionalCache 事务缓存，一次性存入多个缓存，移除多个缓存
 * 11.BlockingCache 可阻塞的缓存,内部实现是ConcurrentHashMap
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