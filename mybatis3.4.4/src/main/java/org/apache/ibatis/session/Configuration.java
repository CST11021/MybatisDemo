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
package org.apache.ibatis.session;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.builder.CacheRefResolver;
import org.apache.ibatis.builder.ResultMapResolver;
import org.apache.ibatis.builder.annotation.MethodResolver;
import org.apache.ibatis.builder.xml.XMLStatementBuilder;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.FifoCache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.decorators.SoftCache;
import org.apache.ibatis.cache.decorators.WeakCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.datasource.jndi.JndiDataSourceFactory;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ReuseExecutor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.loader.ProxyFactory;
import org.apache.ibatis.executor.loader.cglib.CglibProxyFactory;
import org.apache.ibatis.executor.loader.javassist.JavassistProxyFactory;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl;
import org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl;
import org.apache.ibatis.logging.log4j.Log4jImpl;
import org.apache.ibatis.logging.log4j2.Log4j2Impl;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.InterceptorChain;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.LanguageDriverRegistry;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
  该对像用于表示Mybatis的配置信息：MyBatis根据初始化好的Configuration信息，这时候用户就可以使用MyBatis进行数据库操作了。
  可以这么说，MyBatis初始化的过程，就是创建 Configuration对象的过程。解析mybastic配置的实现入口，请参考：XMLConfigBuilder

  MyBatis的初始化可以有两种方式：
        基于XML配置文件：基于XML配置文件的方式是将MyBatis的所有配置信息放在XML文件中，MyBatis通过加载并XML配置文件，将配置文信息组装成内部的Configuration对象
        基于Java API：这种方式不使用XML配置文件，需要MyBatis使用者在Java代码中，手动创建Configuration对象，然后将配置参数set 进入Configuration对象中
 */
public class Configuration {

    /** 表示要解析的配置文件路径，配置文件可能有多个 */
    protected final Set<String> loadedResources = new HashSet<String>();

    /** 表示当前环境对应的 environmentId，比如生产环境、测试环境等 */
    protected Environment environment;
    /** 表示配置文件中的 <properties/> 标签 */
    protected Properties variables = new Properties();
    /** 表示配置文件中的 <reflectorFactory/> 标签 */
    protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    /** 表示配置文件中的 <objectFactory/> 标签 */
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    /** 表示配置文件中的 <objectWrapperFactory/> 标签 */
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();
    /** 表示配置文件中的 <databaseIdProvider/> 标签 */
    protected String databaseId;
    /** <mappers/> 中配置的所有Mapper接口都会注册到这里 */
    protected final MapperRegistry mapperRegistry = new MapperRegistry(this);
    /** 类型处理注册表，表示配置文件中<typeHandlers/> 标签的配置信息 */
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
    /** 别名注册表，表示配置文件中<typeAliases/> 标签的配置信息 */
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    /** <plugins/> 相关的配置 */
    protected final InterceptorChain interceptorChain = new InterceptorChain();

    /** #224 Using internal Javassist instead of OGNL */
    protected ProxyFactory proxyFactory = new JavassistProxyFactory();
    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    /** 在配置文件中<mapper/>里配置的<select>、<update>、<insert>和<delete>都会生成一个 MappedStatement 对象 */
    protected final Map<String, MappedStatement> mappedStatements = new StrictMap<MappedStatement>("Mapped Statements collection");
    /** 保存所有的缓存实例Map<Mapper对应的命名空间, 对应的缓存实例> */
    protected final Map<String, Cache> caches = new StrictMap<Cache>("Caches collection");
    /** 配置文件中<mapper/>里配置的<resultMap>解析完后都会注册到这里，并且特别需要注意的是，同一个ResultMap对象，会注册两次，分别使用不同的key进行注册，详细请看StrictMap的put方法 */
    protected final Map<String, ResultMap> resultMaps = new StrictMap<ResultMap>("Result Maps collection");
    protected final Map<String, ParameterMap> parameterMaps = new StrictMap<ParameterMap>("Parameter Maps collection");
    protected final Map<String, KeyGenerator> keyGenerators = new StrictMap<KeyGenerator>("Key Generators collection");
    /** sql碎片，<mapper/>里可以配置一些模式式的sql语句，mybatis解析完后会将其保存到 sqlFragments 属性中 */
    protected final Map<String, XNode> sqlFragments = new StrictMap<XNode>("XML fragments parsed from previous mappers");

    /** 当解析Mapper对应的SQL出错时，会将对应的XMLStatementBuilder解析器，添加到该变量中，每个<select>、<update>、<insert>和<delete>配置都会对应一个解析器 */
    protected final Collection<XMLStatementBuilder> incompleteStatements = new LinkedList<XMLStatementBuilder>();
    /** 存放未被加载的缓存实例，比如：命名空间A和B、A引用的B的缓存，当加载A的缓存实例时，B还未加载 */
    protected final Collection<CacheRefResolver> incompleteCacheRefs = new LinkedList<CacheRefResolver>();
    /** 存放未被加载的ResultMap配置 */
    protected final Collection<ResultMapResolver> incompleteResultMaps = new LinkedList<ResultMapResolver>();
    /** 存放未被加载的Method配置 */
    protected final Collection<MethodResolver> incompleteMethods = new LinkedList<MethodResolver>();

    /** A map holds cache-ref relationship. The key is the namespace that references a cache bound to another namespace and the value is the namespace which the actual cache is bound to. */
    protected final Map<String, String> cacheRefMap = new HashMap<String, String>();








    /* ------------------------------------------------------- <settings> 标签的配置-------------------------------------------------------- */
    /** 是否启用行内嵌套语句，默认false */
    protected boolean safeRowBoundsEnabled;
    /** 允许在嵌套语句中使用分页（ResultHandler），默认true */
    protected boolean safeResultHandlerEnabled = true;
    /** 是否开启自动驼峰命名规则（camel case）映射，即从经典数据库列名 A_COLUMN 到经典 Java 属性名 aColumn 的类似映射。默认false */
    protected boolean mapUnderscoreToCamelCase;
    /** 当开启时，任何方法的调用都会加载该对象的所有属性，否则，每个属性会按需加载。默认 false (true in ≤3.4.1) */
    protected boolean aggressiveLazyLoading;
    /** 是否允许单一语句返回多结果集（需要兼容驱动） */
    protected boolean multipleResultSetsEnabled = true;
    /** 允许 JDBC 支持自动生成主键，需要驱动兼容。 如果设置为true 则这个设置强制使用自动生成主键，尽管一些驱动不能兼容但仍可正常工作（比如 Derby）。 */
    protected boolean useGeneratedKeys;
    /** 使用列标签代替列名。不同的驱动在这方面会有不同的表现，具体可参考相关驱动文档或通过测试这两种不同的模式来观察所用驱动的结果。 */
    protected boolean useColumnLabel = true;
    /** 该配置影响的所有映射器中配置的缓存的全局开关。 */
    protected boolean cacheEnabled = true;
    /** 指定当结果集中值为 null 的时候是否调用映射对象的 setter（map 对象时为 put）方法，这对于有 Map.keySet() 依赖或 null 值初始化的时候是有用的。注意基本类型（int、boolean等）是不能设置成 null 的。 */
    protected boolean callSettersOnNulls;
    /** 允许使用方法签名中的名称作为语句参数名称。 为了使用该特性，你的工程必须采用Java 8编译，并且加上-parameters选项。（从3.4.1开始） */
    protected boolean useActualParamName = true;
    /** 当返回行的所有列都是空时，MyBatis默认返回null。 当开启这个设置时，MyBatis会返回一个空实例。 请注意，它也适用于嵌套的结果集 (i.e. collectioin and association)。（从3.4.2开始） */
    protected boolean returnInstanceForEmptyRow;
    /** 延迟加载的全局开关。当开启时，所有关联对象都会延迟加载。 特定关联关系中可通过设置fetchType属性来覆盖该项的开关状态。 */
    protected boolean lazyLoadingEnabled = false;


    /** 指定 MyBatis 增加到日志名称的前缀。 */
    protected String logPrefix;
    /** 指定 MyBatis 所用日志的具体实现，未指定时将自动查找:SLF4J | LOG4J | LOG4J2 | JDK_LOGGING | COMMONS_LOGGING | STDOUT_LOGGING | NO_LOGGING */
    protected Class<? extends Log> logImpl;
    protected Class<? extends VFS> vfsImpl;
    /** MyBatis 利用本地缓存机制（Local Cache）防止循环引用（circular references）和加速重复嵌套查询。 默认值为 SESSION，这种情况下会缓存一个会话中执行的所有查询。 若设置值为 STATEMENT，本地会话仅用在语句执行上，对相同 SqlSession 的不同调用将不会共享数据。 */
    protected LocalCacheScope localCacheScope = LocalCacheScope.SESSION;
    /** 当没有为参数提供特定的 JDBC 类型时，为空值指定 JDBC 类型。 某些驱动需要指定列的 JDBC 类型，多数情况直接用一般类型即可，比如 NULL、VARCHAR 或 OTHER。 */
    protected JdbcType jdbcTypeForNull = JdbcType.OTHER;
    /** 指定哪个对象的方法触发一次延迟加载 */
    protected Set<String> lazyLoadTriggerMethods = new HashSet<String>(Arrays.asList(new String[]{"equals", "clone", "hashCode", "toString"}));
    /** 指定动态 SQL 生成的默认语言。 */
    protected Integer defaultStatementTimeout;
    /** 为驱动的结果集获取数量（fetchSize）设置一个提示值。此参数只可以在查询设置中被覆盖。 */
    protected Integer defaultFetchSize;
    /** 配置默认的执行器。SIMPLE 就是普通的执行器；REUSE 执行器会重用预处理语句（prepared statements）； BATCH 执行器将重用语句并执行批量更新。 */
    protected ExecutorType defaultExecutorType = ExecutorType.SIMPLE;
    /** 指定 MyBatis 应如何自动映射列到字段或属性。 NONE 表示取消自动映射；PARTIAL 只会自动映射没有定义嵌套结果集映射的结果集。 FULL 会自动映射任意复杂的结果集（无论是否嵌套）。 */
    protected AutoMappingBehavior autoMappingBehavior = AutoMappingBehavior.PARTIAL;
    // 指定发现自动映射目标未知列（或者未知属性类型）的行为。
    // NONE: 不做任何反应
    // WARNING: 输出提醒日志 ('org.apache.ibatis.session.AutoMappingUnknownColumn
    // Behavior' 的日志等级必须设置为 WARN)
    // FAILING: 映射失败 (抛出 SqlSessionException)
    protected AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior = AutoMappingUnknownColumnBehavior.NONE;
    /**
     * Configuration factory class.
     * Used to create Configuration for loading deserialized unread properties.
     *
     * @see <a href='https://code.google.com/p/mybatis/issues/detail?id=300'>Issue 300 (google code)</a>
     */
    protected Class<?> configurationFactory;

    /* ------------------------------------------------------- <settings> 标签的配置-------------------------------------------------------- */














    public Configuration(Environment environment) {
        this();
        this.environment = environment;
    }

    /**
     * 初始化类型的别名注册表
     */
    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("MANAGED", ManagedTransactionFactory.class);

        typeAliasRegistry.registerAlias("JNDI", JndiDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);

        typeAliasRegistry.registerAlias("PERPETUAL", PerpetualCache.class);
        typeAliasRegistry.registerAlias("FIFO", FifoCache.class);
        typeAliasRegistry.registerAlias("LRU", LruCache.class);
        typeAliasRegistry.registerAlias("SOFT", SoftCache.class);
        typeAliasRegistry.registerAlias("WEAK", WeakCache.class);

        typeAliasRegistry.registerAlias("DB_VENDOR", VendorDatabaseIdProvider.class);

        typeAliasRegistry.registerAlias("XML", XMLLanguageDriver.class);
        typeAliasRegistry.registerAlias("RAW", RawLanguageDriver.class);

        typeAliasRegistry.registerAlias("SLF4J", Slf4jImpl.class);
        typeAliasRegistry.registerAlias("COMMONS_LOGGING", JakartaCommonsLoggingImpl.class);
        typeAliasRegistry.registerAlias("LOG4J", Log4jImpl.class);
        typeAliasRegistry.registerAlias("LOG4J2", Log4j2Impl.class);
        typeAliasRegistry.registerAlias("JDK_LOGGING", Jdk14LoggingImpl.class);
        typeAliasRegistry.registerAlias("STDOUT_LOGGING", StdOutImpl.class);
        typeAliasRegistry.registerAlias("NO_LOGGING", NoLoggingImpl.class);

        typeAliasRegistry.registerAlias("CGLIB", CglibProxyFactory.class);
        typeAliasRegistry.registerAlias("JAVASSIST", JavassistProxyFactory.class);

        languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
        languageRegistry.register(RawLanguageDriver.class);
    }


    /**
     * 添加要解析的配置文件
     *
     * @param resource
     */
    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }

    /**
     * 资源配置文件是否已经加载
     *
     * @param resource
     * @return
     */
    public boolean isResourceLoaded(String resource) {
        return loadedResources.contains(resource);
    }

    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }
    public void setProxyFactory(ProxyFactory proxyFactory) {
        if (proxyFactory == null) {
            proxyFactory = new JavassistProxyFactory();
        }
        this.proxyFactory = proxyFactory;
    }

    /**
     * @since 3.2.2
     */
    public List<Interceptor> getInterceptors() {
        return interceptorChain.getInterceptors();
    }
    public void setDefaultScriptingLanguage(Class<?> driver) {
        if (driver == null) {
            driver = XMLLanguageDriver.class;
        }
        getLanguageRegistry().setDefaultDriverClass(driver);
    }





    public LanguageDriver getDefaultScriptingLanguageInstance() {
        return languageRegistry.getDefaultDriver();
    }

    /**
     * @deprecated Use {@link #getDefaultScriptingLanguageInstance()}
     */
    @Deprecated
    public LanguageDriver getDefaultScriptingLanuageInstance() {
        return getDefaultScriptingLanguageInstance();
    }

    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }

    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
        parameterHandler = (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
        return parameterHandler;
    }

    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ParameterHandler parameterHandler, ResultHandler resultHandler, BoundSql boundSql) {
        ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
        resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
        return resultSetHandler;
    }

    /**
     * 根据执行器类型、MappedStatement对象、sql占位符参数、分页信息、resultHandler对象和封装了sql语句的BoundSql对象，创建一个 StatementHandler 对象
     *
     * @param executor
     * @param mappedStatement
     * @param parameterObject
     * @param rowBounds
     * @param resultHandler
     * @param boundSql
     * @return
     */
    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
        statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
        return statementHandler;
    }

    // 创建一个执行器对象
    public Executor newExecutor(Transaction transaction) {
        return newExecutor(transaction, defaultExecutorType);
    }
    public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
        executorType = executorType == null ? defaultExecutorType : executorType;
        executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
        Executor executor;

        // 根据类型创建执行器，默认使用 SimpleExecutor 执行
        if (ExecutorType.BATCH == executorType) {
            executor = new BatchExecutor(this, transaction);
        } else if (ExecutorType.REUSE == executorType) {
            executor = new ReuseExecutor(this, transaction);
        } else {
            executor = new SimpleExecutor(this, transaction);
        }
        if (cacheEnabled) {
            executor = new CachingExecutor(executor);
        }
        executor = (Executor) interceptorChain.pluginAll(executor);
        return executor;
    }



    public void addKeyGenerator(String id, KeyGenerator keyGenerator) {
        keyGenerators.put(id, keyGenerator);
    }
    public Collection<String> getKeyGeneratorNames() {
        return keyGenerators.keySet();
    }
    public Collection<KeyGenerator> getKeyGenerators() {
        return keyGenerators.values();
    }
    public KeyGenerator getKeyGenerator(String id) {
        return keyGenerators.get(id);
    }
    public boolean hasKeyGenerator(String id) {
        return keyGenerators.containsKey(id);
    }



    public void addCache(Cache cache) {
        caches.put(cache.getId(), cache);
    }
    public Collection<String> getCacheNames() {
        return caches.keySet();
    }
    public Collection<Cache> getCaches() {
        return caches.values();
    }
    public Cache getCache(String id) {
        return caches.get(id);
    }
    public boolean hasCache(String id) {
        return caches.containsKey(id);
    }




    public void addResultMap(ResultMap rm) {
        // 要注意这个put方法，它会注入两个resultMap
        resultMaps.put(rm.getId(), rm);
        checkLocallyForDiscriminatedNestedResultMaps(rm);
        checkGloballyForDiscriminatedNestedResultMaps(rm);
    }
    public Collection<String> getResultMapNames() {
        return resultMaps.keySet();
    }
    public Collection<ResultMap> getResultMaps() {
        return resultMaps.values();
    }
    public ResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }
    public boolean hasResultMap(String id) {
        return resultMaps.containsKey(id);
    }



    public void addParameterMap(ParameterMap pm) {
        parameterMaps.put(pm.getId(), pm);
    }
    public Collection<String> getParameterMapNames() {
        return parameterMaps.keySet();
    }
    public Collection<ParameterMap> getParameterMaps() {
        return parameterMaps.values();
    }
    public ParameterMap getParameterMap(String id) {
        return parameterMaps.get(id);
    }
    public boolean hasParameterMap(String id) {
        return parameterMaps.containsKey(id);
    }
    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }
    public Collection<String> getMappedStatementNames() {
        buildAllStatements();
        return mappedStatements.keySet();
    }
    public Collection<MappedStatement> getMappedStatements() {
        buildAllStatements();
        return mappedStatements.values();
    }


    /**
     * 当解析mapper对应SQL的出错时，会将解析器添加到{@link #incompleteStatements}
     *
     * @param incompleteStatement   解析SQL异常时，对应的解析器实例
     */
    public void addIncompleteStatement(XMLStatementBuilder incompleteStatement) {
        incompleteStatements.add(incompleteStatement);
    }

    /**
     * 解析<cacheRef>失败时对应的解析器实例
     *
     * @param incompleteCacheRef
     */
    public void addIncompleteCacheRef(CacheRefResolver incompleteCacheRef) {
        incompleteCacheRefs.add(incompleteCacheRef);
    }
    public void addIncompleteResultMap(ResultMapResolver resultMapResolver) {
        incompleteResultMaps.add(resultMapResolver);
    }
    public void addIncompleteMethod(MethodResolver builder) {
        incompleteMethods.add(builder);
    }



    public MappedStatement getMappedStatement(String id) {
        return this.getMappedStatement(id, true);
    }
    public MappedStatement getMappedStatement(String id, boolean validateIncompleteStatements) {
        if (validateIncompleteStatements) {
            buildAllStatements();
        }
        return mappedStatements.get(id);
    }



    public void addInterceptor(Interceptor interceptor) {
        interceptorChain.addInterceptor(interceptor);
    }

    public void addMappers(String packageName, Class<?> superType) {
        mapperRegistry.addMappers(packageName, superType);
    }
    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }
    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }
    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    /**
     * 判断是否存在这个statementName对应的{@link MappedStatement}
     *
     * @param statementName
     * @return
     */
    public boolean hasStatement(String statementName) {
        return hasStatement(statementName, true);
    }

    /**
     * 判断是否存在这个statementName对应的{@link MappedStatement}
     *
     * @param statementName
     * @param validateIncompleteStatements
     * @return
     */
    public boolean hasStatement(String statementName, boolean validateIncompleteStatements) {
        if (validateIncompleteStatements) {
            buildAllStatements();
        }
        return mappedStatements.containsKey(statementName);
    }

    public void addCacheRef(String namespace, String referencedNamespace) {
        cacheRefMap.put(namespace, referencedNamespace);
    }

    /**
     * Parses all the unprocessed statement nodes in the cache. It is recommended
     * to call this method once all the mappers are added as it provides fail-fast
     * statement validation.
     */
    protected void buildAllStatements() {
        if (!incompleteResultMaps.isEmpty()) {
            synchronized (incompleteResultMaps) {
                // This always throws a BuilderException.
                incompleteResultMaps.iterator().next().resolve();
            }
        }
        if (!incompleteCacheRefs.isEmpty()) {
            synchronized (incompleteCacheRefs) {
                // This always throws a BuilderException.
                incompleteCacheRefs.iterator().next().resolveCacheRef();
            }
        }
        if (!incompleteStatements.isEmpty()) {
            synchronized (incompleteStatements) {
                // This always throws a BuilderException.
                incompleteStatements.iterator().next().parseStatementNode();
            }
        }
        if (!incompleteMethods.isEmpty()) {
            synchronized (incompleteMethods) {
                // This always throws a BuilderException.
                incompleteMethods.iterator().next().resolve();
            }
        }
    }

    /**
     * Extracts namespace from fully qualified statement id.
     *
     * @param statementId
     * @return namespace or null when id does not contain period.
     */
    protected String extractNamespace(String statementId) {
        int lastPeriod = statementId.lastIndexOf('.');
        return lastPeriod > 0 ? statementId.substring(0, lastPeriod) : null;
    }

    // Slow but a one time cost. A better solution is welcome.
    protected void checkGloballyForDiscriminatedNestedResultMaps(ResultMap rm) {
        if (rm.hasNestedResultMaps()) {
            for (Map.Entry<String, ResultMap> entry : resultMaps.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof ResultMap) {
                    ResultMap entryResultMap = (ResultMap) value;
                    if (!entryResultMap.hasNestedResultMaps() && entryResultMap.getDiscriminator() != null) {
                        Collection<String> discriminatedResultMapNames = entryResultMap.getDiscriminator().getDiscriminatorMap().values();
                        if (discriminatedResultMapNames.contains(rm.getId())) {
                            entryResultMap.forceNestedResultMaps();
                        }
                    }
                }
            }
        }
    }

    // Slow but a one time cost. A better solution is welcome.
    protected void checkLocallyForDiscriminatedNestedResultMaps(ResultMap rm) {
        if (!rm.hasNestedResultMaps() && rm.getDiscriminator() != null) {
            for (Map.Entry<String, String> entry : rm.getDiscriminator().getDiscriminatorMap().entrySet()) {
                String discriminatedResultMapName = entry.getValue();
                if (hasResultMap(discriminatedResultMapName)) {
                    ResultMap discriminatedResultMap = resultMaps.get(discriminatedResultMapName);
                    if (discriminatedResultMap.hasNestedResultMaps()) {
                        rm.forceNestedResultMaps();
                        break;
                    }
                }
            }
        }
    }

    // ResultMapp
    protected static class StrictMap<V> extends HashMap<String, V> {
        private static final long serialVersionUID = -4950446264854982944L;
        private final String name;

        public StrictMap(String name, int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
            this.name = name;
        }
        public StrictMap(String name, int initialCapacity) {
            super(initialCapacity);
            this.name = name;
        }
        public StrictMap(String name) {
            super();
            this.name = name;
        }
        public StrictMap(String name, Map<String, ? extends V> m) {
            super(m);
            this.name = name;
        }

        // 注意这个put方法，他会注入两个不同的key，一个是权限定名，一个是简称
        @Override
        @SuppressWarnings("unchecked")
        public V put(String key, V value) {
            if (containsKey(key)) {
                throw new IllegalArgumentException(name + " already contains value for " + key);
            }
            if (key.contains(".")) {
                final String shortKey = getShortName(key);
                if (super.get(shortKey) == null) {
                    super.put(shortKey, value);
                } else {
                    super.put(shortKey, (V) new Ambiguity(shortKey));
                }
            }
            return super.put(key, value);
        }
        @Override
        public V get(Object key) {
            V value = super.get(key);
            if (value == null) {
                throw new IllegalArgumentException(name + " does not contain value for " + key);
            }
            if (value instanceof Ambiguity) {
                throw new IllegalArgumentException(((Ambiguity) value).getSubject() + " is ambiguous in " + name
                        + " (try using the full name including the namespace, or rename one of the entries)");
            }
            return value;
        }
        private String getShortName(String key) {
            final String[] keyParts = key.split("\\.");
            return keyParts[keyParts.length - 1];
        }
        protected static class Ambiguity {
            final private String subject;

            public Ambiguity(String subject) {
                this.subject = subject;
            }

            public String getSubject() {
                return subject;
            }
        }
    }












    // getter and setter ...
    public String getLogPrefix() {
        return logPrefix;
    }
    public void setLogPrefix(String logPrefix) {
        this.logPrefix = logPrefix;
    }
    public Class<? extends Log> getLogImpl() {
        return logImpl;
    }
    public void setLogImpl(Class<? extends Log> logImpl) {
        if (logImpl != null) {
            this.logImpl = logImpl;
            LogFactory.useCustomLogging(this.logImpl);
        }
    }
    public Class<? extends VFS> getVfsImpl() {
        return this.vfsImpl;
    }
    public void setVfsImpl(Class<? extends VFS> vfsImpl) {
        if (vfsImpl != null) {
            this.vfsImpl = vfsImpl;
            VFS.addImplClass(this.vfsImpl);
        }
    }
    public boolean isCallSettersOnNulls() {
        return callSettersOnNulls;
    }
    public void setCallSettersOnNulls(boolean callSettersOnNulls) {
        this.callSettersOnNulls = callSettersOnNulls;
    }
    public boolean isUseActualParamName() {
        return useActualParamName;
    }
    public void setUseActualParamName(boolean useActualParamName) {
        this.useActualParamName = useActualParamName;
    }
    public boolean isReturnInstanceForEmptyRow() {
        return returnInstanceForEmptyRow;
    }
    public void setReturnInstanceForEmptyRow(boolean returnEmptyInstance) {
        this.returnInstanceForEmptyRow = returnEmptyInstance;
    }
    public String getDatabaseId() {
        return databaseId;
    }
    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }
    public Class<?> getConfigurationFactory() {
        return configurationFactory;
    }
    public void setConfigurationFactory(Class<?> configurationFactory) {
        this.configurationFactory = configurationFactory;
    }
    public boolean isSafeResultHandlerEnabled() {
        return safeResultHandlerEnabled;
    }
    public void setSafeResultHandlerEnabled(boolean safeResultHandlerEnabled) {
        this.safeResultHandlerEnabled = safeResultHandlerEnabled;
    }
    public boolean isSafeRowBoundsEnabled() {
        return safeRowBoundsEnabled;
    }
    public void setSafeRowBoundsEnabled(boolean safeRowBoundsEnabled) {
        this.safeRowBoundsEnabled = safeRowBoundsEnabled;
    }
    public boolean isMapUnderscoreToCamelCase() {
        return mapUnderscoreToCamelCase;
    }
    public void setMapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }
    public Environment getEnvironment() {
        return environment;
    }
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
    public AutoMappingBehavior getAutoMappingBehavior() {
        return autoMappingBehavior;
    }
    public void setAutoMappingBehavior(AutoMappingBehavior autoMappingBehavior) {
        this.autoMappingBehavior = autoMappingBehavior;
    }
    public AutoMappingUnknownColumnBehavior getAutoMappingUnknownColumnBehavior() {
        return autoMappingUnknownColumnBehavior;
    }
    public void setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior) {
        this.autoMappingUnknownColumnBehavior = autoMappingUnknownColumnBehavior;
    }
    public boolean isLazyLoadingEnabled() {
        return lazyLoadingEnabled;
    }
    public void setLazyLoadingEnabled(boolean lazyLoadingEnabled) {
        this.lazyLoadingEnabled = lazyLoadingEnabled;
    }
    public boolean isAggressiveLazyLoading() {
        return aggressiveLazyLoading;
    }
    public void setAggressiveLazyLoading(boolean aggressiveLazyLoading) {
        this.aggressiveLazyLoading = aggressiveLazyLoading;
    }
    public boolean isMultipleResultSetsEnabled() {
        return multipleResultSetsEnabled;
    }
    public void setMultipleResultSetsEnabled(boolean multipleResultSetsEnabled) {
        this.multipleResultSetsEnabled = multipleResultSetsEnabled;
    }
    public Set<String> getLazyLoadTriggerMethods() {
        return lazyLoadTriggerMethods;
    }
    public void setLazyLoadTriggerMethods(Set<String> lazyLoadTriggerMethods) {
        this.lazyLoadTriggerMethods = lazyLoadTriggerMethods;
    }
    public boolean isUseGeneratedKeys() {
        return useGeneratedKeys;
    }
    public void setUseGeneratedKeys(boolean useGeneratedKeys) {
        this.useGeneratedKeys = useGeneratedKeys;
    }
    public ExecutorType getDefaultExecutorType() {
        return defaultExecutorType;
    }
    public void setDefaultExecutorType(ExecutorType defaultExecutorType) {
        this.defaultExecutorType = defaultExecutorType;
    }
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }
    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }
    public Integer getDefaultStatementTimeout() {
        return defaultStatementTimeout;
    }
    public void setDefaultStatementTimeout(Integer defaultStatementTimeout) {
        this.defaultStatementTimeout = defaultStatementTimeout;
    }
    public Integer getDefaultFetchSize() {
        return defaultFetchSize;
    }
    public void setDefaultFetchSize(Integer defaultFetchSize) {
        this.defaultFetchSize = defaultFetchSize;
    }
    public boolean isUseColumnLabel() {
        return useColumnLabel;
    }
    public void setUseColumnLabel(boolean useColumnLabel) {
        this.useColumnLabel = useColumnLabel;
    }
    public LocalCacheScope getLocalCacheScope() {
        return localCacheScope;
    }
    public void setLocalCacheScope(LocalCacheScope localCacheScope) {
        this.localCacheScope = localCacheScope;
    }
    public JdbcType getJdbcTypeForNull() {
        return jdbcTypeForNull;
    }
    public void setJdbcTypeForNull(JdbcType jdbcTypeForNull) {
        this.jdbcTypeForNull = jdbcTypeForNull;
    }
    public Properties getVariables() {
        return variables;
    }
    public void setVariables(Properties variables) {
        this.variables = variables;
    }
    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }
    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }
    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }
    public ReflectorFactory getReflectorFactory() {
        return reflectorFactory;
    }
    public void setReflectorFactory(ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
    }
    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }
    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }
    public ObjectWrapperFactory getObjectWrapperFactory() {
        return objectWrapperFactory;
    }
    public void setObjectWrapperFactory(ObjectWrapperFactory objectWrapperFactory) {
        this.objectWrapperFactory = objectWrapperFactory;
    }
    public LanguageDriverRegistry getLanguageRegistry() {
        return languageRegistry;
    }
    public Collection<XMLStatementBuilder> getIncompleteStatements() {
        return incompleteStatements;
    }
    public Collection<CacheRefResolver> getIncompleteCacheRefs() {
        return incompleteCacheRefs;
    }
    public Collection<ResultMapResolver> getIncompleteResultMaps() {
        return incompleteResultMaps;
    }
    public Collection<MethodResolver> getIncompleteMethods() {
        return incompleteMethods;
    }
    public Map<String, XNode> getSqlFragments() {
        return sqlFragments;
    }















}
