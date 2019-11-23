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
package org.apache.ibatis.session;

/**
 * 缓存作用域
 *
 * SESSION：表示在同一个session实例才共享缓存
 * STATEMENT：表示在同一个statement实例才共享缓存
 *
 *
 * （1）<!-- 全局映射器启用缓存 -->
 *  <setting name="cacheEnabled" value="true"/>
 *  中的 cacheEnabled：false 关闭二级缓存（一级缓存是可用的）
 *
 * （2）mapper.xml中每个select标签都有一个useCache="true",为false时，不使用缓存（一级缓存依然可以使用，二级缓存不能使用）
 *  如果，设置单个select标签不使用二级缓存，可设置 useCache="false"
 *
 *  (3)
 *  a.每个增删改标签有：都有 flushCache="true" 默认为true，所以每次增删改执行完成，会清除缓存，一级缓存清空，二级缓存也会被清空
 *  b.查询标签 flushCache="false" 默认为false，如果设置 flushCache="true" ，每次查询之后都会清空缓存，缓存是没有被使用的
 *
 * （4）sqlSession.clearCache():只是清除session的一级缓存，二级缓存不清除
 * （5）<setting name="localCacheScope" value="SESSION"/>
 *    localCacheScope:本地缓存作用域（一级缓存SESSION），当前会话的所有数据会保存在会话缓存中
 *                 value=" STATEMENT"：可以禁用一级缓存
 *
 * @author Eduardo Macarron
 */
public enum LocalCacheScope {
    SESSION, STATEMENT
}
