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
package org.apache.ibatis.binding;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.session.SqlSession;

/**
 * Mapper代理工厂：用于创建Mapper接口的实现
 * MapperRegistry 注册表注册mapper接口时并不是直接创建mapper接口的实现，而是在从注册表中获取的时候才通过动态代理的方式创建实现类
 * @author Lasse Voss
 */
public class MapperProxyFactory<T> {

    /** 表示mapper接口类型 */
    private final Class<T> mapperInterface;
    /** 保存mapper接口中的方法及对应的方法的代理实现 */
    private final Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<Method, MapperMethod>();


    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }


    /**
     * 根据SqlSession创建，Mapper接口的一个代理实例
     *
     * @param sqlSession
     * @return
     */
    public T newInstance(SqlSession sqlSession) {
        // 根据SqlSession、Mapper接口和方法缓存创建一个将被代理的Mapper对象
        // mybatis创建mapper接口实现类时，其实是将SqlSession织入到实现类中，
        final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
        return newInstance(mapperProxy);
    }
    @SuppressWarnings("unchecked")
    protected T newInstance(MapperProxy<T> mapperProxy) {
        // 使用JDK 动态代理，JDK 动态代理只对接口方法进行代理，这里是对mapperInterface接口的所有方法进行代理
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }



    // getter ...
    public Class<T> getMapperInterface() {
        return mapperInterface;
    }
    public Map<Method, MapperMethod> getMethodCache() {
        return methodCache;
    }


}
