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

import java.util.List;

/**
 * 处理<choose>动态标签，例如：
 * <select id="findActiveBlogLike" resultType="Blog">
 *   SELECT * FROM BLOG WHERE state = ‘ACTIVE’
 *   <choose>
 *     <when test="title != null">
 *       AND title like #{title}
 *     </when>
 *     <when test="author != null and author.name != null">
 *       AND author_name like #{author.name}
 *     </when>
 *     <otherwise>
 *       AND featured = 1
 *     </otherwise>
 *   </choose>
 * </select>
 *
 * @author Clinton Begin
 */
public class ChooseSqlNode implements SqlNode {

    /** 对应内部的<otherwise>标签 */
    private SqlNode defaultSqlNode;

    /** 对应内部的<when>标签 */
    private List<SqlNode> ifSqlNodes;

    public ChooseSqlNode(List<SqlNode> ifSqlNodes, SqlNode defaultSqlNode) {
        this.ifSqlNodes = ifSqlNodes;
        this.defaultSqlNode = defaultSqlNode;
    }

    @Override
    public boolean apply(DynamicContext context) {
        // <when>标签满足时，解析<when>标签内的内容
        for (SqlNode sqlNode : ifSqlNodes) {
            if (sqlNode.apply(context)) {
                return true;
            }
        }

        // 当所有when都不满足条件时，则使用解析<otherwise>标签
        if (defaultSqlNode != null) {
            defaultSqlNode.apply(context);
            return true;
        }
        return false;
    }
}
