/**
 * Copyright 2009-2017 the original author or authors.
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

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import com.whz.entity.Employeer;
import org.apache.ibatis.lang.UsesJava7;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
//
// MapperProxy实现了InvocationHandler接口，所以这里使用的是 JDK 动态代理
public class MapperProxy<T> implements InvocationHandler, Serializable {

    private static final long serialVersionUID = -6424540398559729838L;
    private final SqlSession sqlSession;
    private final Class<T> mapperInterface;
    private final Map<Method, MapperMethod> methodCache;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }


    // 调用代理类对象的指定方法
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            // method.getDeclaringClass()方法返回表示声明由此Method对象表示的方法的类的Class对象
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            }
            // 判断该方法是否被default修饰
            else if (isDefaultMethod(method)) {
                // 如下，SqlSession 执行数据操作有如下三种方式，当使用第三种方式时（第一、二种方式mybatis不会生成代理实现
                // 类，而直接通过SqlSession只执行相应的增删改查操作），会到mybatis的mapper接口注册表中去获取
                // mapper接口对应的 MapperProxyFactory 然后动态生成一个代理的实现类，并调用代理类的相应方法执行增删改查操作。
                // 但是从java8开始，接口方法可以被default关键字修饰，如果是被default修饰的接口则调用相应的默认方法。
                // 第一种：Employeer employeer = session.selectOne("findEmployeerByID", 5);
                // 第二种：Employeer employeer = session.selectOne("com.whz.mapperinterface.IEmployeerMapper.findEmployeerByID", 5);
                // 第三种：Employeer employeer = session.getMapper(com.whz.mapperinterface.IEmployeerMapper.class).findEmployeerByID(5);
                return invokeDefaultMethod(proxy, method, args);
            }
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }

        // 首先缓存调用方法
        final MapperMethod mapperMethod = cachedMapperMethod(method);
        // 执行调用方法
        return mapperMethod.execute(sqlSession, args);
    }
    // 将调用的方法缓存起来，缓存key是根据 会话、方法和方法参数 决定的
    private MapperMethod cachedMapperMethod(Method method) {
        MapperMethod mapperMethod = methodCache.get(method);
        if (mapperMethod == null) {
            mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
            methodCache.put(method, mapperMethod);
        }
        return mapperMethod;
    }

    // 判断method方法是否有被default关键字修饰
    private boolean isDefaultMethod(Method method) {
        return (
                (method.getModifiers() & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC)
                && method.getDeclaringClass().isInterface();
    }

    @UsesJava7
    private Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
        final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                .getDeclaredConstructor(Class.class, int.class);
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        final Class<?> declaringClass = method.getDeclaringClass();
        return constructor
                .newInstance(declaringClass,
                        MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
                                | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC)
                .unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
    }

}
