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
package org.apache.ibatis.executor.resultset;

import org.apache.ibatis.annotations.AutomapConstructor;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.cursor.defaults.DefaultCursor;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.loader.ResultLoader;
import org.apache.ibatis.executor.loader.ResultLoaderMap;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.executor.result.ResultMapException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.lang.reflect.Constructor;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 * @author Iwao AVE!
 * @author Kazuki Shimizu
 */
public class DefaultResultSetHandler implements ResultSetHandler {

    private static final Object DEFERED = new Object();

    /** 表示执行器 */
    private final Executor executor;
    /** Mybastic配置 */
    private final Configuration configuration;
    /** 当前执行的MappedStatement */
    private final MappedStatement mappedStatement;
    /** 分页条件 */
    private final RowBounds rowBounds;
    /** 参数处理器 */
    private final ParameterHandler parameterHandler;

    /** 用于处理SQL的执行结果，Mybastic提供了Object、List和Map三种实现 */
    private final ResultHandler<?> resultHandler;
    /** 封装了提交给DB执行的SQL和参数 */
    private final BoundSql boundSql;
    /** 类型转换器注册表 */
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final ObjectFactory objectFactory;
    private final ReflectorFactory reflectorFactory;

    // nested resultmaps
    private final Map<CacheKey, Object> nestedResultObjects = new HashMap<CacheKey, Object>();
    private final Map<String, Object> ancestorObjects = new HashMap<String, Object>();
    private Object previousRowValue;

    // multiple resultsets
    private final Map<String, ResultMapping> nextResultMaps = new HashMap<String, ResultMapping>();
    private final Map<CacheKey, List<PendingRelation>> pendingRelations = new HashMap<CacheKey, List<PendingRelation>>();

    // Cached Automappings
    private final Map<String, List<UnMappedColumnAutoMapping>> autoMappingsCache = new HashMap<String, List<UnMappedColumnAutoMapping>>();

    // temporary marking flag that indicate using constructor mapping (use field to reduce memory usage)
    private boolean useConstructorMappings;
    private final PrimitiveTypes primitiveTypes;

    // 构造器
    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler, ResultHandler<?> resultHandler, BoundSql boundSql, RowBounds rowBounds) {
        this.executor = executor;
        this.configuration = mappedStatement.getConfiguration();
        this.mappedStatement = mappedStatement;
        this.rowBounds = rowBounds;
        this.parameterHandler = parameterHandler;
        this.boundSql = boundSql;
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.objectFactory = configuration.getObjectFactory();
        this.reflectorFactory = configuration.getReflectorFactory();
        this.resultHandler = resultHandler;
        this.primitiveTypes = new PrimitiveTypes();
    }


    private static class PendingRelation {
        public MetaObject metaObject;
        public ResultMapping propertyMapping;
    }
    private static class UnMappedColumnAutoMapping {
        private final String column;
        private final String property;
        private final TypeHandler<?> typeHandler;
        private final boolean primitive;

        public UnMappedColumnAutoMapping(String column, String property, TypeHandler<?> typeHandler, boolean primitive) {
            this.column = column;
            this.property = property;
            this.typeHandler = typeHandler;
            this.primitive = primitive;
        }
    }








    /* --------------------------------------- 处理结果 ---------------------------------------------- */

    /**
     * 处理集合类型的结果集
     *
     * @param stmt
     * @return          例如：List<User>的集合
     * @throws SQLException
     */
    @Override
    public List<Object> handleResultSets(Statement stmt) throws SQLException {
        ErrorContext.instance().activity("handling results").object(mappedStatement.getId());

        final List<Object> multipleResults = new ArrayList<Object>();

        int resultSetCount = 0;
        // 获取Statement执行完SQL后，对应的 java.sql.ResultSet 结果集
        ResultSetWrapper rsw = getFirstResultSet(stmt);

        // 获取配置文件中配置返回结果集类型，对应<resultMap>标签配置，因为一个 XxxMapper 接口可以配置多个<resultMap> 所以是一个集合类型
        List<ResultMap> resultMaps = mappedStatement.getResultMaps();

        int resultMapCount = resultMaps.size();
        // 校验结果集：
        // 1、sql执行的结果集java.sql.ResultSet必须不能为空；
        // 2、必须配置返回的对象类型，例如：resultType、resultMap
        validateResultMapsCount(rsw, resultMapCount);


        while (rsw != null && resultMapCount > resultSetCount) {
            ResultMap resultMap = resultMaps.get(resultSetCount);
            handleResultSet(rsw, resultMap, multipleResults, null);
            rsw = getNextResultSet(stmt);
            cleanUpAfterHandlingResultSet();
            resultSetCount++;
        }

        String[] resultSets = mappedStatement.getResultSets();
        if (resultSets != null) {
            while (rsw != null && resultSetCount < resultSets.length) {
                ResultMapping parentMapping = nextResultMaps.get(resultSets[resultSetCount]);
                if (parentMapping != null) {
                    String nestedResultMapId = parentMapping.getNestedResultMapId();
                    ResultMap resultMap = configuration.getResultMap(nestedResultMapId);
                    handleResultSet(rsw, resultMap, null, parentMapping);
                }
                rsw = getNextResultSet(stmt);
                cleanUpAfterHandlingResultSet();
                resultSetCount++;
            }
        }

        return collapseSingleResultList(multipleResults);
    }

    /**
     * 处理游标类型的结果集
     *
     * @param stmt
     * @param <E>
     * @return
     * @throws SQLException
     */
    @Override
    public <E> Cursor<E> handleCursorResultSets(Statement stmt) throws SQLException {
        ErrorContext.instance().activity("handling cursor results").object(mappedStatement.getId());

        ResultSetWrapper rsw = getFirstResultSet(stmt);

        List<ResultMap> resultMaps = mappedStatement.getResultMaps();

        int resultMapCount = resultMaps.size();
        validateResultMapsCount(rsw, resultMapCount);
        if (resultMapCount != 1) {
            throw new ExecutorException("Cursor results cannot be mapped to multiple resultMaps");
        }

        ResultMap resultMap = resultMaps.get(0);
        return new DefaultCursor<E>(this, resultMap, rsw, rowBounds);
    }

    /**
     * 处理存储过程的结果集
     *
     * @param cs
     * @throws SQLException
     */
    @Override
    public void handleOutputParameters(CallableStatement cs) throws SQLException {
        final Object parameterObject = parameterHandler.getParameterObject();
        final MetaObject metaParam = configuration.newMetaObject(parameterObject);
        final List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        for (int i = 0; i < parameterMappings.size(); i++) {
            final ParameterMapping parameterMapping = parameterMappings.get(i);
            if (parameterMapping.getMode() == ParameterMode.OUT || parameterMapping.getMode() == ParameterMode.INOUT) {
                if (ResultSet.class.equals(parameterMapping.getJavaType())) {
                    handleRefCursorOutputParameter((ResultSet) cs.getObject(i + 1), parameterMapping, metaParam);
                } else {
                    final TypeHandler<?> typeHandler = parameterMapping.getTypeHandler();
                    metaParam.setValue(parameterMapping.getProperty(), typeHandler.getResult(cs, i + 1));
                }
            }
        }
    }
    private void handleRefCursorOutputParameter(ResultSet rs, ParameterMapping parameterMapping, MetaObject metaParam) throws SQLException {
        if (rs == null) {
            return;
        }
        try {
            final String resultMapId = parameterMapping.getResultMapId();
            final ResultMap resultMap = configuration.getResultMap(resultMapId);
            final DefaultResultHandler resultHandler = new DefaultResultHandler(objectFactory);
            final ResultSetWrapper rsw = new ResultSetWrapper(rs, configuration);
            handleRowValues(rsw, resultMap, resultHandler, new RowBounds(), null);
            metaParam.setValue(parameterMapping.getProperty(), resultHandler.getResultList());
        } finally {
            // issue #228 (close resultsets)
            closeResultSet(rs);
        }
    }

    /* --------------------------------------- 处理结果 ---------------------------------------------- */


    /**
     * 获取Statement执行完SQL后，对应的 java.sql.ResultSet 结果集
     *
     * @param stmt
     * @return
     * @throws SQLException
     */
    private ResultSetWrapper getFirstResultSet(Statement stmt) throws SQLException {
        // 获取结果集
        ResultSet rs = stmt.getResultSet();
        while (rs == null) {
            // 如果驱动程序没有将结果集作为第一个结果返回，则继续前进以获取第一个结果集（HSQLDB 2.1）
            // stmt.getMoreResults()：
            // 如果下一个结果是ResultSet对象，则为true；否则为false。
            // 如果是更新计数或没有更多结果，则返回false
            if (stmt.getMoreResults()) {
                rs = stmt.getResultSet();
            } else {
                // 当满足：stmt.getMoreResults() == false) && stmt.getUpdateCount() == -1 时，将不再有结果
                if (stmt.getUpdateCount() == -1) {
                    // no more results. Must be no resultset
                    break;
                }
            }
        }
        return rs != null ? new ResultSetWrapper(rs, configuration) : null;
    }
    /**
     * 校验结果集：1、rsw必须不能为空；2、配置文件必须配置resultType属性的返回对象类型
     *
     * @param rsw
     * @param resultMapCount
     */
    private void validateResultMapsCount(ResultSetWrapper rsw, int resultMapCount) {
        if (rsw != null && resultMapCount < 1) {
            // 比如在配置文件中：<select id="findAllEmployeer" resultType="alias_Employeer"> 这个 resultType 属性没有配置，则抛出异常
            throw new ExecutorException("A query was run and no Result Maps were found for the Mapped Statement '" + mappedStatement.getId()
                    + "'.  It's likely that neither a Result Type nor a Result Map was specified.");
        }
    }
    /**
     * 对单个结果集进行处理
     *
     * @param rsw                   封装 java.sql.ResultSet 对象
     * @param resultMap             对应Mybastic的<resultMap>标签配置
     * @param multipleResults
     * @param parentMapping         对应<resultMap>标签的extends属性配置，<resultMap>可以继承配置
     * @throws SQLException
     */
    private void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults, ResultMapping parentMapping) throws SQLException {
        try {
            if (parentMapping != null) {
                handleRowValues(rsw, resultMap, null, RowBounds.DEFAULT, parentMapping);
            } else {
                if (resultHandler == null) {
                    DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
                    handleRowValues(rsw, resultMap, defaultResultHandler, rowBounds, null);
                    multipleResults.add(defaultResultHandler.getResultList());
                } else {
                    handleRowValues(rsw, resultMap, resultHandler, rowBounds, null);
                }
            }
        } finally {
            // issue #228 (close resultsets)
            closeResultSet(rsw.getResultSet());
        }
    }
    /**
     *
     * @param rsw
     * @param resultMap
     * @param resultHandler
     * @param rowBounds
     * @param parentMapping
     * @throws SQLException
     */
    public void handleRowValues(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?> resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
        // 该resultMap是否存在嵌套
        if (resultMap.hasNestedResultMaps()) {
            // 检查是否带有offset和limit分页条件，如果safeRowBoundsEnabled = true（即启用行内嵌套语句），并且设置了分页条件，则报错
            ensureNoRowBounds();
            // 确定没有结果集处理器，否则抛出异常
            checkResultHandler();
            // 处理有嵌套的resultMap
            handleRowValuesForNestedResultMap(rsw, resultMap, resultHandler, rowBounds, parentMapping);
        } else {
            // 处理没有嵌套的resultMap
            handleRowValuesForSimpleResultMap(rsw, resultMap, resultHandler, rowBounds, parentMapping);
        }
    }
    private void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            // ignore
        }
    }

    /**
     * 检查是否带有offset和limit分页条件，如果safeRowBoundsEnabled = true（即启用行内嵌套语句），并且设置了分页条件，则报错
     */
    private void ensureNoRowBounds() {
        if (configuration.isSafeRowBoundsEnabled() && rowBounds != null && (rowBounds.getLimit() < RowBounds.NO_ROW_LIMIT || rowBounds.getOffset() > RowBounds.NO_ROW_OFFSET)) {
            throw new ExecutorException("Mapped Statements with nested result mappings cannot be safely constrained by RowBounds. "
                    + "Use safeRowBoundsEnabled=false setting to bypass this check.");
        }
    }
    protected void checkResultHandler() {
        if (resultHandler != null && configuration.isSafeResultHandlerEnabled() && !mappedStatement.isResultOrdered()) {
            // 带有嵌套结果映射的映射语句不能与自定义ResultHandler一起安全使用
            // 使用safeResultHandlerEnabled = false设置可以绕过此检查，或者确保您的语句返回有序数据并在其上设置resultOrdered = true。
            throw new ExecutorException("Mapped Statements with nested result mappings cannot be safely used with a custom ResultHandler. "
                    + "Use safeResultHandlerEnabled=false setting to bypass this check "
                    + "or ensure your statement returns ordered data and set resultOrdered=true on it.");
        }
    }
    // HANDLE NESTED RESULT MAPS
    private void handleRowValuesForNestedResultMap(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?> resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
        final DefaultResultContext<Object> resultContext = new DefaultResultContext<Object>();
        skipRows(rsw.getResultSet(), rowBounds);
        Object rowValue = previousRowValue;
        while (shouldProcessMoreRows(resultContext, rowBounds) && rsw.getResultSet().next()) {
            final ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(rsw.getResultSet(), resultMap, null);
            final CacheKey rowKey = createRowKey(discriminatedResultMap, rsw, null);
            Object partialObject = nestedResultObjects.get(rowKey);
            // issue #577 && #542
            if (mappedStatement.isResultOrdered()) {
                if (partialObject == null && rowValue != null) {
                    nestedResultObjects.clear();
                    storeObject(resultHandler, resultContext, rowValue, parentMapping, rsw.getResultSet());
                }
                rowValue = getRowValue(rsw, discriminatedResultMap, rowKey, null, partialObject);
            } else {
                rowValue = getRowValue(rsw, discriminatedResultMap, rowKey, null, partialObject);
                if (partialObject == null) {
                    storeObject(resultHandler, resultContext, rowValue, parentMapping, rsw.getResultSet());
                }
            }
        }
        if (rowValue != null && mappedStatement.isResultOrdered() && shouldProcessMoreRows(resultContext, rowBounds)) {
            storeObject(resultHandler, resultContext, rowValue, parentMapping, rsw.getResultSet());
            previousRowValue = null;
        } else if (rowValue != null) {
            previousRowValue = rowValue;
        }
    }
    /**
     *
     * @param rsw
     * @param resultMap
     * @param resultHandler
     * @param rowBounds
     * @param parentMapping
     * @throws SQLException
     */
    private void handleRowValuesForSimpleResultMap(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?> resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
        DefaultResultContext<Object> resultContext = new DefaultResultContext<Object>();
        // 将结果集的游标指向offset（起始索引位置）
        skipRows(rsw.getResultSet(), rowBounds);
        while (shouldProcessMoreRows(resultContext, rowBounds) && rsw.getResultSet().next()) {
            ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(rsw.getResultSet(), resultMap, null);
            Object rowValue = getRowValue(rsw, discriminatedResultMap);
            storeObject(resultHandler, resultContext, rowValue, parentMapping, rsw.getResultSet());
        }
    }
    private boolean shouldProcessMoreRows(ResultContext<?> context, RowBounds rowBounds) throws SQLException {
        return !context.isStopped() && context.getResultCount() < rowBounds.getLimit();
    }
    private void storeObject(ResultHandler<?> resultHandler, DefaultResultContext<Object> resultContext, Object rowValue, ResultMapping parentMapping, ResultSet rs) throws SQLException {
        if (parentMapping != null) {
            linkToParents(rs, parentMapping, rowValue);
        } else {
            callResultHandler(resultHandler, resultContext, rowValue);
        }
    }
    @SuppressWarnings("unchecked" /* because ResultHandler<?> is always ResultHandler<Object>*/)
    private void callResultHandler(ResultHandler<?> resultHandler, DefaultResultContext<Object> resultContext, Object rowValue) {
        resultContext.nextResultObject(rowValue);
        ((ResultHandler<Object>) resultHandler).handleResult(resultContext);
    }
    /**
     * 如果有多个结果集则返回第一个，否则直接返回
     *
     * @param multipleResults
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Object> collapseSingleResultList(List<Object> multipleResults) {
        return multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults;
    }
    // 获取下一个结果集
    private ResultSetWrapper getNextResultSet(Statement stmt) throws SQLException {
        // Making this method tolerant of bad JDBC drivers
        try {
            if (stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
                // Crazy Standard JDBC way of determining if there are more results
                if (!((!stmt.getMoreResults()) && (stmt.getUpdateCount() == -1))) {
                    ResultSet rs = stmt.getResultSet();
                    return rs != null ? new ResultSetWrapper(rs, configuration) : null;
                }
            }
        } catch (Exception e) {
            // Intentionally ignored.
        }
        return null;
    }
    private void cleanUpAfterHandlingResultSet() {
        nestedResultObjects.clear();
    }


    /**
     * 将结果集的游标指向offset（起始索引位置）
     *
     * @param rs
     * @param rowBounds
     * @throws SQLException
     */
    private void skipRows(ResultSet rs, RowBounds rowBounds) throws SQLException {
        if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
            if (rowBounds.getOffset() != RowBounds.NO_ROW_OFFSET) {
                rs.absolute(rowBounds.getOffset());
            }
        } else {
            // 将结果集的游标指向offset（起始索引位置）
            for (int i = 0; i < rowBounds.getOffset(); i++) {
                rs.next();
            }
        }
    }
    // DISCRIMINATOR
    public ResultMap resolveDiscriminatedResultMap(ResultSet rs, ResultMap resultMap, String columnPrefix) throws SQLException {
        Set<String> pastDiscriminators = new HashSet<String>();
        Discriminator discriminator = resultMap.getDiscriminator();
        while (discriminator != null) {
            final Object value = getDiscriminatorValue(rs, discriminator, columnPrefix);
            final String discriminatedMapId = discriminator.getMapIdFor(String.valueOf(value));
            if (configuration.hasResultMap(discriminatedMapId)) {
                resultMap = configuration.getResultMap(discriminatedMapId);
                Discriminator lastDiscriminator = discriminator;
                discriminator = resultMap.getDiscriminator();
                if (discriminator == lastDiscriminator || !pastDiscriminators.add(discriminatedMapId)) {
                    break;
                }
            } else {
                break;
            }
        }
        return resultMap;
    }

    /**
     * 将
     *
     * @param rsw
     * @param resultMap
     * @return
     * @throws SQLException
     */
    private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap) throws SQLException {
        final ResultLoaderMap lazyLoader = new ResultLoaderMap();
        Object rowValue = createResultObject(rsw, resultMap, lazyLoader, null);

        // 如果
        if (rowValue != null && !hasTypeHandlerForResultObject(rsw, resultMap.getType())) {
            final MetaObject metaObject = configuration.newMetaObject(rowValue);
            boolean foundValues = this.useConstructorMappings;

            // 是否启用字段自动映射
            if (shouldApplyAutomaticMappings(resultMap, false)) {
                foundValues = applyAutomaticMappings(rsw, resultMap, metaObject, null) || foundValues;
            }

            foundValues = applyPropertyMappings(rsw, resultMap, metaObject, lazyLoader, null) || foundValues;
            foundValues = lazyLoader.size() > 0 || foundValues;
            rowValue = (foundValues || configuration.isReturnInstanceForEmptyRow()) ? rowValue : null;
        }
        return rowValue;
    }

    /**
     * 是否启用字段自动映射
     * 返回<resultMap>标签中的 autoMapping 属性配置，isNested表示是否使用<setting>中的 autoMappingBehavior 配置
     *
     * 当自动映射查询结果时，MyBatis 会获取结果中返回的列名并在 Java 类中查找相同名字的属性（忽略大小写）。这意味着如果发现了ID列和id属性，
     * MyBatis 会将列 ID 的值赋给 id 属性。通常数据库列使用大写字母组成的单词命名，单词间用下划线分隔；而 Java 属性一般遵循驼峰命名法约定。
     * 为了在这两种命名方式之间启用自动映射，需要开启驼峰命名规则，将 mapUnderscoreToCamelCase 设置为 true。甚至在提供了结果映射后，自动映射也能工作。
     *
     * @param resultMap     <resultMap>配置信息
     * @param isNested      对应<setting name="autoMappingBehavior" value="PARTIAL"/>配置
     * @return
     */
    private boolean shouldApplyAutomaticMappings(ResultMap resultMap, boolean isNested) {
        if (resultMap.getAutoMapping() != null) {
            return resultMap.getAutoMapping();
        } else {
            if (isNested) {
                return AutoMappingBehavior.FULL == configuration.getAutoMappingBehavior();
            } else {
                return AutoMappingBehavior.NONE != configuration.getAutoMappingBehavior();
            }
        }
    }

    //
    // PROPERTY MAPPINGS
    //
    private boolean applyPropertyMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
        final List<String> mappedColumnNames = rsw.getMappedColumnNames(resultMap, columnPrefix);
        boolean foundValues = false;
        final List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();
        for (ResultMapping propertyMapping : propertyMappings) {
            String column = prependPrefix(propertyMapping.getColumn(), columnPrefix);
            if (propertyMapping.getNestedResultMapId() != null) {
                // the user added a column attribute to a nested result map, ignore it
                column = null;
            }
            if (propertyMapping.isCompositeResult()
                    || (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH)))
                    || propertyMapping.getResultSet() != null) {
                Object value = getPropertyMappingValue(rsw.getResultSet(), metaObject, propertyMapping, lazyLoader, columnPrefix);
                // issue #541 make property optional
                final String property = propertyMapping.getProperty();
                if (property == null) {
                    continue;
                } else if (value == DEFERED) {
                    foundValues = true;
                    continue;
                }
                if (value != null) {
                    foundValues = true;
                }
                if (value != null || (configuration.isCallSettersOnNulls() && !metaObject.getSetterType(property).isPrimitive())) {
                    // gcode issue #377, call setter on nulls (value is not 'found')
                    metaObject.setValue(property, value);
                }
            }
        }
        return foundValues;
    }

    private Object getPropertyMappingValue(ResultSet rs, MetaObject metaResultObject, ResultMapping propertyMapping, ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
        if (propertyMapping.getNestedQueryId() != null) {
            return getNestedQueryMappingValue(rs, metaResultObject, propertyMapping, lazyLoader, columnPrefix);
        } else if (propertyMapping.getResultSet() != null) {
            addPendingChildRelation(rs, metaResultObject, propertyMapping);   // TODO is that OK?
            return DEFERED;
        } else {
            final TypeHandler<?> typeHandler = propertyMapping.getTypeHandler();
            final String column = prependPrefix(propertyMapping.getColumn(), columnPrefix);
            return typeHandler.getResult(rs, column);
        }
    }

    private List<UnMappedColumnAutoMapping> createAutomaticMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String columnPrefix) throws SQLException {
        final String mapKey = resultMap.getId() + ":" + columnPrefix;
        List<UnMappedColumnAutoMapping> autoMapping = autoMappingsCache.get(mapKey);
        if (autoMapping == null) {
            autoMapping = new ArrayList<UnMappedColumnAutoMapping>();
            final List<String> unmappedColumnNames = rsw.getUnmappedColumnNames(resultMap, columnPrefix);
            for (String columnName : unmappedColumnNames) {
                String propertyName = columnName;
                if (columnPrefix != null && !columnPrefix.isEmpty()) {
                    // When columnPrefix is specified,
                    // ignore columns without the prefix.
                    if (columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
                        propertyName = columnName.substring(columnPrefix.length());
                    } else {
                        continue;
                    }
                }
                final String property = metaObject.findProperty(propertyName, configuration.isMapUnderscoreToCamelCase());
                if (property != null && metaObject.hasSetter(property)) {
                    if (resultMap.getMappedProperties().contains(property)) {
                        continue;
                    }
                    final Class<?> propertyType = metaObject.getSetterType(property);
                    if (typeHandlerRegistry.hasTypeHandler(propertyType, rsw.getJdbcType(columnName))) {
                        final TypeHandler<?> typeHandler = rsw.getTypeHandler(propertyType, columnName);
                        autoMapping.add(new UnMappedColumnAutoMapping(columnName, property, typeHandler, propertyType.isPrimitive()));
                    } else {
                        configuration.getAutoMappingUnknownColumnBehavior()
                                .doAction(mappedStatement, columnName, property, propertyType);
                    }
                } else {
                    configuration.getAutoMappingUnknownColumnBehavior()
                            .doAction(mappedStatement, columnName, (property != null) ? property : propertyName, null);
                }
            }
            autoMappingsCache.put(mapKey, autoMapping);
        }
        return autoMapping;
    }

    private boolean applyAutomaticMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String columnPrefix) throws SQLException {
        List<UnMappedColumnAutoMapping> autoMapping = createAutomaticMappings(rsw, resultMap, metaObject, columnPrefix);
        boolean foundValues = false;
        if (autoMapping.size() > 0) {
            for (UnMappedColumnAutoMapping mapping : autoMapping) {
                final Object value = mapping.typeHandler.getResult(rsw.getResultSet(), mapping.column);
                if (value != null) {
                    foundValues = true;
                }
                if (value != null || (configuration.isCallSettersOnNulls() && !mapping.primitive)) {
                    // gcode issue #377, call setter on nulls (value is not 'found')
                    metaObject.setValue(mapping.property, value);
                }
            }
        }
        return foundValues;
    }

    // MULTIPLE RESULT SETS

    private void linkToParents(ResultSet rs, ResultMapping parentMapping, Object rowValue) throws SQLException {
        CacheKey parentKey = createKeyForMultipleResults(rs, parentMapping, parentMapping.getColumn(), parentMapping.getForeignColumn());
        List<PendingRelation> parents = pendingRelations.get(parentKey);
        if (parents != null) {
            for (PendingRelation parent : parents) {
                if (parent != null && rowValue != null) {
                    linkObjects(parent.metaObject, parent.propertyMapping, rowValue);
                }
            }
        }
    }

    private void addPendingChildRelation(ResultSet rs, MetaObject metaResultObject, ResultMapping parentMapping) throws SQLException {
        CacheKey cacheKey = createKeyForMultipleResults(rs, parentMapping, parentMapping.getColumn(), parentMapping.getColumn());
        PendingRelation deferLoad = new PendingRelation();
        deferLoad.metaObject = metaResultObject;
        deferLoad.propertyMapping = parentMapping;
        List<PendingRelation> relations = pendingRelations.get(cacheKey);
        // issue #255
        if (relations == null) {
            relations = new ArrayList<DefaultResultSetHandler.PendingRelation>();
            pendingRelations.put(cacheKey, relations);
        }
        relations.add(deferLoad);
        ResultMapping previous = nextResultMaps.get(parentMapping.getResultSet());
        if (previous == null) {
            nextResultMaps.put(parentMapping.getResultSet(), parentMapping);
        } else {
            if (!previous.equals(parentMapping)) {
                throw new ExecutorException("Two different properties are mapped to the same resultSet");
            }
        }
    }

    private CacheKey createKeyForMultipleResults(ResultSet rs, ResultMapping resultMapping, String names, String columns) throws SQLException {
        CacheKey cacheKey = new CacheKey();
        cacheKey.update(resultMapping);
        if (columns != null && names != null) {
            String[] columnsArray = columns.split(",");
            String[] namesArray = names.split(",");
            for (int i = 0; i < columnsArray.length; i++) {
                Object value = rs.getString(columnsArray[i]);
                if (value != null) {
                    cacheKey.update(namesArray[i]);
                    cacheKey.update(value);
                }
            }
        }
        return cacheKey;
    }

    //
    // INSTANTIATION & CONSTRUCTOR MAPPING
    //

    /**
     *
     * @param rsw
     * @param resultMap
     * @param lazyLoader
     * @param columnPrefix
     * @return
     * @throws SQLException
     */
    private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
        // 重置先前的映射结果
        this.useConstructorMappings = false;
        final List<Class<?>> constructorArgTypes = new ArrayList<Class<?>>();
        final List<Object> constructorArgs = new ArrayList<Object>();
        Object resultObject = createResultObject(rsw, resultMap, constructorArgTypes, constructorArgs, columnPrefix);
        if (resultObject != null && !hasTypeHandlerForResultObject(rsw, resultMap.getType())) {
            final List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();
            for (ResultMapping propertyMapping : propertyMappings) {
                // issue gcode #109 && issue #149
                if (propertyMapping.getNestedQueryId() != null && propertyMapping.isLazy()) {
                    resultObject = configuration.getProxyFactory().createProxy(resultObject, lazyLoader, configuration, objectFactory, constructorArgTypes, constructorArgs);
                    break;
                }
            }
        }

        // 设置当前映射结果
        this.useConstructorMappings = (resultObject != null && !constructorArgTypes.isEmpty());
        return resultObject;
    }

    /**
     * 返回对应的TypeHanler
     *
     * @param rsw
     * @param resultMap
     * @param constructorArgTypes
     * @param constructorArgs
     * @param columnPrefix
     * @return
     * @throws SQLException
     */
    private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix) throws SQLException {
        // 对应<resultMap type = "">
        final Class<?> resultType = resultMap.getType();
        // 获取类类型的元数据信息
        final MetaClass metaType = MetaClass.forClass(resultType, reflectorFactory);
        // 获取构造器配置
        final List<ResultMapping> constructorMappings = resultMap.getConstructorResultMappings();

        // 判断指定的类类型是否有对应的TypeHandler
        if (hasTypeHandlerForResultObject(rsw, resultType)) {
            return createPrimitiveResultObject(rsw, resultMap, columnPrefix);
        } else if (!constructorMappings.isEmpty()) {
            return createParameterizedResultObject(rsw, resultType, constructorMappings, constructorArgTypes, constructorArgs, columnPrefix);
        } else if (resultType.isInterface() || metaType.hasDefaultConstructor()) {
            // 使用默认构造器，创建一个resultType的实例，例如，new User()
            return objectFactory.create(resultType);
        } else if (shouldApplyAutomaticMappings(resultMap, false)) {
            return createByConstructorSignature(rsw, resultType, constructorArgTypes, constructorArgs, columnPrefix);
        }
        throw new ExecutorException("Do not know how to create an instance of " + resultType);
    }

    Object createParameterizedResultObject(ResultSetWrapper rsw, Class<?> resultType, List<ResultMapping> constructorMappings, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix) {
        boolean foundValues = false;
        for (ResultMapping constructorMapping : constructorMappings) {
            final Class<?> parameterType = constructorMapping.getJavaType();
            final String column = constructorMapping.getColumn();
            final Object value;
            try {
                if (constructorMapping.getNestedQueryId() != null) {
                    value = getNestedQueryConstructorValue(rsw.getResultSet(), constructorMapping, columnPrefix);
                } else if (constructorMapping.getNestedResultMapId() != null) {
                    final ResultMap resultMap = configuration.getResultMap(constructorMapping.getNestedResultMapId());
                    value = getRowValue(rsw, resultMap);
                } else {
                    final TypeHandler<?> typeHandler = constructorMapping.getTypeHandler();
                    value = typeHandler.getResult(rsw.getResultSet(), prependPrefix(column, columnPrefix));
                }
            } catch (ResultMapException e) {
                throw new ExecutorException("Could not process result for mapping: " + constructorMapping, e);
            } catch (SQLException e) {
                throw new ExecutorException("Could not process result for mapping: " + constructorMapping, e);
            }
            constructorArgTypes.add(parameterType);
            constructorArgs.add(value);
            foundValues = value != null || foundValues;
        }
        return foundValues ? objectFactory.create(resultType, constructorArgTypes, constructorArgs) : null;
    }

    private Object createByConstructorSignature(ResultSetWrapper rsw, Class<?> resultType, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix) throws SQLException {
        final Constructor<?>[] constructors = resultType.getDeclaredConstructors();
        final Constructor<?> annotatedConstructor = findAnnotatedConstructor(constructors);
        if (annotatedConstructor != null) {
            return createUsingConstructor(rsw, resultType, constructorArgTypes, constructorArgs, columnPrefix, annotatedConstructor);
        } else {
            for (Constructor<?> constructor : constructors) {
                if (allowedConstructor(constructor, rsw.getClassNames())) {
                    return createUsingConstructor(rsw, resultType, constructorArgTypes, constructorArgs, columnPrefix, constructor);
                }
            }
        }
        throw new ExecutorException("No constructor found in " + resultType.getName() + " matching " + rsw.getClassNames());
    }

    private Object createUsingConstructor(ResultSetWrapper rsw, Class<?> resultType, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix, Constructor<?> constructor) throws SQLException {
        boolean foundValues = false;
        for (int i = 0; i < constructor.getParameterTypes().length; i++) {
            Class<?> parameterType = constructor.getParameterTypes()[i];
            String columnName = rsw.getColumnNames().get(i);
            TypeHandler<?> typeHandler = rsw.getTypeHandler(parameterType, columnName);
            Object value = typeHandler.getResult(rsw.getResultSet(), prependPrefix(columnName, columnPrefix));
            constructorArgTypes.add(parameterType);
            constructorArgs.add(value);
            foundValues = value != null || foundValues;
        }
        return foundValues ? objectFactory.create(resultType, constructorArgTypes, constructorArgs) : null;
    }

    private Constructor<?> findAnnotatedConstructor(final Constructor<?>[] constructors) {
        for (final Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(AutomapConstructor.class)) {
                return constructor;
            }
        }
        return null;
    }

    private boolean allowedConstructor(final Constructor<?> constructor, final List<String> classNames) {
        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        if (typeNames(parameterTypes).equals(classNames)) return true;
        if (parameterTypes.length != classNames.size()) return false;
        for (int i = 0; i < parameterTypes.length; i++) {
            final Class<?> parameterType = parameterTypes[i];
            if (parameterType.isPrimitive() && !primitiveTypes.getWrapper(parameterType).getName().equals(classNames.get(i))) {
                return false;
            } else if (!parameterType.isPrimitive() && !parameterType.getName().equals(classNames.get(i))) {
                return false;
            }
        }
        return true;
    }

    private List<String> typeNames(Class<?>[] parameterTypes) {
        List<String> names = new ArrayList<String>();
        for (Class<?> type : parameterTypes) {
            names.add(type.getName());
        }
        return names;
    }


    private Object createPrimitiveResultObject(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix) throws SQLException {
        // 对应<resultMap type = "">
        final Class<?> resultType = resultMap.getType();

        final String columnName;
        if (!resultMap.getResultMappings().isEmpty()) {
            final List<ResultMapping> resultMappingList = resultMap.getResultMappings();
            final ResultMapping mapping = resultMappingList.get(0);
            // columnName
            columnName = prependPrefix(mapping.getColumn(), columnPrefix);
        } else {
            columnName = rsw.getColumnNames().get(0);
        }

        // 获取数据库字段名对应TypeHandler
        final TypeHandler<?> typeHandler = rsw.getTypeHandler(resultType, columnName);
        // 获取jdbcType对应的javaType
        return typeHandler.getResult(rsw.getResultSet(), columnName);
    }

    //
    // NESTED QUERY
    //

    private Object getNestedQueryConstructorValue(ResultSet rs, ResultMapping constructorMapping, String columnPrefix) throws SQLException {
        final String nestedQueryId = constructorMapping.getNestedQueryId();
        final MappedStatement nestedQuery = configuration.getMappedStatement(nestedQueryId);
        final Class<?> nestedQueryParameterType = nestedQuery.getParameterMap().getType();
        final Object nestedQueryParameterObject = prepareParameterForNestedQuery(rs, constructorMapping, nestedQueryParameterType, columnPrefix);
        Object value = null;
        if (nestedQueryParameterObject != null) {
            final BoundSql nestedBoundSql = nestedQuery.getBoundSql(nestedQueryParameterObject);
            final CacheKey key = executor.createCacheKey(nestedQuery, nestedQueryParameterObject, RowBounds.DEFAULT, nestedBoundSql);
            final Class<?> targetType = constructorMapping.getJavaType();
            final ResultLoader resultLoader = new ResultLoader(configuration, executor, nestedQuery, nestedQueryParameterObject, targetType, key, nestedBoundSql);
            value = resultLoader.loadResult();
        }
        return value;
    }

    private Object getNestedQueryMappingValue(ResultSet rs, MetaObject metaResultObject, ResultMapping propertyMapping, ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
        final String nestedQueryId = propertyMapping.getNestedQueryId();
        final String property = propertyMapping.getProperty();
        final MappedStatement nestedQuery = configuration.getMappedStatement(nestedQueryId);
        final Class<?> nestedQueryParameterType = nestedQuery.getParameterMap().getType();
        final Object nestedQueryParameterObject = prepareParameterForNestedQuery(rs, propertyMapping, nestedQueryParameterType, columnPrefix);
        Object value = null;
        if (nestedQueryParameterObject != null) {
            final BoundSql nestedBoundSql = nestedQuery.getBoundSql(nestedQueryParameterObject);
            final CacheKey key = executor.createCacheKey(nestedQuery, nestedQueryParameterObject, RowBounds.DEFAULT, nestedBoundSql);
            final Class<?> targetType = propertyMapping.getJavaType();
            // 如果有缓存的话，会将结果集，缓存到执行器中，CacheExecutor会再查下前先检查一下缓存
            if (executor.isCached(nestedQuery, key)) {
                executor.deferLoad(nestedQuery, metaResultObject, property, key, targetType);
                value = DEFERED;
            } else {
                final ResultLoader resultLoader = new ResultLoader(configuration, executor, nestedQuery, nestedQueryParameterObject, targetType, key, nestedBoundSql);
                if (propertyMapping.isLazy()) {
                    lazyLoader.addLoader(property, metaResultObject, resultLoader);
                    value = DEFERED;
                } else {
                    value = resultLoader.loadResult();
                }
            }
        }
        return value;
    }

    private Object prepareParameterForNestedQuery(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType, String columnPrefix) throws SQLException {
        if (resultMapping.isCompositeResult()) {
            return prepareCompositeKeyParameter(rs, resultMapping, parameterType, columnPrefix);
        } else {
            return prepareSimpleKeyParameter(rs, resultMapping, parameterType, columnPrefix);
        }
    }

    private Object prepareSimpleKeyParameter(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType, String columnPrefix) throws SQLException {
        final TypeHandler<?> typeHandler;
        if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
            typeHandler = typeHandlerRegistry.getTypeHandler(parameterType);
        } else {
            typeHandler = typeHandlerRegistry.getUnknownTypeHandler();
        }
        return typeHandler.getResult(rs, prependPrefix(resultMapping.getColumn(), columnPrefix));
    }

    private Object prepareCompositeKeyParameter(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType, String columnPrefix) throws SQLException {
        final Object parameterObject = instantiateParameterObject(parameterType);
        final MetaObject metaObject = configuration.newMetaObject(parameterObject);
        boolean foundValues = false;
        for (ResultMapping innerResultMapping : resultMapping.getComposites()) {
            final Class<?> propType = metaObject.getSetterType(innerResultMapping.getProperty());
            final TypeHandler<?> typeHandler = typeHandlerRegistry.getTypeHandler(propType);
            final Object propValue = typeHandler.getResult(rs, prependPrefix(innerResultMapping.getColumn(), columnPrefix));
            // issue #353 & #560 do not execute nested query if key is null
            if (propValue != null) {
                metaObject.setValue(innerResultMapping.getProperty(), propValue);
                foundValues = true;
            }
        }
        return foundValues ? parameterObject : null;
    }

    private Object instantiateParameterObject(Class<?> parameterType) {
        if (parameterType == null) {
            return new HashMap<Object, Object>();
        } else if (ParamMap.class.equals(parameterType)) {
            return new HashMap<Object, Object>(); // issue #649
        } else {
            return objectFactory.create(parameterType);
        }
    }



    private Object getDiscriminatorValue(ResultSet rs, Discriminator discriminator, String columnPrefix) throws SQLException {
        final ResultMapping resultMapping = discriminator.getResultMapping();
        final TypeHandler<?> typeHandler = resultMapping.getTypeHandler();
        return typeHandler.getResult(rs, prependPrefix(resultMapping.getColumn(), columnPrefix));
    }

    /**
     * 给columnName追加前缀
     *
     * @param columnName
     * @param prefix
     * @return
     */
    private String prependPrefix(String columnName, String prefix) {
        if (columnName == null || columnName.length() == 0 || prefix == null || prefix.length() == 0) {
            return columnName;
        }
        return prefix + columnName;
    }


    //
    // GET VALUE FROM ROW FOR NESTED RESULT MAP
    //

    private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap, CacheKey combinedKey, String columnPrefix, Object partialObject) throws SQLException {
        final String resultMapId = resultMap.getId();
        Object rowValue = partialObject;
        if (rowValue != null) {
            final MetaObject metaObject = configuration.newMetaObject(rowValue);
            putAncestor(rowValue, resultMapId, columnPrefix);
            applyNestedResultMappings(rsw, resultMap, metaObject, columnPrefix, combinedKey, false);
            ancestorObjects.remove(resultMapId);
        } else {
            final ResultLoaderMap lazyLoader = new ResultLoaderMap();
            rowValue = createResultObject(rsw, resultMap, lazyLoader, columnPrefix);
            if (rowValue != null && !hasTypeHandlerForResultObject(rsw, resultMap.getType())) {
                final MetaObject metaObject = configuration.newMetaObject(rowValue);
                boolean foundValues = this.useConstructorMappings;
                if (shouldApplyAutomaticMappings(resultMap, true)) {
                    foundValues = applyAutomaticMappings(rsw, resultMap, metaObject, columnPrefix) || foundValues;
                }
                foundValues = applyPropertyMappings(rsw, resultMap, metaObject, lazyLoader, columnPrefix) || foundValues;
                putAncestor(rowValue, resultMapId, columnPrefix);
                foundValues = applyNestedResultMappings(rsw, resultMap, metaObject, columnPrefix, combinedKey, true) || foundValues;
                ancestorObjects.remove(resultMapId);
                foundValues = lazyLoader.size() > 0 || foundValues;
                rowValue = (foundValues || configuration.isReturnInstanceForEmptyRow()) ? rowValue : null;
            }
            if (combinedKey != CacheKey.NULL_CACHE_KEY) {
                nestedResultObjects.put(combinedKey, rowValue);
            }
        }
        return rowValue;
    }

    private void putAncestor(Object resultObject, String resultMapId, String columnPrefix) {
        ancestorObjects.put(resultMapId, resultObject);
    }

    //
    // NESTED RESULT MAP (JOIN MAPPING)
    //

    private boolean applyNestedResultMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String parentPrefix, CacheKey parentRowKey, boolean newObject) {
        boolean foundValues = false;
        for (ResultMapping resultMapping : resultMap.getPropertyResultMappings()) {
            final String nestedResultMapId = resultMapping.getNestedResultMapId();
            if (nestedResultMapId != null && resultMapping.getResultSet() == null) {
                try {
                    final String columnPrefix = getColumnPrefix(parentPrefix, resultMapping);
                    final ResultMap nestedResultMap = getNestedResultMap(rsw.getResultSet(), nestedResultMapId, columnPrefix);
                    if (resultMapping.getColumnPrefix() == null) {
                        // try to fill circular reference only when columnPrefix
                        // is not specified for the nested result map (issue #215)
                        Object ancestorObject = ancestorObjects.get(nestedResultMapId);
                        if (ancestorObject != null) {
                            if (newObject) {
                                linkObjects(metaObject, resultMapping, ancestorObject); // issue #385
                            }
                            continue;
                        }
                    }
                    final CacheKey rowKey = createRowKey(nestedResultMap, rsw, columnPrefix);
                    final CacheKey combinedKey = combineKeys(rowKey, parentRowKey);
                    Object rowValue = nestedResultObjects.get(combinedKey);
                    boolean knownValue = (rowValue != null);
                    instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject); // mandatory
                    if (anyNotNullColumnHasValue(resultMapping, columnPrefix, rsw)) {
                        rowValue = getRowValue(rsw, nestedResultMap, combinedKey, columnPrefix, rowValue);
                        if (rowValue != null && !knownValue) {
                            linkObjects(metaObject, resultMapping, rowValue);
                            foundValues = true;
                        }
                    }
                } catch (SQLException e) {
                    throw new ExecutorException("Error getting nested result map values for '" + resultMapping.getProperty() + "'.  Cause: " + e, e);
                }
            }
        }
        return foundValues;
    }

    private String getColumnPrefix(String parentPrefix, ResultMapping resultMapping) {
        final StringBuilder columnPrefixBuilder = new StringBuilder();
        if (parentPrefix != null) {
            columnPrefixBuilder.append(parentPrefix);
        }
        if (resultMapping.getColumnPrefix() != null) {
            columnPrefixBuilder.append(resultMapping.getColumnPrefix());
        }
        return columnPrefixBuilder.length() == 0 ? null : columnPrefixBuilder.toString().toUpperCase(Locale.ENGLISH);
    }

    private boolean anyNotNullColumnHasValue(ResultMapping resultMapping, String columnPrefix, ResultSetWrapper rsw) throws SQLException {
        Set<String> notNullColumns = resultMapping.getNotNullColumns();
        if (notNullColumns != null && !notNullColumns.isEmpty()) {
            ResultSet rs = rsw.getResultSet();
            for (String column : notNullColumns) {
                rs.getObject(prependPrefix(column, columnPrefix));
                if (!rs.wasNull()) {
                    return true;
                }
            }
            return false;
        } else if (columnPrefix != null) {
            for (String columnName : rsw.getColumnNames()) {
                if (columnName.toUpperCase().startsWith(columnPrefix.toUpperCase())) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private ResultMap getNestedResultMap(ResultSet rs, String nestedResultMapId, String columnPrefix) throws SQLException {
        ResultMap nestedResultMap = configuration.getResultMap(nestedResultMapId);
        return resolveDiscriminatedResultMap(rs, nestedResultMap, columnPrefix);
    }

    //
    // UNIQUE RESULT KEY
    //

    private CacheKey createRowKey(ResultMap resultMap, ResultSetWrapper rsw, String columnPrefix) throws SQLException {
        final CacheKey cacheKey = new CacheKey();
        cacheKey.update(resultMap.getId());
        List<ResultMapping> resultMappings = getResultMappingsForRowKey(resultMap);
        if (resultMappings.size() == 0) {
            if (Map.class.isAssignableFrom(resultMap.getType())) {
                createRowKeyForMap(rsw, cacheKey);
            } else {
                createRowKeyForUnmappedProperties(resultMap, rsw, cacheKey, columnPrefix);
            }
        } else {
            createRowKeyForMappedProperties(resultMap, rsw, cacheKey, resultMappings, columnPrefix);
        }
        if (cacheKey.getUpdateCount() < 2) {
            return CacheKey.NULL_CACHE_KEY;
        }
        return cacheKey;
    }

    private CacheKey combineKeys(CacheKey rowKey, CacheKey parentRowKey) {
        if (rowKey.getUpdateCount() > 1 && parentRowKey.getUpdateCount() > 1) {
            CacheKey combinedKey;
            try {
                combinedKey = rowKey.clone();
            } catch (CloneNotSupportedException e) {
                throw new ExecutorException("Error cloning cache key.  Cause: " + e, e);
            }
            combinedKey.update(parentRowKey);
            return combinedKey;
        }
        return CacheKey.NULL_CACHE_KEY;
    }

    private List<ResultMapping> getResultMappingsForRowKey(ResultMap resultMap) {
        List<ResultMapping> resultMappings = resultMap.getIdResultMappings();
        if (resultMappings.size() == 0) {
            resultMappings = resultMap.getPropertyResultMappings();
        }
        return resultMappings;
    }

    private void createRowKeyForMappedProperties(ResultMap resultMap, ResultSetWrapper rsw, CacheKey cacheKey, List<ResultMapping> resultMappings, String columnPrefix) throws SQLException {
        for (ResultMapping resultMapping : resultMappings) {
            if (resultMapping.getNestedResultMapId() != null && resultMapping.getResultSet() == null) {
                // Issue #392
                final ResultMap nestedResultMap = configuration.getResultMap(resultMapping.getNestedResultMapId());
                createRowKeyForMappedProperties(nestedResultMap, rsw, cacheKey, nestedResultMap.getConstructorResultMappings(),
                        prependPrefix(resultMapping.getColumnPrefix(), columnPrefix));
            } else if (resultMapping.getNestedQueryId() == null) {
                final String column = prependPrefix(resultMapping.getColumn(), columnPrefix);
                final TypeHandler<?> th = resultMapping.getTypeHandler();
                List<String> mappedColumnNames = rsw.getMappedColumnNames(resultMap, columnPrefix);
                // Issue #114
                if (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH))) {
                    final Object value = th.getResult(rsw.getResultSet(), column);
                    if (value != null || configuration.isReturnInstanceForEmptyRow()) {
                        cacheKey.update(column);
                        cacheKey.update(value);
                    }
                }
            }
        }
    }

    private void createRowKeyForUnmappedProperties(ResultMap resultMap, ResultSetWrapper rsw, CacheKey cacheKey, String columnPrefix) throws SQLException {
        final MetaClass metaType = MetaClass.forClass(resultMap.getType(), reflectorFactory);
        List<String> unmappedColumnNames = rsw.getUnmappedColumnNames(resultMap, columnPrefix);
        for (String column : unmappedColumnNames) {
            String property = column;
            if (columnPrefix != null && !columnPrefix.isEmpty()) {
                // When columnPrefix is specified, ignore columns without the prefix.
                if (column.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
                    property = column.substring(columnPrefix.length());
                } else {
                    continue;
                }
            }
            if (metaType.findProperty(property, configuration.isMapUnderscoreToCamelCase()) != null) {
                String value = rsw.getResultSet().getString(column);
                if (value != null) {
                    cacheKey.update(column);
                    cacheKey.update(value);
                }
            }
        }
    }

    private void createRowKeyForMap(ResultSetWrapper rsw, CacheKey cacheKey) throws SQLException {
        List<String> columnNames = rsw.getColumnNames();
        for (String columnName : columnNames) {
            final String value = rsw.getResultSet().getString(columnName);
            if (value != null) {
                cacheKey.update(columnName);
                cacheKey.update(value);
            }
        }
    }

    private void linkObjects(MetaObject metaObject, ResultMapping resultMapping, Object rowValue) {
        final Object collectionProperty = instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject);
        if (collectionProperty != null) {
            final MetaObject targetMetaObject = configuration.newMetaObject(collectionProperty);
            targetMetaObject.add(rowValue);
        } else {
            metaObject.setValue(resultMapping.getProperty(), rowValue);
        }
    }

    private Object instantiateCollectionPropertyIfAppropriate(ResultMapping resultMapping, MetaObject metaObject) {
        final String propertyName = resultMapping.getProperty();
        Object propertyValue = metaObject.getValue(propertyName);
        if (propertyValue == null) {
            Class<?> type = resultMapping.getJavaType();
            if (type == null) {
                type = metaObject.getSetterType(propertyName);
            }
            try {
                if (objectFactory.isCollection(type)) {
                    propertyValue = objectFactory.create(type);
                    metaObject.setValue(propertyName, propertyValue);
                    return propertyValue;
                }
            } catch (Exception e) {
                throw new ExecutorException("Error instantiating collection property for result '" + resultMapping.getProperty() + "'.  Cause: " + e, e);
            }
        } else if (objectFactory.isCollection(propertyValue.getClass())) {
            return propertyValue;
        }
        return null;
    }

    /**
     * 判断指定的类是否有对应的TypeHandler
     *
     * @param rsw
     * @param resultType
     * @return
     */
    private boolean hasTypeHandlerForResultObject(ResultSetWrapper rsw, Class<?> resultType) {
        // 返回列就一列，说明返回的可能是应用行数
        if (rsw.getColumnNames().size() == 1) {
            return typeHandlerRegistry.hasTypeHandler(resultType, rsw.getJdbcType(rsw.getColumnNames().get(0)));
        }

        // 自定义类型
        return typeHandlerRegistry.hasTypeHandler(resultType);
    }

}
