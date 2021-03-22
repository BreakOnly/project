package com.jrmf.interceptor;

import com.jrmf.domain.TaskBaseConfig;
import com.jrmf.service.TaskBaseConfigService;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.jdbc.SpringContextHelper;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

/**
 * 为定时任务数据隔离自定义mybatis拦截器
 * <p>
 * Signature：指明自定义拦截器需要拦截哪一个类型，哪一个方法
 * type：对应四种类型中的一种
 * Executor：MyBatis执行器，是MyBatis 调度的核心，负责SQL语句的生成和查询缓存的维护
 * ParameterHandler：负责对用户传递的参数转换成JDBC Statement 所需要的参数
 * ResultHandler：负责将JDBC返回的Result结果集对象转换成List类型的集合
 * StatementHandler：封装了JDBC Statement操作，负责对JDBC statement 的操作，如设置参数、将Statement结果集转换成List集合
 * method：对应接口中的哪类方法
 * args：对应哪一个方法（因为可能存在重载方法）
 * <p>
 * 请不要用idea进行全类format,影响注释格式
 *
 * @author linsong
 * @date 2020/4/10
 *
 * 统一放到SqlSessionInterceptor中，方法不再调用
 */
//@Component
@Deprecated
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class JobServiceInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(JobServiceInterceptor.class);


    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        /**
         * 在Mybatis里面RoutingStatementHandler是SimpleStatementHandler(对应Statement)
         * PreparedStatementHandler(对应PreparedStatement)
         * CallableStatementHandler(对应CallableStatement)的路由类
         * 所以需要拦截StatementHandler里面的方法的时候,对RoutingStatementHandler做拦截处理就可以
         *
         */
        if (invocation.getTarget() instanceof RoutingStatementHandler) {

            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
            MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
            //先拦截到RoutingStatementHandler，里面有个StatementHandler类型的delegate变量，其实现类是BaseStatementHandler，然后就到BaseStatementHandler的成员变量mappedStatement
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

            //注解逻辑判断  添加注解了才拦截
            Class<?> classType = Class.forName(mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf(".")));
            String mName = mappedStatement.getId().substring(mappedStatement.getId().lastIndexOf(".") + 1);
            for (Method method : classType.getDeclaredMethods()) {
                if (method.isAnnotationPresent(InterceptJobServiceAnnotation.class) && mName.equals(method.getName())) {
                    InterceptJobServiceAnnotation interceptorAnnotation = method.getAnnotation(InterceptJobServiceAnnotation.class);
                    if (interceptorAnnotation.flag()) {

                        BoundSql boundSql = statementHandler.getBoundSql();

                        //获取到原始sql语句
                        String sql = boundSql.getSql();
                        String newSql = this.processSql(method.getName(), sql);

//                        logger.error("原sql:{} 数据隔离后sql:{}", sql, newSql);
                        //id为执行的mapper方法的全路径名
//                    String id = mappedStatement.getId();
                        //sql语句类型 select、delete、insert、update
//                    String sqlCommandType = mappedStatement.getSqlCommandType().toString();

                        //通过反射修改sql语句
                        Field field = boundSql.getClass().getDeclaredField("sql");
                        field.setAccessible(true);
                        field.set(boundSql, newSql);

                        break;
                    }
                }
            }

        }
        return invocation.proceed();

    }


    private String processSql(String method, String sql) {


        TaskBaseConfigService taskBaseConfigService = SpringContextHelper.getBean(TaskBaseConfigService.class);

        StringBuilder newSql = new StringBuilder(sql);

        TaskBaseConfig taskBaseConfig = taskBaseConfigService.getConfigByOsId();
        if (taskBaseConfig != null && !StringUtil.isEmpty(taskBaseConfig.getCustomKeys())) {

            CronJobMethod cronJobMethod = CronJobMethod.codeOf(method);

            if (cronJobMethod != null) {
                newSql.append(" and ").append(taskBaseConfig.getTaskType()).append("(").append(cronJobMethod.getAliasName()).append(",\'").append(taskBaseConfig.getCustomKeys()).append("\')");
            }
        }

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
