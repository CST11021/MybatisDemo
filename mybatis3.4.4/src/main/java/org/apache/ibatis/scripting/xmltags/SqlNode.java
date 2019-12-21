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
package org.apache.ibatis.scripting.xmltags;

/**
 * 传统的使用JDBC的方法，相信大家在组合复杂的的SQL语句的时候，需要去拼接，稍不注意哪怕少了个空格，都会导致错误。
 * Mybatis的动态SQL功能正是为了解决这种问题，其通过if, choose, when, otherwise, trim, where, set, foreach标签，可组合成非常灵活的SQL语句，从而提高开发人员的效率
 *
 * @author Clinton Begin
 */
public interface SqlNode {

    /**
     * 用于解析对应的动态标签，将动态标签解析完后，append到{@link DynamicContext#sqlBuilder}中
     *
     * @param context
     * @return
     */
    boolean apply(DynamicContext context);

}
