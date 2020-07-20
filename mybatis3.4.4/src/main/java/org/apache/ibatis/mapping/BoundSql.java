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

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.session.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 一个BoundSql对象，代表了一次sql语句的实际执行，而SqlSource对象的责任，就是根据传入的参数对象，动态计算出这个BoundSql，也就是说Mapper文件中的
 * <if/>节点的计算，是由SqlSource对象完成的。
 *
 * <p>
 * 处理任何动态内容后，从{@link SqlSource}获取的实际SQL字符串.
 * SQL可能具有SQL占位符“？”以及参数映射的列表（有序）以及每个参数的附加信息（至少是从中读取值的输入对象的属性名称）.
 * 也可以具有由动态语言创建的其他参数（用于循环，绑定...）.
 *
 * @author Clinton Begin
 */
public class BoundSql {

    /** 表示每次实际数据库操作的SQL，例如：select * from `t_employeer` where employeer_department = ? and employeer_worktype = ? */
    private String sql;

    private List<ParameterMapping> parameterMappings;

    /** 表示SQL的入参 */
    private Object parameterObject;
    private Map<String, Object> additionalParameters;
    private MetaObject metaParameters;

    public BoundSql(Configuration configuration, String sql, List<ParameterMapping> parameterMappings, Object parameterObject) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.parameterObject = parameterObject;
        this.additionalParameters = new HashMap<String, Object>();
        this.metaParameters = configuration.newMetaObject(additionalParameters);
    }

    public String getSql() {
        return sql;
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }

    public Object getParameterObject() {
        return parameterObject;
    }

    public boolean hasAdditionalParameter(String name) {
        String paramName = new PropertyTokenizer(name).getName();
        return additionalParameters.containsKey(paramName);
    }

    public void setAdditionalParameter(String name, Object value) {
        metaParameters.setValue(name, value);
    }

    public Object getAdditionalParameter(String name) {
        return metaParameters.getValue(name);
    }
}
