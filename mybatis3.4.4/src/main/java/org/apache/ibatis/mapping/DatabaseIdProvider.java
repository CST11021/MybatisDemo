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

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 应该返回一个ID来标识此数据库的类型，以后可以使用该ID为每种数据库类型构建不同的查询。此机制可支持多个供应商或版本
 *
 * @author Eduardo Macarron
 */
public interface DatabaseIdProvider {

    void setProperties(Properties p);

    String getDatabaseId(DataSource dataSource) throws SQLException;

}
