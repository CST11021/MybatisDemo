/**
 * Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.executor.keygen;

import java.sql.Statement;
import java.util.List;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.RowBounds;

/**
 * SelectKeyGenerator：主要是通过 XML 配置或者注解设置 selectKey ，然后单独发出查询语句，在返回拦截方法中使用反射设置主键，
 * 其中两个拦截方法只能使用其一，默认 使用 processBefore，在 selectKey.order 属性中设置 AFTER|BEFORE 来确定；
 *
 *
 * NoKeyGenerator：默认空实现，不需要对主键单独处理；
 * Jdbc3KeyGenerator：主要用于数据库的自增主键，比如 MySQL、PostgreSQL；
 * SelectKeyGenerator：主要用于数据库不支持自增主键的情况，比如 Oracle、DB2；
 *
 *
 * @author Clinton Begin
 * @author Jeff Butler
 */
public class SelectKeyGenerator implements KeyGenerator {

    public static final String SELECT_KEY_SUFFIX = "!selectKey";

    /** 是否在执行前进行拦截 */
    private boolean executeBefore;
    private MappedStatement keyStatement;

    public SelectKeyGenerator(MappedStatement keyStatement, boolean executeBefore) {
        this.executeBefore = executeBefore;
        this.keyStatement = keyStatement;
    }


    /**
     * processBefore 是在生成 StatementHandler 的时候；
     *
     * protected BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
     *   ...
     *   if (boundSql == null) { // issue #435, get the key before calculating the statement
     *     generateKeys(parameterObject);
     *     boundSql = mappedStatement.getBoundSql(parameterObject);
     *   }
     *   ...
     * }
     *
     * protected void generateKeys(Object parameter) {
     *   KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
     *   ErrorContext.instance().store();
     *   keyGenerator.processBefore(executor, mappedStatement, null, parameter);
     *   ErrorContext.instance().recall();
     * }
     *
     *
     * @param executor
     * @param ms
     * @param stmt
     * @param parameter
     */
    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        if (executeBefore) {
            processGeneratedKeys(executor, ms, parameter);
        }
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        if (!executeBefore) {
            processGeneratedKeys(executor, ms, parameter);
        }
    }

    /**
     * processAfter 则是在完成插入返回结果之前，但是 PreparedStatementHandler、SimpleStatementHandler、CallableStatementHandler 的代码稍微有一点不同，但是位置是不变的，这里以 PreparedStatementHandler 举例：
     *
     * @Override
     * public int update(Statement statement) throws SQLException {
     *   PreparedStatement ps = (PreparedStatement) statement;
     *   ps.execute();
     *   int rows = ps.getUpdateCount();
     *   Object parameterObject = boundSql.getParameterObject();
     *   KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
     *   keyGenerator.processAfter(executor, mappedStatement, ps, parameterObject);
     *   return rows;
     * }
     *
     *
     * @param executor
     * @param ms
     * @param parameter
     */
    private void processGeneratedKeys(Executor executor, MappedStatement ms, Object parameter) {
        try {
            if (parameter != null && keyStatement != null && keyStatement.getKeyProperties() != null) {
                String[] keyProperties = keyStatement.getKeyProperties();
                final Configuration configuration = ms.getConfiguration();
                final MetaObject metaParam = configuration.newMetaObject(parameter);
                if (keyProperties != null) {
                    // Do not close keyExecutor.
                    // The transaction will be closed by parent executor.
                    Executor keyExecutor = configuration.newExecutor(executor.getTransaction(), ExecutorType.SIMPLE);
                    List<Object> values = keyExecutor.query(keyStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
                    if (values.size() == 0) {
                        throw new ExecutorException("SelectKey returned no data.");
                    } else if (values.size() > 1) {
                        throw new ExecutorException("SelectKey returned more than one value.");
                    } else {
                        MetaObject metaResult = configuration.newMetaObject(values.get(0));
                        if (keyProperties.length == 1) {
                            if (metaResult.hasGetter(keyProperties[0])) {
                                setValue(metaParam, keyProperties[0], metaResult.getValue(keyProperties[0]));
                            } else {
                                // no getter for the property - maybe just a single value object so try that
                                setValue(metaParam, keyProperties[0], values.get(0));
                            }
                        } else {
                            handleMultipleProperties(keyProperties, metaParam, metaResult);
                        }
                    }
                }
            }
        } catch (ExecutorException e) {
            throw e;
        } catch (Exception e) {
            throw new ExecutorException("Error selecting key or setting result to parameter object. Cause: " + e, e);
        }
    }

    private void handleMultipleProperties(String[] keyProperties, MetaObject metaParam, MetaObject metaResult) {
        String[] keyColumns = keyStatement.getKeyColumns();

        if (keyColumns == null || keyColumns.length == 0) {
            // no key columns specified, just use the property names
            for (String keyProperty : keyProperties) {
                setValue(metaParam, keyProperty, metaResult.getValue(keyProperty));
            }
        } else {
            if (keyColumns.length != keyProperties.length) {
                throw new ExecutorException("If SelectKey has key columns, the number must match the number of key properties.");
            }
            for (int i = 0; i < keyProperties.length; i++) {
                setValue(metaParam, keyProperties[i], metaResult.getValue(keyColumns[i]));
            }
        }
    }

    /**
     * 通过{@link MetaObject}封装的反射机制，设置对应的属性值
     *
     * @param metaParam     表示源数据对象对应的MetaObject实例
     * @param property      表示要设置的属性名
     * @param value         表示要设置的属性值
     */
    private void setValue(MetaObject metaParam, String property, Object value) {
        if (metaParam.hasSetter(property)) {
            metaParam.setValue(property, value);
        } else {
            throw new ExecutorException("No setter found for the keyProperty '" + property + "' in " + metaParam.getOriginalObject().getClass().getName() + ".");
        }
    }
}
