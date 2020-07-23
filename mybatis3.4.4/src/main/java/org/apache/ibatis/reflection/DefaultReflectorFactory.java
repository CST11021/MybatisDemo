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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 获取Class对应的Reflector实例，Reflector封装了一些常用的反射方法
 */
public class DefaultReflectorFactory implements ReflectorFactory {

    /** 用于设置Class对应的Reflector实例是否可以缓存，默认可以缓存 */
    private boolean classCacheEnabled = true;

    /** 保存Class对应的Reflector实例 */
    private final ConcurrentMap<Class<?>, Reflector> reflectorMap = new ConcurrentHashMap<Class<?>, Reflector>();

    public DefaultReflectorFactory() {
    }

    /**
     * 是否缓存Reflector实例
     *
     * @return
     */
    @Override
    public boolean isClassCacheEnabled() {
        return classCacheEnabled;
    }

    /**
     * 设置Class对应的Reflector实例是否可以缓存
     *
     * @param classCacheEnabled
     */
    @Override
    public void setClassCacheEnabled(boolean classCacheEnabled) {
        this.classCacheEnabled = classCacheEnabled;
    }

    /**
     * 获取type对应的Reflector实例
     *
     * @param type
     * @return
     */
    @Override
    public Reflector findForClass(Class<?> type) {
        if (classCacheEnabled) {
            // synchronized (type) removed see issue #461
            Reflector cached = reflectorMap.get(type);
            if (cached == null) {
                cached = new Reflector(type);
                reflectorMap.put(type, cached);
            }
            return cached;
        } else {
            return new Reflector(type);
        }
    }

}
