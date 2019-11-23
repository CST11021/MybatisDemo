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
 * SQL执行结果处理器，Mybastic提供了Object、List和Map三种实现，分别对应：
 * ObjectWrapperResultHandler
 * DefaultResultHandler
 * DefaultMapResultHandler
 *
 * @author Clinton Begin
 */
public interface ResultHandler<T> {

    /**
     * 处理结果集
     *
     * @param resultContext
     */
    void handleResult(ResultContext<? extends T> resultContext);

}
