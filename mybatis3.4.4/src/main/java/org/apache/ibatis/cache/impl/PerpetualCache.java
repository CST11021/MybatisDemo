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
package org.apache.ibatis.cache.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;

/**
 * 1.一级缓存: 基于PerpetualCache 的 HashMap本地缓存，其存储作用域为 Session，当 Session flush 或 close 之后，该Session中的所有 Cache 就将清空。
 * 2.二级缓存与一级缓存其机制相同，默认也是采用 PerpetualCache，HashMap存储，不同在于其存储作用域为 Mapper(Namespace)，并且可自定义存储源，如 Ehcache。
 * 3.对于缓存数据更新机制，当某一个作用域(一级缓存Session/二级缓存Namespaces)的进行了 C/U/D 操作后，默认该作用域下所有 select 中的缓存将被clear。
 *
 * 二级缓存补充说明
 *
 * 　　1. 映射语句文件中的所有select语句将会被缓存。
 * 　　2. 映射语句文件中的所有insert，update和delete语句会刷新缓存。
 * 　　3. 缓存会使用Least Recently Used（LRU，最近最少使用的）算法来收回。
 * 　　4. 缓存会根据指定的时间间隔来刷新。
 * 　　5. 缓存会存储1024个对象
 *
 * cache标签常用属性：
 *
 *     <cache
 *         eviction="FIFO"  <!--回收策略为先进先出-->
 *         flushInterval="60000" <!--自动刷新时间60s-->
 *         size="512" <!--最多缓存512个引用对象-->
 *         readOnly="true"/> <!--只读-->
 *
 * @author Clinton Begin
 */
public class PerpetualCache implements Cache {

    /**
     * 表示缓存的唯一标识
     */
    private String id;

    /**
     * 用来存放缓存数据
     */
    private Map<Object, Object> cache = new HashMap<Object, Object>();

    public PerpetualCache(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
    @Override
    public int getSize() {
        return cache.size();
    }
    @Override
    public void putObject(Object key, Object value) {
        cache.put(key, value);
    }
    @Override
    public Object getObject(Object key) {
        return cache.get(key);
    }
    @Override
    public Object removeObject(Object key) {
        return cache.remove(key);
    }
    @Override
    public void clear() {
        cache.clear();
    }
    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }




    @Override
    public boolean equals(Object o) {
        if (getId() == null) {
            throw new CacheException("Cache instances require an ID.");
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cache)) {
            return false;
        }

        Cache otherCache = (Cache) o;
        return getId().equals(otherCache.getId());
    }
    @Override
    public int hashCode() {
        if (getId() == null) {
            throw new CacheException("Cache instances require an ID.");
        }
        return getId().hashCode();
    }

}
