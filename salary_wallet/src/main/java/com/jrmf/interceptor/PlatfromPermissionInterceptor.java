package com.jrmf.interceptor;

import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.OrganizationNode;
import com.jrmf.domain.TaskBaseConfig;
import com.jrmf.service.TaskBaseConfigService;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.jdbc.SpringContextHelper;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Properties;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 为平台权限角色数据隔离自定义mybatis拦截器
 *
 * @author linsong
 * @date 2020/9/21
 *
 * 统一放到SqlSessionInterceptor中，方法不再调用
 */
//@Component
@Deprecated
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PlatfromPermissionInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(PlatfromPermissionInterceptor.class);


    @Override
    public Object intercept(Invocation invocation) throws Throwable {


        if (invocation.getTarget() instanceof RoutingStatementHandler) {

            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
            MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

            Class<?> classType = Class.forName(mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf(".")));
            String mName = mappedStatement.getId().substring(mappedStatement.getId().lastIndexOf(".") + 1);
            for (Method method : classType.getDeclaredMethods()) {
                if (method.isAnnotationPresent(InterceptPlatformPermissionAnnotation.class) && mName
                    .equals(method.getName())) {
                    InterceptPlatformPermissionAnnotation interceptorAnnotation = method
                        .getAnnotation(InterceptPlatformPermissionAnnotation.class);

                    BoundSql boundSql = statementHandler.getBoundSql();

                    String aliasName = interceptorAnnotation.aliasName();

                    //获取到原始sql语句
                    String sql = boundSql.getSql();
                    String newSql = this.processSql(aliasName, sql);

                    //通过反射修改sql语句
                    Field field = boundSql.getClass().getDeclaredField("sql");
                    field.setAccessible(true);
                    field.set(boundSql, newSql);

                    break;
                }
            }

        }
        return invocation.proceed();

    }


    private String processSql(String aliasName, String sql) {

        ChannelCustom localUser = (ChannelCustom) UserThreadLocal.getLocalUser();

        StringBuilder newSql = new StringBuilder(sql);
        logger.info("print platfromPermission sourceSql:{}" ,sql);

        if (CustomType.PLATFORM.getCode() == localUser.getCustomType()) {
            newSql.append(" and ").append(aliasName).append(" = ").append(localUser.getId());
            logger.info("print platfromPermission newSql:{}" ,newSql);
        }

        UserThreadLocal.removeUser();

        return newSql.toString();
    }

    @Override
    public Object plugin(Object target) {
        return (target instanceof RoutingStatementHandler) ? Plugin.wrap(target, this) : target;
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
