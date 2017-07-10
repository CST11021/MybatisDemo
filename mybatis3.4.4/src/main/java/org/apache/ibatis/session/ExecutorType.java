/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.session;

/**
 * 用于表示SQL执行器的类型：
 * 默认的执行器SIMPLE
 * 执行器重用 REUSE
 * 执行器重用语句 批量更新 BATCH
 *
 *
 * mybatis创建sqlsession经过了以下几个主要步骤：
 1)       从核心配置文件mybatis-config.xml中获取Environment（这里面是数据源）；
 2)       从Environment中取得DataSource；
 3)       从Environment中取得TransactionFactory；
 4)       从DataSource里获取数据库连接对象Connection；
 5)       在取得的数据库连接上创建事务对象Transaction；
 6)       创建Executor对象（该对象非常重要，事实上sqlsession的所有操作都是通过它完成的）；
 7)       创建sqlsession对象
 *
 * @author Clinton Begin
 */
public enum ExecutorType {
  SIMPLE, REUSE, BATCH
}
