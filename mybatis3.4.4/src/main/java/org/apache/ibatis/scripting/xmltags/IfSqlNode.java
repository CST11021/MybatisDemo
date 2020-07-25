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
 * 处理<if>动态标签
 *
 * @author Clinton Begin
 */
public class IfSqlNode implements SqlNode {
    /** 用于判断<if>标签中的test表达式是否为true */
    private ExpressionEvaluator evaluator;
    /** 对应<if>标签中的test表达式 */
    private String test;
    /** 表示<if>标签内的其他动态标签 */
    private SqlNode contents;

    public IfSqlNode(SqlNode contents, String test) {
        this.test = test;
        this.contents = contents;
        this.evaluator = new ExpressionEvaluator();
    }

    /**
     * 当test表达式满足条件时，解析<if>标签内的内容
     *
     * @param context
     * @return
     */
    @Override
    public boolean apply(DynamicContext context) {
        // 解析<if>标签的test表达式是否为true
        if (evaluator.evaluateBoolean(test, context.getBindings())) {
            // 将<if>标签的内容追加到SQL文本中
            contents.apply(context);
            return true;
        }
        return false;
    }

}
