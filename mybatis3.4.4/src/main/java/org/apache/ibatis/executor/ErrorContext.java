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
package org.apache.ibatis.executor;

/**
 * @author Clinton Begin
 */
public class ErrorContext {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    /** 每个线程执行SQL时都对应一个error上下文 */
    private static final ThreadLocal<ErrorContext> LOCAL = new ThreadLocal<ErrorContext>();

    /** 表示当前线程的error上下文 */
    private ErrorContext stored;
    /** 表示该mapper接口对应的配置的所在配置文件名，如：IEmployeerMapper.xml */
    private String resource;
    /** 表示当前执行的活动，比如："executing an update" 或者 "executing an query" */
    private String activity;
    /** 一般设置为SQL对应的id配置 */
    private String object;
    /** 错误 */
    private String message;
    /** 异常SQL */
    private String sql;
    /** 异常堆栈 */
    private Throwable cause;

    private ErrorContext() {
    }

    /**
     * 实例化一个当前线程的上下文
     *
     * @return
     */
    public static ErrorContext instance() {
        ErrorContext context = LOCAL.get();
        if (context == null) {
            context = new ErrorContext();
            LOCAL.set(context);
        }
        return context;
    }

    /**
     * 重置上下文，并将从当前线程移除，一般在创建SqlSession的时候，会将当前上下文reset调
     *
     * @return
     */
    public ErrorContext reset() {
        resource = null;
        activity = null;
        object = null;
        message = null;
        sql = null;
        cause = null;
        LOCAL.remove();
        return this;
    }

    /**
     * 创建并返回一个ErrorContext实例，并保存当前线程的上下文
     *
     * @return
     */
    public ErrorContext store() {
        stored = this;
        LOCAL.set(new ErrorContext());
        return LOCAL.get();
    }

    /**
     * 清除当前线程的上下文
     *
     * @return
     */
    public ErrorContext recall() {
        if (stored != null) {
            LOCAL.set(stored);
            stored = null;
        }
        return LOCAL.get();
    }

    /**
     * 设置当前error上下文来自于哪个mapper.xml文件
     *
     * @param resource
     * @return
     */
    public ErrorContext resource(String resource) {
        this.resource = resource;
        return this;
    }

    public ErrorContext activity(String activity) {
        this.activity = activity;
        return this;
    }

    public ErrorContext object(String object) {
        this.object = object;
        return this;
    }

    /**
     * 设置错误信息
     *
     * @param message
     * @return
     */
    public ErrorContext message(String message) {
        this.message = message;
        return this;
    }

    /**
     * 设置当前error上下文的执行SQL
     *
     * @param sql
     * @return
     */
    public ErrorContext sql(String sql) {
        this.sql = sql;
        return this;
    }

    /**
     * 设置异常原因
     *
     * @param cause
     * @return
     */
    public ErrorContext cause(Throwable cause) {
        this.cause = cause;
        return this;
    }



    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();

        // message
        if (this.message != null) {
            description.append(LINE_SEPARATOR);
            description.append("### ");
            description.append(this.message);
        }

        // resource
        if (resource != null) {
            description.append(LINE_SEPARATOR);
            description.append("### The error may exist in ");
            description.append(resource);
        }

        // object
        if (object != null) {
            description.append(LINE_SEPARATOR);
            description.append("### The error may involve ");
            description.append(object);
        }

        // activity
        if (activity != null) {
            description.append(LINE_SEPARATOR);
            description.append("### The error occurred while ");
            description.append(activity);
        }

        // activity
        if (sql != null) {
            description.append(LINE_SEPARATOR);
            description.append("### SQL: ");
            description.append(sql.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim());
        }

        // cause
        if (cause != null) {
            description.append(LINE_SEPARATOR);
            description.append("### Cause: ");
            description.append(cause.toString());
        }

        return description.toString();
    }

}
