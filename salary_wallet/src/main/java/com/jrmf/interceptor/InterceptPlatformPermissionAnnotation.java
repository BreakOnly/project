package com.jrmf.interceptor;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实现平台角色权限数据隔离
 *
 * @author linsong
 * @date 2020/9/21
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface InterceptPlatformPermissionAnnotation {
    //数据拦截时作为隔离数据的别名字段
    String aliasName();
}
