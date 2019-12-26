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
 * 用于处理<bind>标签：
 * bind 元素可以使用 OGNL 表达式创建一个变量并将其绑定到当前SQL节点的上下文。
 *
 * <select id="selectBlogsLike" parameterType="BlogQuery" resultType="Blog">
 *   <bind name="pattern" value="'%' + title + '%'" />
 *   SELECT * FROM BLOG
 *   WHERE title LIKE #{pattern}
 * </select>
 * 对于这种情况，bind还可以用来预防 SQL 注入。
 *
 * @author Frank D. Martinez [mnesarco]
 */
public class VarDeclSqlNode implements SqlNode {

    /** 对应<bind>标签的name属性 */
    private final String name;
    /** 对应<bind>标签的value属性，这里是一个OGNL表达式 */
    private final String expression;

    public VarDeclSqlNode(String var, String exp) {
        name = var;
        expression = exp;
    }

    @Override
    public boolean apply(DynamicContext context) {
        // 计算表达式的值
        final Object value = OgnlCache.getValue(expression, context.getBindings());
        context.bind(name, value);
        return true;
    }

}
