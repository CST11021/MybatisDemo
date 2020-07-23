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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.reflection.Jdk;
import org.apache.ibatis.reflection.ParamNameUtil;
import org.apache.ibatis.session.Configuration;

/**
 * 对应 <resultMap> 标签配置：
 *
 *     <resultMap id="BaseResultMap" type="com.tuya.hecate.core.entity.swo.WorkOrderDO">
 *         <id column="id" property="id" javaType="Long"/>
 *         <result column="order_code" property="orderCode" />
 *         <result column="order_type" property="orderType" />
 *
 *     </resultMap>
 *
 * @author Clinton Begin
 */
public class ResultMap {
    private Configuration configuration;

    /** 标识这个结果结果集的id，如果没有配置则mybatis会自动生成 */
    private String id;
    /** 表示返回结果集的类型 */
    private Class<?> type;
    /**  对应<resultMap>里的<result>标签 */
    private List<ResultMapping> resultMappings;
    /**  对应<resultMap>里的<id>标签 */
    private List<ResultMapping> idResultMappings;
    /**  对应<resultMap>里的<constructor>标签 */
    private List<ResultMapping> constructorResultMappings;
    private List<ResultMapping> propertyResultMappings;
    private Set<String> mappedColumns;
    private Set<String> mappedProperties;
    private Discriminator discriminator;
    private boolean hasNestedResultMaps;
    private boolean hasNestedQueries;
    /**
     * 是否启用字段自动映射：
     * 当自动映射查询结果时，MyBatis 会获取结果中返回的列名并在 Java 类中查找相同名字的属性（忽略大小写）。这意味着如果发现了ID列和id属性，
     * MyBatis 会将列 ID 的值赋给 id 属性。通常数据库列使用大写字母组成的单词命名，单词间用下划线分隔；而 Java 属性一般遵循驼峰命名法约定。
     * 为了在这两种命名方式之间启用自动映射，需要开启驼峰命名规则，将 mapUnderscoreToCamelCase 设置为 true。甚至在提供了结果映射后，自动映射也能工作。
     */
    private Boolean autoMapping;

    private ResultMap() {}
    public static class Builder {
        private static final Log log = LogFactory.getLog(Builder.class);

        private ResultMap resultMap = new ResultMap();

        public Builder(Configuration configuration, String id, Class<?> type, List<ResultMapping> resultMappings) {
            this(configuration, id, type, resultMappings, null);
        }
        public Builder(Configuration configuration, String id, Class<?> type, List<ResultMapping> resultMappings, Boolean autoMapping) {
            resultMap.configuration = configuration;
            resultMap.id = id;
            resultMap.type = type;
            resultMap.resultMappings = resultMappings;
            resultMap.autoMapping = autoMapping;
        }
        public Builder discriminator(Discriminator discriminator) {
            resultMap.discriminator = discriminator;
            return this;
        }

        public Class<?> type() {
            return resultMap.type;
        }
        public ResultMap build() {
            if (resultMap.id == null) {
                throw new IllegalArgumentException("ResultMaps must have an id");
            }
            resultMap.mappedColumns = new HashSet<String>();
            resultMap.mappedProperties = new HashSet<String>();
            resultMap.idResultMappings = new ArrayList<ResultMapping>();
            resultMap.constructorResultMappings = new ArrayList<ResultMapping>();
            resultMap.propertyResultMappings = new ArrayList<ResultMapping>();
            final List<String> constructorArgNames = new ArrayList<String>();

            for (ResultMapping resultMapping : resultMap.resultMappings) {
                resultMap.hasNestedQueries = resultMap.hasNestedQueries || resultMapping.getNestedQueryId() != null;
                resultMap.hasNestedResultMaps = resultMap.hasNestedResultMaps || (resultMapping.getNestedResultMapId() != null && resultMapping.getResultSet() == null);
                final String column = resultMapping.getColumn();
                if (column != null) {
                    resultMap.mappedColumns.add(column.toUpperCase(Locale.ENGLISH));
                } else if (resultMapping.isCompositeResult()) {
                    for (ResultMapping compositeResultMapping : resultMapping.getComposites()) {
                        final String compositeColumn = compositeResultMapping.getColumn();
                        if (compositeColumn != null) {
                            resultMap.mappedColumns.add(compositeColumn.toUpperCase(Locale.ENGLISH));
                        }
                    }
                }
                final String property = resultMapping.getProperty();
                if (property != null) {
                    resultMap.mappedProperties.add(property);
                }
                if (resultMapping.getFlags().contains(ResultFlag.CONSTRUCTOR)) {
                    resultMap.constructorResultMappings.add(resultMapping);
                    if (resultMapping.getProperty() != null) {
                        constructorArgNames.add(resultMapping.getProperty());
                    }
                } else {
                    resultMap.propertyResultMappings.add(resultMapping);
                }
                if (resultMapping.getFlags().contains(ResultFlag.ID)) {
                    resultMap.idResultMappings.add(resultMapping);
                }
            }

            if (resultMap.idResultMappings.isEmpty()) {
                resultMap.idResultMappings.addAll(resultMap.resultMappings);
            }
            if (!constructorArgNames.isEmpty()) {
                final List<String> actualArgNames = argNamesOfMatchingConstructor(constructorArgNames);
                if (actualArgNames == null) {
                    throw new BuilderException("Error in result map '" + resultMap.id
                            + "'. Failed to find a constructor in '"
                            + resultMap.getType().getName() + "' by arg names " + constructorArgNames
                            + ". There might be more info in debug log.");
                }
                Collections.sort(resultMap.constructorResultMappings, new Comparator<ResultMapping>() {
                    @Override
                    public int compare(ResultMapping o1, ResultMapping o2) {
                        int paramIdx1 = actualArgNames.indexOf(o1.getProperty());
                        int paramIdx2 = actualArgNames.indexOf(o2.getProperty());
                        return paramIdx1 - paramIdx2;
                    }
                });
            }
            // lock down collections
            resultMap.resultMappings = Collections.unmodifiableList(resultMap.resultMappings);
            resultMap.idResultMappings = Collections.unmodifiableList(resultMap.idResultMappings);
            resultMap.constructorResultMappings = Collections.unmodifiableList(resultMap.constructorResultMappings);
            resultMap.propertyResultMappings = Collections.unmodifiableList(resultMap.propertyResultMappings);
            resultMap.mappedColumns = Collections.unmodifiableSet(resultMap.mappedColumns);
            return resultMap;
        }
        private List<String> argNamesOfMatchingConstructor(List<String> constructorArgNames) {
            Constructor<?>[] constructors = resultMap.type.getDeclaredConstructors();
            for (Constructor<?> constructor : constructors) {
                Class<?>[] paramTypes = constructor.getParameterTypes();
                if (constructorArgNames.size() == paramTypes.length) {
                    List<String> paramNames = getArgNames(constructor);
                    if (constructorArgNames.containsAll(paramNames)
                            && argTypesMatch(constructorArgNames, paramTypes, paramNames)) {
                        return paramNames;
                    }
                }
            }
            return null;
        }
        private boolean argTypesMatch(final List<String> constructorArgNames, Class<?>[] paramTypes, List<String> paramNames) {
            for (int i = 0; i < constructorArgNames.size(); i++) {
                Class<?> actualType = paramTypes[paramNames.indexOf(constructorArgNames.get(i))];
                Class<?> specifiedType = resultMap.constructorResultMappings.get(i).getJavaType();
                if (!actualType.equals(specifiedType)) {
                    if (log.isDebugEnabled()) {
                        log.debug("While building result map '" + resultMap.id
                                + "', found a constructor with arg names " + constructorArgNames
                                + ", but the type of '" + constructorArgNames.get(i)
                                + "' did not match. Specified: [" + specifiedType.getName() + "] Declared: ["
                                + actualType.getName() + "]");
                    }
                    return false;
                }
            }
            return true;
        }
        private List<String> getArgNames(Constructor<?> constructor) {
            if (resultMap.configuration.isUseActualParamName() && Jdk.parameterExists) {
                return ParamNameUtil.getParamNames(constructor);
            } else {
                List<String> paramNames = new ArrayList<String>();
                final Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
                int paramCount = paramAnnotations.length;
                for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
                    String name = null;
                    for (Annotation annotation : paramAnnotations[paramIndex]) {
                        if (annotation instanceof Param) {
                            name = ((Param) annotation).value();
                            break;
                        }
                    }
                    paramNames.add(name != null ? name : "arg" + paramIndex);
                }
                return paramNames;
            }
        }
    }



    public String getId() {
        return id;
    }
    public boolean hasNestedResultMaps() {
        return hasNestedResultMaps;
    }
    public boolean hasNestedQueries() {
        return hasNestedQueries;
    }
    public Class<?> getType() {
        return type;
    }
    public List<ResultMapping> getResultMappings() {
        return resultMappings;
    }
    public List<ResultMapping> getConstructorResultMappings() {
        return constructorResultMappings;
    }
    public List<ResultMapping> getPropertyResultMappings() {
        return propertyResultMappings;
    }
    public List<ResultMapping> getIdResultMappings() {
        return idResultMappings;
    }
    public Set<String> getMappedColumns() {
        return mappedColumns;
    }
    public Set<String> getMappedProperties() {
        return mappedProperties;
    }
    public Discriminator getDiscriminator() {
        return discriminator;
    }
    public void forceNestedResultMaps() {
        hasNestedResultMaps = true;
    }
    public Boolean getAutoMapping() {
        return autoMapping;
    }

}
