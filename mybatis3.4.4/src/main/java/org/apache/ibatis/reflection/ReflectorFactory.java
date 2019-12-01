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
package org.apache.ibatis.reflection;

/**
 * Reflector工厂，用于获取Class对应的Reflector实例，默认实现带有缓存机制
 */
public interface ReflectorFactory {

    /**
     * 是否缓存Reflector实例
     *
     * @return
     */
    boolean isClassCacheEnabled();

    /**
     * 设置Class对应的Reflector实例是否可以缓存
     *
     * @param classCacheEnabled
     */
    void setClassCacheEnabled(boolean classCacheEnabled);

    /**
     * 获取type对应的Reflector实例
     *
     * @param type
     * @return
     */
    Reflector findForClass(Class<?> type);

}