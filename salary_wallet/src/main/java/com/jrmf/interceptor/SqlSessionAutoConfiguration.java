package com.jrmf.interceptor;

import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;


/**
 * 用于改变mybatis中Configuration类 addInterceptor方法顺序 优先执行自定义拦截器
 * @author linsong
 * @date 2020/9/23
 */
@Configuration
@AutoConfigureAfter(PageHelperAutoConfiguration.class)
public class SqlSessionAutoConfiguration {

  @Autowired
  private List<SqlSessionFactory> sqlSessionFactoryList;

  @PostConstruct
  public void addInterceptor() {
    for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
      sqlSessionFactory.getConfiguration().addInterceptor(new SqlSessionInterceptor());
    }
  }

}
