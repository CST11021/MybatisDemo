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
package org.apache.ibatis.builder;

import java.util.List;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

/**
 * 表示一个静态的SQL，动态SQL解析完后也会转为静态SQL，静态即不包含动态标签<if>等其他动态的标签的SQL
 *
 * @author Clinton Begin
 */
public class StaticSqlSource implements SqlSource {

    /** 表示一个静态SQL，例如：select * from `t_employeer` where employeer_department = ? and employeer_worktype = ? */
    private String sql;
    /** SQL入参站位符对应的参数类型，注意：这里是类型并不包含实际的参数值 */
    private List<ParameterMapping> parameterMappings;
    private Configuration configuration;

    public StaticSqlSource(Configuration configuration, String sql) {
        this(configuration, sql, null);
    }

    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }

    /**
     * 获取一个可以交给Statement处理的BoundSql实例
     *
     * @param parameterObject   表示SQL的入参
     * @return
     */
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // 通过带有站位符的sql + 占位符对应的参数类型 + 实际的参数值，得到一个可以交给Statement处理的BoundSql
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }

}
