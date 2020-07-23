/**
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.io;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * 用于查找某个类的类实例
 *
 *
 * <p>ResolverUtil用于查找例如：/a类路径中可用并满足任意条件的类.
 * 最常见的两个条件是，一个类实现/扩展了另一个类，或者用特定的注释对其进行了注释。但是，通过使用{@link Test}类，可以使用任意条件进行搜索。
 *
 * <p>A ClassLoader is used to locate all locations (directories and jar files) in the class
 * path that contain classes within certain packages, and then to load those classes and
 * check them. By default the ClassLoader returned by
 * {@code Thread.currentThread().getContextClassLoader()} is used, but this can be overridden
 * by calling {@link #setClassLoader(ClassLoader)} prior to invoking any of the {@code find()}
 * methods.</p>
 *
 * <p>General searches are initiated by calling the
 * {@link #find(org.apache.ibatis.io.ResolverUtil.Test, String)} ()} method and supplying
 * a package name and a Test instance. This will cause the named package <b>and all sub-packages</b>
 * to be scanned for classes that meet the test. There are also utility methods for the common
 * use cases of scanning multiple packages for extensions of particular classes, or classes
 * annotated with a specific annotation.</p>
 *
 * <p>The standard usage pattern for the ResolverUtil class is as follows:</p>
 *
 * <pre>
 * ResolverUtil&lt;ActionBean&gt; resolver = new ResolverUtil&lt;ActionBean&gt;();
 * resolver.findImplementation(ActionBean.class, pkg1, pkg2);
 * resolver.find(new CustomTest(), pkg1);
 * resolver.find(new CustomTest(), pkg2);
 * Collection&lt;ActionBean&gt; beans = resolver.getClasses();
 * </pre>
 *
 * @author Tim Fennell
 */
public class ResolverUtil<T> {

    private static final Log log = LogFactory.getLog(ResolverUtil.class);

    /** ResolverUtil产生的一个结果集 */
    private Set<Class<? extends T>> matches = new HashSet<Class<? extends T>>();
    private ClassLoader classloader;

    /**
     * 定义一个接口，用于确定它们是否被包含在由resolverutil产生的结果
     */
    public static interface Test {
        /**
         * 将多次调用候选的class，如果这个方法返回true，则表示这个Class可以注册到matches
         *
         * @param type
         * @return
         */
        boolean matches(Class<?> type);
    }

    /**
     * 如果type可以是parent的实例，则返回true
     */
    public static class IsA implements Test {

        private Class<?> parent;

        public IsA(Class<?> parentType) {
            this.parent = parentType;
        }

        /**
         * 如果type可以是parent的实例，则返回true
         *
         * @param type
         * @return
         */
        @Override
        public boolean matches(Class<?> type) {
            return type != null && parent.isAssignableFrom(type);
        }

        @Override
        public String toString() {
            return "is assignable to " + parent.getSimpleName();
        }
    }

    /**
     * 同IsA（针对注解类）
     */
    public static class AnnotatedWith implements Test {
        private Class<? extends Annotation> annotation;

        public AnnotatedWith(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
        }

        @Override
        public boolean matches(Class<?> type) {
            return type != null && type.isAnnotationPresent(annotation);
        }

        @Override
        public String toString() {
            return "annotated with @" + annotation.getSimpleName();
        }
    }


    /**
     * 查找指定包路径下的所有类，如果是parent的类实例，则将其添加到matches
     *
     * @param parent
     * @param packageNames
     * @return
     */
    public ResolverUtil<T> findImplementations(Class<?> parent, String... packageNames) {
        if (packageNames == null) {
            return this;
        }

        Test test = new IsA(parent);
        for (String pkg : packageNames) {
            find(test, pkg);
        }

        return this;
    }

    public ResolverUtil<T> findAnnotated(Class<? extends Annotation> annotation, String... packageNames) {
        if (packageNames == null) {
            return this;
        }

        Test test = new AnnotatedWith(annotation);
        for (String pkg : packageNames) {
            find(test, pkg);
        }

        return this;
    }

    /**
     * 查找这packageName包路径下所有的class，如果匹配这个test则将其注册到matches
     *
     * @param test
     * @param packageName
     * @return
     */
    public ResolverUtil<T> find(Test test, String packageName) {
        String path = getPackagePath(packageName);

        try {
            // 返回该路径所有的包名和.class文件
            List<String> children = VFS.getInstance().list(path);
            for (String child : children) {
                if (child.endsWith(".class")) {
                    addIfMatching(test, child);
                }
            }
        } catch (IOException ioe) {
            log.error("Could not read package: " + packageName, ioe);
        }

        return this;
    }

    /**
     * 将包名转化为包路径，如：java.lang.util --> java/lang/util
     *
     * @param packageName
     * @return
     */
    protected String getPackagePath(String packageName) {
        return packageName == null ? null : packageName.replace('.', '/');
    }

    /**
     * 如果fqn对应的类是 test 对应的parent Class的实例，则将该类加入到matches
     *
     * @param test  封装一个Class，并提供一个指定的类型是否为该class的实例的方法
     * @param fqn   fqn表示一个class类的路径，比如：com/whz/mapperinterface/com.whz.mapperinterface.IEmployeerMapper.class
     */
    @SuppressWarnings("unchecked")
    protected void addIfMatching(Test test, String fqn) {
        try {
            // 比如：将com/whz/mapperinterface/com.whz.mapperinterface.IEmployeerMapper.class 转化为 com.whz.mapperinterface.IEmployeerMapper.class
            String externalName = fqn.substring(0, fqn.indexOf('.')).replace('/', '.');
            ClassLoader loader = getClassLoader();
            if (log.isDebugEnabled()) {
                log.debug("Checking to see if class " + externalName + " matches criteria [" + test + "]");
            }

            Class<?> type = loader.loadClass(externalName);
            if (test.matches(type)) {
                matches.add((Class<T>)type);
            }
        } catch (Throwable t) {
            log.warn("Could not examine class '" + fqn + "'" + " due to a " + t.getClass().getName() + " with message: "
                     + t.getMessage());
        }
    }




    public Set<Class<? extends T>> getClasses() {
        return matches;
    }
    public ClassLoader getClassLoader() {
        return classloader == null ? Thread.currentThread().getContextClassLoader() : classloader;
    }
    public void setClassLoader(ClassLoader classloader) {
        this.classloader = classloader;
    }

}