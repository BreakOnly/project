package com.jrmf.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author: YJY
 * @date: 2020/10/23 17:20
 * @description:
 */
@Configuration
public class WebConfigurer implements WebMvcConfigurer {


  @Autowired
  private YuncrInterceptor yuncrInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 添加拦截器，配置拦截地址
    registry.addInterceptor(yuncrInterceptor).addPathPatterns("/api/individual/**");
  }

}
