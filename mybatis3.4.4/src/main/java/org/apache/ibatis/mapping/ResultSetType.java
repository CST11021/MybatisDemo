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

import java.sql.ResultSet;

/**
 * 对应JDBC中可滚动游标结果集的类型，JDBC中有4中类型的游标结果集，分别是：
 * 1、最基本的ResultSet
 * 2、可滚动的ResultSet
 * 3、可更新的ResultSet
 * 4、可保持的ResultSet
 *
 * 每种不同的ResultSet下又对应了不同的操作特性这里，的ResultSetType对应的是可滚动的ResultSet类型下的三种特性
 *
 * @author Clinton Begin
 */
public enum ResultSetType {

    /** 只能向前滚动的ResultSet类型 */
    FORWARD_ONLY(ResultSet.TYPE_FORWARD_ONLY),
    /** 结果集的游标可以上下移动，当数据库变化时，当前结果集不变 */
    SCROLL_INSENSITIVE(ResultSet.TYPE_SCROLL_INSENSITIVE),
    /** 返回可滚动的结果集，当数据库变化时，当前结果集同步改变 */
    SCROLL_SENSITIVE(ResultSet.TYPE_SCROLL_SENSITIVE);

    private int value;

    ResultSetType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
