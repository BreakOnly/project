package com.jrmf.interceptor;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义一个方法层面的注解，实现局部指定拦截定时器
 *
 * @author linsong
 * @date 2020/4/10
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface InterceptJobServiceAnnotation {
    boolean flag() default true;
}
