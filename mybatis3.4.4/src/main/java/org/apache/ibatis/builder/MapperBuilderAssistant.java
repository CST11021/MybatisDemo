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
package org.apache.ibatis.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.CacheBuilder;
import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

/**
 * 解析<mapper>标签的辅助类
 *
 * @author Clinton Begin
 */
public class MapperBuilderAssistant extends BaseBuilder {

    /** 表示 <mapper namespace="xxx"> 配置的命名空间，例如：<mapper namespace="com.whz.mapperinterface.IEmployeerMapper"> */
    private String currentNamespace;
    /** 表示对应的配置文件 */
    private String resource;
    /** 表示命名空间对应的缓存实例 */
    private Cache currentCache;
    /** 表示当前命名空间共享的缓存实例是否还未被加载，比如：命名空间A和B、A引用的B的缓存，当加载A的缓存实例时，B还未加载 */
    // issue #676
    private boolean unresolvedCacheRef;

    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        ErrorContext.instance().resource(resource);
        this.resource = resource;
    }

    /**
     * 添加命名空间的引用，例如：
     *
     * @param base
     * @param isReference   是否引用当前的命名空间，如果为true，一般该配置的作用域对应每个mapper接口下
     * @return
     */
    public String applyCurrentNamespace(String base, boolean isReference) {
        if (base == null) {
            return null;
        }

        if (isReference) {
            // is it qualified with any namespace yet?
            // 它是否已经限定了任何名称空间?
            if (base.contains(".")) {
                return base;
            }
        } else {
            // is it qualified with this namespace yet?
            if (base.startsWith(currentNamespace + ".")) {
                return base;
            }
            if (base.contains(".")) {
                throw new BuilderException("Dots are not allowed in element names, please remove it from " + base);
            }
        }
        return currentNamespace + "." + base;
    }

    /**
     * 根据命名空间获取对应的缓存实例，如果该实例还未被创建，则抛出IncompleteElementException异常
     *
     * @param namespace
     * @return
     */
    public Cache useCacheRef(String namespace) {
        if (namespace == null) {
            throw new BuilderException("cache-ref element requires a namespace attribute.");
        }

        try {
            unresolvedCacheRef = true;
            Cache cache = configuration.getCache(namespace);
            if (cache == null) {
                throw new IncompleteElementException("No cache for namespace '" + namespace + "' could be found.");
            }

            currentCache = cache;
            unresolvedCacheRef = false;
            return cache;
        } catch (IllegalArgumentException e) {
            throw new IncompleteElementException("No cache for namespace '" + namespace + "' could be found.", e);
        }
    }

    /**
     * 创建一个缓存实例
     *
     * @param typeClass         缓存实现类
     * @param evictionClass     eviction：代表的是缓存收回策略，有一下策略：
     *                          1. LRU， 最近最少使用的，移除最长时间不用的对象。
     *                          2. FIFO，先进先出，按对象进入缓存的顺序来移除他们
     *                          3. SOFT， 软引用，移除基于垃圾回收器状态和软引用规则的对象。
     *                          4. WEAK，若引用，更积极的移除基于垃圾收集器状态和若引用规则的对象
     * @param flushInterval     缓存自动清除的时间间隔
     * @param size              缓存最大大小
     * @param readWrite         缓存是否只读
     * @param blocking          是否设置为阻塞缓存，参考{@link org.apache.ibatis.cache.decorators.BlockingCache}
     * @param props             初始化参数实例的变量
     * @return
     */
    public Cache useNewCache(Class<? extends Cache> typeClass, Class<? extends Cache> evictionClass, Long flushInterval, Integer size, boolean readWrite, boolean blocking, Properties props) {
        // 根据指定的命名空间创建一个缓存实例
        Cache cache = new CacheBuilder(currentNamespace)
                .implementation(valueOrDefault(typeClass, PerpetualCache.class))
                .addDecorator(valueOrDefault(evictionClass, LruCache.class))
                .clearInterval(flushInterval)
                .size(size)
                .readWrite(readWrite)
                .blocking(blocking)
                .properties(props)
                .build();
        configuration.addCache(cache);
        currentCache = cache;
        return cache;
    }

    /**
     * 获取一个<parameterMap>配置的实例
     *
     * @param id                    对应<parameterMap>的id配置
     * @param parameterClass        对应<parameterMap>的type配置
     * @param parameterMappings     表示一个<parameterMap>配置的详细信息
     * @return
     */
    public ParameterMap addParameterMap(String id, Class<?> parameterClass, List<ParameterMapping> parameterMappings) {
        id = applyCurrentNamespace(id, false);
        ParameterMap parameterMap = new ParameterMap.Builder(configuration, id, parameterClass, parameterMappings).build();
        configuration.addParameterMap(parameterMap);
        return parameterMap;
    }

    public ParameterMapping buildParameterMapping(Class<?> parameterType, String property, Class<?> javaType, JdbcType jdbcType, String resultMap, ParameterMode parameterMode, Class<? extends TypeHandler<?>> typeHandler, Integer numericScale) {
        resultMap = applyCurrentNamespace(resultMap, true);

        // Class parameterType = parameterMapBuilder.type();
        Class<?> javaTypeClass = resolveParameterJavaType(parameterType, property, javaType, jdbcType);
        TypeHandler<?> typeHandlerInstance = resolveTypeHandler(javaTypeClass, typeHandler);

        return new ParameterMapping.Builder(configuration, property, javaTypeClass)
                .jdbcType(jdbcType)
                .resultMapId(resultMap)
                .mode(parameterMode)
                .numericScale(numericScale)
                .typeHandler(typeHandlerInstance)
                .build();
    }

    /**
     * 添加一个<resultMap>配置的实例
     *
     * @param id                对应<resultMap>的id配置
     * @param type              对应<parameterMap>的type配置
     * @param extend
     * @param discriminator
     * @param resultMappings
     * @param autoMapping
     * @return
     */
    public ResultMap addResultMap(String id, Class<?> type, String extend, Discriminator discriminator, List<ResultMapping> resultMappings, Boolean autoMapping) {
        id = applyCurrentNamespace(id, false);
        extend = applyCurrentNamespace(extend, true);

        if (extend != null) {
            if (!configuration.hasResultMap(extend)) {
                throw new IncompleteElementException("Could not find a parent resultmap with id '" + extend + "'");
            }
            ResultMap resultMap = configuration.getResultMap(extend);
            List<ResultMapping> extendedResultMappings = new ArrayList<ResultMapping>(resultMap.getResultMappings());
            extendedResultMappings.removeAll(resultMappings);
            // Remove parent constructor if this resultMap declares a constructor.
            boolean declaresConstructor = false;
            for (ResultMapping resultMapping : resultMappings) {
                if (resultMapping.getFlags().contains(ResultFlag.CONSTRUCTOR)) {
                    declaresConstructor = true;
                    break;
                }
            }
            if (declaresConstructor) {
                Iterator<ResultMapping> extendedResultMappingsIter = extendedResultMappings.iterator();
                while (extendedResultMappingsIter.hasNext()) {
                    if (extendedResultMappingsIter.next().getFlags().contains(ResultFlag.CONSTRUCTOR)) {
                        extendedResultMappingsIter.remove();
                    }
                }
            }
            resultMappings.addAll(extendedResultMappings);
        }
        ResultMap resultMap = new ResultMap.Builder(configuration, id, type, resultMappings, autoMapping)
                .discriminator(discriminator)
                .build();
        configuration.addResultMap(resultMap);
        return resultMap;
    }

    public Discriminator buildDiscriminator(Class<?> resultType, String column, Class<?> javaType, JdbcType jdbcType, Class<? extends TypeHandler<?>> typeHandler, Map<String, String> discriminatorMap) {
        ResultMapping resultMapping = buildResultMapping(
                resultType,
                null,
                column,
                javaType,
                jdbcType,
                null,
                null,
                null,
                null,
                typeHandler,
                new ArrayList<ResultFlag>(),
                null,
                null,
                false);
        Map<String, String> namespaceDiscriminatorMap = new HashMap<String, String>();
        for (Map.Entry<String, String> e : discriminatorMap.entrySet()) {
            String resultMap = e.getValue();
            resultMap = applyCurrentNamespace(resultMap, true);
            namespaceDiscriminatorMap.put(e.getKey(), resultMap);
        }
        return new Discriminator.Builder(configuration, resultMapping, namespaceDiscriminatorMap).build();
    }

    /**
     * 添加一个MappedStatement配置，在mybatis中我们将select|insert|update|delete 这些配置节点信息抽象为MappedStatement
     *
     * @param id                    对应配置的SQL语句的id
     * @param sqlSource
     * @param statementType         对应SQL语句中的statementType属性配置
     * @param sqlCommandType        表示SQL语句的类型
     * @param fetchSize             对应SQL语句中的fetchSize属性配置
     * @param timeout               对应SQL语句中的timeout属性配置
     * @param parameterMap          对应SQL语句中的parameterMap属性配置
     * @param parameterType         对应SQL语句中的parameterType属性配置
     * @param resultMap             对应SQL语句中的resultMap属性配置
     * @param resultType            对应SQL语句中的resultType属性配置
     * @param resultSetType         对应SQL语句中的resultSetType属性配置
     * @param flushCache            对应SQL语句中的flushCache属性配置
     * @param useCache              对应SQL语句中的useCache属性配置
     * @param resultOrdered         对应SQL语句中的resultOrdered属性配置
     * @param keyGenerator          表示所使用的{@link KeyGenerator}实现方式
     * @param keyProperty           对应SQL语句中的keyProperty属性配置
     * @param keyColumn             对应SQL语句中的keyColumn属性配置
     * @param databaseId            对应SQL语句中的databaseId属性配置
     * @param lang                  对应SQL语句中的lang属性配置
     * @param resultSets            对应SQL语句中的resultSets属性配置
     * @return
     */
    public MappedStatement addMappedStatement(String id, SqlSource sqlSource, StatementType statementType, SqlCommandType sqlCommandType, Integer fetchSize, Integer timeout, String parameterMap, Class<?> parameterType, String resultMap, Class<?> resultType, ResultSetType resultSetType, boolean flushCache, boolean useCache, boolean resultOrdered, KeyGenerator keyGenerator, String keyProperty, String keyColumn, String databaseId, LanguageDriver lang, String resultSets) {

        if (unresolvedCacheRef) {
            throw new IncompleteElementException("Cache-ref not yet resolved");
        }

        id = applyCurrentNamespace(id, false);
        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlSource, sqlCommandType)
                .resource(resource)
                .fetchSize(fetchSize)
                .timeout(timeout)
                .statementType(statementType)
                .keyGenerator(keyGenerator)
                .keyProperty(keyProperty)
                .keyColumn(keyColumn)
                .databaseId(databaseId)
                .lang(lang)
                .resultOrdered(resultOrdered)
                .resultSets(resultSets)
                .resultMaps(getStatementResultMaps(resultMap, resultType, id))
                .resultSetType(resultSetType)
                .flushCacheRequired(valueOrDefault(flushCache, !isSelect))
                .useCache(valueOrDefault(useCache, isSelect))
                .cache(currentCache);

        ParameterMap statementParameterMap = getStatementParameterMap(parameterMap, parameterType, id);
        if (statementParameterMap != null) {
            statementBuilder.parameterMap(statementParameterMap);
        }

        MappedStatement statement = statementBuilder.build();
        configuration.addMappedStatement(statement);
        return statement;
    }

    /**
     * 如果value为null，则返回返回默认值，否则返回value
     *
     * @param value
     * @param defaultValue
     * @param <T>
     * @return
     */
    private <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     *
     * @param parameterMapName      对应SQL语句中的parameterMap属性配置
     * @param parameterTypeClass    对应SQL语句中的parameterType属性配置
     * @param statementId           对应配置的SQL语句的id
     * @return
     */
    private ParameterMap getStatementParameterMap(String parameterMapName, Class<?> parameterTypeClass, String statementId) {
        parameterMapName = applyCurrentNamespace(parameterMapName, true);
        ParameterMap parameterMap = null;
        if (parameterMapName != null) {
            try {
                parameterMap = configuration.getParameterMap(parameterMapName);
            } catch (IllegalArgumentException e) {
                throw new IncompleteElementException("Could not find parameter map " + parameterMapName, e);
            }
        } else if (parameterTypeClass != null) {
            List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
            parameterMap = new ParameterMap.Builder(
                    configuration,
                    statementId + "-Inline",
                    parameterTypeClass,
                    parameterMappings).build();
        }
        return parameterMap;
    }

    private List<ResultMap> getStatementResultMaps(String resultMap, Class<?> resultType, String statementId) {
        resultMap = applyCurrentNamespace(resultMap, true);

        List<ResultMap> resultMaps = new ArrayList<ResultMap>();
        if (resultMap != null) {
            String[] resultMapNames = resultMap.split(",");
            for (String resultMapName : resultMapNames) {
                try {
                    resultMaps.add(configuration.getResultMap(resultMapName.trim()));
                } catch (IllegalArgumentException e) {
                    throw new IncompleteElementException("Could not find result map " + resultMapName, e);
                }
            }
        } else if (resultType != null) {
            ResultMap inlineResultMap = new ResultMap.Builder(
                    configuration,
                    statementId + "-Inline",
                    resultType,
                    new ArrayList<ResultMapping>(),
                    null).build();
            resultMaps.add(inlineResultMap);
        }
        return resultMaps;
    }

    public ResultMapping buildResultMapping(Class<?> resultType, String property, String column, Class<?> javaType, JdbcType jdbcType, String nestedSelect, String nestedResultMap, String notNullColumn, String columnPrefix, Class<? extends TypeHandler<?>> typeHandler, List<ResultFlag> flags, String resultSet, String foreignColumn, boolean lazy) {
        Class<?> javaTypeClass = resolveResultJavaType(resultType, property, javaType);
        TypeHandler<?> typeHandlerInstance = resolveTypeHandler(javaTypeClass, typeHandler);
        List<ResultMapping> composites = parseCompositeColumnName(column);
        return new ResultMapping.Builder(configuration, property, column, javaTypeClass)
                .jdbcType(jdbcType)
                .nestedQueryId(applyCurrentNamespace(nestedSelect, true))
                .nestedResultMapId(applyCurrentNamespace(nestedResultMap, true))
                .resultSet(resultSet)
                .typeHandler(typeHandlerInstance)
                .flags(flags == null ? new ArrayList<ResultFlag>() : flags)
                .composites(composites)
                .notNullColumns(parseMultipleColumnNames(notNullColumn))
                .columnPrefix(columnPrefix)
                .foreignColumn(foreignColumn)
                .lazy(lazy)
                .build();
    }

    private Set<String> parseMultipleColumnNames(String columnName) {
        Set<String> columns = new HashSet<String>();
        if (columnName != null) {
            if (columnName.indexOf(',') > -1) {
                StringTokenizer parser = new StringTokenizer(columnName, "{}, ", false);
                while (parser.hasMoreTokens()) {
                    String column = parser.nextToken();
                    columns.add(column);
                }
            } else {
                columns.add(columnName);
            }
        }
        return columns;
    }

    private List<ResultMapping> parseCompositeColumnName(String columnName) {
        List<ResultMapping> composites = new ArrayList<ResultMapping>();
        if (columnName != null && (columnName.indexOf('=') > -1 || columnName.indexOf(',') > -1)) {
            StringTokenizer parser = new StringTokenizer(columnName, "{}=, ", false);
            while (parser.hasMoreTokens()) {
                String property = parser.nextToken();
                String column = parser.nextToken();
                ResultMapping complexResultMapping = new ResultMapping.Builder(
                        configuration, property, column, configuration.getTypeHandlerRegistry().getUnknownTypeHandler()).build();
                composites.add(complexResultMapping);
            }
        }
        return composites;
    }

    /**
     * 解析返回的结果类型
     *
     * @param resultType
     * @param property
     * @param javaType
     * @return
     */
    private Class<?> resolveResultJavaType(Class<?> resultType, String property, Class<?> javaType) {
        if (javaType == null && property != null) {
            try {
                MetaClass metaResultType = MetaClass.forClass(resultType, configuration.getReflectorFactory());
                javaType = metaResultType.getSetterType(property);
            } catch (Exception e) {
                //ignore, following null check statement will deal with the situation
            }
        }
        if (javaType == null) {
            javaType = Object.class;
        }
        return javaType;
    }

    /**
     * 解析参数类型
     *
     * @param resultType
     * @param property
     * @param javaType
     * @param jdbcType
     * @return
     */
    private Class<?> resolveParameterJavaType(Class<?> resultType, String property, Class<?> javaType, JdbcType jdbcType) {
        if (javaType == null) {
            // Oracle的游标类型对应java的 JdbcType.CURSOR
            if (JdbcType.CURSOR.equals(jdbcType)) {
                javaType = java.sql.ResultSet.class;
            } else if (Map.class.isAssignableFrom(resultType)) {
                javaType = Object.class;
            } else {
                MetaClass metaResultType = MetaClass.forClass(resultType, configuration.getReflectorFactory());
                javaType = metaResultType.getGetterType(property);
            }
        }

        if (javaType == null) {
            javaType = Object.class;
        }
        return javaType;
    }

    /**
     * Backward compatibility signature（向后兼容性的签名）
     */
    public ResultMapping buildResultMapping(Class<?> resultType, String property, String column, Class<?> javaType, JdbcType jdbcType, String nestedSelect, String nestedResultMap, String notNullColumn, String columnPrefix, Class<? extends TypeHandler<?>> typeHandler, List<ResultFlag> flags) {
        return buildResultMapping(
                resultType, property, column, javaType, jdbcType, nestedSelect,
                nestedResultMap, notNullColumn, columnPrefix, typeHandler, flags, null, null, configuration.isLazyLoadingEnabled());
    }

    /**
     * 根据class类型，从注册表获取对应的实例
     *
     * @param langClass
     * @return
     */
    public LanguageDriver getLanguageDriver(Class<?> langClass) {
        if (langClass != null) {
            configuration.getLanguageRegistry().register(langClass);
        } else {
            langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        }
        return configuration.getLanguageRegistry().getDriver(langClass);
    }

    /**
     * Backward compatibility signature（向后兼容性的签名）
     */
    public MappedStatement addMappedStatement(String id, SqlSource sqlSource, StatementType statementType, SqlCommandType sqlCommandType, Integer fetchSize, Integer timeout, String parameterMap, Class<?> parameterType, String resultMap, Class<?> resultType, ResultSetType resultSetType, boolean flushCache, boolean useCache, boolean resultOrdered, KeyGenerator keyGenerator, String keyProperty, String keyColumn, String databaseId, LanguageDriver lang) {
        return addMappedStatement(
                id, sqlSource, statementType, sqlCommandType, fetchSize, timeout,
                parameterMap, parameterType, resultMap, resultType, resultSetType,
                flushCache, useCache, resultOrdered, keyGenerator, keyProperty,
                keyColumn, databaseId, lang, null);
    }


    /**
     * 获取命名空间
     *
     * @return
     */
    public String getCurrentNamespace() {
        return currentNamespace;
    }

    /**
     * 设置命名空间
     *
     * @param currentNamespace
     */
    public void setCurrentNamespace(String currentNamespace) {
        if (currentNamespace == null) {
            throw new BuilderException("The mapper element requires a namespace attribute to be specified.");
        }

        if (this.currentNamespace != null && !this.currentNamespace.equals(currentNamespace)) {
            throw new BuilderException("Wrong namespace. Expected '"
                    + this.currentNamespace + "' but found '" + currentNamespace + "'.");
        }

        this.currentNamespace = currentNamespace;
    }

}
