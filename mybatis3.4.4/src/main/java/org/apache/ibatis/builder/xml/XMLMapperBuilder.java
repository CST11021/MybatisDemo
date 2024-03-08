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
package org.apache.ibatis.builder.xml;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.CacheRefResolver;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.ResultMapResolver;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

/**
 * 该类主要是用来解析 XxxMapper.xml 文件，每个 Mapper.xml 配置文件（或者使用注解方式配置的接口类）对应一个XMLMapperBuilder实例
 *
 * @author Clinton Begin
 */
public class XMLMapperBuilder extends BaseBuilder {

    /** 该对象封装了一个Document对象，用于表示将要解析的mybatis的配置文件 */
    private XPathParser parser;
    /** 该变量在构造器的时候初始化，用来解析<mapper>标签的辅助类 */
    private MapperBuilderAssistant builderAssistant;
    /** 表示sql碎片，解析<mapper>标签内的<sql>标签后，会将解析结果保存到 sqlFragments 中 */
    private Map<String, XNode> sqlFragments;
    /** 表示当前要解析的配置文件路径 */
    private String resource;

    @Deprecated
    public XMLMapperBuilder(Reader reader, Configuration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
        this(reader, configuration, resource, sqlFragments);
        this.builderAssistant.setCurrentNamespace(namespace);
    }
    @Deprecated
    public XMLMapperBuilder(Reader reader, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
        this(new XPathParser(reader, true, configuration.getVariables(), new XMLMapperEntityResolver()),
                configuration, resource, sqlFragments);
    }
    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
        this(inputStream, configuration, resource, sqlFragments);
        this.builderAssistant.setCurrentNamespace(namespace);
    }
    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
        this(new XPathParser(inputStream, true, configuration.getVariables(), new XMLMapperEntityResolver()), configuration, resource, sqlFragments);
    }
    private XMLMapperBuilder(XPathParser parser, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
        super(configuration);
        this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
        this.parser = parser;
        this.sqlFragments = sqlFragments;
        this.resource = resource;
    }

    /**
     * 解析XxxMapper.xml文件的入口
     */
    public void parse() {
        // 判断mybastic是否解析过该配置文件
        if (!configuration.isResourceLoaded(resource)) {
            // 开始解析<mapper>节点
            configurationElement(parser.evalNode("/mapper"));
            // 保存解析过的配置文件，防止重复解析
            configuration.addLoadedResource(resource);
            // 绑定命名空间：保存当前要解析的配置文件文件，命名空间及对应的Mapper接口等信息，防止重复解析
            bindMapperForNamespace();
        }

        // 解析之前未解析的<resultMap>标签
        parsePendingResultMaps();
        // 解析哪些当前还未加载的缓存实例
        parsePendingCacheRefs();
        // 在上面configurationElement()方法中，当解析Mapper对应的SQL无法解析时（例如：配置的<select>标签中引用其他配置的id，而该id
        // Mybastic还未解析加载，则会导致当期的SQL无法解析），会将对应的XMLStatementBuilder解析器，添加到该变量中，等到都是解析完了，再来解析之前未解析的标签
        parsePendingStatements();
    }

    private void parsePendingResultMaps() {
        Collection<ResultMapResolver> incompleteResultMaps = configuration.getIncompleteResultMaps();
        synchronized (incompleteResultMaps) {
            Iterator<ResultMapResolver> iter = incompleteResultMaps.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().resolve();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // ResultMap is still missing a resource...
                }
            }
        }
    }
    private void parsePendingCacheRefs() {
        Collection<CacheRefResolver> incompleteCacheRefs = configuration.getIncompleteCacheRefs();
        synchronized (incompleteCacheRefs) {
            Iterator<CacheRefResolver> iter = incompleteCacheRefs.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().resolveCacheRef();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // Cache ref is still missing a resource...
                }
            }
        }
    }
    private void parsePendingStatements() {
        Collection<XMLStatementBuilder> incompleteStatements = configuration.getIncompleteStatements();
        synchronized (incompleteStatements) {
            Iterator<XMLStatementBuilder> iter = incompleteStatements.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().parseStatementNode();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // Statement is still missing a resource...
                }
            }
        }
    }

    /**
     * 绑定命名空间：保存当前要解析的配置文件文件，命名空间及对应的Mapper接口等信息，防止重复解析
     */
    private void bindMapperForNamespace() {
        String namespace = builderAssistant.getCurrentNamespace();
        if (namespace != null) {
            // namespace表示 <mapper namespace="xxx"> 配置的命名空间，这里对应的Mapper接口，例如：<mapper namespace="com.whz.mybatis.mapperinterface.IEmployeerMapper">
            Class<?> boundType = null;
            try {
                boundType = Resources.classForName(namespace);
            } catch (ClassNotFoundException e) {
                //ignore, bound type is not required
            }

            if (boundType != null) {
                if (!configuration.hasMapper(boundType)) {
                    // Spring可能不知道真正的资源名，因此我们设置了一个标志来防止再次从mapper接口加载该资源
                    configuration.addLoadedResource("namespace:" + namespace);
                    // 添加对应的Mapper接口
                    configuration.addMapper(boundType);
                }
            }
        }
    }

    /**
     * 根据id查找SQL碎片
     *
     * @param refid
     * @return
     */
    public XNode getSqlFragment(String refid) {
        return sqlFragments.get(refid);
    }



    /* ----------------------------------------- 解析<mapper>节点 --------------------------------------- */

    /**
     * 解析<mapper>节点
     *
     * @param context
     */
    private void configurationElement(XNode context) {
        try {
            // 例如：<mapper namespace="com.whz.mybatis.mapperinterface.IEmployeerMapper"> 获取该配置的命名空间
            String namespace = context.getStringAttribute("namespace");
            if (namespace == null || namespace.equals("")) {
                throw new BuilderException("Mapper's namespace cannot be empty");
            }
            builderAssistant.setCurrentNamespace(namespace);
            // 解析<cache-ref>标签：缓存相关参考：https://mybatis.org/mybatis-3/zh/sqlmap-xml.html#cache
            cacheRefElement(context.evalNode("cache-ref"));
            // 解析<cache>标签，缓存配置相关：缓存相关参考：https://mybatis.org/mybatis-3/zh/sqlmap-xml.html#cache
            cacheElement(context.evalNode("cache"));
            // 解析<parameterMap>标签
            parameterMapElement(context.evalNodes("/mapper/parameterMap"));
            // 解析<resultMap> 标签
            resultMapElements(context.evalNodes("/mapper/resultMap"));
            // 解析<mapper>标签内的<sql>标签
            sqlElement(context.evalNodes("/mapper/sql"));
            // 解析 select|insert|update|delete 标签
            buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing Mapper XML. Cause: " + e, e);
        }
    }

    /**
     * 解析 select|insert|update|delete 标签，这些标签的解析过程会委托给 XMLStatementBuilder 来解析
     *
     * @param list
     */
    private void buildStatementFromContext(List<XNode> list) {
        // 如果配置了databaseId，则会解析带有databaseId配置的SQL
        if (configuration.getDatabaseId() != null) {
            buildStatementFromContext(list, configuration.getDatabaseId());
        }
        //
        buildStatementFromContext(list, null);
    }
    private void buildStatementFromContext(List<XNode> list, String requiredDatabaseId) {
        // 这里list表示所有 select|insert|update|delete 的配置
        for (XNode context : list) {
            // 使用XMLStatementBuilder 对象类解析这些标签
            final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant, context, requiredDatabaseId);
            try {
                statementParser.parseStatementNode();
            } catch (IncompleteElementException e) {
                configuration.addIncompleteStatement(statementParser);
            }
        }
    }

    /**
     * 解析<cache-ref>标签：
     * 对某一命名空间的语句，只会使用该命名空间的缓存进行缓存或刷新，但你可能会想要在多个命名空间中共享相同的缓存配置和实例，要实现这种需求，
     * 你可以使用 cache-ref 元素来引用另一个缓存，例如：
     *
     *  <cache-ref namespace="com.someone.application.data.SomeMapper"/>
     *
     * @param context
     */
    private void cacheRefElement(XNode context) {
        if (context != null) {
            // builderAssistant.getCurrentNamespace()：获取当前XxxMapper.xml文件的命名空间
            configuration.addCacheRef(builderAssistant.getCurrentNamespace(), context.getStringAttribute("namespace"));

            CacheRefResolver cacheRefResolver = new CacheRefResolver(builderAssistant, context.getStringAttribute("namespace"));
            try {
                cacheRefResolver.resolveCacheRef();
            } catch (IncompleteElementException e) {
                configuration.addIncompleteCacheRef(cacheRefResolver);
            }
        }
    }

    /**
     * 解析<cache>标签，缓存配置相关，例如：
     * <cache-ref namespace="com.whz.mybatis.cache.IEmployeerMapper"/>
     *
     * <cache eviction="FIFO" flushInterval="60000" size="512" readOnly="true"/>
     *
     * @param context
     * @throws Exception
     */
    private void cacheElement(XNode context) throws Exception {
        if (context != null) {
            // 缓存实现类，默认为PERPETUAL，对应PerpetualCache实现类
            String type = context.getStringAttribute("type", "PERPETUAL");
            Class<? extends Cache> typeClass = typeAliasRegistry.resolveAlias(type);
            // eviction：代表的是缓存收回策略，有一下策略：
            // 1. LRU， 最近最少使用的，移除最长时间不用的对象。
            // 2. FIFO，先进先出，按对象进入缓存的顺序来移除他们
            // 3. SOFT， 软引用，移除基于垃圾回收器状态和软引用规则的对象。
            // 4. WEAK，弱引用，更积极的移除基于垃圾收集器状态和弱引用规则的对象
            String eviction = context.getStringAttribute("eviction", "LRU");
            // 获取对应的缓存策略实现
            Class<? extends Cache> evictionClass = typeAliasRegistry.resolveAlias(eviction);

            // flushInterval（刷新间隔）属性可以被设置为任意的正整数，设置的值应该是一个以毫秒为单位的合理时间量。 默认情况是不设置，也就是没有刷新间隔，缓存仅仅会在调用语句时刷新。
            Long flushInterval = context.getLongAttribute("flushInterval");
            // 设置缓存的最大大小：size（引用数目）属性可以被设置为任意正整数，要注意欲缓存对象的大小和运行环境中可用的内存资源。默认值是 1024
            Integer size = context.getIntAttribute("size");
            // readOnly（只读）属性可以被设置为 true 或 false。只读的缓存会给所有调用者返回缓存对象的相同实例。 因此这些对象不能被修改。这就提供了可观的性能提升。而可读写的缓存会（通过序列化）返回缓存对象的拷贝。 速度上会慢一些，但是更安全，因此默认值是 false。
            boolean readWrite = !context.getBooleanAttribute("readOnly", false);
            boolean blocking = context.getBooleanAttribute("blocking", false);
            Properties props = context.getChildrenAsProperties();
            builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, props);
        }
    }

    /**
     * 解析<parameterMap>标签，例如：
     * <parameterMap id="userParameterMap" type="com.whz.mybatis.entity.User">
     *      <parameter property="age" jdbcType="int" typeHandler="INTEGER" javaType="int" resultMap="userResultMap" mode="" scale=""/>
     *      省略。。。
     * </parameterMap>
     *
     * @param list
     * @throws Exception
     */
    private void parameterMapElement(List<XNode> list) throws Exception {
        for (XNode parameterMapNode : list) {
            String id = parameterMapNode.getStringAttribute("id");
            String type = parameterMapNode.getStringAttribute("type");
            Class<?> parameterClass = resolveClass(type);

            // 解析子标签<parameter>
            List<XNode> parameterNodes = parameterMapNode.evalNodes("parameter");
            List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
            for (XNode parameterNode : parameterNodes) {
                String property = parameterNode.getStringAttribute("property");
                String javaType = parameterNode.getStringAttribute("javaType");
                String jdbcType = parameterNode.getStringAttribute("jdbcType");
                // 对应<resultMap>标签配置的id
                String resultMap = parameterNode.getStringAttribute("resultMap");
                String mode = parameterNode.getStringAttribute("mode");
                String typeHandler = parameterNode.getStringAttribute("typeHandler");
                // 小数点后保留的位数
                Integer numericScale = parameterNode.getIntAttribute("numericScale");
                ParameterMode modeEnum = resolveParameterMode(mode);
                // 获取javaType对应Java类型
                Class<?> javaTypeClass = resolveClass(javaType);
                // 获取jdbcType对应的jdbcType
                JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
                @SuppressWarnings("unchecked")
                Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);
                ParameterMapping parameterMapping = builderAssistant.buildParameterMapping(parameterClass, property, javaTypeClass, jdbcTypeEnum, resultMap, modeEnum, typeHandlerClass, numericScale);
                parameterMappings.add(parameterMapping);
            }

            // build ParameterMap实例，并添加全局配置后返回
            builderAssistant.addParameterMap(id, parameterClass, parameterMappings);
        }
    }

    /**
     * 解析所有的<resultMap> 标签
     *
     * @param list
     * @throws Exception
     */
    private void resultMapElements(List<XNode> list) throws Exception {
        for (XNode resultMapNode : list) {
            try {
                resultMapElement(resultMapNode);
            } catch (IncompleteElementException e) {
                // ignore, it will be retried
            }
        }
    }

    /**
     * 解析一个 <resultMap> 标签
     *
     * @param resultMapNode
     * @return
     * @throws Exception
     */
    private ResultMap resultMapElement(XNode resultMapNode) throws Exception {
        return resultMapElement(resultMapNode, Collections.<ResultMapping>emptyList());
    }

    /**
     *
     * @param resultMapNode             当前解析的<resultMap>标签
     * @param additionalResultMappings  额外的ResultMapping配置
     * @return
     * @throws Exception
     */
    private ResultMap resultMapElement(XNode resultMapNode, List<ResultMapping> additionalResultMappings) throws Exception {
        ErrorContext.instance().activity("processing " + resultMapNode.getValueBasedIdentifier());
        String id = resultMapNode.getStringAttribute("id", resultMapNode.getValueBasedIdentifier());
        String type = resultMapNode.getStringAttribute("type",
                resultMapNode.getStringAttribute("ofType",
                resultMapNode.getStringAttribute("resultType",
                resultMapNode.getStringAttribute("javaType"))));

        String extend = resultMapNode.getStringAttribute("extends");
        Boolean autoMapping = resultMapNode.getBooleanAttribute("autoMapping");
        Class<?> typeClass = resolveClass(type);
        Discriminator discriminator = null;
        List<ResultMapping> resultMappings = new ArrayList<ResultMapping>();
        resultMappings.addAll(additionalResultMappings);
        List<XNode> resultChildren = resultMapNode.getChildren();
        for (XNode resultChild : resultChildren) {
            if ("constructor".equals(resultChild.getName())) {
                processConstructorElement(resultChild, typeClass, resultMappings);
            } else if ("discriminator".equals(resultChild.getName())) {
                discriminator = processDiscriminatorElement(resultChild, typeClass, resultMappings);
            } else {
                List<ResultFlag> flags = new ArrayList<ResultFlag>();
                if ("id".equals(resultChild.getName())) {
                    flags.add(ResultFlag.ID);
                }
                resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
            }
        }
        ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, typeClass, extend, discriminator, resultMappings, autoMapping);
        try {
            return resultMapResolver.resolve();
        } catch (IncompleteElementException e) {
            configuration.addIncompleteResultMap(resultMapResolver);
            throw e;
        }
    }

    /**
     * 处理<constructor>标签
     *
     * @param resultChild
     * @param resultType
     * @param resultMappings
     * @throws Exception
     */
    private void processConstructorElement(XNode resultChild, Class<?> resultType, List<ResultMapping> resultMappings) throws Exception {
        List<XNode> argChildren = resultChild.getChildren();
        for (XNode argChild : argChildren) {
            List<ResultFlag> flags = new ArrayList<ResultFlag>();
            flags.add(ResultFlag.CONSTRUCTOR);
            if ("idArg".equals(argChild.getName())) {
                flags.add(ResultFlag.ID);
            }
            resultMappings.add(buildResultMappingFromContext(argChild, resultType, flags));
        }
    }

    /**
     * 处理<discriminator>标签
     *
     * @param context
     * @param resultType
     * @param resultMappings
     * @return
     * @throws Exception
     */
    private Discriminator processDiscriminatorElement(XNode context, Class<?> resultType, List<ResultMapping> resultMappings) throws Exception {
        String column = context.getStringAttribute("column");
        String javaType = context.getStringAttribute("javaType");
        String jdbcType = context.getStringAttribute("jdbcType");
        String typeHandler = context.getStringAttribute("typeHandler");
        Class<?> javaTypeClass = resolveClass(javaType);
        @SuppressWarnings("unchecked")
        Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
        Map<String, String> discriminatorMap = new HashMap<String, String>();
        for (XNode caseChild : context.getChildren()) {
            String value = caseChild.getStringAttribute("value");
            String resultMap = caseChild.getStringAttribute("resultMap", processNestedResultMappings(caseChild, resultMappings));
            discriminatorMap.put(value, resultMap);
        }
        return builderAssistant.buildDiscriminator(resultType, column, javaTypeClass, jdbcTypeEnum, typeHandlerClass, discriminatorMap);
    }

    private ResultMapping buildResultMappingFromContext(XNode context, Class<?> resultType, List<ResultFlag> flags) throws Exception {
        String property;
        if (flags.contains(ResultFlag.CONSTRUCTOR)) {
            property = context.getStringAttribute("name");
        } else {
            property = context.getStringAttribute("property");
        }
        String column = context.getStringAttribute("column");
        String javaType = context.getStringAttribute("javaType");
        String jdbcType = context.getStringAttribute("jdbcType");
        String nestedSelect = context.getStringAttribute("select");
        String nestedResultMap = context.getStringAttribute("resultMap",
                processNestedResultMappings(context, Collections.<ResultMapping>emptyList()));
        String notNullColumn = context.getStringAttribute("notNullColumn");
        String columnPrefix = context.getStringAttribute("columnPrefix");
        String typeHandler = context.getStringAttribute("typeHandler");
        String resultSet = context.getStringAttribute("resultSet");
        String foreignColumn = context.getStringAttribute("foreignColumn");
        boolean lazy = "lazy".equals(context.getStringAttribute("fetchType", configuration.isLazyLoadingEnabled() ? "lazy" : "eager"));
        Class<?> javaTypeClass = resolveClass(javaType);
        @SuppressWarnings("unchecked")
        Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
        return builderAssistant.buildResultMapping(resultType, property, column, javaTypeClass, jdbcTypeEnum, nestedSelect, nestedResultMap, notNullColumn, columnPrefix, typeHandlerClass, flags, resultSet, foreignColumn, lazy);
    }

    /**
     * 处理<association>标签
     *
     * @param context
     * @param resultMappings
     * @return
     * @throws Exception
     */
    private String processNestedResultMappings(XNode context, List<ResultMapping> resultMappings) throws Exception {
        if ("association".equals(context.getName())
                || "collection".equals(context.getName())
                || "case".equals(context.getName())) {
            if (context.getStringAttribute("select") == null) {
                ResultMap resultMap = resultMapElement(context, resultMappings);
                return resultMap.getId();
            }
        }
        return null;
    }

    /**
     * 解析<mapper>标签内的<sql>标签
     *
     * @param list
     * @throws Exception
     */
    private void sqlElement(List<XNode> list) throws Exception {
        if (configuration.getDatabaseId() != null) {
            sqlElement(list, configuration.getDatabaseId());
        }
        sqlElement(list, null);
    }

    /**
     * 解析<mapper>标签内的<sql>标签
     *
     * @param list
     * @param requiredDatabaseId
     * @throws Exception
     */
    private void sqlElement(List<XNode> list, String requiredDatabaseId) throws Exception {
        for (XNode context : list) {
            String databaseId = context.getStringAttribute("databaseId");
            String id = context.getStringAttribute("id");
            id = builderAssistant.applyCurrentNamespace(id, false);
            if (databaseIdMatchesCurrent(id, databaseId, requiredDatabaseId)) {
                sqlFragments.put(id, context);
            }
        }
    }

    /**
     * 判断id对应的SQL是否匹配当前要求的databaseId
     *
     * @param id                    SQL对应的id
     * @param databaseId            SQL配置的databaseId
     * @param requiredDatabaseId    当前要求的databaseId
     * @return
     */
    private boolean databaseIdMatchesCurrent(String id, String databaseId, String requiredDatabaseId) {
        if (requiredDatabaseId != null) {
            if (!requiredDatabaseId.equals(databaseId)) {
                return false;
            }
        } else {
            if (databaseId != null) {
                return false;
            }
            // skip this fragment if there is a previous one with a not null databaseId
            if (this.sqlFragments.containsKey(id)) {
                XNode context = this.sqlFragments.get(id);
                if (context.getStringAttribute("databaseId") != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /* ----------------------------------------- 解析<mapper>节点 --------------------------------------- */

}
