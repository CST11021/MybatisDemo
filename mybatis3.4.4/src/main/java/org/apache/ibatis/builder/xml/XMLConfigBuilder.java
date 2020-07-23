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
package org.apache.ibatis.builder.xml;

import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;
import javax.sql.DataSource;

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.loader.ProxyFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.AutoMappingUnknownColumnBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.JdbcType;

/**
 * 用于解析mybatis配置文件，并创建一个内存表示的 Configuration 对象
 */
public class XMLConfigBuilder extends BaseBuilder {

    /** 用于标记XMLConfigBuilder实例是否解析过配置文件 */
    private boolean parsed;
    /** 该对象封装了将要被解析的配置文件信息 */
    private XPathParser parser;
    /**
     * 解析<environments>标签：
     * Mybatis可以配置成适应多种环境，这种机制有助于将SQL映射应用于多种数据库之中，现实情况下有多种理由需要这么做。
     * 例如：开发，测试和生产环境需要不同的配置。配置环境可以注册多个数据源，每一个数据源分为两大部分：一个是数据库源的配置，另一个是数据库事物的配置。如：
     * <environments default="development">
     *    <environment id="development">
     *
     *       <transactionManager type="JDBC" >
     *          <property name="autoCommit" value="false"/>
     *       </transactionManager>
     *
     *       <dataSource type="POOLED">
     *          <property name="driver" value="${driver}" />
     *          <property name="url" value="${url}" />
     *          <property name="username" value="${username}" />
     *          <property name="password" value="${password}" />
     *       </dataSource>
     *
     *    </environment>
     * </environments>
     */
    private String environment;
    /** Reflector工厂，用于获取Class对应的Reflector实例，默认实现带有缓存机制 */
    private ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();

    public XMLConfigBuilder(Reader reader) {
        this(reader, null, null);
    }
    public XMLConfigBuilder(Reader reader, String environment) {
        this(reader, environment, null);
    }
    public XMLConfigBuilder(Reader reader, String environment, Properties props) {
        this(new XPathParser(reader, true, props, new XMLMapperEntityResolver()), environment, props);
    }
    public XMLConfigBuilder(InputStream inputStream) {
        this(inputStream, null, null);
    }
    public XMLConfigBuilder(InputStream inputStream, String environment) {
        this(inputStream, environment, null);
    }
    public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
        this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
    }
    private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {
        super(new Configuration());
        ErrorContext.instance().resource("SQL Mapper Configuration");
        this.configuration.setVariables(props);
        this.parsed = false;
        this.environment = environment;
        this.parser = parser;
    }

    // 核心方法，解析配置文件信息的入口
    public Configuration parse() {
        if (parsed) {
            // 如果XMLConfigBuilder 对象已经被使用过了，则抛出异常
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        }
        parsed = true;
        // 使用XPath来解析出配置文件的根节点
        parseConfiguration(parser.evalNode("/configuration"));
        return configuration;
    }

    // 从根节点开始解析mybatis的配置文件
    /**
     * 从Mybastic的根节点开始解析xml配置，解析根节点下的所有标签和属性，并将解析后的数据保存到全局配置类{@link #configuration}
     *
     * @param root  表示mybastic的根节点<configuration>
     */
    private void parseConfiguration(XNode root) {
        try {
            // 解析<properties>标签，并将解析结果封装到Configuration对象中
            propertiesElement(root.evalNode("properties"));

            // 解析<settings>标签，返回一个Properties对象
            Properties settings = settingsAsProperties(root.evalNode("settings"));

            // 加载<settings>标签配置的VFS
            loadCustomVfs(settings);

            // 解析并注册类对应的别名
            typeAliasesElement(root.evalNode("typeAliases"));

            // 解析<plugins>插件配置
            pluginElement(root.evalNode("plugins"));

            // 解析<objectFactory>标签
            objectFactoryElement(root.evalNode("objectFactory"));

            // 解析<objectWrapperFactory/>标签
            objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));

            // 解析<reflectorFactory/>标签
            reflectorFactoryElement(root.evalNode("reflectorFactory"));

            // 将<settings>标签对应的配置set到全局配置类中
            settingsElement(settings);

            // 解析<environments>标签：在objectFactory和objectWrapperFactory之后读取它
            environmentsElement(root.evalNode("environments"));

            // 解析<databaseIdProvider>标签
            databaseIdProviderElement(root.evalNode("databaseIdProvider"));

            // 解析<typeHandlers/>标签
            typeHandlerElement(root.evalNode("typeHandlers"));

            // 解析<mappers>标签
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
    }

    // 解析<settings>标签，并将解析结果封装到Properties对象中返回
    /**
     * 解析<settings>标签，并将解析结果封装到Properties对象中返回
     *
     * <settings>标签配置如下：
     * 	<settings>
     * 	    ...
     * 		<setting name="cacheEnabled" value="true"/>
     * 		<setting name="lazyLoadingEnabled" value="true"/>
     * 		<setting name="multipleResultSetsEnabled" value="true"/>
     * 		<setting name="useColumnLabel" value="true"/>
     * 		<setting name="useGeneratedKeys" value="true"/>
     * 		<setting name="defaultExecutorType" value="SIMPLE"/>
     * 		<setting name="defaultStatementTimeout" value="25000"/>
     * 	    ...
     * 	</settings>
     *
     * @param context   表示<settings>标签
     * @return  返回<settings>标签配置信息
     */
    private Properties settingsAsProperties(XNode context) {
        if (context == null) {
            return new Properties();
        }

        // 获取所有的子标签，并将子标签中的name和value属性返回
        Properties props = context.getChildrenAsProperties();
        // 校验<settings>标签中的配置是否正确，方式属性名配置错，导致Mybastic无法处理
        MetaClass metaConfig = MetaClass.forClass(Configuration.class, localReflectorFactory);
        for (Object key : props.keySet()) {
            if (!metaConfig.hasSetter(String.valueOf(key))) {
                throw new BuilderException("The setting " + key + " is not known.  Make sure you spelled it correctly (case sensitive).");
            }
        }
        return props;
    }

    // 如果配置vfs实现，则加载这个vfs

    /**
     * 检查<settings>标签中是否配置vfsImpl属性，如果配置了，将vfsImpl实现类添加到全局配置中
     *
     * @param props
     * @throws ClassNotFoundException
     */
    private void loadCustomVfs(Properties props) throws ClassNotFoundException {
        // 判断<settings>标签中是否配置vfsImpl属性
        String value = props.getProperty("vfsImpl");

        // 如果配置了，将vfsImpl实现类添加到全局配置中
        if (value != null) {
            String[] clazzes = value.split(",");
            for (String clazz : clazzes) {
                if (!clazz.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Class<? extends VFS> vfsImpl = (Class<? extends VFS>) Resources.classForName(clazz);
                    configuration.setVfsImpl(vfsImpl);
                }
            }
        }
    }

    // 解析<typeAliases>标签，并完成别名注册
    /**
     * 解析<typeAliases>标签，并完成别名注册，配置例如：
     *
     * <typeAliases>
     *  <package name="com.whz"/>
     *  <typeAlias type="com.whz.User" alias="user"/>
     * </typeAliases>
     *
     * @param parent
     */
    private void typeAliasesElement(XNode parent) {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                // <typeAliases>标签下的<package>标签
                if ("package".equals(child.getName())) {
                    String typeAliasPackage = child.getStringAttribute("name");
                    configuration.getTypeAliasRegistry().registerAliases(typeAliasPackage);
                }
                // 解析<typeAliases/>标签
                else {
                    String alias = child.getStringAttribute("alias");
                    String type = child.getStringAttribute("type");
                    try {
                        // 解析type配置对应的类型
                        Class<?> clazz = Resources.classForName(type);
                        if (alias == null) {
                            // 如果没有定义别名，也会注册进来，Mybatis会使用默认的别名
                            typeAliasRegistry.registerAlias(clazz);
                        } else {
                            typeAliasRegistry.registerAlias(alias, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new BuilderException("Error registering typeAlias for '" + alias + "'. Cause: " + e, e);
                    }
                }
            }
        }
    }

    // 解析<plugins>插件配置

    /**
     * 例如：<plugins>
     *         <plugin interceptor="com.whz.mybatis.plugins.example.QueryPlugin">
     *             <property name="" value=""/>
     *         </plugin>
     *     </plugins>
     *
     * @param parent
     * @throws Exception
     */
    private void pluginElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                // 获取<plugin>标签的interceptor属性，mybastic插件的本质就是拦截器
                String interceptor = child.getStringAttribute("interceptor");
                // 获取插件的自定义配置属性
                Properties properties = child.getChildrenAsProperties();
                // 实例化插件类
                Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).newInstance();
                // 注入自定义属性
                interceptorInstance.setProperties(properties);
                // 添加到全局配置
                configuration.addInterceptor(interceptorInstance);
            }
        }
    }

    // 解析<objectFactory>标签

    /**
     * 解析<objectFactory>标签，例如：
     *
     * <objectFactory type="com.whz.MyObjectFactory">
     * 		<property name="" value=""/>
     * 	</objectFactory>
     *
     * @param context
     * @throws Exception
     */
    private void objectFactoryElement(XNode context) throws Exception {
        if (context != null) {
            // 获取 ObjectFactory 实现类
            String type = context.getStringAttribute("type");
            // 获取自定义属性
            Properties properties = context.getChildrenAsProperties();
            // 实例化ObjectFactory
            ObjectFactory factory = (ObjectFactory) resolveClass(type).newInstance();
            // 注入自定义属性
            factory.setProperties(properties);
            // 添加全局配置
            configuration.setObjectFactory(factory);
        }
    }

    // 解析<objectWrapperFactory/>标签
    /**
     * 解析<objectWrapperFactory/>标签，配置例如：
     *
     * <objectWrapperFactory type=""/>
     *
     * @param context
     * @throws Exception
     */
    private void objectWrapperFactoryElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            ObjectWrapperFactory factory = (ObjectWrapperFactory) resolveClass(type).newInstance();
            configuration.setObjectWrapperFactory(factory);
        }
    }

    // 解析<reflectorFactory/>标签
    private void reflectorFactoryElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            ReflectorFactory factory = (ReflectorFactory) resolveClass(type).newInstance();
            configuration.setReflectorFactory(factory);
        }
    }

    // 解析<properties>标签

    /**
     * 配置示例：
     * <properties resource="mysql.properties" url="file:///D:/test.properties"/>
     *
     * 	<properties>
     * 		<property name="" value=""/>
     * 		...
     * 	</properties>
     *
     * @param context
     * @throws Exception
     */
    private void propertiesElement(XNode context) throws Exception {
        if (context != null) {
            // 获取所有<properties>的子标签
            Properties defaults = context.getChildrenAsProperties();

            String resource = context.getStringAttribute("resource");
            String url = context.getStringAttribute("url");
            // <properties>标签不允许同时配置resource属性和url属性
            if (resource != null && url != null) {
                throw new BuilderException("The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
            }

            if (resource != null) {
                defaults.putAll(Resources.getResourceAsProperties(resource));
            } else if (url != null) {
                defaults.putAll(Resources.getUrlAsProperties(url));
            }

            Properties vars = configuration.getVariables();
            if (vars != null) {
                defaults.putAll(vars);
            }

            // 将<properties>配置设置到解析器中和全局配置类configuration中
            parser.setVariables(defaults);
            configuration.setVariables(defaults);
        }
    }

    // 解析<settings>标签，并设置到configuration
    private void settingsElement(Properties props) throws Exception {
        configuration.setAutoMappingBehavior(AutoMappingBehavior.valueOf(props.getProperty("autoMappingBehavior", "PARTIAL")));
        configuration.setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.valueOf(props.getProperty("autoMappingUnknownColumnBehavior", "NONE")));
        configuration.setCacheEnabled(booleanValueOf(props.getProperty("cacheEnabled"), true));
        configuration.setProxyFactory((ProxyFactory) createInstance(props.getProperty("proxyFactory")));
        configuration.setLazyLoadingEnabled(booleanValueOf(props.getProperty("lazyLoadingEnabled"), false));
        configuration.setAggressiveLazyLoading(booleanValueOf(props.getProperty("aggressiveLazyLoading"), false));
        configuration.setMultipleResultSetsEnabled(booleanValueOf(props.getProperty("multipleResultSetsEnabled"), true));
        configuration.setUseColumnLabel(booleanValueOf(props.getProperty("useColumnLabel"), true));
        configuration.setUseGeneratedKeys(booleanValueOf(props.getProperty("useGeneratedKeys"), false));
        configuration.setDefaultExecutorType(ExecutorType.valueOf(props.getProperty("defaultExecutorType", "SIMPLE")));
        configuration.setDefaultStatementTimeout(integerValueOf(props.getProperty("defaultStatementTimeout"), null));
        configuration.setDefaultFetchSize(integerValueOf(props.getProperty("defaultFetchSize"), null));
        configuration.setMapUnderscoreToCamelCase(booleanValueOf(props.getProperty("mapUnderscoreToCamelCase"), false));
        configuration.setSafeRowBoundsEnabled(booleanValueOf(props.getProperty("safeRowBoundsEnabled"), false));
        configuration.setLocalCacheScope(LocalCacheScope.valueOf(props.getProperty("localCacheScope", "SESSION")));
        configuration.setJdbcTypeForNull(JdbcType.valueOf(props.getProperty("jdbcTypeForNull", "OTHER")));
        configuration.setLazyLoadTriggerMethods(stringSetValueOf(props.getProperty("lazyLoadTriggerMethods"), "equals,clone,hashCode,toString"));
        configuration.setSafeResultHandlerEnabled(booleanValueOf(props.getProperty("safeResultHandlerEnabled"), true));
        configuration.setDefaultScriptingLanguage(resolveClass(props.getProperty("defaultScriptingLanguage")));
        configuration.setCallSettersOnNulls(booleanValueOf(props.getProperty("callSettersOnNulls"), false));
        configuration.setUseActualParamName(booleanValueOf(props.getProperty("useActualParamName"), true));
        configuration.setReturnInstanceForEmptyRow(booleanValueOf(props.getProperty("returnInstanceForEmptyRow"), false));
        configuration.setLogPrefix(props.getProperty("logPrefix"));
        @SuppressWarnings("unchecked")
        Class<? extends Log> logImpl = (Class<? extends Log>) resolveClass(props.getProperty("logImpl"));
        configuration.setLogImpl(logImpl);
        configuration.setConfigurationFactory(resolveClass(props.getProperty("configurationFactory")));
    }

    /**
     * 解析<environments>标签：
     * Mybatis可以配置成适应多种环境，这种机制有助于将SQL映射应用于多种数据库之中，现实情况下有多种理由需要这么做。
     * 例如：开发，测试和生产环境需要不同的配置。配置环境可以注册多个数据源，每一个数据源分为两大部分：一个是数据库源的配置，另一个是数据库事物的配置。如：
     * <environments default="development">
     *    <environment id="development">
     *
     *       <transactionManager type="JDBC" >
     *          <property name="autoCommit" value="false"/>
     *       </transactionManager>
     *
     *       <dataSource type="POOLED">
     *          <property name="driver" value="${driver}" />
     *          <property name="url" value="${url}" />
     *          <property name="username" value="${username}" />
     *          <property name="password" value="${password}" />
     *       </dataSource>
     *
     *    </environment>
     * </environments>
     */
    private void environmentsElement(XNode context) throws Exception {
        if (context != null) {
            if (environment == null) {
                // 一开始默认使用<environments default="development"> 配置中的default属性
                environment = context.getStringAttribute("default");
            }
            for (XNode child : context.getChildren()) {
                // 获取<environment id="development"> 这个配置的id属性
                String id = child.getStringAttribute("id");
                // 判断配置的这个id 属性和上面的default 属性是否一样
                if (isSpecifiedEnvironment(id)) {
                    // 解析<transactionManager>标签
                    TransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
                    // 解析<dataSource>标签
                    DataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
                    DataSource dataSource = dsFactory.getDataSource();

                    // 这里使用了建造者模式来构建一个 Environment 对象
                    Environment.Builder environmentBuilder = new Environment.Builder(id)
                            .transactionFactory(txFactory)
                            .dataSource(dataSource);
                    configuration.setEnvironment(environmentBuilder.build());
                }
            }
        }
    }

    // 解析<environments/>标签内的<transactionManager/>标签
    private TransactionFactory transactionManagerElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            TransactionFactory factory = (TransactionFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a TransactionFactory.");
    }

    // 解析<environments/>标签内的<dataSource/>标签
    private DataSourceFactory dataSourceElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            DataSourceFactory factory = (DataSourceFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a DataSourceFactory.");
    }

    // 解析<databaseIdProvider>标签
    /**
     * 解析<databaseIdProvider>标签，例如：
     *
     * 	<databaseIdProvider type="">
     * 		<property name="" value=""/>
     * 	</databaseIdProvider>
     *
     * @param context
     * @throws Exception
     */
    private void databaseIdProviderElement(XNode context) throws Exception {
        DatabaseIdProvider databaseIdProvider = null;
        if (context != null) {
            String type = context.getStringAttribute("type");
            // 糟糕的补丁以保持向后兼容性
            if ("VENDOR".equals(type)) {
                type = "DB_VENDOR";
            }

            Properties properties = context.getChildrenAsProperties();
            databaseIdProvider = (DatabaseIdProvider) resolveClass(type).newInstance();
            databaseIdProvider.setProperties(properties);
        }

        Environment environment = configuration.getEnvironment();
        if (environment != null && databaseIdProvider != null) {
            String databaseId = databaseIdProvider.getDatabaseId(environment.getDataSource());
            configuration.setDatabaseId(databaseId);
        }
    }

    // 解析<typeHandlers/>标签
    /**
     * 解析<typeHandlers/>标签，例如：
     * 	<typeHandlers>
     * 		<package name=""/>
     * 		<typeHandler handler="" jdbcType="" javaType=""/>
     * 	</typeHandlers>
     * @param parent
     * @throws Exception
     */
    private void typeHandlerElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                // 扫码指定包下的所有的TypeHandler类实现
                if ("package".equals(child.getName())) {
                    String typeHandlerPackage = child.getStringAttribute("name");
                    typeHandlerRegistry.register(typeHandlerPackage);
                } else {

                    // 解析单个TypeHandler类实现

                    String javaTypeName = child.getStringAttribute("javaType");
                    String jdbcTypeName = child.getStringAttribute("jdbcType");
                    String handlerTypeName = child.getStringAttribute("handler");
                    Class<?> javaTypeClass = resolveClass(javaTypeName);
                    JdbcType jdbcType = resolveJdbcType(jdbcTypeName);
                    Class<?> typeHandlerClass = resolveClass(handlerTypeName);
                    if (javaTypeClass != null) {
                        if (jdbcType == null) {
                            typeHandlerRegistry.register(javaTypeClass, typeHandlerClass);
                        } else {
                            typeHandlerRegistry.register(javaTypeClass, jdbcType, typeHandlerClass);
                        }
                    } else {
                        typeHandlerRegistry.register(typeHandlerClass);
                    }
                }
            }
        }
    }

    // 解析<mappers>标签，这里会去解析每个POJO对应的Mapper，该方法中会将XxxMapper.xml配置文件委托给 XMLMapperBuilder 来解析
    private void mapperElement(XNode parent) throws Exception {
        if (parent != null) {
            // 遍历每个<mapper>标签和<package>标签
            for (XNode child : parent.getChildren()) {

                if ("package".equals(child.getName())) {
                    String mapperPackage = child.getStringAttribute("name");
                    configuration.addMappers(mapperPackage);
                } else {
                    //<mapper>可以配置三个属性：resource、url和class
                    String resource = child.getStringAttribute("resource");
                    String url = child.getStringAttribute("url");
                    String mapperClass = child.getStringAttribute("class");

                    // 使用resource属性
                    if (resource != null && url == null && mapperClass == null) {
                        ErrorContext.instance().resource(resource);
                        InputStream inputStream = Resources.getResourceAsStream(resource);
                        XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
                        mapperParser.parse();
                    }

                    // 使用url属性
                    else if (resource == null && url != null && mapperClass == null) {
                        ErrorContext.instance().resource(url);
                        InputStream inputStream = Resources.getUrlAsStream(url);
                        XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
                        mapperParser.parse();
                    }

                    // 使用class属性
                    else if (resource == null && url == null && mapperClass != null) {
                        Class<?> mapperInterface = Resources.classForName(mapperClass);
                        configuration.addMapper(mapperInterface);
                    } else {
                        throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
                    }
                }

            }
        }
    }

    /**
    <environments default="development">
		<environment id="development">
			...
		</environment>
	</environments>
	*/
    // 如上，这里的入参 id 表示上面配置中的 id 属性配置，该方法用来判断这个id和上面配置的default属性是否相同
    private boolean isSpecifiedEnvironment(String id) {
        if (environment == null) {
            throw new BuilderException("No environment specified.");
        } else if (id == null) {
            throw new BuilderException("Environment requires an id attribute.");
        } else if (environment.equals(id)) {
            return true;
        }
        return false;
    }

}
