package com.jrmf.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;


/**
 * 微服务——服务注册和发现
 * 所有的服务都向服务注册中心注册，能够方便查看每个服务的状况、服务是否可用，以及每个服务有哪些服务实例
 *
 * @author linsong
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaApplication.class, args);
	}

}
