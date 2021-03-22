package com.jrmf.config;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * 微服务——分布式服务配置中心
 * 所有服务的配置文件由 platform-cloud-config 管理， platform-cloud-config可以从远程 Git 仓库读取，也可以从本地仓库读取
 *
 * @author linsong
 */
@SpringCloudApplication
@EnableConfigServer
public class ConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigApplication.class, args);
	}

}
