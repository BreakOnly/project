package com.jrmf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

/**
 * @author jrmf
 */
@SpringBootApplication
@EnableFeignClients
@ImportResource(locations= {"classpath:spring-*.xml"})
@EnableScheduling
@ServletComponentScan
@Configuration
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 设置文件上传大小限制
     * Date 22:05 2018/11/23
     * Param []
     * return javax.servlet.MultipartConfigElement
     **/
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //单个文件最大
        //KB,MB
        factory.setMaxFileSize(DataSize.parse("5120000KB"));
        /// 设置总上传数据总大小
        factory.setMaxRequestSize(DataSize.parse("10240000KB"));
        return factory.createMultipartConfig();
    }
}
