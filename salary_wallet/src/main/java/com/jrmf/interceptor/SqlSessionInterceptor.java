package com.jrmf.interceptor;

import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.TaskBaseConfig;
import com.jrmf.service.TaskBaseConfigService;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.exception.LoginException;
import com.jrmf.utils.jdbc.SpringContextHelper;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * sql拦截处理
 * <p>
 * Signature：指明自定义拦截器需要拦截哪一个类型，哪一个方法 type：对应四种类型中的一种 Executor：MyBatis执行器，是MyBatis
 * 调度的核心，负责SQL语句的生成和查询缓存的维护 ParameterHandler：负责对用户传递的参数转换成JDBC Statement 所需要的参数
 * ResultHandler：负责将JDBC返回的Result结果集对象转换成List类型的集合 StatementHandler：封装了JDBC Statement操作，负责对JDBC
 * statement 的操作，如设置参数、将Statement结果集转换成List集合 method：对应接口中的哪类方法 args：对应哪一个方法（因为可能存在重载方法）
 * <p>
 *
 * @author linsong
 * @date 2020/09/22
 */
@Intercepts({@Signature(
    type = Executor.class,
    method = "query",
    args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
), @Signature(
    type = Executor.class,
    method = "query",
    args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
        CacheKey.class, BoundSql.class}
)})
public class SqlSessionInterceptor implements Interceptor {

  private static final Logger logger = LoggerFactory.getLogger(SqlSessionInterceptor.class);


  @Override
  public Object intercept(Invocation invocation) throws Throwable {

    /**
     * 在Mybatis里面RoutingStatementHandler是SimpleStatementHandler(对应Statement)
     * PreparedStatementHandler(对应PreparedStatement)
     * CallableStatementHandler(对应CallableStatement)的路由类
     * 所以需要拦截StatementHandler里面的方法的时候,对RoutingStatementHandler做拦截处理就可以
     *
     */
    Object[] args = invocation.getArgs();
    MappedStatement mappedStatement = (MappedStatement) args[0];
    Object parameter = args[1];
    BoundSql boundSql;
    if (args.length == 4) {
      boundSql = mappedStatement.getBoundSql(parameter);
    } else {
      boundSql = (BoundSql) args[5];
    }

    //注解逻辑判断  添加注解了才拦截
    Class<?> classType = Class
        .forName(mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf(".")));
    String mName = mappedStatement.getId()
        .substring(mappedStatement.getId().lastIndexOf(".") + 1);
    for (Method method : classType.getDeclaredMethods()) {
      //兼容pagehelper生成的count sql
      if (method.isAnnotationPresent(InterceptPlatformPermissionAnnotation.class) && (mName
          .equals(method.getName()) || mName.equals(method.getName() + "_COUNT"))) {
        InterceptPlatformPermissionAnnotation interceptorAnnotation = method
            .getAnnotation(InterceptPlatformPermissionAnnotation.class);

        String aliasName = interceptorAnnotation.aliasName();
        this.platfromPermissionProcessSql(boundSql, aliasName);

        break;
      }

      if (method.isAnnotationPresent(InterceptJobServiceAnnotation.class) && mName
          .equals(method.getName())) {
        InterceptJobServiceAnnotation interceptorAnnotation = method
            .getAnnotation(InterceptJobServiceAnnotation.class);
        if (interceptorAnnotation.flag()) {

          this.jobServiceProcessSql(boundSql, method.getName());
          break;
        }
      }
    }

//    return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);

    return invocation.proceed();

  }


  private void jobServiceProcessSql(BoundSql boundSql, String method)
      throws NoSuchFieldException, IllegalAccessException {

    //获取到原始sql语句
    String sql = boundSql.getSql();

    TaskBaseConfigService taskBaseConfigService = SpringContextHelper
        .getBean(TaskBaseConfigService.class);

    StringBuilder newSql = new StringBuilder(sql);

    TaskBaseConfig taskBaseConfig = taskBaseConfigService.getConfigByOsId();
    if (taskBaseConfig != null && !StringUtil.isEmpty(taskBaseConfig.getCustomKeys())) {

      CronJobMethod cronJobMethod = CronJobMethod.codeOf(method);

      if (cronJobMethod != null) {
        newSql.append(" and ").append(taskBaseConfig.getTaskType()).append("(")
            .append(cronJobMethod.getAliasName()).append(",\'")
            .append(taskBaseConfig.getCustomKeys()).append("\')");
      }
    }

    //通过反射修改sql语句
    Field field = boundSql.getClass().getDeclaredField("sql");
    field.setAccessible(true);
    field.set(boundSql, newSql.toString());

  }

  private void platfromPermissionProcessSql(BoundSql boundSql, String aliasName)
      throws NoSuchFieldException, IllegalAccessException {

    ChannelCustom localUser = (ChannelCustom) UserThreadLocal.getLocalUser();
    if (localUser == null) {
      //无法确认当前登录用户信息
      throw new LoginException("无法确认当前登录用户信息");
    }

    //获取到原始sql语句
    String sql = boundSql.getSql();

    StringBuilder newSql = new StringBuilder(sql.replaceAll("WHERE", "where"));

    if (!CommonString.ROOT.equals(localUser.getCustomkey()) && !CommonString.ROOT
        .equals(localUser.getMasterCustom())) {

      Integer platformId = null;
      if (CustomType.ROOT.getCode() == localUser.getCustomType()
          && CustomType.PLATFORM.getCode() == localUser.getMasterCustomType()) {
        platformId = localUser.getBusinessPlatformId();
      } else if (CustomType.PLATFORM.getCode() == localUser.getCustomType()) {
        platformId = localUser.getId();
      }

      if (platformId != null && platformId > 0) {
        int whereStrIdex = newSql.lastIndexOf("where");

        StringBuilder subSql = new StringBuilder(" ").append(aliasName).append(" = ")
            .append(platformId);
        //根据=号判断 where后是否已有条件，有条件则追加一个and
        if (newSql.indexOf("=", whereStrIdex) > 0) {
          subSql.append(" and ");
        }

        //判断是否存在where条件
        if (whereStrIdex > 0) {
          newSql.insert(whereStrIdex + 5, subSql);
        } else {
          newSql.append(" where ").append(subSql);
        }

        //通过反射修改sql语句
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, newSql.toString());
      }
    }

  }

  @Override
  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  @Override
  public void setProperties(Properties properties) {

  }
}
