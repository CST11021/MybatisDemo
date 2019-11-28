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
package org.apache.ibatis.mapping;

import org.apache.ibatis.session.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * 对应<parameterMap>标签，例如：
 * <parameterMap id="userParameterMap" type="com.whz.entity.User">
 *      <parameter property="age" jdbcType="int" typeHandler="INTEGER" javaType="int" resultMap="userResultMap" mode="" scale=""/>
 *      省略。。。
 * </parameterMap>
 *
 * @author Clinton Begin
 */
public class ParameterMap {

    /** 表示Mybastic中配置的SQL语句的id */
    private String id;

    /** 对应SQL语句中的parameterType属性配置的java类型 */
    private Class<?> type;

    /** <parameter>子标签配置列表 */
    private List<ParameterMapping> parameterMappings;

    private ParameterMap() {
    }

    public static class Builder {
        private ParameterMap parameterMap = new ParameterMap();

        public Builder(Configuration configuration, String id, Class<?> type, List<ParameterMapping> parameterMappings) {
            parameterMap.id = id;
            parameterMap.type = type;
            parameterMap.parameterMappings = parameterMappings;
        }

        public Class<?> type() {
            return parameterMap.type;
        }

        public ParameterMap build() {
            //lock down collections
            parameterMap.parameterMappings = Collections.unmodifiableList(parameterMap.parameterMappings);
            return parameterMap;
        }
    }

    public String getId() {
        return id;
    }

    public Class<?> getType() {
        return type;
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }

}
