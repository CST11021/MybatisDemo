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
package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 简单的阻塞缓存装饰器实现
 *
 * 简单和低效的EhCache的BlockingCache装饰器。
 * 当在缓存中找不到元素时，它设置一个对缓存key的锁。
 * 通过这种方式，其他线程将一直等待，直到该元素被填满，而不是到达数据库。
 *
 * @author Eduardo Macarron
 *
 */
public class BlockingCache implements Cache {

    /** 被装饰的缓存实例 */
    private final Cache delegate;

    /**
     * 锁的超时时间
     */
    private long timeout;

    /** Map<缓存Key, 条件锁> */
    private final ConcurrentHashMap<Object, ReentrantLock> locks;

    public BlockingCache(Cache delegate) {
        this.delegate = delegate;
        this.locks = new ConcurrentHashMap<Object, ReentrantLock>();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public void putObject(Object key, Object value) {
        try {
            delegate.putObject(key, value);
        } finally {
            releaseLock(key);
        }
    }

    /**
     * 从缓存获取数据的时候进行加锁，获取完数据以后再释放锁
     *
     * @param key The key
     * @return
     */
    @Override
    public Object getObject(Object key) {
        acquireLock(key);
        Object value = delegate.getObject(key);
        // 注意value为空的时候不释放锁，当有value放到缓存时，才释放锁，通过这种方式使得其他线程将一直等待，直到该元素被填满，而不是到达数据库
        if (value != null) {
            releaseLock(key);
        }
        return value;
    }

    /**
     * 移除缓存的时候，也会释放锁
     *
     * @param key The key
     * @return
     */
    @Override
    public Object removeObject(Object key) {
        // despite of its name, this method is called only to release locks
        releaseLock(key);
        return null;
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    /**
     * 根据缓存key获取一个ReentrantLock实例
     *
     * @param key
     * @return
     */
    private ReentrantLock getLockForKey(Object key) {
        ReentrantLock lock = new ReentrantLock();
        // 缺少的时候，放进去，并返回之前的
        ReentrantLock previous = locks.putIfAbsent(key, lock);
        return previous == null ? lock : previous;
    }

    /**
     * 从缓存获取一个数据的时候，会调用该方法，让这个key获取一个锁，并设置超时时间
     *
     * @param key
     */
    private void acquireLock(Object key) {
        Lock lock = getLockForKey(key);
        if (timeout > 0) {
            try {
                boolean acquired = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
                if (!acquired) {
                    throw new CacheException("Couldn't get a lock in " + timeout + " for the key " + key + " at the cache " + delegate.getId());
                }
            } catch (InterruptedException e) {
                throw new CacheException("Got interrupted while trying to acquire lock for key " + key, e);
            }
        } else {
            lock.lock();
        }
    }

    /**
     * 释放锁
     *
     * @param key
     */
    private void releaseLock(Object key) {
        ReentrantLock lock = locks.get(key);
        // isHeldByCurrentThread()当前线程是否持有这个锁
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}