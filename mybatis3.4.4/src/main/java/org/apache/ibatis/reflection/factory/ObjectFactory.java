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
package org.apache.ibatis.reflection.factory;

import java.util.List;
import java.util.Properties;

/**
 * Mybastic使用ObjectFactory来创建所有需要的实例，对应的实现类是：{@link DefaultObjectFactory}
 *
 * @author Clinton Begin
 */
public interface ObjectFactory {

    /**
     * 设置锁配置的属性
     *
     * @param properties configuration properties
     */
    void setProperties(Properties properties);

    /**
     * 使用默认构造器创建一个 T 的实例
     *
     * @param type Object type
     * @return
     */
    <T> T create(Class<T> type);

    /**
     * 使用自定义的构造器和参数来创建一个 T 实例
     *
     * @param type Object type
     * @param constructorArgTypes Constructor argument types
     * @param constructorArgs Constructor argument values
     * @return
     */
    <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

    /**
     * 判断 T 是否为集合类型
     *
     * Returns true if this object can have a set of other objects.
     * It's main purpose is to support non-java.util.Collection objects like Scala collections.
     *
     * @param type Object type
     * @return whether it is a collection or not
     * @since 3.1.0
     */
    <T> boolean isCollection(Class<T> type);

}
