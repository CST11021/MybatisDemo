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
package org.apache.ibatis.mapping;

import org.apache.ibatis.builder.InitializingObject;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.cache.decorators.*;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Clinton Begin
 */
public class CacheBuilder {
    /** 命名空间id */
    private String id;
    /** 表示缓存的实现类 */
    private Class<? extends Cache> implementation;
    /** 缓存实例的装饰列表 */
    private List<Class<? extends Cache>> decorators;
    /** 缓存最大大小 */
    private Integer size;
    /** 缓存自动清除时间间隔 */
    private Long clearInterval;
    /** 缓存是否只读 */
    private boolean readWrite;
    /** 对应缓存实例的属性，通过配置的方式注入 */
    private Properties properties;
    private boolean blocking;

    public CacheBuilder(String id) {
        this.id = id;
        this.decorators = new ArrayList<Class<? extends Cache>>();
    }

    /**
     * 设置缓存的实现类
     *
     * @param implementation
     * @return
     */
    public CacheBuilder implementation(Class<? extends Cache> implementation) {
        this.implementation = implementation;
        return this;
    }

    /**
     * 添加缓存装饰器实例
     *
     * @param decorator
     * @return
     */
    public CacheBuilder addDecorator(Class<? extends Cache> decorator) {
        if (decorator != null) {
            this.decorators.add(decorator);
        }
        return this;
    }

    /**
     * 设置缓存最大大小
     *
     * @param size
     * @return
     */
    public CacheBuilder size(Integer size) {
        this.size = size;
        return this;
    }

    /**
     * 设置缓存自动清除时间间隔
     *
     * @param clearInterval
     * @return
     */
    public CacheBuilder clearInterval(Long clearInterval) {
        this.clearInterval = clearInterval;
        return this;
    }

    /**
     * 设置缓存是否只读
     *
     * @param readWrite
     * @return
     */
    public CacheBuilder readWrite(boolean readWrite) {
        this.readWrite = readWrite;
        return this;
    }

    public CacheBuilder blocking(boolean blocking) {
        this.blocking = blocking;
        return this;
    }

    /**
     * 设置缓存实例的相关属性
     *
     * @param properties
     * @return
     */
    public CacheBuilder properties(Properties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * 创建一个缓存实例
     *
     * @return
     */
    public Cache build() {
        // 设置默认的缓存实现
        setDefaultImplementations();
        Cache cache = newBaseCacheInstance(implementation, id);
        setCacheProperties(cache);
        // issue #352, do not apply decorators to custom caches
        if (PerpetualCache.class.equals(cache.getClass())) {
            for (Class<? extends Cache> decorator : decorators) {
                cache = newCacheDecoratorInstance(decorator, cache);
                setCacheProperties(cache);
            }
            // 设置一些默认的装饰器，包括：ScheduledCache、SerializedCache、LoggingCache、SynchronizedCache和BlockingCache
            cache = setStandardDecorators(cache);
        } else if (!LoggingCache.class.isAssignableFrom(cache.getClass())) {
            cache = new LoggingCache(cache);
        }
        return cache;
    }

    /**
     * 设置默认的缓存实现类{@link PerpetualCache}
     */
    private void setDefaultImplementations() {
        if (implementation == null) {
            implementation = PerpetualCache.class;
            if (decorators.isEmpty()) {
                decorators.add(LruCache.class);
            }
        }
    }

    /**
     * 设置一些默认的装饰器，包括：ScheduledCache、SerializedCache、LoggingCache、SynchronizedCache和BlockingCache
     *
     * @param cache
     * @return
     */
    private Cache setStandardDecorators(Cache cache) {
        try {
            MetaObject metaCache = SystemMetaObject.forObject(cache);
            // 设置缓存大小
            if (size != null && metaCache.hasSetter("size")) {
                metaCache.setValue("size", size);
            }

            // 设置缓存刷新时间
            if (clearInterval != null) {
                cache = new ScheduledCache(cache);
                ((ScheduledCache) cache).setClearInterval(clearInterval);
            }

            // ???
            if (readWrite) {
                cache = new SerializedCache(cache);
            }

            cache = new LoggingCache(cache);
            cache = new SynchronizedCache(cache);
            if (blocking) {
                cache = new BlockingCache(cache);
            }
            return cache;
        } catch (Exception e) {
            throw new CacheException("Error building standard cache decorators.  Cause: " + e, e);
        }
    }

    /**
     * 设置缓存实例的属性，例如，配置一个BlockingCache类型的缓存，我们可以设置它独有timeout属性
     * <cache type="BLOCKING"...>
     *     <property name="timeout" value="1000"/>
     * </cache>
     *
     * @param cache
     */
    private void setCacheProperties(Cache cache) {
        if (properties != null) {
            MetaObject metaCache = SystemMetaObject.forObject(cache);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String name = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (metaCache.hasSetter(name)) {
                    Class<?> type = metaCache.getSetterType(name);
                    if (String.class == type) {
                        metaCache.setValue(name, value);
                    } else if (int.class == type
                            || Integer.class == type) {
                        metaCache.setValue(name, Integer.valueOf(value));
                    } else if (long.class == type
                            || Long.class == type) {
                        metaCache.setValue(name, Long.valueOf(value));
                    } else if (short.class == type
                            || Short.class == type) {
                        metaCache.setValue(name, Short.valueOf(value));
                    } else if (byte.class == type
                            || Byte.class == type) {
                        metaCache.setValue(name, Byte.valueOf(value));
                    } else if (float.class == type
                            || Float.class == type) {
                        metaCache.setValue(name, Float.valueOf(value));
                    } else if (boolean.class == type
                            || Boolean.class == type) {
                        metaCache.setValue(name, Boolean.valueOf(value));
                    } else if (double.class == type
                            || Double.class == type) {
                        metaCache.setValue(name, Double.valueOf(value));
                    } else {
                        throw new CacheException("Unsupported property type for cache: '" + name + "' of type " + type);
                    }
                }
            }
        }

        if (InitializingObject.class.isAssignableFrom(cache.getClass())) {
            try {
                ((InitializingObject) cache).initialize();
            } catch (Exception e) {
                throw new CacheException("Failed cache initialization for '" +
                        cache.getId() + "' on '" + cache.getClass().getName() + "'", e);
            }
        }
    }

    /**
     * 根据id和缓存实例，创建一个新的缓存实例
     *
     * @param cacheClass
     * @param id
     * @return
     */
    private Cache newBaseCacheInstance(Class<? extends Cache> cacheClass, String id) {
        Constructor<? extends Cache> cacheConstructor = getBaseCacheConstructor(cacheClass);
        try {
            return cacheConstructor.newInstance(id);
        } catch (Exception e) {
            throw new CacheException("Could not instantiate cache implementation (" + cacheClass + "). Cause: " + e, e);
        }
    }

    /**
     * 获取缓存的构造器，必须有id入参的构造器
     *
     * @param cacheClass
     * @return
     */
    private Constructor<? extends Cache> getBaseCacheConstructor(Class<? extends Cache> cacheClass) {
        try {
            return cacheClass.getConstructor(String.class);
        } catch (Exception e) {
            throw new CacheException("Invalid base cache implementation (" + cacheClass + ").  " +
                    "Base cache implementations must have a constructor that takes a String id as a parameter.  Cause: " + e, e);
        }
    }

    /**
     * 基于现有的缓存实例，创建新的缓存实例，这里使用了装饰器模式
     *
     * @param cacheClass
     * @param base
     * @return
     */
    private Cache newCacheDecoratorInstance(Class<? extends Cache> cacheClass, Cache base) {
        Constructor<? extends Cache> cacheConstructor = getCacheDecoratorConstructor(cacheClass);
        try {
            return cacheConstructor.newInstance(base);
        } catch (Exception e) {
            throw new CacheException("Could not instantiate cache decorator (" + cacheClass + "). Cause: " + e, e);
        }
    }

    /**
     * 获取缓存的构造器，必须带有Cache入参的构造器，这里要要确保缓存可以被装饰
     *
     * @param cacheClass
     * @return
     */
    private Constructor<? extends Cache> getCacheDecoratorConstructor(Class<? extends Cache> cacheClass) {
        try {
            return cacheClass.getConstructor(Cache.class);
        } catch (Exception e) {
            throw new CacheException("Invalid cache decorator (" + cacheClass + ").  " +
                    "Cache decorators must have a constructor that takes a Cache instance as a parameter.  Cause: " + e, e);
        }
    }
}
