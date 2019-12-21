package com.whz.mybatis.plugins.example;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

@Intercepts({
        // 要拦截的方法：这里拦截Executor的query，带有4个入参的query方法
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
        )
})
public class QueryPlugin implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] params = invocation.getArgs();

        MappedStatement mappedStatement = (MappedStatement) params[0];
        Object sqlParam = params[1];
        RowBounds rowBounds = (RowBounds) params[2];
        ResultHandler resultHandler = (ResultHandler) params[3];

        System.out.println("执行的映射器方法：" + mappedStatement.getId());
        System.out.println("执行的SQL：" + mappedStatement.getBoundSql(sqlParam).getSql());

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties arg0) {
    }

}