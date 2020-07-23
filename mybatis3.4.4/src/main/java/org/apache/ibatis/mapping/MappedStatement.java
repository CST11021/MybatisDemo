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
package org.apache.ibatis.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.whz.entity.Employeer;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;

/**
 * 在mybatis中我们将select|insert|update|delete 这些配置节点信息抽象为MappedStatement
 *
 * @author Clinton Begin
 */
public final class MappedStatement {

    /** 表示该mapper接口对应的配置的所在配置文件名，如：IEmployeerMapper.xml */
    private String resource;
    /** 全局配置的引用 */
    private Configuration configuration;

    /**
     * 表示该MappedStatement对应的id，这里使用方法名作为id，每个id对应一个MappedStatement应用，在解析过程中，同一个MappedStatement对象对应两个不同的id，如：
     * 1、key:findEmployeerByID
     * 2、key:com.whz.mapperinterface.IEmployeerMapper.findEmployeerByID
     * 这样就可以使用如下两个方式调用对应的Mapper接口
     * Employeer employeer = session.selectOne("findEmployeerByID", 5);
     * Employeer employeer = session.selectOne("com.whz.mapperinterface.IEmployeerMapper.findEmployeerByID", 5);
     */
    private String id;
    /** 如果没有特别指定fetchSize，默认为null */
    private Integer fetchSize;
    /** 如果没有特别指定timeout，默认为null */
    private Integer timeout;
    /** 表示 Statement 接口的实现类型，对应CURD SQL的 statementType 属性配置，Mybastic中有STATEMENT, PREPARED, CALLABLE三种类型，分别对应底层JDBC执行数据库操作的三种实现方式，它们分别是：Statement、PreparedStatement和CallableStatement的三种方式 */
    private StatementType statementType;
    /** 对应CURD SQL的 resultSetType 属性配置 */
    private ResultSetType resultSetType;
    /** 封装对应的SQL语句，SqlSource接口有多个实现，解析时根据不同的sql类型，使用不同的实现类 */
    private SqlSource sqlSource;
    /** 对应二级缓存的缓存实例，二级缓存是mapper级别的缓存，也就是同一个namespace的mappe.xml，当多个SqlSession使用同一个Mapper操作数据库的时候，得到的数据会缓存在同一个二级缓存区域 */
    private Cache cache;
    /** 封装对应的 parameterType 属性配置 */
    private ParameterMap parameterMap;
    /** 封装对应的<resultMap>标签配置，一个Mapper接口可以配置多个<resultMap>标签，所以是一个集合类型 */
    private List<ResultMap> resultMaps;
    /** 对应flushCache 属性配置，缓存相关配置表示是否及时清空缓存 */
    private boolean flushCacheRequired;
    /** 对应的CURD SQL的 useCache 属性配置，表示是否启用/禁用二级缓存 */
    private boolean useCache;
    /** 这个设置仅针对嵌套结果 select 语句：如果为 true，将会假设包含了嵌套结果集或是分组，当返回一个主结果行时，就不会产生对前面结果集的引用。 这就使得在获取嵌套结果集的时候不至于内存不够用。默认值：false。 */
    private boolean resultOrdered;
    private SqlCommandType sqlCommandType;
    /** 对应返回主键相关配置，如：useGeneratedKeys="true" keyProperty="employeer_id" */
    private KeyGenerator keyGenerator;
    /** selectKey 语句结果应该被设置到的目标属性。如果生成列不止一个，可以用逗号分隔多个属性名称 */
    private String[] keyProperties;
    /** 返回结果集中生成列属性的列名。如果生成列不止一个，可以用逗号分隔多个属性名称 */
    private String[] keyColumns;

    private boolean hasNestedResultMaps;
    /** 如果配置了数据库厂商标识（databaseIdProvider），MyBatis 会加载所有不带 databaseId 或匹配当前 databaseId 的语句；如果带和不带的语句都有，则不带的会被忽略 */
    private String databaseId;
    /** 表示相应的日志实现 */
    private Log statementLog;
    private LanguageDriver lang;
    /** 这个设置仅适用于多结果集的情况。它将列出语句执行后返回的结果集并赋予每个结果集一个名称，多个名称之间以逗号分隔 */
    private String[] resultSets;

    /**
     * 构造器
     *
     * @return
     */
    MappedStatement() {
        // constructor disabled
    }


    /**
     * 使用建造者模式构建 MappedStatement 对象
     */
    public static class Builder {

        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration, String id, SqlSource sqlSource, SqlCommandType sqlCommandType) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlSource = sqlSource;
            mappedStatement.statementType = StatementType.PREPARED;
            mappedStatement.parameterMap = new ParameterMap.Builder(configuration, "defaultParameterMap", null, new ArrayList<ParameterMapping>()).build();
            mappedStatement.resultMaps = new ArrayList<ResultMap>();
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType) ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
            String logId = id;
            if (configuration.getLogPrefix() != null) {
                logId = configuration.getLogPrefix() + id;
            }
            mappedStatement.statementLog = LogFactory.getLog(logId);
            mappedStatement.lang = configuration.getDefaultScriptingLanguageInstance();
        }

        public Builder resource(String resource) {
            mappedStatement.resource = resource;
            return this;
        }
        public String id() {
            return mappedStatement.id;
        }
        public Builder parameterMap(ParameterMap parameterMap) {
            mappedStatement.parameterMap = parameterMap;
            return this;
        }
        public Builder resultMaps(List<ResultMap> resultMaps) {
            mappedStatement.resultMaps = resultMaps;
            for (ResultMap resultMap : resultMaps) {
                mappedStatement.hasNestedResultMaps = mappedStatement.hasNestedResultMaps || resultMap.hasNestedResultMaps();
            }
            return this;
        }
        public Builder fetchSize(Integer fetchSize) {
            mappedStatement.fetchSize = fetchSize;
            return this;
        }
        public Builder timeout(Integer timeout) {
            mappedStatement.timeout = timeout;
            return this;
        }
        public Builder statementType(StatementType statementType) {
            mappedStatement.statementType = statementType;
            return this;
        }
        public Builder resultSetType(ResultSetType resultSetType) {
            mappedStatement.resultSetType = resultSetType;
            return this;
        }
        public Builder cache(Cache cache) {
            mappedStatement.cache = cache;
            return this;
        }
        public Builder flushCacheRequired(boolean flushCacheRequired) {
            mappedStatement.flushCacheRequired = flushCacheRequired;
            return this;
        }
        public Builder useCache(boolean useCache) {
            mappedStatement.useCache = useCache;
            return this;
        }
        public Builder resultOrdered(boolean resultOrdered) {
            mappedStatement.resultOrdered = resultOrdered;
            return this;
        }
        public Builder keyGenerator(KeyGenerator keyGenerator) {
            mappedStatement.keyGenerator = keyGenerator;
            return this;
        }
        public Builder keyProperty(String keyProperty) {
            mappedStatement.keyProperties = delimitedStringToArray(keyProperty);
            return this;
        }
        public Builder keyColumn(String keyColumn) {
            mappedStatement.keyColumns = delimitedStringToArray(keyColumn);
            return this;
        }
        public Builder databaseId(String databaseId) {
            mappedStatement.databaseId = databaseId;
            return this;
        }
        public Builder lang(LanguageDriver driver) {
            mappedStatement.lang = driver;
            return this;
        }
        public Builder resultSets(String resultSet) {
            mappedStatement.resultSets = delimitedStringToArray(resultSet);
            return this;
        }

        @Deprecated
        public Builder resulSets(String resultSet) {
            mappedStatement.resultSets = delimitedStringToArray(resultSet);
            return this;
        }

        public MappedStatement build() {
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            assert mappedStatement.sqlSource != null;
            assert mappedStatement.lang != null;
            mappedStatement.resultMaps = Collections.unmodifiableList(mappedStatement.resultMaps);
            return mappedStatement;
        }
    }
    /**
     * 将输入的字符串","分隔，以数组的方式返回
     *
     * @param in
     * @return
     */
    private static String[] delimitedStringToArray(String in) {
        if (in == null || in.trim().length() == 0) {
            return null;
        } else {
            return in.split(",");
        }
    }


    /**
     * MapperdStatement本身封装了sql执行语句，这里配合sql的占位符参数，将它们封装为一个BoundSql对象。
     * BoundSql封装了每次向数据库发起执行SQL命令的具体执行语句信息
     *
     * @param parameterObject
     * @return
     */
    public BoundSql getBoundSql(Object parameterObject) {
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings == null || parameterMappings.isEmpty()) {
            boundSql = new BoundSql(configuration, boundSql.getSql(), parameterMap.getParameterMappings(), parameterObject);
        }

        // check for nested result maps in parameter mappings (issue #30)
        for (ParameterMapping pm : boundSql.getParameterMappings()) {
            String rmId = pm.getResultMapId();
            if (rmId != null) {
                ResultMap rm = configuration.getResultMap(rmId);
                if (rm != null) {
                    hasNestedResultMaps |= rm.hasNestedResultMaps();
                }
            }
        }

        return boundSql;
    }














    // getter ...

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }
    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }
    public String getResource() {
        return resource;
    }
    public Configuration getConfiguration() {
        return configuration;
    }
    public String getId() {
        return id;
    }
    public boolean hasNestedResultMaps() {
        return hasNestedResultMaps;
    }
    public Integer getFetchSize() {
        return fetchSize;
    }
    public Integer getTimeout() {
        return timeout;
    }
    public StatementType getStatementType() {
        return statementType;
    }
    public ResultSetType getResultSetType() {
        return resultSetType;
    }
    public SqlSource getSqlSource() {
        return sqlSource;
    }
    public ParameterMap getParameterMap() {
        return parameterMap;
    }
    public List<ResultMap> getResultMaps() {
        return resultMaps;
    }
    public Cache getCache() {
        return cache;
    }
    public boolean isFlushCacheRequired() {
        return flushCacheRequired;
    }
    public boolean isUseCache() {
        return useCache;
    }
    public boolean isResultOrdered() {
        return resultOrdered;
    }
    public String getDatabaseId() {
        return databaseId;
    }
    public String[] getKeyProperties() {
        return keyProperties;
    }
    public String[] getKeyColumns() {
        return keyColumns;
    }
    public Log getStatementLog() {
        return statementLog;
    }
    public LanguageDriver getLang() {
        return lang;
    }
    public String[] getResultSets() {
        return resultSets;
    }
    @Deprecated
    public String[] getResulSets() {
        return resultSets;
    }

}
